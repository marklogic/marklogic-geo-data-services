import org.junit.Test;
import io.restassured.RestAssured;

import static org.hamcrest.Matchers.*;

public class ModelServiceTests extends AbstractTest {
    public void setup() {
        super.setup();
        RestAssured.basePath = "/LATEST/resources/modelService";
    }

    private String[] expectedModels = new String[] { "GeoLocation", "GDeltGKG", "DataSourceArrayExample" };

    @Test
    public void allModels() {
        RestAssured
            .get()
            .then()
                .log().ifError()
                .statusCode(200)
                .log().ifValidationFails()
                .body("models.size()", equalTo(expectedModels.length))
                .body("models.keySet()", hasItems(expectedModels))
        ;
    }

    @Test
    public void getModel() {
        for (String model : expectedModels) {
            RestAssured
                .given()
                    .queryParam("rs:id", model)
                .when()
                    .get()
                .then()
                    .log().ifError()
                    .statusCode(200)
                    .log().ifValidationFails()
                    .body("info.name", equalTo(model))
                    .body("layers.size()", greaterThan(0))
            ;
        }
    }
}
