import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class WKTGeometry  extends AbstractFeatureServiceTest {

    // "geometry" : {
    //   "type" : "Polygon",
    //   "format" : "wkt",
    //   "coordinateSystem" : "wgs84",
    //   "source" : {
    //     "xpath" : "/envelope/instance/boundary"
    //   }
    // }

    @Test
    public void testXPathExtraction() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 6)
                .withObjectIds("900001")
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].geometry.coordinates.size()", is(1))
                .body("features[0].geometry.coordinates[0].size()", is(5))
                .body("features[0].geometry.coordinates[0][0].size()", is(2))
                .body("features[0].geometry.coordinates[0][0]", hasItems(30, 10))
        ;
    }

    // "geometry" : {
    //   "type" : "Polygon",
    //   "format" : "wkt",
    //   "coordinateSystem" : "wgs84",
    //   "source" : {
    //     "column" : "boundary"
    //   }
    // }
    @Test
    public void testColumnExtraction() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 7)
                .withObjectIds("900001")
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].geometry.coordinates.size()", is(1))
                .body("features[0].geometry.coordinates[0].size()", is(5))
                .body("features[0].geometry.coordinates[0][0].size()", is(2))
                .body("features[0].geometry.coordinates[0][0]", hasItems(30, 10))
        ;
    }
}
