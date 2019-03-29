import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

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
                    .post(url)
                .then()
                    .log().ifError()
                    .statusCode(200)
                    .log().all()
                    .log().ifValidationFails()
                    .body("displayFieldName", is(""))
                    .body("fieldAliases.domain", is("domain"))
                    .body("fieldAliases.average_urltone", is("average_urltone"))

                    .body("fields.size()", is(2))
                    .body("fields.name", hasItems("domain", "average_urltone"))

                    .body("features.size()", is(2455))
                    .body("features[0].domain", is("newsbeast.gr"))
                    .body("features[0].average_urltone", is(12.96f))

                    .body("features[9].domain", is("camdencourier.com.au"))
                    .body("features[9].average_urltone", is(8.33f))
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
                    .post(url)
                .then()
                    .log().ifError()
                    .statusCode(200)
                    .log().all()
                    .log().ifValidationFails()
                    .body("displayFieldName", is(""))
                    .body("fieldAliases.domain", is("domain"))
                    .body("fieldAliases.average_urltone", is("average_urltone"))
                    .body("fieldAliases.minimum_urltone", is("minimum_urltone"))
                    .body("fieldAliases.maximum_urltone", is("maximum_urltone"))

                    .body("fields.size()", is(4))
                    .body("fields.name", hasItems("domain", "maximum_urltone", "average_urltone", "minimum_urltone"))

                    .body("features.size()", is(2455))
                    .body("features[0].domain", is("newsbeast.gr"))
                    .body("features[0].average_urltone", is(12.96f))
                    .body("features[0].minimum_urltone", is(12.96f))
                    .body("features[0].maximum_urltone", is(12.96f))
            ;
        }
        finally{
            RestAssured.reset();
        }
    }


	@Test
    public void testStddevAndVarUrltone() {
        JsonPath postBody = getJson("testStddevAndVarUrltone.json");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(postBody.prettyPrint())
            .when()
                .log().uri()
                .post(url)
            .then()
                .log().ifError()
                .statusCode(200)
                .log().all()
                .log().ifValidationFails()
                .body("displayFieldName", is(""))
                .body("fieldAliases.count_urltone", is("count_urltone"))
                .body("fieldAliases.min_urltone", is("min_urltone"))
                .body("fieldAliases.max_urltone", is("max_urltone"))
                .body("fieldAliases.avg_urltone", is("avg_urltone"))
                .body("fieldAliases.stddev_urltone", is("stddev_urltone"))
                .body("fieldAliases.var_urltone", is("var_urltone"))

                .body("fields.size()", is(6))
                .body("fields.name", hasItems("count_urltone", "min_urltone", "max_urltone", "avg_urltone", "stddev_urltone", "var_urltone"))

                .body("features.size()", is(1))
                .body("features[0].count_urltone", is(38765))
                .body("features[0].min_urltone", is(-21.77f))
                .body("features[0].max_urltone", is(16.23f))
                .body("features[0].avg_urltone", is(-1.1373726299497f))
                .body("features[0].stddev_urltone", is(3.6335345176664f))
                .body("features[0].var_urltone", is(13.2025730910732f))
        ;
    }
}
