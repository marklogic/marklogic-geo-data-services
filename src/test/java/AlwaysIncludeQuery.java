import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class AlwaysIncludeQuery extends AbstractFeatureServiceTest{

    @Test
    public void testGkgCountLayer() {
        JsonPath postBody = getJson("testGkgAlwaysIncludeCount.json");
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
                //this is a count of layers 0 + 1
                .body("count", is(38765 + 3557))
            ;
    }
}
