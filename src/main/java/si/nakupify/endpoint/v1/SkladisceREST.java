package si.nakupify.endpoint.v1;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import si.nakupify.service.SkladisceService;
import si.nakupify.service.dto.*;

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
                requestDTO.getTenant() == null || requestDTO.getTenant().isBlank() ||
                requestDTO.getQuantityAdd() == null || requestDTO.getQuantityAdd() < 0 ||
                requestDTO.getQuantityRemove() == null || requestDTO.getQuantityRemove() < 0) {
            log.info("Validation fail: RequestDTO mora imeti podana polja: id_product, id_user, tenant, type, quantityAdd, quantityRemove");
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

        if (zalogaDTO.getId_product() == null || zalogaDTO.getTenant() == null || zalogaDTO.getTenant().isBlank()) {
            log.info("Validation fail: ZalogaDTO mora imeti podana polja: id_product, tenant");
            String msg = "Polje id_product ne sme biti prazno!";
            return new ErrorDTO(400, msg);
        }

        return null;
    }


    @GET
    @Path("/zaloga/{id}")
    @Operation(
            summary="Pridobi zalogo",
            description="Pridobi zalogo za izdelek s podanim id.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="200",
                    description="(OK) Uspešno vrnjena zaloga.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ZalogaDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD_REQUEST) Podana nepravilna oblika URL.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="404",
                    description="(NOT_FOUND) Ni bilo mogoče najti vseh potrebnih podatkov.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
    public Response getZaloga(@PathParam("id") Long id) {
        if (id == null) {
            ErrorDTO parameterError = new ErrorDTO(400, "V URL mora biti podan parameter id.");
            log.info("Path parameter error: V URL ni podanega id");
            return Response.status(parameterError.getErrorCode()).entity(parameterError).build();
        }

        PairDTO<ZalogaDTO, ErrorDTO> pair = skladisceService.pridobiZalogo(id);
        ZalogaDTO zaloga = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(200).entity(zaloga).build();
    }

    @POST
    @Path("/zaloga")
    @Operation(
            summary="Ustvari novo zalogo",
            description="Ustvari in dodaj zalogo za izdelek.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="201",
                    description="(CREATED) Uspešno dodana zaloga za izdelek.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD_REQUEST) Podana nepravilna oblika vhodnega objekta ZalogaDTO.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="409",
                    description="(CONFLICT) Zaloga za podani izdelek že obstaja.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
    public Response createIzdelek(ZalogaDTO zalogaDTO) {
        ErrorDTO validationError = validacijaZalogaDTO(zalogaDTO);
        if (validationError != null) {
            return Response.status(400).entity(validationError).build();
        }

        PairDTO<ZalogaDTO, ErrorDTO> pair = skladisceService.dodajNovIzdelek(zalogaDTO);
        ZalogaDTO zaloga = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(201).entity(zaloga).build();
    }

    @POST
    @Operation(
            summary="Dodaj nov dogodek.",
            description="Dodaj nov dogodek v skladišče, kot so na primer: <br>" +
                    "STOCK_ADDED, STOCK_REMOVED, RESERVATION_ADDED, RESERVATION_REMOVED, RESERVATION_EXPIRED, RESERVATION_UPDATED, SOLD.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="201",
                    description="(CREATED) Uspešno dodan nov dogodek.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD_REQUEST) Podana nepravilna oblika vhodnega objekta RequestDTO.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="404",
                    description="(NOT_FOUND) Ni bilo mogoče najti vseh potrebnih podatkov.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
    public Response createEvent(RequestDTO requestDTO) {
        ErrorDTO validationError = validacijaRequestDTO(requestDTO);
        if (validationError != null) {
            return Response.status(400).entity(validationError).build();
        }

        PairDTO<ResponseDTO, ErrorDTO> pair = skladisceService.handleRequest(requestDTO);
        ResponseDTO response = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(201).entity(response).build();
    }
}
