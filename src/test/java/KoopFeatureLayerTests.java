import io.restassured.RestAssured;
import org.junit.*;

import static org.hamcrest.Matchers.is;

public class KoopFeatureLayerTests extends AbstractFeatureServiceTest {
    private static String endpoint = "/LATEST/resources/KoopFeatureLayer";

    @Test
    public void testServiceExists() {
        RestAssured
            .given()
                .param("rs:service", "GeoLocation")
            .when()
                .log().uri()
                .get(endpoint)
            .then()
                .log().ifError()
                .statusCode(200)
                .body("layers.size()", is(11))
            ;
    }

    @Test
    public void testServiceDoesntExist() {
        RestAssured
            .given()
                .param("rs:service", "__SERVICE_MODEL_THAT_SHOULDNT_EXIST__")
            .when()
                .log().uri()
                .get(endpoint)
            .then()
                .statusCode(404)
            ;
    }

    @Test
    public void testNoServiceParameter() {
        RestAssured
            .given()
            .when()
                .log().uri()
                .get(endpoint)
            .then()
                .statusCode(400)
            ;
    }    
}
