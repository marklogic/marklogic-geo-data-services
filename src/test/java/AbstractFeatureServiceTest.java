import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.module.jsv.JsonSchemaValidator;

import static io.restassured.module.jsv.JsonSchemaValidator.*;

public abstract class AbstractFeatureServiceTest extends AbstractTest  {
    private static JsonSchemaValidator geoJsonFeatureCollectionValidator = null;

    public void setup() {
        super.setup();
        RestAssured.basePath = "/LATEST/resources/geoQueryService";
    }

    public JsonSchemaValidator isValidFeatureCollection() {
        if (geoJsonFeatureCollectionValidator == null) {
            geoJsonFeatureCollectionValidator = matchesJsonSchema(AbstractFeatureServiceTest.class.getResource("geojson-schema/FeatureCollection.json"));
        }
        return geoJsonFeatureCollectionValidator;
    }

    public JsonPath getJson(String requestFile) {
        String pathToFile = "/" + getClass().getName() + "/" + requestFile;
        getLogger().info("pathToFile: " + pathToFile);
        return new JsonPath(AbstractFeatureServiceTest.class.getResource(pathToFile)).using(new JsonPathConfig(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }
}
