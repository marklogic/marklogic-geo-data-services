import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class CountLayerTest extends AbstractFeatureServiceTest{

    @Test
    public void testGkgCountLayer0() {
        JsonPath postBody = getJson("gkgCountLayer0.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("count", is(38765))
            ;
    }

    @Test
    public void testGkgCountLayer1() {
        JsonPath postBody = getJson("gkgCountLayer1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("count", is(3557))
            ;
    }
}
