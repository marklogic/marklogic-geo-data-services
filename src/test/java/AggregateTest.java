import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class AggregateTest extends AbstractFeatureServiceTest {

    @Test
    public void testGkgObjectIds() {
        JsonPath postBody = getJson("gkgObjectIds.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post()
            .then()
                .log().ifError()
                .statusCode(200)
                .log().ifValidationFails()

                .body("features.size()", is(0))
                .body("count", is(2))
        ;
    }
}
