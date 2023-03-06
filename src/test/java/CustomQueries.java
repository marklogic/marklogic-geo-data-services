import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CustomQueries extends AbstractFeatureServiceTest {

    @Test
    public void testCustomPolygon1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 4)
                .intersectsPolygon(
                    60.99609375, 9.96885060854611,
                    60.99609375, 37.78808138412046,
                    86.1328125, 37.78808138412046,
                    86.1328125, 9.96885060854611,
                    60.99609375, 9.96885060854611
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
            ;
    }

    @Test
    public void testCustomGeometryPolygon1() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomGeometryPolygon1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
                ;
    }

    @Test
    public void testCustomPolygon2() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomPolygon2.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
            ;
    }

    @Test
    public void testCustomGeometryPolygon2() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomGeometryPolygon2.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
                ;
    }

    @Test
    public void testCustomPolygon3() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomPolygon3.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
            ;
    }

    @Test
    public void testCustomGeometryPolygon3() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomGeometryPolygon3.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
            ;
    }

    @Test
    public void testCustomPolygon4() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomPolygon4.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
            ;
    }

    @Test
    public void testCustomPolygon5() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomPolygon5.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
            ;
    }

    @Test
    public void testCustomGeometryPolygon5() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testCustomGeometryPolygon5.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"))
            ;
    }

    // Envelope Test cases
    @Test
    public void testCustomEnvelope1() {
        JsonPath postBody = getJson("testCustomEnvelope1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))            ;
       }

    @Test
    public void testCustomGeometryEnvelope1() {
        JsonPath postBody = getJson("testCustomGeometryEnvelope1.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(14))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana"))
                .body("features.geometry.size()", is(14))
                .body("features.geometry.points.size()", not(0))
            ;
       }

    @Test
    public void testCustomEnvelope2() {
        JsonPath postBody = getJson("testCustomEnvelope2.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
                ;
       }

    @Test
    public void testCustomGeometryEnvelope2() {
        JsonPath postBody = getJson("testCustomGeometryEnvelope2.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
                ;
       }

    @Test
    public void testCustomEnvelope3() {
        JsonPath postBody = getJson("testCustomEnvelope3.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
            ;
       }

    @Test
    public void testCustomGeometryEnvelope3() {
        JsonPath postBody = getJson("testCustomGeometryEnvelope3.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features.geometry.size()", is(5))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Himachal Pradesh","Uttar Pradesh","Jammu and Kashmir","Rajasthan","Haryana"))
            ;
       }


    @Test
    public void testCustomEnvelope4() {
        JsonPath postBody = getJson("testCustomEnvelope4.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
            ;
       }

    @Test
    public void testCustomEnvelope5() {
        JsonPath postBody = getJson("testCustomEnvelope5.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.properties.name", hasItems("Gujarat"))
                ;
       }

    @Test
    public void testCustomGeometryEnvelope5() {
        JsonPath postBody = getJson("testCustomGeometryEnvelope5.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Gujarat"));
       }

    @Test
    public void testCustomPoint1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 4)
                .intersectsPoint(73.432617, 27.391277)
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Rajasthan"))
            ;
    }

    @Test
    public void testCustomGeometryPoint1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 4)
                .intersectsPoint(73.432617, 27.391277)
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Rajasthan"))
            ;
    }

    @Test
    public void testCustomPoint2() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 4)
                .intersectsPoint(92.46093749999999, 39.095962936305476)
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
            ;
    }

    @Test
    public void testCustomPoint3() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 4)
                .intersectsPoint(84.803467, 20.94092)
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Odisha"))
            ;
    }

    @Test
    public void testCustomGeometryPoint3() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 4)
                .intersectsPoint(84.803467, 20.940920000000002)
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.size()", is(1))
                .body("features.geometry.points.size()", not(0))
                .body("features[0].properties.name", is("Odisha"))
            ;
    }

    @Test
    public void testCustomAllFields() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 4)
                .withOutFields("*"))
                .body(isValidFeatureCollection())
                .body("features.size()", is(17))
                .body("features.geometry.size()", is(17))
                .body("features.geometry.points.size()", not(0))
                .body("features.properties.name", hasItems("Kerala","Himachal Pradesh","Odisha","Chhattisgarh","Madhya Pradesh","Uttar Pradesh","Jammu and Kashmir","Karnataka","Rajasthan","Maharashtra","Gujarat","Haryana","Tamil Nadu","Telangana","West Bengal","Assam","Tripura"))
                ;
    }
}
