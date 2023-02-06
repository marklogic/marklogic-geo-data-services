import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class AggregateTest extends AbstractFeatureServiceTest {

    @Test
    public void testGkgObjectIds() {
        postGeoQueryRequest(new GeoQueryRequest()
                      .withObjectIds("56577", "56576")
                      .returnCountOnly()
        )
            .body("features.size()", is(0))
            .body("count", is(2));
    }
}
