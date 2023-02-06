import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class AlwaysIncludeQuery extends AbstractFeatureServiceTest {

    @Test
    public void testGkgCountLayer() {
        postGeoQueryRequest(new GeoQueryRequest(7)
                      .returnCountOnly()
        )
            .body(isValidFeatureCollection())
            //this is a count of layers 0 + 1
            .body("count", is(38765 + 3557));
    }
}
