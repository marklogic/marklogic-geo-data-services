import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class StatisticsTest  extends AbstractFeatureServiceTest{

    @Test
    public void testAverageTone() {

        try {
            JsonPath postBody = getJson("testAverageTone.json");

            RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(postBody.prettyPrint())
                .when()
                    .log().uri()
                    .post()
                .then()
                    .log().ifError()
                    .statusCode(200)
                    
                    .log().ifValidationFails()
                    .body(isValidFeatureCollection())
                    //TODO missing .body("displayFieldName", is(""))
                    //TODO missing .body("fieldAliases.domain", is("domain"))
                    //TODO missing .body("fieldAliases.average_urltone", is("average_urltone"))

                    //TODO missing .body("fields.size()", is(2))
                    //TODO missing .body("fields.name", hasItems("domain", "average_urltone"))

                    .body("statistics.size()", is(2455))
                    .body("statistics[0].domain", is("newsbeast.gr"))
                    .body("statistics[0].average_urltone", is(12.96f))

                    .body("statistics[9].domain", is("camdencourier.com.au"))
                    .body("statistics[9].average_urltone", is(8.33f))
            ;
        }
        finally {
            RestAssured.reset();
        }
    }

    @Test
    public void testAverageMinMaxTone() {

        try{
            JsonPath postBody = getJson("testAverageMinMaxTone.json");

            RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(postBody.prettyPrint())
                .when()
                    .log().uri()
                    .post()
                .then()
                    .log().ifError()
                    .statusCode(200)
                    
                    .log().ifValidationFails()
                    .body(isValidFeatureCollection())
                    //TODO missing .body("displayFieldName", is(""))
                    //TODO missing .body("fieldAliases.domain", is("domain"))
                    //TODO missing .body("fieldAliases.average_urltone", is("average_urltone"))
                    //TODO missing .body("fieldAliases.minimum_urltone", is("minimum_urltone"))
                    //TODO missing .body("fieldAliases.maximum_urltone", is("maximum_urltone"))

                    //TODO missing .body("fields.size()", is(4))
                    //TODO missing .body("fields.name", hasItems("domain", "maximum_urltone", "average_urltone", "minimum_urltone"))

                    .body("statistics.size()", is(2455))
                    .body("statistics[0].domain", is("newsbeast.gr"))
                    .body("statistics[0].average_urltone", is(12.96f))
                    .body("statistics[0].minimum_urltone", is(12.96f))
                    .body("statistics[0].maximum_urltone", is(12.96f))
            ;
        }
        finally{
            RestAssured.reset();
        }
    }


	@Test
    @Ignore("This is failing on Jenkins because the variance library doesn't seem to be getting loaded")
    public void testStddevAndVarUrltone() {
        JsonPath postBody = getJson("testStddevAndVarUrltone.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post()
            .then()
                .log().ifError()
                .statusCode(200)
                
                .log().ifValidationFails()
                .body(isValidFeatureCollection())
                //TODO missing .body("displayFieldName", is(""))
                //TODO missing .body("fieldAliases.count_urltone", is("count_urltone"))
                //TODO missing .body("fieldAliases.min_urltone", is("min_urltone"))
                //TODO missing .body("fieldAliases.max_urltone", is("max_urltone"))
                //TODO missing .body("fieldAliases.avg_urltone", is("avg_urltone"))
                //TODO missing .body("fieldAliases.stddev_urltone", is("stddev_urltone"))
                //TODO missing .body("fieldAliases.var_urltone", is("var_urltone"))

                //TODO missing .body("fields.size()", is(6))
                //TODO missing .body("fields.name", hasItems("count_urltone", "min_urltone", "max_urltone", "avg_urltone", "stddev_urltone", "var_urltone"))

                .body("statistics.size()", is(1))
                .body("statistics[0].count_urltone", is(38765))
                .body("statistics[0].min_urltone", is(-21.77f))
                .body("statistics[0].max_urltone", is(16.23f))
                .body("statistics[0].avg_urltone", is(-1.1373726299497f))
                .body("statistics[0].stddev_urltone", allOf(greaterThanOrEqualTo(3.6330f), lessThanOrEqualTo(3.6340f)))
                .body("statistics[0].var_urltone", allOf(greaterThanOrEqualTo(13.2020f), lessThanOrEqualTo(13.2030f)))
        ;
    }
}
