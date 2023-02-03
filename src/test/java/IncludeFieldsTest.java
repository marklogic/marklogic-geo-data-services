import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class IncludeFieldsTest extends AbstractFeatureServiceTest {

    @Test
    public void testIncludeFieldsInFirstDataSourcesObject() {
        JsonPath postBody = getJson("testIncludeFieldsInFirstDataSourcesObject.json");
        postQuery(postBody)


                .body(isValidFeatureCollection())

                .body("features.size()", is(5))

                .body("features[0].properties.OBJECTID", is(20643))
                .body("features[0].properties.urlpubtimedate", is("2017-05-24T09:00:00Z"))
                .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[0].properties.url", is("http://satnews.com/story.php?number=1191513746"))
                .body("features[0].properties.name", is("Aalborg, Nordjylland, Denmark"))

                //TODO missing .body("features[0].attributes", not(hasKey("urltone")))
                //TODO missing .body("features[0].attributes", not(hasKey("domain")))
                //TODO missing .body("features[0].attributes", not(hasKey("urllangcode")))
                //TODO missing .body("features[0].attributes", not(hasKey("geores")))
        ;
    }

    @Test
    public void testIncludeFieldsInOriginalSource() {
        JsonPath postBody = getJson("testIncludeFieldsInOriginalSource.json");
        postQuery(postBody)


                .body(isValidFeatureCollection())

                .body("features.size()", is(5))

                .body("features[0].properties.OBJECTID", is(20643))
                .body("features[0].properties.urlpubtimedate", is("2017-05-24T09:00:00Z"))
                .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[0].properties.url", is("http://satnews.com/story.php?number=1191513746"))
                .body("features[0].properties.name", is("Aalborg, Nordjylland, Denmark"))

                //TODO missing .body("features[0].attributes", not(hasKey("urltone")))
                //TODO missing .body("features[0].attributes", not(hasKey("domain")))
                //TODO missing .body("features[0].attributes", not(hasKey("urllangcode")))
                //TODO missing .body("features[0].attributes", not(hasKey("geores")))
        ;
    }
}
