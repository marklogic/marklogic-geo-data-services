import io.restassured.path.json.JsonPath;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class PointQueries  extends AbstractFeatureServiceTest {

    //testOnePolygonIntersects
    @Test
    public void testPointIntersects1() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testPointIntersects1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("MarkLogic Neighborhood"))
        ;
    }

    //testTwoPolygonIntersects
    @Test
    public void testPointIntersects2() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testPointIntersects2.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("Wildlife Refuge", "MarkLogic Neighborhood"))
        ;
    }


    //Inside single polygon Expected- WildLife Refuge
    @Test
    public void testPointContains1() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testPointContains1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(1))
                .body("features[0].properties.name", is("Wildlife Refuge"))
        ;
    }

    //Inside two polygon Expected- WildLife Refuge
    @Test
    public void testPointContains2() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testPointContains2.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(2))
                .body("features.properties.name", hasItems("Airport", "MarkLogic Neighborhood"))
        ;
    }

    //External Point Expected- No Features
    @Test
    public void testPointContains3() throws UnsupportedEncodingException, ParseException  {
        JsonPath postBody = getJson("testPointContains3.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }
}
