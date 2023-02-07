import java.util.HashMap;
import java.util.Map;

/**
 * Knows how to build up the JSON for a request to geoQueryService.
 */
public class GeoserverRequest {

    final private Map<String, String> params;

    public GeoserverRequest() {
        this.params = new HashMap<>();
    }

    public GeoserverRequest withParam(String paramName, String paramValue) {
        this.params.put(paramName, paramValue);
        return this;
    }

    public Map<String, String> getParams() { return params; }

}
