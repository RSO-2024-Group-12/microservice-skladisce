package si.nakupify.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import si.nakupify.entity.Event;
import si.nakupify.entity.Snapshot;
import si.nakupify.entity.SnapshotProduct;
import si.nakupify.service.dto.*;
import si.nakupify.service.repository.SnapshotProductRepository;
import si.nakupify.service.repository.SnapshotRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ApplicationScoped
public class SkladisceService {

    @Inject
    SnapshotRepository snapshotRepository;

    @Inject
    SnapshotProductRepository snapshotProductRepository;

    @Inject
    KafkaEventReplayService kafkaEventReplayService;

    @Channel("events-out")
    Emitter<Event> emitter;

    @ConfigProperty(name="snapshot.frequency")
    private Integer snapshotFrequency;

    private Snapshot latestSnapshot;

    private Integer processedEvents = 0;

    private Map<Long, SnapshotProduct> skladisce = new ConcurrentHashMap<>();

    private Logger log = Logger.getLogger(SkladisceService.class.getName());

    @PostConstruct
    private void init() {
        pridobiSkladisce();
        log.info("Inicializacija microservice-skladisce.");
    }

    @PreDestroy
    private void destroy() {
        log.info("Ustavitev microservice-skladisce.");
    }

    public void setEmmiter(Emitter<Event> emitter) {
        this.emitter = emitter;
    }

    @Transactional
    public void pridobiSkladisce() {
        skladisce.clear();

        latestSnapshot = snapshotRepository.getLatestSnapshot();
        List<SnapshotProduct> snapshotProducts = snapshotProductRepository.findBySnapshotId(latestSnapshot.id);

        for (SnapshotProduct snapshotProduct : snapshotProducts) {
            skladisce.put(snapshotProduct.id_izdelek, snapshotProduct);
        }

        List<Event> events = kafkaEventReplayService.replayEvents(latestSnapshot.eventOffset, 0);
        for (Event event : events) {
            processedEvents++;

            if (event.getSuccess()) {
                SnapshotProduct state = skladisce.get(event.getId_product());

                switch (event.getType()) {
                    case "STOCK_ADDED":
                        state.zaloga += event.getQuantityAdd();
                        break;
                    case "STOCK_REMOVED":
                        state.zaloga -= event.getQuantityRemove();
                        break;
                    case "RESERVATION_ADDED":
                        state.zaloga -= event.getQuantityAdd();
                        state.rezervirano += event.getQuantityAdd();
                        break;
                    case "RESERVATION_REMOVED", "RESERVATION_EXPIRED":
                        state.rezervirano -= event.getQuantityRemove();
                        state.zaloga += event.getQuantityRemove();
                        break;
                    case "RESERVATION_UPDATED":
                        Integer razlika = event.getQuantityAdd() - event.getQuantityRemove();
                        state.zaloga -= razlika;
                        state.rezervirano += razlika;
                        break;
                    case "SOLD":
                        state.rezervirano -= event.getQuantityRemove();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Transactional
    public void createSnapshot() {
        Snapshot newSnapshot = new Snapshot();
        newSnapshot.eventOffset = latestSnapshot.eventOffset + snapshotFrequency;
        snapshotRepository.persist(newSnapshot);

        for (Map.Entry<Long, SnapshotProduct> entry : skladisce.entrySet()) {
            SnapshotProduct snapshotProduct = entry.getValue();
            SnapshotProduct newSnapshotProduct = new SnapshotProduct();
            newSnapshotProduct.id_snapshot = newSnapshot.id;
            newSnapshotProduct.id_izdelek = snapshotProduct.id_izdelek;
            newSnapshotProduct.zaloga = snapshotProduct.zaloga;
            newSnapshotProduct.rezervirano = snapshotProduct.rezervirano;

            snapshotProductRepository.persist(newSnapshotProduct);
        }

        latestSnapshot = newSnapshot;
        processedEvents = 0;
    }

    public PairDTO<ZalogaDTO, ErrorDTO> pridobiZalogo(Long id_izdelek) {
        SnapshotProduct state = skladisce.get(id_izdelek);
        if (state == null) {
            log.info("Not Found Error: Zaloge za izdelek z id=" + id_izdelek + " ni bilo mogoče najti");
            ErrorDTO notFoundError = new ErrorDTO(404, "Zaloge izdelka s podanim id_izdelek ni bilo mogoče najti!");
            return new PairDTO<>(null, notFoundError);
        }

        ZalogaDTO zaloga = new ZalogaDTO(id_izdelek, state.zaloga, state.rezervirano);

        return new PairDTO<>(zaloga, null);
    }

    @Transactional
    public PairDTO<ZalogaDTO, ErrorDTO> dodajNovIzdelek(ZalogaDTO zalogaDTO) {
        SnapshotProduct state = skladisce.get(zalogaDTO.getId_product());
        if (state != null) {
            log.info("Conflict Error: Zaloga za podani izdelek že obstaja");
            ErrorDTO conflictError = new ErrorDTO(409, "Zaloga za podani izdelek že obstaja.");
            return new PairDTO<>(null, conflictError);
        }

        SnapshotProduct snapshotProduct = new SnapshotProduct();
        snapshotProduct.id_snapshot = latestSnapshot.id;
        snapshotProduct.id_izdelek = zalogaDTO.getId_product();
        snapshotProduct.zaloga = zalogaDTO.getStock();
        snapshotProduct.rezervirano = zalogaDTO.getReserved();

        snapshotProductRepository.persist(snapshotProduct);
        skladisce.put(snapshotProduct.id_izdelek, snapshotProduct);

        ZalogaDTO zaloga = new ZalogaDTO(snapshotProduct.id_izdelek, snapshotProduct.zaloga, snapshotProduct.rezervirano);

        return new PairDTO<>(zaloga, null);
    }

    public void createEvent(RequestDTO requestDTO, Boolean success) {
        Event event = new Event();
        event.setId_request(requestDTO.getId_request());
        event.setType(requestDTO.getType());
        event.setId_product(requestDTO.getId_product());
        event.setId_user(requestDTO.getId_user());
        event.setQuantityAdd(requestDTO.getQuantityAdd());
        event.setQuantityRemove(requestDTO.getQuantityRemove());
        event.setTimestamp(new Timestamp(System.currentTimeMillis()));
        event.setSuccess(success);

        emitter.send(event);
    }

    public PairDTO<ResponseDTO, ErrorDTO> handleRequest(RequestDTO requestDTO) {
        SnapshotProduct state = skladisce.get(requestDTO.getId_product());
        if (state == null) {
            log.info("Not Found Error: Zaloge za izdelek z id=" + requestDTO.getId_product() + " ni bilo mogoče najti");
            ErrorDTO notFoundError = new ErrorDTO(404, "Zaloge za podani izdelek ni bilo mogoče najti.");
            return new PairDTO<>(null, notFoundError);
        }

        ResponseDTO responseDTO = new ResponseDTO();

        switch (requestDTO.getType()) {
            case "STOCK_ADDED":
                state.zaloga += requestDTO.getQuantityAdd();
                createEvent(requestDTO, true);
                responseDTO = new ResponseDTO(requestDTO.getId_request(), true);
                break;
            case "STOCK_REMOVED":
                if (state.zaloga >= requestDTO.getQuantityRemove()) {
                    state.zaloga -= requestDTO.getQuantityRemove();
                    createEvent(requestDTO, true);
                    responseDTO = new ResponseDTO(requestDTO.getId_request(), true);
                } else {
                    createEvent(requestDTO, false);
                    responseDTO = new ResponseDTO(requestDTO.getId_request(), false);
                }
                break;
            case "RESERVATION_ADDED":
                if (state.zaloga >= requestDTO.getQuantityAdd()) {
                    state.zaloga -= requestDTO.getQuantityAdd();
                    state.rezervirano += requestDTO.getQuantityAdd();
                    createEvent(requestDTO, true);
                    responseDTO = new ResponseDTO(requestDTO.getId_request(), true);
                } else {
                    createEvent(requestDTO, false);
                    responseDTO = new ResponseDTO(requestDTO.getId_request(), false);
                }
                break;
            case "RESERVATION_REMOVED", "RESERVATION_EXPIRED":
                state.rezervirano -= requestDTO.getQuantityRemove();
                state.zaloga += requestDTO.getQuantityRemove();
                createEvent(requestDTO, true);
                responseDTO = new ResponseDTO(requestDTO.getId_request(), true);
                break;
            case "RESERVATION_UPDATED":
                Integer razlika = requestDTO.getQuantityAdd() - requestDTO.getQuantityRemove();
                if (state.zaloga - razlika > 0) {
                    state.zaloga -= razlika;
                    state.rezervirano += razlika;
                    createEvent(requestDTO, true);
                    responseDTO = new ResponseDTO(requestDTO.getId_request(), true);
                } else {
                    createEvent(requestDTO, false);
                    responseDTO = new ResponseDTO(requestDTO.getId_request(), false);
                }
                break;
            case "SOLD":
                state.rezervirano -= requestDTO.getQuantityRemove();
                createEvent(requestDTO, true);
                responseDTO = new ResponseDTO(requestDTO.getId_request(), true);
                break;
            default:
                break;
        }

        processedEvents++;
        if (processedEvents == snapshotFrequency) {
            createSnapshot();
        }

        return new PairDTO<>(responseDTO, null);
    }
}
