import com.marklogic.gds.GeoQueryRequest;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static io.restassured.RestAssured.basic;
import static org.hamcrest.Matchers.is;

/**
 * These tests are included for manual testing of the arcgis-hub-example project. Just comment out the Ignore
 * annotation to run them.
 */
@Ignore
public class ArcgisHubServiceTest extends AbstractFeatureServiceTest {

    @Before
    public void setup() {
        super.setup();
        RestAssured.port = 8065;
        RestAssured.authentication = basic("gds-example-user", "password");
    }

    @Test
    public void where() {
        postGeoQueryRequest(
            new GeoQueryRequest("ArcGIS Hub Data", 0)
                .where("OBJECTID = 250 and citation = 'Vokesimurex hirasei (Muricidae from Fiji and Tonga 250)'")
                .returnGeometry()
        )
            .log().body()
            .body(isValidFeatureCollection())
            .body("features.size()", is(1))
            .body("features[0].properties.OBJECTID", is(250))
        ;
    }

    @Test
    public void envelope() {
        postGeoQueryRequest(
            new GeoQueryRequest("ArcGIS Hub Data", 0)
                .where("OBJECTID = 250")
                .returnGeometry()
                .intersectsEnvelope(
                    -180, -20,
                    -178, -20,
                    -178, -18,
                    -180, -18,
                    -180, -20
                )
        )
            .log().body()
            .body(isValidFeatureCollection())
            .body("features.size()", is(1))
            .body("features[0].properties.OBJECTID", is(250))
        ;
    }

    /**
     * Can verify this with the following query in qconsole:
     * <p>
     * const paths = [
     * "geometry[type='Point']/array-node('coordinates')",
     * "geometry[type='MultiPoint']/array-node('coordinates')/array-node('coordinates')"
     * ];
     * <p>
     * const point = cts.longLatPoint("178.03333,-18.36667");
     * const query = cts.pathGeospatialQuery(paths, point, ["coordinate-system=wgs84","type=long-lat-point"], 1);
     * <p>
     * [
     * "Estimate: " + cts.estimate(query),
     * "Actual: " + cts.search(query).toArray().length,
     * fn.head(cts.search(query))
     * ]
     */
    @Test
    public void containsPoint() {
        postGeoQueryRequest(
            new GeoQueryRequest("ArcGIS Hub Data", 0)
                .returnGeometry()
                .containsPoint(178.03333, -18.36667)
        )
            .log().body()
            .body(isValidFeatureCollection())
            .body("features.size()", is(5))
        ;
    }
}
