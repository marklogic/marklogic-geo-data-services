import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

public class WhereTest extends AbstractFeatureServiceTest{

	@Test
    public void testGkgCountWhere() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .where("domain = 'karamapress.com'")
                .returnCountOnly()
        )
            .body(isValidFeatureCollection())
            .body("count", is(197));
    }

	@Test
    public void testGkgWhereISNOTNULL() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .where("name IS NOT NULL")
                .returnCountOnly()
        )
            .body(isValidFeatureCollection())
            .body("count", is(38765));
    }

	@Test
    public void testGkgWhereISNULL() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .where("name IS NULL")
                .returnCountOnly()
        )
            .body(isValidFeatureCollection())
            .body("count", is(0));
    }

	@Test
    public void testGkgWhereIn() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .where("OBJECTID IN (56577, 56576)")
                .orderByFields("OBJECTID DESC")
        )

            .log().body();
//                .body(isValidFeatureCollection())
//                //TODO missing .body("objectIdFieldName", is("OBJECTID"))
//                //TODO missing .body("globalIdFieldName", is(""))
//                //TODO missing .body("hasZ", is(false))
//                //TODO missing .body("hasM", is(false))
//
//                //TODO missing .body("spatialReference.wkid", is(4326))
//
//                .body("features.size()", is(2))
//                .body("features[1].properties.OBJECTID", is(56576))
//                .body("features[1].properties.urlpubtimedate", is("2017-05-24T14:30:00Z"))
//                .body("features[1].properties.urlpubdate", is("2017-05-24Z"))
//                .body("features[1].properties.url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
//                .body("features[1].properties.name", is("Australia"))
//                .body("features[1].properties.urltone", is(-3.91f))
//                .body("features[1].properties.domain", is("bendigoadvertiser.com.au"))
//                .body("features[1].properties.urllangcode", is("eng"))
//                .body("features[1].properties.geores", is(1))
//
//                .body("features[0].properties.OBJECTID", is(56577))
//                .body("features[0].properties.urlpubtimedate", is("2017-05-24T14:30:00Z"))
//                .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
//                .body("features[0].properties.url", is("http://www.bendigoadvertiser.com.au/story/4685559/meet-the-real-high-taxpayers-theyre-not-high-earners/"))
//                .body("features[0].properties.name", is("Australia"))
//                .body("features[0].properties.urltone", is(-3.91f))
//                .body("features[0].properties.domain", is("bendigoadvertiser.com.au"))
//                .body("features[0].properties.urllangcode", is("eng"))
//                .body("features[0].properties.geores", is(1))
//
//                .body("metadata.limitExceeded", is(false))
//        ;
    }


	@Test
    public void testGkgWhereNotIn() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .where("OBJECTID NOT IN (1738, 3233, 38530, 35645)")
                .returnCountOnly()
        )
            .body(isValidFeatureCollection())
            .body("count", is(38763));
    }

	@Test
    public void testGkgtoDateWhere() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                //.where("urlpubtimedate > TO_DATE('2017-12-01 12:00:00','YYYY-MM-DD HH24:MI:SS')")
                .where("urlpubtimedate > '2017-12-01T12:00:00'")
                .returnCountOnly()
        )
            .body(isValidFeatureCollection())
            .body("count", is(5427));
    }

    @Test
    public void testOneField() {
        JsonPath postBody = getJson("testOneField.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(29))
                .body("features.properties.domain", everyItem(isOneOf("nikkei.com")))
        ;
    }

    @Test
    public void testOrTwoFields() {
        JsonPath postBody = getJson("testOrTwoFields.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(177))
                .body("features.properties.domain", everyItem(isOneOf("livetradingnews.com", "nikkei.com")))
            ;
    }

    @Test
    public void testBetweenDates1() {
        JsonPath postBody = getJson("testBetweenDates1.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("count", is(33338))
        ;
    }

    @Test
    public void testBetweenDates2() {
        JsonPath postBody = getJson("testBetweenDates2.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("count", is(5427))
        ;
    }

    @Test
    public void testBetweenDatesNoMatch() {
        JsonPath postBody = getJson("testBetweenDatesNoMatch.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("count", is(0))
        ;
    }

    @Test
    public void testGreaterThanDate() {
        JsonPath postBody = getJson("testGreaterThanDate.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("count", is(5427))
        ;
    }

    @Test
    public void testGreaterThanTimestamp() {
        JsonPath postBody = getJson("testGreaterThanTimestamp.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("count", is(33462))
        ;
    }

    @Test
    public void testLike() {
        JsonPath postBody = getJson("testLike.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(227))
                .body("features.properties.domain", everyItem(containsString("journal")))
        ;
    }

    @Test
    public void testCombinedSearch() {
        JsonPath postBody = getJson("testCombinedSearch.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(29))
        ;
    }

    @Test
    public void testCombinedSearchFalse() {
        JsonPath postBody = getJson("testCombinedSearchFalse.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())
                .body("features.size()", is(0))
        ;
    }
}
