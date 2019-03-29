import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class WhereTest extends AbstractFeatureServiceTest{

	@Test
    public void testGkgCountWhere() {

        String path = request2path("gkgCountWhere.json");

        JsonPath postBody = getJson("testGkgCountWhere.json");

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
                .body("count", is(197))
            ;
    }

	@Test
    public void testGkgWhereISNOTNULL() {

        JsonPath postBody = getJson("testGkgWhereISNOTNULL.json");

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
                .body("count", is(38765))
            ;
    }

	@Test
    public void testGkgWhereISNULL() {

        String path = request2path("whereISNULL.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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

	@Test
    public void testGkgWhereIn() {

        String path = request2path("gkgWhereIn.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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
                .body("objectIdFieldName", is("OBJECTID"))
                .body("globalIdFieldName", is(""))
                .body("hasZ", is(false))
                .body("hasM", is(false))

                .body("spatialReference.wkid", is(4326))

                .body("features.size()", is(2))
                .body("features[1].OBJECTID", is(56576))
                .body("features[1].urlpubtimedate", is(1495636200000L))
                .body("features[1].urlpubdate", is(1495584000000L))
                .body("features[1].url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
                .body("features[1].name", is("Australia"))
                .body("features[1].urltone", is(-3.91f))
                .body("features[1].domain", is("bendigoadvertiser.com.au"))
                .body("features[1].urllangcode", is("eng"))
                .body("features[1].geores", is(1))

                .body("features[0].OBJECTID", is(56577))
                .body("features[0].urlpubtimedate", is(1495636200000L))
                .body("features[0].urlpubdate", is(1495584000000L))
                .body("features[0].url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
                .body("features[0].name", is("Australia"))
                .body("features[0].urltone", is(-3.91f))
                .body("features[0].domain", is("bendigoadvertiser.com.au"))
                .body("features[0].urllangcode", is("eng"))
                .body("features[0].geores", is(1))

                .body("exceededTransferLimit", is(false))
            ;
    }


	@Test
    public void testGkgWhereNotIn() {

        String path = request2path("gkgWhereNotIn.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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
                .body("count", is(38763))
            ;
    }

	@Test
    public void testGkgtoDateWhere() {

        String path = request2path("toDateWhere.json");

        RestAssured
        .given()

        .when()
            .log().uri()
            .get(path)

        .then()
            .log().ifError()
            .statusCode(200)
            .log().ifValidationFails()
            .body("count", is(5427))
        ;
    }

    @Test
    public void testOneField() {

        String path = request2path("whereOneField.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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
                .body("features.size()", is(29))
                .body("features.domain", everyItem(isOneOf("nikkei.com")))
            ;
    }

    @Test
    public void testOrTwoFields() {

        String path = request2path("whereOr.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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
                .body("features.size()", is(177))
                .body("features.domain", everyItem(isOneOf("livetradingnews.com", "nikkei.com")))
            ;
    }

    @Test
    public void testBetweenDates() {

        //System.out.println("Method = " + Thread.currentThread().getStackTrace()[1].getMethodName());

        String path1 = request2path("whereBetweenDates1.json");

        RestAssured
            .given()
            .when()
                .log().uri()
                .get(path1)

            .then()
                .log().ifError()
                .statusCode(200)
                .log().ifValidationFails()
                .body("count", is(33338))
            ;

        String path2 = request2path("whereBetweenDates2.json");

        RestAssured
            .given()
            .when()
                .log().uri()
                .get(path2)

            .then()
                .log().ifError()
                .statusCode(200)
                .log().ifValidationFails()
                .body("count", is(5427))
            ;

    }

    @Test
    public void testBetweenDatesNoMatch() {

        String path = request2path("whereBetweenDatesNoMatch.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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

    @Test
    public void testGreaterThanDate() {

        String path = request2path("whereGreaterThanDate.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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
                .body("count", is(5427))
            ;
    }

    @Test
    public void testGreaterThanTimestamp() {

        String path = request2path("whereGreaterThanTimestamp.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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
                .body("count", is(33462))
            ;
    }

    @Test
    public void testLike() {
        String path = request2path("whereLike.json");

        JsonPath postBody = getJson("gkgToneStandardDeviation0.json");

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
                .body("features.size()", is(227))
                .body("features.domain", everyItem(containsString("journal")))
            ;
    }

}

