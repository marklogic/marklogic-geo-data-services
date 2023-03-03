import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class DataTypes extends AbstractFeatureServiceTest {

    @Test
    public void testDefaultStringLength() {
        postGeoQueryRequest(new GeoQueryRequest(3))
                .body("metadata.fields.find { it.name == 'domain' }.length", is(1024))
        ;
    }
}
