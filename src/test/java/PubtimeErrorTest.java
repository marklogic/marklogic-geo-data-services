import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class PubtimeErrorTest extends AbstractFeatureServiceTest{

	@Test
    public void testGkgPubtimeError0() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .where("( urlpubtimedate >= TIMESTAMP '2015-12-3 23:00:00' ) AND ( urlpubtimedate < TIMESTAMP '2015-12-4 00:00:00' )")
                .returnCountOnly()
        )
                .body(isValidFeatureCollection())
	            .body("count", is(0))
        ;
    }
}
