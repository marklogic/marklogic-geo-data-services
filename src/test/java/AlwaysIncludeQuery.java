import com.marklogic.gds.Query;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class AlwaysIncludeQuery extends AbstractFeatureServiceTest{

    @Test
    public void testGkgCountLayer() {
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(new Query(7).returnCountOnly().toString())
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
