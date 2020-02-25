
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class EnvelopeQueries extends AbstractFeatureServiceTest{

    //====================================Intersect========================================
    //  Default operation is : Intersects
    //testEnvelopeMarkLogicNeighborhoodIntersects
    @Test
    public void testEnvelopeIntersects1() {
        JsonPath postBody = getJson("testEnvelopeIntersects1.json");

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
                .body("features.size()", is(9))
                .body("features.properties.name", hasItems("MarkLogic HQ","Museum","Restaurant","Shopping Center","MarkLogic Neighborhood", "Wildlife Refuge","Airport","Hwy 101","Holly St"))
        ;
    }
    
    //testEnvelopeAirportIntersects
    @Test
    public void testEnvelopeIntersects2() {
        JsonPath postBody = getJson("testEnvelopeIntersects2.json");

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
                .body("features.size()", is(6))
                .body("features.properties.name", hasItems("Museum","MarkLogic Neighborhood", "Wildlife Refuge","Airport","Hwy 101","Holly St"))
        ; 
    }	
    
    //testEnvelopeShoppingCentreIntersects
    @Test
    public void testEnvelopeIntersects3() {
        JsonPath postBody = getJson("testEnvelopeIntersects3.json");

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
                .body("features[0].properties.name", is("Shopping Center"))
        ; 
    }
    
    //testEnvelopeWildlifeRefugeIntersects
    @Test
    public void testEnvelopeIntersects4() {
        JsonPath postBody = getJson("testEnvelopeIntersects4.json");

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
                .body("features.size()", is(4))
                .body("features.properties.name", hasItems("MarkLogic HQ","MarkLogic Neighborhood", "Wildlife Refuge","Airport"))
        ; 
    }

    //testEnvelopeAroundPointIntersects
    @Test
    public void testEnvelopeIntersects5() {
        JsonPath postBody = getJson("testEnvelopeIntersects5.json");

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
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("Restaurant","MarkLogic Neighborhood"))
        ; 
    }
    
    //testEnvelopeEmptyIntersects
    @Test
    public void testEnvelopeIntersects6() {
        JsonPath postBody = getJson("testEnvelopeIntersects6.json");

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
                .body("features.size()", is(0))
        ; 
    }
    
    //=========================Contains============================================
    //Inside single Envelope - Expected : MarkLogic Neighborhood
    @Test
    public void testEnvelopeContains1() {
        JsonPath postBody = getJson("testEnvelopeContains1.json");

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
                .body("features[0].properties.name", is("MarkLogic Neighborhood"))
        ; 
    }
    
    //Inside two Envelope - Expected : MarkLogic Neighborhood , Airport
    @Test
    public void testEnvelopeContains2() {
        JsonPath postBody = getJson("testEnvelopeContains2.json");

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
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("MarkLogic Neighborhood","Airport"))
        ;
    }

    //Inside a Envelope and intersecting 2 other features - Expected : MarkLogic Neighborhood 
    @Test
    public void testEnvelopeContains3() {
        JsonPath postBody = getJson("testEnvelopeContains3.json");

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
                .body("features[0].properties.name", is("MarkLogic Neighborhood"))
        ; 
    }
}
