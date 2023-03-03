import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class PointQueries  extends AbstractFeatureServiceTest {

    //testOnePolygonIntersects
    @Test
    public void testPointIntersects1() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .intersectsPoint(-122.25972175598143, 37.51871254735555)
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("MarkLogic Neighborhood"))
        ;
    }

    //testTwoPolygonIntersects
    @Test
    public void testPointIntersects2() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .intersectsPoint(-122.24564552307129, 37.513198107015064)
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("Wildlife Refuge", "MarkLogic Neighborhood"))
        ;
    }


    //Inside single polygon Expected- WildLife Refuge
    @Test
    public void testPointContains1() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .containsPoint(-122.2411823272705, 37.50918115940604)
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Wildlife Refuge"))
        ;
    }

    //Inside two polygon Expected- WildLife Refuge
    @Test
    public void testPointContains2() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .containsPoint(-122.25208282470703, 37.51571709945411)
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("Airport", "MarkLogic Neighborhood"))
        ;
    }

    //External Point Expected- No Features
    @Test
    public void testPointContains3() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .containsPoint(-122.24152565002441, 37.52068675409422)
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }
}
