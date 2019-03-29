import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class GroupByTest extends AbstractFeatureServiceTest{

    @Test
    public void testGkgGroupBy() {
        JsonPath postBody = getJson("testGkgGroupBy.json");

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
                //TODO missing .body("displayFieldName", is(""))
                //TODO missing .body("fieldAliases.domain", is("domain"))
                //TODO missing .body("fieldAliases.domain_count", is("domain_count"))

                //TODO missing .body("fields.size()", is(2))
                //TODO missing .body("fields.name", hasItems("domain", "domain_count"))
                //TODO missing .body("fields.type", hasItems("esriFieldTypeString", "esriFieldTypeInteger"))

                .body("statistics.size()", is(2455))
                .body("statistics[0].domain", is("fax.al"))
                .body("statistics[0].domain_count", is(1259))

                .body("statistics[9].domain", is("entornointeligente.com"))
                .body("statistics[9].domain_count", is(199))
            ;
    }

    @Test
    public void testGroupByTwoFields() {
        JsonPath postBody = getJson("testGroupByTwoFields.json");

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
                .body("statistics.size()", is(9061))
                .body(
                    "statistics.find { it.domain == '1057fmthefan.com' & it.name == 'Chile' }.objectid_count",
                    is(5)
                )
                .body(
                    "statistics.find { it.domain == '4-traders.com' & it.name == 'Japan' }.objectid_count",
                    is(8)
                )
                .body(
                    "statistics.find { it.domain == '9news.com.au' & it.name == 'Australia' }.objectid_count",
                    is(12)
                )
                .body(
                    "statistics.find { it.domain == '9news.com.au' & it.name == 'New Zealand' }.objectid_count",
                    is(1)
                )
            ;
    }

    @Test
    public void testGroupByOrderByCount() {
        JsonPath postBody = getJson("testGroupByOrderByCount.json");

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
                .body("statistics.size()", is(2455))
                .body("statistics[0].domain", is("fax.al"))
                .body("statistics[0].objectid_count", is(1259))
            ;
    }

    @Test
    public void testGroupByWithFilter() {
        JsonPath postBody = getJson("testGroupByWithFilter.json");

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
                .body("statistics.size()", is(2))
                .body("statistics.find { it.domain == '4-traders.com' }.objectid_count", is(178))
                .body("statistics.find { it.domain == '9news.com.au' }.objectid_count", is(14))
            ;
    }
}
