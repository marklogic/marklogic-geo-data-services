import io.restassured.path.json.JsonPath;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.is;

public class DataTypes extends AbstractFeatureServiceTest {

    @Test
    public void testDefaultStringLength() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testDefaultStringLength.json");
        postQuery(postBody)
                .body("metadata.fields.find { it.name == 'domain' }.length", is(1024))
        ;
    }
}
