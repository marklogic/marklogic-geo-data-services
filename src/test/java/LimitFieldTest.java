import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class LimitFieldTest extends AbstractFeatureServiceTest{

	@Test
    public void testGkgLimitFields() {
        JsonPath postBody = getJson("testGkgLimitFields.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                //TODO missing .body("objectIdFieldName", is("OBJECTID"))
                //TODO missing .body("globalIdFieldName", is(""))
                //TODO missing .body("hasZ", is(false))
                //TODO missing .body("hasM", is(false))

                .body("spatialReference.wkid", is(4326))

                .body("fields.size()", is(3))
                .body("fields[0].name", is("OBJECTID"))
                .body("fields[0].type", is("esriFieldTypeOID"))
                .body("fields[2].name", is("domain"))
                .body("fields[2].type", is("esriFieldTypeString"))
                .body("fields[2].alias", is("domain"))

                .body("features.size()", is(20))

                .body("features[0].OBJECTID", is(1))
                .body("features[0].name", is("United Kingdom"))
                .body("features[0].domain", is("rtbf.be"))
                .body("features[0].geometry.x", is(-2))
                .body("features[0].geometry.y", is(54))

                .body("features[19].OBJECTID", is(43))
                .body("features[19].name", is("Greece"))
                .body("features[19].domain", is("candianews.gr"))
                .body("features[19].geometry.x", is(22))
                .body("features[19].geometry.y", is(39))
        ;
    }

    @Test
    public void testGkgAllLimitFields() {
        JsonPath postBody = getJson("testGkgAllLimitFields.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body("features.size()", is(5000))
        ;
    }

    @Test
    public void testGkgLimitResultRecordCount1() {
        JsonPath postBody = getJson("testGkgLimitResultRecordCount1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body("objectIds.size()", is(3500))
        ;
    }

    @Test
    public void testGkgLimitResultRecordCount2() {
        JsonPath postBody = getJson("testGkgLimitResultRecordCount2.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body("objectIds.size()", is(7000))
        ;
    }

    @Test
    public void testGkgLimitResultRecordCount3() {
        JsonPath postBody = getJson("testGkgLimitResultRecordCount3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body("features.size()", is(4000))
        ;
    }

    @Test
    public void testGkgLimitReturnIdsOnly0() {
        JsonPath postBody = getJson("testGkgLimitReturnIdsOnly0.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body("objectIds.size()", is(10))
        ;
    }
}

