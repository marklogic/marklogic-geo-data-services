import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

public class ModelServiceTest extends AbstractTest {
    public void setup() {
        super.setup();
        RestAssured.basePath = "/LATEST/resources/modelService";
    }

    private String[] expectedModels = new String[] { "GeoLocation", "GDeltGKG", "GDeltSearch", "DataSourceArrayExample" };

    @Test
    public void allModels() {
        RestAssured
            .get()
            .then()

                .statusCode(200)

                .body("models.size()", equalTo(expectedModels.length))
                .body("models.keySet()", hasItems(expectedModels))
        ;
    }

    @Test
    public void allSearchEnabledModels() {
        RestAssured
            .given()
                .queryParam("rs:filter", "search")
            .when()
                .get()
            .then()

                .statusCode(200)

                .body("models.size()", equalTo(1))
                .body("models.keySet()", hasItem("GDeltSearch" ))
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

                    .statusCode(200)

                    .body("info.name", equalTo(model))
                    .body("layers.size()", greaterThan(0))
            ;
        }
    }
}
