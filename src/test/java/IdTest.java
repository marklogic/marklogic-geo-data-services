import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class IdTest extends AbstractFeatureServiceTest {

    @Test
    public void testGkgIdsOnly() {
        postGeoQueryRequest(
            new GeoQueryRequest()
                .recordCount(20)
                .orderByFields("name")
                .returnIdsOnly()
        )
            .body(isValidFeatureCollection())
            .body("metadata.idField", is("OBJECTID"))
            .body("metadata.fields[0].name", is("OBJECTID"))
        ;
    }

    @Test
    public void testGkgObjectIds() {
        postGeoQueryRequest(
            new GeoQueryRequest()
                .recordCount(20)
                .withObjectIds("56577", "56576")
                .orderByFields("OBJECTID DESC")
        )
            .body(isValidFeatureCollection())
            .body("features.size()", is(2))

            .body("features[0].properties.OBJECTID", is(56577))
            .body("features[0].properties.urlpubtimedate", is("2017-05-24T14:30:00Z"))
            .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
            .body("features[0].properties.url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
            .body("features[0].properties.name", is("Australia"))
            .body("features[0].properties.urltone", is(-3.91f))
            .body("features[0].properties.domain", is("bendigoadvertiser.com.au"))
            .body("features[0].properties.urllangcode", is("eng"))
            .body("features[0].properties.geores", is(1))

            .body("features[1].properties.OBJECTID", is(56576))
            .body("features[1].properties.urlpubtimedate", is("2017-05-24T14:30:00Z"))
            .body("features[1].properties.urlpubdate", is("2017-05-24Z"))
            .body("features[1].properties.url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
            .body("features[1].properties.name", is("Australia"))
            .body("features[1].properties.urltone", is(-3.91f))
            .body("features[1].properties.domain", is("bendigoadvertiser.com.au"))
            .body("features[1].properties.urllangcode", is("eng"))
            .body("features[1].properties.geores", is(1))
        ;
    }

    @Test
    public void testGeoLocationIdsOnlyWithCorrectIdFieldName() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 0)
                .recordCount(20)
                .returnIdsOnly()
        )
            .body(isValidFeatureCollection())
            .body("metadata.idField", is("ID"))
            .body("metadata.fields[0].name", is("ID"))
        ;
    }

    @Test
    public void testGeoLocationWhereWithCorrectIdFieldName() {
        postGeoQueryRequest(
            new GeoQueryRequest("GeoLocation", 0)
                .withObjectIds("10","11")
                .orderByFields("ID DESC")
                .returnIdsOnly()
        )
            .body(isValidFeatureCollection())
            .body("metadata.idField", is("ID"))
            .body("metadata.fields[0].name", is("ID"))
            .body("features.size()", is(2))
        ;
    }
}

//"24457","24973","5632","24974","27161","56371","49518","49416","32295","32309","32296","47923","32293","8384","44483","32724","22445","22455","1807","5538"
