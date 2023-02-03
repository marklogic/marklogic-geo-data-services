import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.config.JsonPathConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import static io.restassured.RestAssured.*;

public abstract class AbstractTest {
    private String host, username, password;
    private int port;

    @Before
    public void setup() {
        this.host = System.getProperty("feature.host", "localhost");
        this.port = Integer.valueOf(System.getProperty("feature.port", "8096"));

        RestAssured.baseURI = "http://" + this.host;
        RestAssured.port = this.port;

        this.username = System.getProperty("feature.username", "test-geo-data-services-writer");
        this.password = System.getProperty("feature.password", "test-geo-data-services-writer");

        RestAssured.authentication = basic(this.username, this.password);
        RestAssured.filters(new RequestLoggingFilter(LogDetail.URI));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
    }

    Log getLogger() {
        return LogFactory.getLog(AbstractTest.class);
    }

    /**
     * @deprecated Don't use this, use the Query class instead.
     */
    @Deprecated
    public JsonPath getJson(String requestFile) {
        String pathToFile = "/" + getClass().getName() + "/" + requestFile;
        getLogger().debug("pathToFile: " + pathToFile);
        return new JsonPath(AbstractFeatureServiceTest.class.getResource(pathToFile)).using(new JsonPathConfig(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }
}
