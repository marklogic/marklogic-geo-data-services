import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import static org.hamcrest.Matchers.*;



public class ServiceDescriptorTest extends AbstractFeatureServiceTest {

    @Test
    public void testServiceDescriptor() {
        JsonPath postBody = getJson("testServiceDescriptor.json");

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
                .log().ifValidationFails()
                .body("currentVersion", is(10.51f))
                .body("serviceDescription", notNullValue())
                .body("hasVersionedData", is(false))
                .body("currentVersion", is(10.51f))
                .body("maxRecordCount", is(5000))
                .body("hasStaticData", is(false))
                .body("capabilities", is("Query"))
                .body("description", notNullValue())

                .body("spatialReference.wkid", is(4326))
                .body("spatialReference.latestWkid", is(4326))

                .body("initialExtent.xmin", is(-180))
                .body("initialExtent.ymin", is(-90))
                .body("initialExtent.xmax", is(180))
                .body("initialExtent.ymax", is(90))
                .body("initialExtent.spatialReference.wkid", is(4326))
                .body("initialExtent.spatialReference.latestWkid", is(4326))

                .body("fullExtent.xmin", is(-180))
                .body("fullExtent.ymin", is(-90))
                .body("fullExtent.xmax", is(180))
                .body("fullExtent.ymax", is(90))
                .body("fullExtent.spatialReference.wkid", is(4326))
                .body("fullExtent.spatialReference.latestWkid", is(4326))


                .body("allowGeometryUpdates", is(false))
                .body("units", is("esriDecimalDegrees"))
                .body("syncEnabled", is(false))

                .body("layers.size()", is(6))
                .body("layers.name", hasItems("GKG level 1", "GKG level 2", "GKG level 3"))
            ;

        // we should probably add more validation here or just add new tests if there are
        // other specific fields we want to inspect
    }

    @Test
    public void testLayerDescriptor() {
        JsonPath postBody = getJson("testLayerDescriptor.json");

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
                .log().ifValidationFails()
                .body("layers.metadata.id", is(0))
                .body("layers.metadata.name", is("GKG level 1"))
                .body("layers.metadata.type", is("Feature Layer"))
                .body("layers.metadata.description", notNullValue())
                .body("layers.metadata.geometry.type", is("Point"))
                //TODO missing .body("layers.metadata.copyrightText", is(" "))
                //TODO missing .body("layers.metadata.parentLayer", IsNull.nullValue())
                //TODO missing .body("layers.metadata.subLayers", IsNull.nullValue())
                //TODO missing .body("layers.metadata.minScale", is(0))
                //TODO missing .body("layers.metadata.maxScale", is(0))
                //TODO missing .body("layers.metadata.defaultVisibility", is(true))
                .body("layers.metadata.extent.xmin", is(-180))
                .body("layers.metadata.extent.ymin", is(-90))
                .body("layers.metadata.extent.xmax", is(180))
                .body("layers.metadata.extent.ymax", is(90))
                .body("layers.metadata.extent.spatialReference.wkid", is(4326))
                .body("layers.metadata.extent.spatialReference.latestWkid", is(4326))

                .body("layers.metadata.fields.size()", is(9))
                .body("layers.metadata.fields.name", 
                    hasItems("OBJECTID", "urlpubtimedate", "urlpubdate", "url", "name", "urltone", "domain", "urllangcode", "geores"))
                //TODO missing .body("hasStaticData", is(false))
            ;

      // we should probably add more validation here or just add new tests if there are
      // other specific fields we want to inspect
    }

    @Test
    public void testLayers() {
        String path = request2path("featureService.json") + "/layers";

        RestAssured
            .given()
            .when()
                .log().uri()
                .get(path)

            .then()
                .log().ifError()
                .statusCode(200)
                .log().ifValidationFails()
                .body("layers.size()", is(6))
                .body("layers.name", hasItems("GKG level 1", "GKG level 2", "GKG level 3","GKG level 4"))
            ;

      // we should probably add more validation here or just add new tests if there are
      // other specific fields we want to inspect
    }
}


