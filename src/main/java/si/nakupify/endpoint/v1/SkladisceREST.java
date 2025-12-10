package si.nakupify.endpoint.v1;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import si.nakupify.service.SkladisceService;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.RequestDTO;
import si.nakupify.service.dto.ResponseDTO;
import si.nakupify.service.dto.ZalogaDTO;

import java.util.Arrays;
import java.util.logging.Logger;

@Path("/v1/skladisce")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SkladisceREST {

    @Inject
    SkladisceService skladisceService;

    private final String[] eventTypes = {"STOCK_ADDED", "STOCK_REMOVED", "RESERVATION_ADDED", "RESERVATION_REMOVED", "RESERVATION_EXPIRED", "RESERVATION_UPDATED", "SOLD"};

    private Logger log = Logger.getLogger(SkladisceREST.class.getName());

    public ErrorDTO validacijaRequestDTO(RequestDTO requestDTO) {
        if (requestDTO == null) {
            log.info("Validation fail: RequestDTO ne sme biti null");
            String msg = "Mora biti podan RequestDTO!";
            return new ErrorDTO(400, msg);
        }

        if (requestDTO.getId_request() == null) {
            log.info("Validation fail: RequestDTO mora imeti podana polja: id_request");
            String msg = "Polje id_request mora biti podano!";
            return new ErrorDTO(400, msg);
        }

        if (requestDTO.getId_product() == null || requestDTO.getId_user() == null ||
                requestDTO.getType() == null || requestDTO.getType().isBlank() ||
                requestDTO.getQuantityAdd() == null || requestDTO.getQuantityAdd() < 0 ||
                requestDTO.getQuantityRemove() == null || requestDTO.getQuantityRemove() < 0) {
            log.info("Validation fail: RequestDTO mora imeti podana polja: id_product, id_user, type, quantityAdd, quantityRemove");
            String msg = "Polja id_product, id_user, type, quantityAdd, quantityRemove ne smejo biti prazna!";
            return new ErrorDTO(400, msg);
        }

        if (!Arrays.asList(eventTypes).contains(requestDTO.getType())) {
            log.info("Validation fail: RequestDTO ima neveljaven type");
            String msg = "Podan neveljaven type!";
            return new ErrorDTO(400, msg);
        }

        return null;
    }

    public ErrorDTO validacijaZalogaDTO(ZalogaDTO zalogaDTO) {
        if (zalogaDTO == null) {
            log.info("Validation fail: ZalogaDTO ne sme biti null");
            String msg = "Mora biti podan ZalogaDTO!";
            return new ErrorDTO(400, msg);
        }

        if (zalogaDTO.getId_product() == null) {
            log.info("Validation fail: ZalogaDTO mora imeti podana polja: id_product");
            String msg = "Polje id_product ne sme biti prazno!";
            return new ErrorDTO(400, msg);
        }

        return null;
    }


    @GET
    @Path("/zaloga/{id}")
    public Response getZaloga(@PathParam("id") Long id) {
        if (id == null) {
            ErrorDTO parameterError = new ErrorDTO(400, "V URL mora biti podan parameter id.");
            log.info("Path parameter error: V URL ni podanega id");
            return Response.status(Response.Status.BAD_REQUEST).entity(parameterError).build();
        }

        ZalogaDTO zaloga = skladisceService.pridobiZalogo(id);
        if (zaloga == null) {
            ErrorDTO notFoundError = new ErrorDTO(404, "Zaloge izdelka s podanim id_izdelek ni bilo mogoče najti!");
            return Response.status(Response.Status.NOT_FOUND).entity(notFoundError).build();
        }

        return Response.status(Response.Status.OK).entity(zaloga).build();
    }

    @POST
    @Path("/zaloga")
    public Response createIzdelek(ZalogaDTO zalogaDTO) {
        ErrorDTO validationError = validacijaZalogaDTO(zalogaDTO);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(validationError).build();
        }

        ZalogaDTO zaloga = skladisceService.dodajNovIzdelek(zalogaDTO);
        if (zaloga == null) {
            ErrorDTO conflictError = new ErrorDTO(400, "Zaloga izdelka s podanim id_izdelek že obstaja!");
            return Response.status(Response.Status.CONFLICT).entity(conflictError).build();
        }

        return Response.status(Response.Status.CREATED).entity(zaloga).build();
    }

    @POST
    public Response createEvent(RequestDTO requestDTO) {
        ErrorDTO validationError = validacijaRequestDTO(requestDTO);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(validationError).build();
        }

        ResponseDTO response = skladisceService.handleRequest(requestDTO);
        if (response == null) {
            ErrorDTO notFoundError = new ErrorDTO(400, "Zaloge izdelka s podanim id_izdelek ni bilo mogoče najti!");
            return Response.status(Response.Status.NOT_FOUND).entity(notFoundError).build();
        }

        return Response.status(Response.Status.OK).entity(response).build();
    }
}
