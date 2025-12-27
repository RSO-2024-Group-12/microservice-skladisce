package si.nakupify;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import si.nakupify.service.SkladisceService;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.PairDTO;
import si.nakupify.service.dto.ResponseDTO;
import si.nakupify.service.dto.ZalogaDTO;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class SkladisceEndpointTest {

    @InjectMock
    SkladisceService skladisceService;

    private ZalogaDTO zalogaDTO(Long id, Integer stock, Integer reserved) {
        ZalogaDTO zalogaDTO = new ZalogaDTO();
        zalogaDTO.setId_product(id);
        zalogaDTO.setStock(stock);
        zalogaDTO.setReserved(reserved);
        return zalogaDTO;
    }

    private ResponseDTO responseDTO() {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setId_request("test");
        responseDTO.setStatus(true);
        return responseDTO;
    }

    private ErrorDTO errorDTO(int code, String message) {
        return new ErrorDTO(code, message);
    }

    @Test
    void getZaloga_test() {
        when(skladisceService.pridobiZalogo(1L)).thenReturn(new PairDTO<>(zalogaDTO(1L, 100, 0), null));

        given()
                .accept(ContentType.JSON)
        .when()
                .get("/v1/skladisce/zaloga/1")
        .then()
                .statusCode(200)
                .body("id_product", equalTo(1))
                .body("stock", equalTo(100))
                .body("reserved", equalTo(0));

        verify(skladisceService).pridobiZalogo(1L);
    }

    @Test
    void createIzdelek_test() {
        when(skladisceService.dodajNovIzdelek(any())).thenReturn(new PairDTO<>(zalogaDTO(5L, 0, 0), null));

        String requestBody = """
        {
            "id_product": 5,
            "stock": 0,
            "reserved": 0
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/v1/skladisce/zaloga")
        .then()
                .statusCode(201)
                .body("id_product", equalTo(5))
                .body("stock", equalTo(0))
                .body("reserved", equalTo(0));

        verify(skladisceService).dodajNovIzdelek(any());
    }

    @Test
    void createEvent_test() {
        when(skladisceService.handleRequest(any())).thenReturn(new PairDTO<>(responseDTO(), null));

        String requestBody = """
        {
            "id_request": "test",
                "type": "STOCK_ADDED",
                "id_product": 1,
                "id_user": 1,
                "quantityAdd": 100,
                "quantityRemove": 0
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/v1/skladisce")
        .then()
                .statusCode(201)
                .body("id_request", equalTo("test"))
                .body("status", equalTo(true));

        verify(skladisceService).handleRequest(any());
    }
}
