import io.restassured.path.json.JsonPath;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class GeoJSONGeometry  extends AbstractFeatureServiceTest {

    // "geometry" : {
    //   "type" : "Polygon",
    //   "format" : "geojson",
    //   "coordinateSystem" : "wgs84",
    //   "source" : {
    //     "xpath" : "/envelope/instance/boundary"
    //   }
    // }

    @Test
    public void testXPathExtraction() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testXPathExtraction.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.coordinates.size()", is(1))
                .body("features.geometry.coordinates[0][0].size()", is(5))
                .body("features.geometry.coordinates[0][0][0].size()", is(2))
                .body("features.geometry.coordinates[0][0][0]", hasItems(30, 10))
        ;
    }

    // "geometry" : {
    //   "type" : "Polygon",
    //   "format" : "geojson",
    //   "coordinateSystem" : "wgs84",
    //   "source" : {
    //     "column" : "boundary"
    //   }
    // }
    @Test
    public void testColumnExtraction() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testColumnExtraction.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.coordinates.size()", is(1))
                .body("features.geometry.coordinates[0][0].size()", is(5))
                .body("features.geometry.coordinates[0][0][0].size()", is(2))
                .body("features.geometry.coordinates[0][0][0]", hasItems(30, 10))
        ;
    }

    // "geometry" : {
    //   "type" : "Polygon",
    //   "format" : "geojson",
    //   "coordinateSystem" : "wgs84",
    //   "source" : {
    //     "xpath" : "/envelope/header/ctsRegion"
    //     "format" : "cts",
    //   }
    // }
    @Test
    public void testXPathCtsExtraction() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testXPathCtsExtraction.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features.geometry.coordinates.size()", is(1))
                .body("features.geometry.coordinates[0][0].size()", is(5))
                .body("features.geometry.coordinates[0][0][0].size()", is(2))
                .body("features.geometry.coordinates[0][0][0]", hasItems(30, 10))
        ;
    }
}
