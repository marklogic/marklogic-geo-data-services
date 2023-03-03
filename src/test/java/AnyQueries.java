import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class AnyQueries extends AbstractFeatureServiceTest {

	@Test
	public void testAnyPolygon1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsPolygon(
                    60.99609375, 9.96885060854611,
                    60.99609375, 37.78808138412046,
                    86.1328125, 37.78808138412046,
                    86.1328125, 9.96885060854611,
                    86.1328125, 9.96885060854611
                )
        )
				.body(isValidFeatureCollection())
				.body("features.size()", is(14))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana"));
	}

	@Test
	public void testAnyGeometryPolygon1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsPolygon(
                    60.99609375, 9.96885060854611,
                    60.99609375, 37.78808138412046,
                    86.1328125, 37.78808138412046,
                    86.1328125, 9.96885060854611,
                    86.1328125, 9.96885060854611
                )
                .returnGeometry()
        )
				.body(isValidFeatureCollection())
				.body("features.size()", is(14))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana"))
				.body("features.geometry.size()", is(14))
				.body("features.geometry.points.size()", not(0));
	}

	@Test
	public void testAnyPolygon2() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyPolygon2.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(17))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana", "West Bengal", "Assam", "Tripura"));
	}

	@Test
	public void testAnyGeometryPolygon2() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyGeometryPolygon2.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(17))
				.body("features.geometry.size()", is(17))
				.body("features.geometry.points.size()", not(0))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana", "West Bengal", "Assam", "Tripura"));
	}

	@Test
	public void testAnyPolygon3() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyPolygon3.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(5))
				.body("features.properties.name",
						hasItems("Himachal Pradesh", "Uttar Pradesh", "Jammu and Kashmir", "Rajasthan", "Haryana"));
	}

	@Test
	public void testAnyGeometryPolygon3() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyGeometryPolygon3.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(5))
				.body("features.geometry.size()", is(5))
				.body("features.geometry.points.size()", not(0))
				.body("features.properties.name",
						hasItems("Himachal Pradesh", "Uttar Pradesh", "Jammu and Kashmir", "Rajasthan", "Haryana"));
	}

	@Test
	public void testAnyPolygon4() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyPolygon4.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	@Test
	public void testAnyPolygon5() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyPolygon5.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features.properties.name", hasItems("Gujarat"));
	}

	@Test
	public void testAnyGeometryPolygon5() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyGeometryPolygon5.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features.geometry.size()", is(1))
				.body("features.geometry.points.size()", not(0))
				.body("features.properties.name", hasItems("Gujarat"));
	}

	// Envelope Test cases
	@Test
	public void testAnyEnvelope1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsEnvelope(
                    -61, -170,
                    85, -170,
                    85, 180,
                    -61, 180,
                    -61, -170
                ))
            .body(isValidFeatureCollection())
            .body("features.size()", is(14))
            .body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana"));
	}

	@Test
	public void testAnyGeometryEnvelope1() {
		JsonPath postBody = getJson("testAnyGeometryEnvelope1.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(14))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana"))
				.body("features.geometry.size()", is(14))
				.body("features.geometry.points.size()", not(0));
	}

	@Test
	public void testAnyEnvelope2() {
		JsonPath postBody = getJson("testAnyEnvelope2.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(17))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana", "West Bengal", "Assam", "Tripura"));
	}

	@Test
	public void testAnyGeometryEnvelope2() {
		JsonPath postBody = getJson("testAnyGeometryEnvelope2.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(17))
				.body("features.geometry.size()", is(17))
				.body("features.geometry.points.size()", not(0))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana", "West Bengal", "Assam", "Tripura"));
	}

	@Test
	public void testAnyEnvelope3() {
		JsonPath postBody = getJson("testAnyEnvelope3.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(5))
				.body("features.properties.name",
						hasItems("Himachal Pradesh", "Uttar Pradesh", "Jammu and Kashmir", "Rajasthan", "Haryana"));
	}

	@Test
	public void testAnyGeometryEnvelope3() {
		JsonPath postBody = getJson("testAnyGeometryEnvelope3.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(5))
				.body("features.geometry.size()", is(5))
				.body("features.geometry.points.size()", not(0))
				.body("features.properties.name",
						hasItems("Himachal Pradesh", "Uttar Pradesh", "Jammu and Kashmir", "Rajasthan", "Haryana"));
	}

	@Test
	public void testAnyEnvelope4() {
		JsonPath postBody = getJson("testAnyEnvelope4.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	@Test
	public void testAnyEnvelope5() {
		JsonPath postBody = getJson("testAnyEnvelope5.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features.properties.name", hasItems("Gujarat"));
	}

	@Test
	public void testAnyGeometryEnvelope5() {
		JsonPath postBody = getJson("testAnyGeometryEnvelope5.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features.geometry.size()", is(1))
				.body("features.geometry.points.size()", not(0))
				.body("features.properties.name", hasItems("Gujarat"));
	}

	// Point test cases
	@Test
	public void testAnyPoint1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsPoint(73.432617, 27.391277)
                .returnGeometry()
        )
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features[0].properties.name", is("Rajasthan"));
	}

	@Test
	public void testAnyGeometryPoint1() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsPoint(73.432617, 27.391277)
                .returnGeometry()
        )
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features.geometry.size()", is(1))
				.body("features.geometry.points.size()", not(0))
				.body("features[0].properties.name", is("Rajasthan"));
	}

	@Test
	public void testAnyPoint2() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsPoint(92.46093749999999, 39.095962936305476)
        )
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	@Test
	public void testAnyPoint3() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsPoint(84.803467, 20.94092)
        )
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features[0].properties.name", is("Odisha"));
	}

	@Test
	public void testAnyGeometryPoint3() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 5)
                .intersectsPoint(84.803467, 20.94092)
                .returnGeometry()
        )
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features.geometry.size()", is(1))
				.body("features.geometry.points.size()", not(0))
				.body("features[0].properties.name", is("Odisha"));
	}

	@Test
	public void testAnyAllFields() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testAnyAllFields.json");
        postQuery(postBody)
				.body(isValidFeatureCollection())
				.body("features.size()", is(17))
				.body("features.geometry.size()", is(17))
				.body("features.geometry.points.size()", not(0))
				.body("features.properties.name",
						hasItems("Kerala", "Himachal Pradesh", "Odisha", "Chhattisgarh", "Madhya Pradesh", "Uttar Pradesh",
								"Jammu and Kashmir", "Karnataka", "Rajasthan", "Maharashtra", "Gujarat", "Haryana", "Tamil Nadu",
								"Telangana", "West Bengal", "Assam", "Tripura"));
	}
}

/// marklogic/GeoLocation/FeatureServer/0/query?outFields=*
