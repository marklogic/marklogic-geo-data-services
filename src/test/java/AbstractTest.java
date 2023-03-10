import com.marklogic.gds.GeoQueryRequest;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;

import static io.restassured.RestAssured.basic;

public abstract class AbstractTest {

    @Before
    public void setup() {
        // Once we have a reason for these to be dynamic, we can bring in marklogic-junit5 and read these from
        // gradle.properties and gradle-local.properties.
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8096;
        RestAssured.authentication = basic("test-geo-data-services-writer", "test-geo-data-services-writer");

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
    }

    protected final ValidatableResponse postQuery(JsonPath postBody) {
        return postForResponse(postBody.prettyPrint()).then().statusCode(200);
    }

    /**
     * Convenience method for the common happy path of submitting a request to geoQueryService and getting back a 200.
     *
     * @param request
     * @return
     */
    protected final ValidatableResponse postGeoQueryRequest(GeoQueryRequest request) {
        return postForResponse(request.toString()).then().statusCode(200);
    }

    protected final ValidatableResponse postQueryForError(GeoQueryRequest postQuery, Integer expectedErrorCode) {
        return postForResponse(postQuery.toString()).then().statusCode(expectedErrorCode);
    }

    protected final Response postForResponse(JsonPath postBody) {
        return postForResponse(postBody.prettyPrint());
    }

    protected final Response postForResponse(String postBody) {
        return RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(postBody)
            .when()
            .post();
    }

    /**
     * @deprecated Don't use this, use the Query class instead.
     */
    @Deprecated
    public JsonPath getJson(String requestFile) {
        String pathToFile = "/" + getClass().getName() + "/" + requestFile;
        return new JsonPath(AbstractFeatureServiceTest.class.getResource(pathToFile)).using(new JsonPathConfig(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }
}
