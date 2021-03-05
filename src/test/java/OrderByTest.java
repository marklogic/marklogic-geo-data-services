import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class OrderByTest extends AbstractFeatureServiceTest {

	@Test
    public void testGkgOrderbyTop10() {
        JsonPath postBody = getJson("testGkgOrderbyTop10.json");

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
                .body("features.size()", is(10))

                .body("features[0].properties.OBJECTID", is(8991))
                .body("features[0].properties.urlpubtimedate", is("2017-05-24T06:00:00Z"))
                .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[0].properties.url", is("http://zz.diena.lv/kriminalzinas/vugd/maras-ielas-kapnutelpa-deg-atkritumi-229596"))
                .body("features[0].properties.name", is("Latvia"))
                .body("features[0].properties.urltone", is(-2.86f))
                .body("features[0].properties.domain", is("zz.diena.lv"))
                .body("features[0].properties.urllangcode", is("lav"))
                .body("features[0].properties.geores", is(1))

                .body("features[9].properties.OBJECTID", is(31999))
                .body("features[9].properties.urlpubtimedate", is("2017-05-24T11:00:00Z"))
                .body("features[9].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[9].properties.url", is("http://zpravy.idnes.cz/cholera-cesko-nakaza-0la-/domaci.aspx"))
                .body("features[9].properties.name", is("Ukraine"))
                .body("features[9].properties.urltone", is(-0.57f))
                .body("features[9].properties.domain", is("zpravy.idnes.cz"))
                .body("features[9].properties.urllangcode", is("ces"))
                .body("features[9].properties.geores", is(1))
        ;
    }

    @Test
    public void testGkgOrderbyLeadingWhitespace() {
        JsonPath postBody = getJson("testGkgOrderbyLeadingWhitespace.json");

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
                //TODO missing .body("objectIdFieldName", is("OBJECTID"))
                //TODO missing .body("globalIdFieldName", is(""))
                //TODO missing .body("hasZ", is(false))
                //TODO missing .body("hasM", is(false))
                //TODO missing .body("spatialReference.wkid", is(4326))
                .body("features.size()", is(10))
        ;
    }

}
