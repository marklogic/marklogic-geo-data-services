import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class NonexistentTest extends AbstractFeatureServiceTest{

    @Test
    public void testNonexistentServiceName() {
        postQueryForError(
            new GeoQueryRequest("nonExistentServiceName", 99),
            404
        )
            .body("errorResponse.status", is("Not Found"))
            .body("errorResponse.messageCode", is("RESTAPI-SRVEXERR"))
            .body("errorResponse.message", is("Service nonExistentServiceName not found"))
        ;
    }

    @Test
    public void testNonexistentLayerId() {
        postQueryForError(
            new GeoQueryRequest(99),
            404
        )
            .body("errorResponse.status", is("Not Found"))
            .body("errorResponse.messageCode", is("RESTAPI-SRVEXERR"))
            .body("errorResponse.message", is("Layer 99 not found"))
        ;
    }

    @Test
    public void testNonexistentObjectId() {
        postGeoQueryRequest(
            new GeoQueryRequest().withObjectIds("9999999")
        )
            .body("features.size()", is(0))
        ;
    }
}
