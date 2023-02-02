import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FieldsTest extends AbstractFeatureServiceTest {

    @Test
    public void testAllFields() {
        JsonPath postBody = getJson("testAllFields.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features[0].properties.OBJECTID", notNullValue())
                .body("features[0].properties.urlpubtimedate", notNullValue())
                .body("features[0].properties.urlpubdate", notNullValue())
                .body("features[0].properties.url", notNullValue())
                .body("features[0].properties.name", notNullValue())
                .body("features[0].properties.urltone", notNullValue())
                .body("features[0].properties.domain", notNullValue())
                .body("features[0].properties.urllangcode", notNullValue())
                .body("features[0].properties.geores", notNullValue())
                .body("features[0].geometry.coordinates[0]", notNullValue()) // was x
                .body("features[0].geometry.coordinates[1]", notNullValue()) // was y
            ;
    }
}
