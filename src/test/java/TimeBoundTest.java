import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.RestAssured;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TimeBoundTest extends AbstractFeatureServiceTest {
    Long minBound = 1493596800000L;
    Long maxBound = 1496275200000L;

    @Test
    public void testTimeBound() {

        JsonPath postBody = getJson("gkgTimeBound.json");

        Response response = 
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
                    .log().all()
                    .body(isValidFeatureCollection())
                    .extract().response()
            ;

        List<Long> jsonResponse = response.jsonPath().getList("features.attributes.urlpubtimedate");

        //Assert that each feature returned is within the time bounds
        for (Long item : jsonResponse) {
            if(item < minBound || item > maxBound) {
                System.out.println(item + " is NOT between " + minBound + " and "+ maxBound);
            }

            assertTrue(item >= minBound);
            assertTrue(item <= maxBound);
        }
    }

    // Support numeric parameters as described in 
    // https://doc.arcgis.com/en/operations-dashboard/help/url-parameters.htm
    @Test
    public void testTimeBoundLeftOpen() {

        JsonPath postBody = getJson("gkgTimeBoundLeftOpen.json");

        Response response = 
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
                    .log().ifValidationFails()
                    .body(isValidFeatureCollection())
                    .extract().response()
            ;

        List<Long> jsonResponse = response.jsonPath().getList("features.attributes.urlpubtimedate");

        //Assert that each feature returned is within the time bounds
        for (Long item : jsonResponse) {
            if(item > maxBound) {
                System.out.println(item + " is NOT less than " + maxBound);
            }

            assertTrue(item <= maxBound);
        }
    }

    @Test
    public void testTimeBoundRightOpen() {

        JsonPath postBody = getJson("gkgTimeBoundRightOpen.json");

        Response response = 
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
                    .log().ifValidationFails()
                    .body(isValidFeatureCollection())
                    .extract().response()
            ;

        List<Long> jsonResponse = response.jsonPath().getList("features.attributes.urlpubtimedate");

        //Assert that each feature returned is within the time bounds
        for (Long item : jsonResponse) {
            if(item <= minBound) {
                System.out.println(item + " is NOT greater than " + minBound);
            }
            assertTrue(item >= minBound);
        }
    }

    @Test
    public void testTimeBoundInstant() {

        JsonPath postBody = getJson("gkgTimeBoundInstant.json");

        Response response = 
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
                    .log().ifValidationFails()
                    .body(isValidFeatureCollection())
                    .extract().response()
            ;

        List<Long> jsonResponse = response.jsonPath().getList("features.attributes.urlpubtimedate");

        //Assert that each feature returned is within the time bounds
        for (Long item : jsonResponse) {
            if(item != minBound) {
                System.out.println(item + " is NOT equal to " + minBound);
            }

            assertTrue(item == minBound);
        }
    }

    @Test
    public void testTimeBoundNull() {

        JsonPath postBody = getJson("gkgTimeBoundNull.json");

        Response response = 
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
                    .log().ifValidationFails()
                    .body(isValidFeatureCollection())
                    .extract().response()
            ;

        List<Long> jsonResponse = response.jsonPath().getList("features");

        assertTrue(jsonResponse.size() >= 20);
    }
}

//"24457","24973","5632","24974","27161","56371","49518","49416","32295","32309","32296","47923","32293","8384","44483","32724","22445","22455","1807","5538"
