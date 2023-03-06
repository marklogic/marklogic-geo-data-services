import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class LineStringQueries extends AbstractFeatureServiceTest {

    // Crosses Single line Expected : Holly St
    @Test
    public void testLineStringCrosses1() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .crossesLineString(
                    -122.26143836975098, 37.51217686964284,
                    -122.25603103637695, 37.50897690205704
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Holly St"));
    }

    // Crosses Double line = Expected : WildLife refuge
    @Test
    public void testLineStringCrosses2() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .crossesLineString(
                    -122.2555160522461, 37.51660213066696,
                    -122.25422859191895, 37.51020243776711
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("Hwy 101", "Holly St"));
        ;
    }

    // Crosses Lines and Polygons = Expected : 6 features
    @Test
    public void testLineStringCrosses3() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .crossesLineString(
                    -122.25688934326172, 37.51707868158789,
                    -122.25680351257324, 37.511291785950505,
                    -122.24864959716797, 37.50169135780772,
                    -122.24985122680663, 37.514083168101116,
                    -122.24375724792479, 37.512721531313645
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(6))
                .body("features.properties.name", hasItems("MarkLogic Neighborhood", "Shopping Center", "Wildlife Refuge",
                        "Hwy 101", "Holly St", "Airport"));
        ;
    }

    // Crosses Polygons = Expected : WildLife refuge
    @Test
    public void testLineStringCrosses4() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .crossesLineString(
                    -122.2397232055664, 37.51925716132821,
                    -122.23809242248537, 37.505436352534616
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Wildlife Refuge"));
    }

    // http://localhost:9080/marklogic/GDeltGKG/FeatureServer/3/query?geometryType=esriGeometryPolygon&geometry={"paths":[[[-122.24143981933594,37.520720791683374],[-122.24156856536865,37.51432145198483]]],"spatialReference"
    // : {"wkid" : 4326}}

    // Intersect single Polygons = Expected : WildLife refuge
    @Test
    public void testLineStringIntersect1() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .intersectsLineString(
                    -122.24143981933594, 37.520720791683374,
                    -122.24156856536865, 37.51432145198483
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Wildlife Refuge"));
    }

    // Intersect Two Polygons = Expected : WildLife refuge, MLNH
    @Test
    public void testLineStringIntersect2() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .intersectsLineString(
                    -122.24324226379393, 37.5124492009751,
                    -122.24710464477538, 37.51210878665452
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("Wildlife Refuge", "MarkLogic Neighborhood"));
        ;
    }

    // Intersect Multiple Polygons and LineString = Expected : 4 features
    @Test
    public void testLineStringIntersect3() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .intersectsLineString(
                    -122.25414276123047, 37.505844886049545,
                    -122.25457191467285, 37.511496036964935,
                    -122.24684715270996, 37.51217686964284,
                    -122.24135398864748, 37.51108753437713,
                    -122.24195480346678, 37.50918115940604
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(4))
                .body("features.properties.name", hasItems("Airport", "Wildlife Refuge", "MarkLogic Neighborhood", "Hwy 101"));
        ;
    }

    // Complete inside polygon without Intersection = Expected :MLNH
    @Test
    public void testLineStringIntersect4() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .intersectsLineString(
                    -122.25654602050781, 37.51081519807655,
                    -122.25688934326172, 37.50693429782622
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("MarkLogic Neighborhood"));
    }

    // Reverse test -polyline with one end as a point in database Expected : MLNH

    @Test
    public void testLineStringIntersect5() {
        postGeoQueryRequest(
            new GeoQueryRequest(3)
                .intersectsLineString(
                    -122.26075172424316, 37.511836454080196,
                    -122.2582, 37.5128
                )
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("MarkLogic Neighborhood"));
    }
}
