import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class PolygonQueries extends AbstractFeatureServiceTest {

	// ===============================Intersect=============================================

	// Wildlife Refuge testSinglePolygonIntersects
	@Test
	public void testPolygonIntersects1() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonIntersects1.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()
				//
				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(1))
				.body("features[0].properties.name", is("Wildlife Refuge"));
	}

	// testSinglePolygonLineStringIntersects1 Expected : Marklogic neighbourhood,
	// (Holly St)Linestring
	@Test
	public void testPolygonIntersects2() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonIntersects2.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()
				//
				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(2))
				.body("features.properties.name", hasItems("Holly St", "MarkLogic Neighborhood"));
	}

	// testSinglePolygonLineStringIntersects2 Expected : Holly ST(LineString) ,
	// Airport (Polygon)
	@Test
	public void testPolygonIntersects3() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonIntersects3.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(3))
				.body("features.properties.name", hasItems("Airport", "Holly St", "MarkLogic Neighborhood"));
	}

	// testTwoLineStringIntersects Expected : Cross two linestrings- Holly ST & Hy
	// 101
	@Test
	public void testPolygonIntersects4() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonIntersects4.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(3))
				.body("features.properties.name", hasItems("Hwy 101", "Holly St", "MarkLogic Neighborhood"));
	}

	// outside - should not return any feature
	@Test
	public void testPolygonIntersects5() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonIntersects5.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	// Only cross outer polygon ( Should return only outer polygon)
	@Test
	public void testPolygonIntersects6() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonIntersects6.json");
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
				.body("features[0].properties.name", is("MarkLogic Neighborhood"));
	}

	// Only covering one point (Should return the point and Outer polygon)
	@Test
	public void testPolygonIntersects7() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonIntersects7.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(2))
				.body("features.properties.name", hasItems("Restaurant", "MarkLogic Neighborhood"));
	}

	// ================================Within=======================================

	// Polygon1 ( Features within Polygon ) Expected : Restaurant
	@Test
	public void testPolygonWithin1() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonWithin1.json");
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
				.body("features[0].properties.name", is("Restaurant"));
	}

	// Polygon2 ( Features within Polygon ) Expected : Airport
	@Test
	public void testPolygonWithin2() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonWithin2.json");
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
				.body("features[0].properties.name", is("Airport"));
	}

	// Polygon3 ( Features within Polygon - Around MarkLogic NH) Expected : 6
	// features
	@Test
	public void testPolygonWithin3() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonWithin3.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(6))
				.body("features.properties.name",
						hasItems("MarkLogic Neighborhood", "Restaurant", "Holly St", "Airport", "Museum", "MarkLogic HQ"));
		;
	}

	// Polygon4 ( No Features within Polygon) Expected : Zero results
	@Test
	public void testPolygonWithin4() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonWithin4.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	// Polygon5 (Features within Polygon - WildLife refuge) Expected : WildLife
	// refuge
	@Test
	public void testPolygonWithin5() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonWithin5.json");
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
				.body("features[0].properties.name", is("Wildlife Refuge"));
	}

	// Polygon6 (Reverse Test -Intersecting wildlife refuge - not within ) Expected
	// : Zero results
	@Test
	public void testPolygonWithin6() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonWithin6.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	// Polygon7 (Reverse Test -External polygon - Not related to any feature )
	// Expected : Zero results
	@Test
	public void testPolygonWithin7() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonWithin7.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	// =============================================Contains====================================================

	// Polygon1 Contains (Inside single polygon ) Expected : WildLife refuge
	@Test
	public void testPolygonContains1() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonContains1.json");
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
				.body("features[0].properties.name", is("Wildlife Refuge"));
	}

	// Polygon2 Contains (Inside two polygon ) Expected : Airport, MLNH
	@Test
	public void testPolygonContains2() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonContains2.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(2))
				.body("features.properties.name", hasItems("MarkLogic Neighborhood", "Airport"));
	}

	// Polygon2 Contains ( Inside a polygon and intersecting 2 other polygons )
	// Expected : A, MLNH
	@Test
	public void testPolygonContains3() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonContains3.json");
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
				.body("features[0].properties.name", is("MarkLogic Neighborhood"));
	}

	// ==================================Touch===========================================
	// Touched Hvy 101(Linestring) ( from Endpoint )
	@Test
	public void testPolygonTouches1() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonTouches1.json");
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
				.body("features[0].properties.name", is("Hwy 101"));
	}

	// Reverse test ( Intersecting )
	@Test
	public void testPolygonTouches2() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonTouches2.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	// Not touching anything
	@Test
	public void testPolygonTouches3() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonTouches3.json");
		RestAssured
			.given()
				.contentType(ContentType.JSON)
				.body(postBody.prettyPrint())
			.when()

				.post()
			.then()

				.statusCode(200)
				.body(isValidFeatureCollection())
				.body("features.size()", is(0));
	}

	// ==============================overlap=========================================

	@Test
	public void testPolygonOverlaps1() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonOverlaps1.json");
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
				.body("features[0].properties.name", is("MarkLogic Neighborhood"));
	}

	@Test
	public void testPolygonOverlaps2() throws UnsupportedEncodingException, ParseException {
		JsonPath postBody = getJson("testPolygonOverlaps2.json");
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
				.body("features[0].properties.name", is("Wildlife Refuge"));
	}
}
