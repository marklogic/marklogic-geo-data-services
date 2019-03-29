import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class LimitExceedTest extends AbstractFeatureServiceTest{
         
    @Test
    public void testGkgLimitExceed1() {
        JsonPath postBody = getJson("gkgExceededTransferLimit1.json");

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
                .log().all()
                .log().ifValidationFails()
                .body("metadata.limitExceeded", is(true))
                .body("features.size()", is(5000))
        ;
    }

    @Test
    public void testGkgLimitExceed2() {
        JsonPath postBody = getJson("gkgExceededTransferLimit2.json");

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
                .log().all()
                .log().ifValidationFails()
                .body("metadata.limitExceeded", is(false))
                .body("features.size()", is(3557))
        ;
    }   

    @Test
    public void testGkgLimitExceed3() {
        JsonPath postBody = getJson("gkgExceededTransferLimit3.json");

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
                .log().all()
                .log().ifValidationFails()
                .body("metadata.limitExceeded", is(true))
                .body("features.size()", is(3500))
        ;
    } 

    @Test
    public void testGkgLimitExceed4() {
        JsonPath postBody = getJson("gkgExceededTransferLimit4.json");

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
                .log().all()
                .log().ifValidationFails()
                .body("metadata.limitExceeded", is(true))
                .body("features.size()", is(5))
        ;
    }                                       
}

