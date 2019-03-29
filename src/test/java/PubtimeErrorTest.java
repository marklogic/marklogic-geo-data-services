import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class PubtimeErrorTest extends AbstractFeatureServiceTest{

	@Test
    public void testGkgPubtimeError0() {

        JsonPath postBody = getJson("testGkgPubtimeError0.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .statusCode(200)
	            .log().ifValidationFails()
	            .body("count", is(0))
        ;
    }
}
