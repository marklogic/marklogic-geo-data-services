import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class OffsetAndLimitTest extends AbstractFeatureServiceTest {

	@Test
    public void testGkgOffsetAndLimit() {
        JsonPath postBody = getJson("testGkgOffsetAndLimit.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("features.size()", is(10))

                .body("features[0].properties.OBJECTID", is(3728))
                .body("features[0].properties.urlpubtimedate", is("2017-05-24T06:00:00Z"))
                .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[0].properties.url", is("http://www.ziuanews.ro/stiri/cozmin-gu-dovedirea-fraud-rii-alegerilor-din-2009-este-important-pentru-democra-ia-noastr-704622"))
                .body("features[0].properties.name", is("Romania"))
                .body("features[0].properties.urltone", is(-1.43f))
                .body("features[0].properties.domain", is("ziuanews.ro"))
                .body("features[0].properties.urllangcode", is("ron"))
                .body("features[0].properties.geores", is(1))

                .body("features[9].properties.OBJECTID", is(25653))
                .body("features[9].properties.urlpubtimedate", is("2017-05-24T09:00:00Z"))
                .body("features[9].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[9].properties.url", is("http://www.zimbabwesituation.com/news/zimsit-m-govt-urged-to-export-transformers-to-raise-forex/"))
                .body("features[9].properties.name", is("Zimbabwe"))
                .body("features[9].properties.urltone", is(-2.99f))
                .body("features[9].properties.domain", is("zimbabwesituation.com"))
                .body("features[9].properties.urllangcode", is("eng"))
                .body("features[9].properties.geores", is(1))
        ;
    }

}
