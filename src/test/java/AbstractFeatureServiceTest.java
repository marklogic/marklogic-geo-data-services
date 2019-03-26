import java.util.Map;
import org.junit.*;

import java.net.URL;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.*;

public abstract class AbstractFeatureServiceTest {

    static String url = "/LATEST/resources/KoopProvider";
    static String port = System.getProperty("featureServer.port");
    Log logger = null;


    @Before
    public void setupRestAssured() {
        logger = getLogger();

        if (port == null) {
            port = "9080";
        }
        RestAssured.port = Integer.valueOf(port);

        String host = System.getProperty("featureServer.host");
        if (host == null) {
            host = "localhost";
        }
        RestAssured.baseURI = "http://" + host;

        String user = System.getProperty("featureServer.user");
        if (user == null) {
            user = "admin";
        }
        String password = System.getProperty("featureServer.password");
        if (password == null) {
            password = "admin";
        }

        RestAssured.authentication = basic(user, password);
        RestAssured.urlEncodingEnabled = false; // we encode the URL parameters manually
    }

	Log getLogger() {
		return LogFactory.getLog(AbstractFeatureServiceTest.class);
    }
    
    public JsonPath getJson(String requestFile) {
        String pathToFile = "/" + getClass().getName() + "/" + requestFile;
        logger.info("request2path -> pathToFile: " + pathToFile);
        return new JsonPath(AbstractFeatureServiceTest.class.getResource(pathToFile));
    }

    public String request2path(String requestFile, boolean urlEncode) {
        
        JsonPath jsonPath = getJson(requestFile);

        String service = jsonPath.getString("params.id");
        String url = "/marklogic/" + service + "/FeatureServer";

        String layer = jsonPath.getString("params.layer");
        if (layer != null) {
            url += "/" + layer;
        }

        String method = jsonPath.getString("params.method");
        if (method != null) {
            url += "/" + method;
        }

        Map<String, Object> query = jsonPath.getJsonObject("query");
        if (query != null) {
            url += "?";
            for (String param : query.keySet()) {

                Object value = query.get(param);
                if (value != null) {
                    if (urlEncode) {
                        try {
                            value = URLEncoder.encode(value.toString(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    url += param + "=" + value.toString() + "&";
                }
            }
        }

        return url;
    }
}
