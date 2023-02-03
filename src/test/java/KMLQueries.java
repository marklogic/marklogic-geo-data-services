import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class KMLQueries extends AbstractFeatureServiceTest {

    @Test
    public void testKMLPolygon1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPolygon1.json");

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
    public void testKMLGeometryPolygon1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLGeometryPolygon1.json");

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
    public void testKMLPolygon2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPolygon2.json");

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
    public void testKMLGeometryPolygon2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLGeometryPolygon2.json");

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
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
            ;
    }

    @Test
    public void testKMLPolygon3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPolygon3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testKMLGeometryPolygon3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLGeometryPolygon3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
    }

    @Test
    public void testKMLPolygon4() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPolygon4.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testKMLPolygon5() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPolygon5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
        ;
    }

    @Test
    public void testKMLGeometryPolygon5() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLGeometryPolygon5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

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
    public void testKMLEnvelope1() {
        JsonPath postBody = getJson("testKMLEnvelope1.json");

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
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))            ;
       }

    @Test
    public void testKMLGeometryEnvelope1() {
        JsonPath postBody = getJson("testKMLGeometryEnvelope1.json");

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
    public void testKMLEnvelope2() {
        JsonPath postBody = getJson("testKMLEnvelope2.json");

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
    public void testKMLGeometryEnvelope2() {
        JsonPath postBody = getJson("testKMLGeometryEnvelope2.json");

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
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
            ;
       }

    @Test
    public void testKMLEnvelope3() {
        JsonPath postBody = getJson("testKMLEnvelope3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
       }

    @Test
    public void testKMLGeometryEnvelope3() {
        JsonPath postBody = getJson("testKMLGeometryEnvelope3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
        ;
       }


    @Test
    public void testKMLEnvelope4() {
        JsonPath postBody = getJson("testKMLEnvelope4.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
       }

    @Test
    public void testKMLEnvelope5() {
        JsonPath postBody = getJson("testKMLEnvelope5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
            ;
       }

    @Test
    public void testKMLGeometryEnvelope5() {
        JsonPath postBody = getJson("testKMLGeometryEnvelope5.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"));
       }

    //Point test cases
    @Test
    public void testKMLPoint1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPoint1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testKMLGeometryPoint1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLGeometryPoint1.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Rajasthan"))
        ;
    }

    @Test
    public void testKMLPoint2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPoint2.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }

    @Test
    public void testKMLPoint3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLPoint3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testKMLGeometryPoint3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLGeometryPoint3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Odisha"))
        ;
    }

    @Test
    public void testKMLAllFields() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testKMLAllFields.json");

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
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
        ;
    }
}



///marklogic/GeoLocation/FeatureServer/0/query?outFields=*
