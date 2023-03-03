import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FieldsTest extends AbstractFeatureServiceTest {

    @Test
    public void testAllFields() {
        postGeoQueryRequest(new GeoQueryRequest(0)
                                .recordCount(5)
                                .withOutFields("*"))
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features[0].properties.OBJECTID", notNullValue())
                .body("features[0].properties.urlpubtimedate", notNullValue())
                .body("features[0].properties.urlpubdate", notNullValue())
                .body("features[0].properties.url", notNullValue())
                .body("features[0].properties.name", notNullValue())
                .body("features[0].properties.urltone", notNullValue())
                .body("features[0].properties.domain", notNullValue())
                .body("features[0].properties.urllangcode", notNullValue())
                .body("features[0].properties.geores", notNullValue())
                .body("features[0].geometry.coordinates[0]", notNullValue()) // was x
                .body("features[0].geometry.coordinates[1]", notNullValue()) // was y
            ;
    }
}
