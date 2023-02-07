import io.restassured.RestAssured;

public abstract class AbstractGeoserverServiceTest extends AbstractTest {
    public void setup() {
        super.setup();
        RestAssured.basePath = "/LATEST/resources/geoserverService";
    }
}
