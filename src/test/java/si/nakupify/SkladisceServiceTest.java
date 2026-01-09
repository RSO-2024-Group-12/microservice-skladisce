package si.nakupify;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import si.nakupify.entity.Event;
import si.nakupify.entity.Snapshot;
import si.nakupify.entity.SnapshotProduct;
import si.nakupify.service.KafkaEventReplayService;
import si.nakupify.service.SkladisceService;
import si.nakupify.service.dto.*;
import si.nakupify.service.repository.SnapshotProductRepository;
import si.nakupify.service.repository.SnapshotRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@QuarkusTest
public class SkladisceServiceTest {

    @InjectSpy
    SkladisceService skladisceService;

    @InjectMock
    SnapshotRepository snapshotRepository;

    @InjectMock
    SnapshotProductRepository snapshotProductRepository;

    @InjectMock
    KafkaEventReplayService kafkaEventReplayService;

    private ZalogaDTO makeZalogaDTO(Long id, String tenant, Integer stock, Integer reserved) {
        ZalogaDTO zalogaDTO = new ZalogaDTO();
        zalogaDTO.setId_product(id);
        zalogaDTO.setTenant(tenant);
        zalogaDTO.setStock(stock);
        zalogaDTO.setReserved(reserved);
        return zalogaDTO;
    }

    @BeforeEach
    void setup() {
        Snapshot snapshot = new Snapshot();
        snapshot.id = 1L;
        snapshot.eventOffset = 0L;
        when(snapshotRepository.getLatestSnapshot()).thenReturn(snapshot);

        SnapshotProduct product = new SnapshotProduct();
        product.id_izdelek = 1L;
        product.tenant = "org1";
        product.zaloga = 100;
        product.rezervirano = 20;
        when(snapshotProductRepository.findBySnapshotId(snapshot.id)).thenReturn(List.of(product));

        when(kafkaEventReplayService.replayEvents(anyLong(), anyInt())).thenReturn(Collections.emptyList());

        skladisceService.pridobiSkladisce();
    }

    @Test
    void pridobiZalogo_test() {
        PairDTO<ZalogaDTO, ErrorDTO> result = skladisceService.pridobiZalogo(1L);

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        ZalogaDTO zalogaDTO = result.getValue();

        assertEquals(1L, zalogaDTO.getId_product());
        assertEquals("org1", zalogaDTO.getTenant());
        assertEquals(100, zalogaDTO.getStock());
        assertEquals(20 , zalogaDTO.getReserved());

        verify(snapshotRepository, atLeastOnce()).getLatestSnapshot();
        verify(snapshotProductRepository, atLeastOnce()).findBySnapshotId(1L);
        verify(kafkaEventReplayService).replayEvents(0L, 0);
    }

    @Test
    void dodajNovIzdelek_test() {
        doAnswer(invocation -> {
            SnapshotProduct snapshotProduct = invocation.getArgument(0);
            snapshotProduct.id = 2L;
            return null;
        }).when(snapshotProductRepository).persist(any(SnapshotProduct.class));

        PairDTO<ZalogaDTO, ErrorDTO> result = skladisceService.dodajNovIzdelek(makeZalogaDTO(2L, "org1", 0, 0));

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        ZalogaDTO zalogaDTO = result.getValue();

        assertEquals(2L, zalogaDTO.getId_product());
        assertEquals("org1", zalogaDTO.getTenant());
        assertEquals(0, zalogaDTO.getStock());
        assertEquals(0 , zalogaDTO.getReserved());

        verify(snapshotProductRepository).persist(any(SnapshotProduct.class));
    }

    @Test
    void handleRequest_test() {
        Emitter<Event> emitterMock = mock(Emitter.class);
        skladisceService.setEmmiter(emitterMock);

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setId_request("test");
        requestDTO.setType("STOCK_ADDED");
        requestDTO.setId_product(1L);
        requestDTO.setId_user(1L);
        requestDTO.setTenant("org1");
        requestDTO.setQuantityAdd(100);
        requestDTO.setQuantityRemove(0);

        PairDTO<ResponseDTO, ErrorDTO> result = skladisceService.handleRequest(requestDTO);

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        ResponseDTO responseDTO = result.getValue();

        assertEquals("test", responseDTO.getId_request());
        assertEquals(true, responseDTO.getStatus());

        verify(emitterMock).send(any(Event.class));
    }
}
