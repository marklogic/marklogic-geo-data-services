
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class GeoServerTest extends AbstractFeatureServiceTest{

    public void setup() {
        super.setup();
        RestAssured.basePath = "/LATEST/resources/geoQueryService";
    }

    @Test
    public void testGetLayerNames() {
        JsonPath postBody = getJson("testGetLayerNames.json");

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
                .body("0", is("DataSourceArrayExample_0_Points"))
                .body("1", is("DataSourceArrayExample_1_Points"))
                .body("2", is("DataSourceArrayExample_2_Points"))
                .body("3", is("DataSourceArrayExample_3_Polygons"))
                .body("4", is("DataSourceArrayExample_4_Points"))
                .body("5", is("DataSourceArrayExample_5_Points"))
                .body("6", is("DataSourceArrayExample_6_Points"))
                .body("7", is("DataSourceArrayExample_7_Points"))
                .body("8", is("DataSourceArrayExample_8_Points"))
                .body("9", is("DataSourceArrayExample_9_Points"))
                .body("10", is("DataSourceArrayExample_10_Points"))
                .body("11", is("DataSourceArrayExample_11_Polygons"))
                .body("12", is("DataSourceArrayExample_12_Points"))
                .body("13", is("DataSourceArrayExample_13_Points"))
                .body("14", is("DataSourceArrayExample_14_Points"))
                .body("15", is("DataSourceArrayExample_15_Points"))
        ;
    }
    
    @Test
    public void testGetLayerSchema() {
        JsonPath postBody = getJson("testGetLayerSchema.json");

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
                .body("metadata.id", is(0))
        ;
    }

    @Test
    public void testViewAsRoot() {
        JsonPath postBody = getJson("testViewAsRoot.json");

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
                .body("metadata.id", is(5));
    }

    @Test
    public void testDataSourceWithViewAsRoot() {
        JsonPath postBody = getJson("testDataSourceWithViewAsRoot.json");

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
                .body("metadata.id", is(6));
    }

    @Test
    public void testDataSourcesWithLeftOuterJoin() {
        JsonPath postBody = getJson("testDataSourcesWithLeftOuterJoin.json");

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
                .body("metadata.id", is(12));
    }

    @Test
    public void testDataSourcesWithFullOuterJoin() {
        JsonPath postBody = getJson("testDataSourcesWithFullOuterJoin.json");

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
                .body("metadata.id", is(13));
    }

    @Test
    public void testDataSourceWithViewAsRootAndSparqlJoin() {
        JsonPath postBody = getJson("testDataSourceWithViewAsRootAndSparqlJoin.json");

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
                .body("metadata.id", is(7));
    }

    @Test
    public void testDataSourceWithSparqlAsRoot() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRoot.json");

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
                .body("metadata.id", is(8));
    }



    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithFieldsElement() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRootAndViewJoinWithFieldsElement.json");

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
                .body("metadata.id", is(9));
    }

    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElement() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElement.json");

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
                .body("metadata.id", is(10));
    }

    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElementStats() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElementStats.json");

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
                .body("metadata.id", is(11));
    }
}