import io.restassured.RestAssured;
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
        this.port = Integer.valueOf(System.getProperty("feature.port", "9080"));
        
        RestAssured.baseURI = "http://" + this.host;
        RestAssured.port = this.port;

        this.username = System.getProperty("feature.username", "admin");
        this.password = System.getProperty("feature.password", "admin");
        
        RestAssured.authentication = basic(this.username, this.password);
    }

    Log getLogger() {
        return LogFactory.getLog(AbstractTest.class);
    }
}
