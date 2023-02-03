import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class GMLQueries extends AbstractFeatureServiceTest {

    @Test
    public void testGMLPolygon1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPolygon1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
        ;
    }

    @Test
    public void testGMLGeometryPolygon1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLGeometryPolygon1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
        ;
    }

    @Test
    public void testGMLPolygon2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPolygon2.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testGMLGeometryPolygon2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLGeometryPolygon2.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testGMLPolygon3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPolygon3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testGMLGeometryPolygon3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLGeometryPolygon3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testGMLPolygon4() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPolygon4.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testGMLPolygon5() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPolygon5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    @Test
    public void testGMLGeometryPolygon5() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLGeometryPolygon5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    // Envelope Test cases
    @Test
    public void testGMLEnvelope1() {
        JsonPath postBody = getJson("testGMLEnvelope1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
        ;
    }

    @Test
    public void testGMLGeometryEnvelope1() {
        JsonPath postBody = getJson("testGMLGeometryEnvelope1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
        ;
    }

    @Test
    public void testGMLEnvelope2() {
        JsonPath postBody = getJson("testGMLEnvelope2.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testGMLGeometryEnvelope2() {
        JsonPath postBody = getJson("testGMLGeometryEnvelope2.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }

    @Test
    public void testGMLEnvelope3() {
        JsonPath postBody = getJson("testGMLEnvelope3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testGMLGeometryEnvelope3() {
        JsonPath postBody = getJson("testGMLGeometryEnvelope3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }


    @Test
    public void testGMLEnvelope4() {
        JsonPath postBody = getJson("testGMLEnvelope4.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testGMLEnvelope5() {
        JsonPath postBody = getJson("testGMLEnvelope5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    @Test
    public void testGMLGeometryEnvelope5() {
        JsonPath postBody = getJson("testGMLGeometryEnvelope5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    //Point test cases
    @Test
    public void testGMLPoint1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPoint1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testGMLGeometryPoint1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLGeometryPoint1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testGMLPoint2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPoint2.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testGMLPoint3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLPoint3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testGMLGeometryPoint3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLGeometryPoint3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testGMLAllFields() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testGMLAllFields.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)

                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }
}
