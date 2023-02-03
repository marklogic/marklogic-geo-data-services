import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class PubtimeErrorTest extends AbstractFeatureServiceTest{

	@Test
    public void testGkgPubtimeError0() {

        JsonPath postBody = getJson("testGkgPubtimeError0.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
	            .body("count", is(0))
        ;
    }
}
