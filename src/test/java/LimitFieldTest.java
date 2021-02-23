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
                .post()
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body(isValidFeatureCollection())
                //TODO missing .body("objectIdFieldName", is("OBJECTID"))
                //TODO missing .body("globalIdFieldName", is(""))
                //TODO missing .body("hasZ", is(false))
                //TODO missing .body("hasM", is(false))

                //TODO missing .body("spatialReference.wkid", is(4326))

                .body("metadata.fields.size()", is(3))
                .body("metadata.fields[0].name", is("OBJECTID"))
                .body("metadata.fields[0].type", is("Integer"))
                .body("metadata.fields[1].name", is("name"))
                .body("metadata.fields[1].type", is("String"))
                .body("metadata.fields[2].name", is("domain"))
                .body("metadata.fields[2].type", is("String"))
                .body("metadata.fields[2].alias", IsNull.nullValue())

                .body("features.size()", is(20))

                .body("features[0].properties.OBJECTID", is(1))
                .body("features[0].properties.name", is("United Kingdom"))
                .body("features[0].properties.domain", is("rtbf.be"))
                .body("features[0].geometry.coordinates[0]", is(-2)) // x
                .body("features[0].geometry.coordinates[1]", is(54)) // y

                .body("features[19].properties.OBJECTID", is(43))
                .body("features[19].properties.name", is("Greece"))
                .body("features[19].properties.domain", is("candianews.gr"))
                .body("features[19].geometry.coordinates[0]", is(22)) // x
                .body("features[19].geometry.coordinates[1]", is(39)) // y
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
                .post()
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body(isValidFeatureCollection())
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
                .post()
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body(isValidFeatureCollection())
                .body("features.size()", is(3500))
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
                .post()
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body(isValidFeatureCollection())
                .body("features.size()", is(7000))
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
                .post()
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body(isValidFeatureCollection())
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
                .post()
            .then()
                .log().ifError()
                .log().all()
                .statusCode(200)
                .log().ifValidationFails()
                .body(isValidFeatureCollection())
                .body("features.size()", is(10))
        ;
    }
}

