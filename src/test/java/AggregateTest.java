import com.marklogic.gds.Query;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class AggregateTest extends AbstractFeatureServiceTest {

    @Test
    public void testGkgObjectIds() {
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(new Query(0)
                    .withObjectIds("56577", "56576")
                    .returnCountOnly()
                    .toString()
                )
            .when()
                .post()
            .then()
                .statusCode(200)
                .body("features.size()", is(0))
                .body("count", is(2))
        ;
    }
}
