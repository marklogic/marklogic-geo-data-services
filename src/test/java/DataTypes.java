import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class DataTypes extends AbstractFeatureServiceTest {

    @Test
    public void testDefaultStringLength() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testDefaultStringLength.json");

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
                .body("metadata.fields.find { it.name == 'domain' }.length", is(1024))
        ;
    }
}
