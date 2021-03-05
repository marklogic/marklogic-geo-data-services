import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class MCGMQueries extends AbstractFeatureServiceTest {

    @Test
    public void testMCGMPolygon1() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPolygon1.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
        ;
    }

    @Test
    public void testMCGMGeometryPolygon1() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMGeometryPolygon1.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
        ;
    }

    @Test
    public void testMCGMPolygon2() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPolygon2.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testMCGMGeometryPolygon2() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMGeometryPolygon2.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testMCGMPolygon3() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPolygon3.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testMCGMGeometryPolygon3() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMGeometryPolygon3.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testMCGMPolygon4() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPolygon4.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testMCGMPolygon5() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPolygon5.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    @Test
    public void testMCGMGeometryPolygon5() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMGeometryPolygon5.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    // Envelope Test cases
    @Test
    public void testMCGMEnvelope1() {
        JsonPath postBody = getJson("testMCGMEnvelope1.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
        ;
    }

    @Test
    public void testMCGMGeometryEnvelope1() {
        JsonPath postBody = getJson("testMCGMGeometryEnvelope1.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
        ;
    }

    @Test
    public void testMCGMEnvelope2() {
        JsonPath postBody = getJson("testMCGMEnvelope2.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testMCGMGeometryEnvelope2() {
        JsonPath postBody = getJson("testMCGMGeometryEnvelope2.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testMCGMEnvelope3() {
        JsonPath postBody = getJson("testMCGMEnvelope3.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testMCGMGeometryEnvelope3() {
        JsonPath postBody = getJson("testMCGMGeometryEnvelope3.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }


    @Test
    public void testMCGMEnvelope4() {
        JsonPath postBody = getJson("testMCGMEnvelope4.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testMCGMEnvelope5() {
        JsonPath postBody = getJson("testMCGMEnvelope5.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    @Test
    public void testMCGMGeometryEnvelope5() {
        JsonPath postBody = getJson("testMCGMGeometryEnvelope5.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    //Point test cases
    @Test
    public void testMCGMPoint1() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPoint1.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testMCGMGeometryPoint1() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMGeometryPoint1.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testMCGMPoint2() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPoint2.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testMCGMPoint3() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMPoint3.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testMCGMGeometryPoint3() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMGeometryPoint3.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testMCGMAllFields() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testMCGMAllFields.json");

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
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }
}
