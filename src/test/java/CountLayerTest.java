import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class CountLayerTest extends AbstractFeatureServiceTest{

    @Test
    public void testGkgCountLayer0() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .returnCountOnly()
        )
                .body(isValidFeatureCollection())
                .body("count", is(38765));
    }

    @Test
    public void testGkgCountLayer1() {
        postGeoQueryRequest(
            new GeoQueryRequest(1)
                .returnCountOnly()
        )
                .body(isValidFeatureCollection())
                .body("count", is(3557))
            ;
    }
}
