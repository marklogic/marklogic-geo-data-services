import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ServiceDescriptorTest extends AbstractFeatureServiceTest {

    @Test
    public void getServiceDescriptor() {
        // The version is currently hardcoded in the codebase, so it's hardcoded here as well.
        // Once we can populate it dynamically in the codebase, we'll do so in the test.
        postGeoQueryRequest(new GeoQueryRequest("GDeltGKG"))
            .log().body()
            .body("$version", is("1.4-SNAPSHOT"))
            .body("layers[0].metadata.extent.xmin", is(-180))
            .body("layers[0].metadata.extent.ymin", is(-90))
            .body("layers[0].metadata.extent.xmax", is(180))
            .body("layers[0].metadata.extent.ymax", is(90))
            .body("layers[0].metadata.extent.spatialReference.wkid", is(4326))
            .body("layers[0].metadata.extent.spatialReference.latestWkid", is(4326))
            .body("layers.size()", is(8))
            .body("layers.metadata.name", hasItems("GKG level 1", "GKG level 2", "GKG level 3"));
    }

    @Test
    public void testLayerDescriptor() {
        postGeoQueryRequest(new GeoQueryRequest())
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
        postQuery(postBody)

                .body("layers.size()", is(8))
                .body("layers.metadata.name", hasItems("GKG level 1", "GKG level 2", "GKG level 3","GKG level 4"))
            ;

      // we should probably add more validation here or just add new tests if there are
      // other specific fields we want to inspect
    }
}
