import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class LimitExceedTest extends AbstractFeatureServiceTest{

    @Test
    public void testGkgLimitExceed1() {
        JsonPath postBody = getJson("gkgExceededTransferLimit1.json");
        postQuery(postBody)


                .body(isValidFeatureCollection())
                .body("metadata.limitExceeded", is(true))
                .body("features.size()", is(5000))
        ;
    }

    @Test
    public void testGkgLimitExceed2() {
        JsonPath postBody = getJson("gkgExceededTransferLimit2.json");
        postQuery(postBody)


                .body(isValidFeatureCollection())
                .body("metadata.limitExceeded", is(false))
                .body("features.size()", is(3557))
        ;
    }

    @Test
    public void testGkgLimitExceed3() {
        JsonPath postBody = getJson("gkgExceededTransferLimit3.json");
        postQuery(postBody)


                .body(isValidFeatureCollection())
                .body("metadata.limitExceeded", is(true))
                .body("features.size()", is(3500))
        ;
    }

    @Test
    public void testGkgLimitExceed4() {
        JsonPath postBody = getJson("gkgExceededTransferLimit4.json");
        postQuery(postBody)


                .body(isValidFeatureCollection())
                .body("metadata.limitExceeded", is(true))
                .body("features.size()", is(5))
        ;
    }
}
