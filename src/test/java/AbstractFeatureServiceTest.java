import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

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
}
