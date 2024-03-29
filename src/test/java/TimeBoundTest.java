import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class TimeBoundTest extends AbstractFeatureServiceTest {
    Long minBound = 1493596800000L;
    Long maxBound = 1496275200000L;

    @Test
    public void testTimeBound() {
        Response response = postGeoQueryRequest(
            new GeoQueryRequest(6)
                .withTimeRange("1493596800000,1496275200000")
                .recordCount(20)
                .returnIdsOnly()
        )
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
        Response response = postForResponse(postBody);
        response.then()
                    .statusCode(200)
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
        Response response = postForResponse(postBody);
        response.then()
                    .statusCode(200)
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
        Response response = postForResponse(postBody);
        response.then()
                    .statusCode(200)
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
        Response response = postForResponse(postBody);
        response.then()
                    .statusCode(200)
                    .body(isValidFeatureCollection())
                    .extract().response()
            ;

        List<Long> jsonResponse = response.jsonPath().getList("features");

        assertTrue(jsonResponse.size() >= 20);
    }
}
