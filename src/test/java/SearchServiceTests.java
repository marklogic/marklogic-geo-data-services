import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import static org.hamcrest.Matchers.*;

public class SearchServiceTests extends AbstractTest {
    public void setup() {
        super.setup();
        RestAssured.basePath = "/LATEST/resources/geoSearchService";
    }

    @Test
    public void noBody() {
        RestAssured
            .post().then().statusCode(422)
        ;
    }

    @Test
    public void searchAll() {
        JsonPath body = getJson("searchAll.json");
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(body.prettyPrint())
            .when()
                .post()
            .then()

                .statusCode(200)

                .body("total", equalTo(67613))
                .body("results.size()", greaterThan(0))
                .body("facets.GeoResolution.facetValues.size()", greaterThan(0))
                .body("facets.Language.facetValues.size()", greaterThan(0))
                .body("facets.Domain.facetValues.size()", greaterThan(0))
                .body("values.Location.total", equalTo(1044))
                .body("values.Location.pointClusters.size()", equalTo(641))
                .body("values.Location.points.size()", equalTo(403))
        ;
    }

    @Test
    public void searchViewport() {
        JsonPath body = getJson("searchViewport.json");
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(body.prettyPrint())
            .when()
                .post()
            .then()

                .statusCode(200)

                .body("total", equalTo(30454))
                .body("results.size()", greaterThan(0))
                .body("facets.GeoResolution.facetValues.size()", greaterThan(0))
                .body("facets.Language.facetValues.size()", greaterThan(0))
                .body("facets.Domain.facetValues.size()", greaterThan(0))
                .body("values.Location.total", equalTo(505))
                .body("values.Location.pointClusters.size()", equalTo(303))
                .body("values.Location.points.size()", equalTo(202))
        ;
    }

    @Test
    public void saveSearch() {
        RestAssured.basePath = "";

        String modelId = "GDeltSearch";
        String expectedLayerName = "New saved layer";

        // save layer
        JsonPath body = getJson("saveSearch.json");
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(body.prettyPrint())
            .when()
                .put("/LATEST/resources/geoSearchService")
            .then()

                .statusCode(200)

                .body("id", equalTo(modelId))
                .body("layers.0.layerId", equalTo(1))
                .body("layers.0.name", equalTo(expectedLayerName))
        ;

        // retrieve descriptor and verify
        RestAssured
            .given()
                .queryParam("rs:id", modelId)
            .when()
                .get("/LATEST/resources/modelService")
            .then()

                .statusCode(200)

                .body("info.name", equalTo(modelId))
                .body("layers.size()", equalTo(3))
                .body("layers[1].name", equalTo(expectedLayerName))
                .body("layers[1].search.lastModifiedOn", notNullValue())
                .body("layers[1].search.lastModifiedBy", notNullValue())
        ;

        // "reset" the descriptor
        JsonPath origDescriptor = getJson("GDeltSearch.json"); // copy of the descriptor's original expected state
        RestAssured
            .given()
                .queryParam("uri", "/feature-services/test/GDeltSearch.json")
                .contentType(ContentType.JSON)
                .body(origDescriptor.prettyPrint())
            .when()
                .put("/LATEST/documents")
            .then()
                .statusCode(204)
        ;
    }
}
