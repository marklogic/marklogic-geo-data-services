import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class LineStringQueries extends AbstractFeatureServiceTest {

    // Crosses Single line Expected : Holly St
    @Test
    public void testLineStringCrosses1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringCrosses1.json");

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
                .body("features[0].properties.name", is("Holly St"));
    }

    // Crosses Double line = Expected : WildLife refuge
    @Test
    public void testLineStringCrosses2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringCrosses2.json");

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
                .body("features.properties.name", hasItems("Hwy 101", "Holly St"));
        ;
    }

    // Crosses Lines and Polygons = Expected : 6 features
    @Test
    public void testLineStringCrosses3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringCrosses3.json");

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
                .body("features.properties.name", hasItems("MarkLogic Neighborhood", "Shopping Center", "Wildlife Refuge",
                        "Hwy 101", "Holly St", "Airport"));
        ;
    }

    // Crosses Polygons = Expected : WildLife refuge
    @Test
    public void testLineStringCrosses4() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringCrosses4.json");

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

    // http://localhost:9080/marklogic/GDeltGKG/FeatureServer/3/query?geometryType=esriGeometryPolygon&geometry={"paths":[[[-122.24143981933594,37.520720791683374],[-122.24156856536865,37.51432145198483]]],"spatialReference"
    // : {"wkid" : 4326}}

    // Intersect single Polygons = Expected : WildLife refuge
    @Test
    public void testLineStringIntersect1() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringIntersect1.json");

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

    // Intersect Two Polygons = Expected : WildLife refuge, MLNH
    @Test
    public void testLineStringIntersect2() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringIntersect2.json");

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
                .body("features.properties.name", hasItems("Wildlife Refuge", "MarkLogic Neighborhood"));
        ;
    }

    // Intersect Multiple Polygons and LineString = Expected : 4 features
    @Test
    public void testLineStringIntersect3() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringIntersect3.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()

                .post()
            .then()

                .statusCode(200)
                .body(isValidFeatureCollection())
                .body("features.size()", is(4))
                .body("features.properties.name", hasItems("Airport", "Wildlife Refuge", "MarkLogic Neighborhood", "Hwy 101"));
        ;
    }

    // Complete inside polygon without Intersection = Expected :MLNH
    @Test
    public void testLineStringIntersect4() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringIntersect4.json");

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

    // Reverse test -polyline with one end as a point in database Expected : MLNH

    @Test
    public void testLineStringIntersect5() throws UnsupportedEncodingException, ParseException {
        JsonPath postBody = getJson("testLineStringIntersect5.json");

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
}
