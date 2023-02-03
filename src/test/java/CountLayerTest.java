import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class CountLayerTest extends AbstractFeatureServiceTest{

    @Test
    public void testGkgCountLayer0() {
        JsonPath postBody = getJson("gkgCountLayer0.json");
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .post()
            .then()
                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("count", is(38765))
            ;
    }

    @Test
    public void testGkgCountLayer1() {
        JsonPath postBody = getJson("gkgCountLayer1.json");
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .post()
            .then()
                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("count", is(3557))
            ;
    }
}
