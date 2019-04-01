import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class RSSQueries extends AbstractFeatureServiceTest {

    @Test
    public void testRSSPolygon1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPolygon1.json");

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
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
        ;
    }

    @Test
    public void testRSSGeometryPolygon1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSGeometryPolygon1.json");

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
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
        ;
    }

    @Test
    public void testRSSPolygon2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPolygon2.json");

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
                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testRSSGeometryPolygon2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSGeometryPolygon2.json");

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
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testRSSPolygon3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPolygon3.json");

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
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testRSSGeometryPolygon3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSGeometryPolygon3.json");

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
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testRSSPolygon4() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPolygon4.json");

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
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testRSSPolygon5() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPolygon5.json");

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
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    @Test
    public void testRSSGeometryPolygon5() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSGeometryPolygon5.json");

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
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    // Envelope Test cases
    @Test
    public void testRSSEnvelope1() {
        JsonPath postBody = getJson("testRSSEnvelope1.json");

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
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
        ;
    }

    @Test
    public void testRSSGeometryEnvelope1() {
        JsonPath postBody = getJson("testRSSGeometryEnvelope1.json");

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
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
        ;
    }

    @Test
    public void testRSSEnvelope2() {
        JsonPath postBody = getJson("testRSSEnvelope2.json");

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
                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testRSSGeometryEnvelope2() {
        JsonPath postBody = getJson("testRSSGeometryEnvelope2.json");

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
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testRSSEnvelope3() {
        JsonPath postBody = getJson("testRSSEnvelope3.json");

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
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testRSSGeometryEnvelope3() {
        JsonPath postBody = getJson("testRSSGeometryEnvelope3.json");

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
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }


    @Test
    public void testRSSEnvelope4() {
        JsonPath postBody = getJson("testRSSEnvelope4.json");

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
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testRSSEnvelope5() {
        JsonPath postBody = getJson("testRSSEnvelope5.json");

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
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    @Test
    public void testRSSGeometryEnvelope5() {
        JsonPath postBody = getJson("testRSSGeometryEnvelope5.json");

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
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    //Point test cases
    @Test
    public void testRSSPoint1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPoint1.json");

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
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testRSSGeometryPoint1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSGeometryPoint1.json");

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
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testRSSPoint2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPoint2.json");

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
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testRSSPoint3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSPoint3.json");

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
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testRSSGeometryPoint3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSGeometryPoint3.json");

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
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testRSSAllFields() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testRSSAllFields.json");

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
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }
}
