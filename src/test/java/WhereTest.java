import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

public class WhereTest extends AbstractFeatureServiceTest{

	@Test
    public void testGkgCountWhere() {
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
        JsonPath postBody = getJson("testGkgWhereISNULL.json");

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
        JsonPath postBody = getJson("testGkgWhereIn.json");

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
                //TODO missing .body("objectIdFieldName", is("OBJECTID"))
                //TODO missing .body("globalIdFieldName", is(""))
                //TODO missing .body("hasZ", is(false))
                //TODO missing .body("hasM", is(false))

                //TODO missing .body("spatialReference.wkid", is(4326))

                .body("features.size()", is(2))
                .body("features[1].properties.OBJECTID", is(56576))
                .body("features[1].properties.urlpubtimedate", is("2017-05-24T14:30:00Z"))
                .body("features[1].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[1].properties.url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
                .body("features[1].properties.name", is("Australia"))
                .body("features[1].properties.urltone", is(-3.91f))
                .body("features[1].properties.domain", is("bendigoadvertiser.com.au"))
                .body("features[1].properties.urllangcode", is("eng"))
                .body("features[1].properties.geores", is(1))

                .body("features[0].properties.OBJECTID", is(56577))
                .body("features[0].properties.urlpubtimedate", is("2017-05-24T14:30:00Z"))
                .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[0].properties.url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
                .body("features[0].properties.name", is("Australia"))
                .body("features[0].properties.urltone", is(-3.91f))
                .body("features[0].properties.domain", is("bendigoadvertiser.com.au"))
                .body("features[0].properties.urllangcode", is("eng"))
                .body("features[0].properties.geores", is(1))

                .body("metadata.limitExceeded", is(false))
        ;
    }


	@Test
    public void testGkgWhereNotIn() {
        JsonPath postBody = getJson("testGkgWhereNotIn.json");

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
        JsonPath postBody = getJson("testGkgtoDateWhere.json");

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
    public void testOneField() {
        JsonPath postBody = getJson("testOneField.json");

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
                .body("features.properties.domain", everyItem(isOneOf("nikkei.com")))
        ;
    }

    @Test
    public void testOrTwoFields() {
        JsonPath postBody = getJson("testOrTwoFields.json");

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
                .body("features.properties.domain", everyItem(isOneOf("livetradingnews.com", "nikkei.com")))
            ;
    }

    @Test
    public void testBetweenDates1() {
        JsonPath postBody = getJson("testBetweenDates1.json");

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
                .body("count", is(33338))
        ;
    }

    @Test
    public void testBetweenDates2() {
        JsonPath postBody = getJson("testBetweenDates2.json");

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
    public void testBetweenDatesNoMatch() {
        JsonPath postBody = getJson("testBetweenDatesNoMatch.json");

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
        JsonPath postBody = getJson("testGreaterThanDate.json");

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
        JsonPath postBody = getJson("testGreaterThanTimestamp.json");

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
        JsonPath postBody = getJson("testLike.json");

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
                .body("features.properties.domain", everyItem(containsString("journal")))
        ;
    }
}

