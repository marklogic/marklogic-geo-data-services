import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems; import static org.hamcrest.Matchers.is;

public class WKTGeometry  extends AbstractFeatureServiceTest {

    // "geometry" : {
    //   "type" : "Polygon",
    //   "format" : "wkt",
    //   "coordinateSystem" : "wgs84",
    //   "source" : {
    //     "xpath" : "/envelope/instance/boundary"
    //   }
    // }

    @Test
    public void testXPathExtraction() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testXPathExtraction.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].geometry.coordinates.size()", is(1))
                .body("features[0].geometry.coordinates[0].size()", is(5))
                .body("features[0].geometry.coordinates[0][0].size()", is(2))
                .body("features[0].geometry.coordinates[0][0]", hasItems(30, 10))
        ;
    }

    // "geometry" : {
    //   "type" : "Polygon",
    //   "format" : "wkt",
    //   "coordinateSystem" : "wgs84",
    //   "source" : {
    //     "column" : "boundary"
    //   }
    // }
    @Test
    public void testColumnExtraction() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testColumnExtraction.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].geometry.coordinates.size()", is(1))
                .body("features[0].geometry.coordinates[0].size()", is(5))
                .body("features[0].geometry.coordinates[0][0].size()", is(2))
                .body("features[0].geometry.coordinates[0][0]", hasItems(30, 10))
        ;
    }
}
