import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
                //TODO missing .body("currentVersion", is(10.51f))
                //TODO missing .body("serviceDescription", notNullValue())
                //TODO missing .body("hasVersionedData", is(false))
                //TODO missing .body("currentVersion", is(10.51f))
                //TODO missing .body("maxRecordCount", is(5000))
                //TODO missing .body("hasStaticData", is(false))
                //TODO missing .body("capabilities", is("Query"))
                //TODO missing .body("description", notNullValue())

                //TODO missing .body("spatialReference.wkid", is(4326))
                //TODO missing .body("spatialReference.latestWkid", is(4326))

                .body("layers[0].metadata.extent.xmin", is(-180))
                .body("layers[0].metadata.extent.ymin", is(-90))
                .body("layers[0].metadata.extent.xmax", is(180))
                .body("layers[0].metadata.extent.ymax", is(90))
                .body("layers[0].metadata.extent.spatialReference.wkid", is(4326))
                .body("layers[0].metadata.extent.spatialReference.latestWkid", is(4326))

                //TODO missing .body("initialExtent.xmin", is(-180))
                //TODO missing .body("initialExtent.ymin", is(-90))
                //TODO missing .body("initialExtent.xmax", is(180))
                //TODO missing .body("initialExtent.ymax", is(90))
                //TODO missing .body("initialExtent.spatialReference.wkid", is(4326))
                //TODO missing .body("initialExtent.spatialReference.latestWkid", is(4326))

                //TODO missing .body("fullExtent.xmin", is(-180))
                //TODO missing .body("fullExtent.ymin", is(-90))
                //TODO missing .body("fullExtent.xmax", is(180))
                //TODO missing .body("fullExtent.ymax", is(90))
                //TODO missing .body("fullExtent.spatialReference.wkid", is(4326))
                //TODO missing .body("fullExtent.spatialReference.latestWkid", is(4326))


                //TODO missing .body("allowGeometryUpdates", is(false))
                //TODO missing .body("units", is("esriDecimalDegrees"))
                //TODO missing .body("syncEnabled", is(false))

                .body("layers.size()", is(7))
                .body("layers.metadata.name", hasItems("GKG level 1", "GKG level 2", "GKG level 3"))
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
                .body("metadata.id", is(0))
                .body("metadata.name", is("GKG level 1"))
                //TODO missing .body("metadata.type", is("Feature Layer"))
                .body("metadata.description", notNullValue())
                .body("metadata.geometryType", is("Point"))
                //TODO missing .body("layers.metadata.copyrightText", is(" "))
                //TODO missing .body("layers.metadata.parentLayer", IsNull.nullValue())
                //TODO missing .body("layers.metadata.subLayers", IsNull.nullValue())
                //TODO missing .body("layers.metadata.minScale", is(0))
                //TODO missing .body("layers.metadata.maxScale", is(0))
                //TODO missing .body("layers.metadata.defaultVisibility", is(true))
                .body("metadata.extent.xmin", is(-180))
                .body("metadata.extent.ymin", is(-90))
                .body("metadata.extent.xmax", is(180))
                .body("metadata.extent.ymax", is(90))
                .body("metadata.extent.spatialReference.wkid", is(4326))
                .body("metadata.extent.spatialReference.latestWkid", is(4326))

                .body("metadata.fields.size()", is(9))
                .body("metadata.fields.name",
                    hasItems("OBJECTID", "urlpubtimedate", "urlpubdate", "url", "name", "urltone", "domain", "urllangcode", "geores"))
                //TODO missing .body("hasStaticData", is(false))
            ;

      // we should probably add more validation here or just add new tests if there are
      // other specific fields we want to inspect
    }

    @Test
    public void testLayers() {
        JsonPath postBody = getJson("testLayers.json");

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
                .body("layers.size()", is(7))
                .body("layers.metadata.name", hasItems("GKG level 1", "GKG level 2", "GKG level 3","GKG level 4"))
            ;

      // we should probably add more validation here or just add new tests if there are
      // other specific fields we want to inspect
    }
}


