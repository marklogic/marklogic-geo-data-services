import com.marklogic.gds.GeoQueryRequest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class DataSourcesTest extends AbstractFeatureServiceTest {

    private GeoQueryRequest requestWithLayer(int layerNumber) {
        return new GeoQueryRequest("DataSourceArrayExample", layerNumber);
    }

    @Test
    public void testViewAsRoot() {
        postGeoQueryRequest(
            requestWithLayer(5)
                .recordCount(5)
                .orderByFields("OBJECTID ASC")
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features[0].properties.OBJECTID", is(1))
                .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.OBJECT_ID", is(1))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"));
    }

    @Test
    public void testDataSourceWithViewAsRoot() {
        postGeoQueryRequest(
            requestWithLayer(6)
                .recordCount(5)
                .orderByFields("OBJECTID ASC")
        )
                    .body(isValidFeatureCollection())
                    .body("features.size()", is(5))
                    .body("features[0].properties.OBJECTID", is(1))
                    .body("features[0].properties.urlpubtimedate", is("2017-05-24T06:00:00Z"))
                    .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
                    .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                    .body("features[0].properties.name", is("United Kingdom"))
                    .body("features[0].properties.urltone", is(-3.41f))
                    .body("features[0].properties.domain", is("rtbf.be"))
                    .body("features[0].properties.urllangcode", is("fra"))
                    .body("features[0].properties.geores", is(1));
    }

    @Test
    public void testDataSourcesWithLeftOuterJoin() {
        postGeoQueryRequest(
            requestWithLayer(12)
                .recordCount(5000)
                .orderByFields("OBJECTID ASC")
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(1502))
                .body("features[0].properties.OBJECTID", is(49))
                .body("features[0].properties.name_join", is("Ukraine"));
    }

    /**
     * This test unfortunately takes awhile because it gets back 67k results. Not sure
     * if that's important to the purpose of the test. 
     */
    @Test
    public void testDataSourcesWithFullOuterJoin() {
        postGeoQueryRequest(
            requestWithLayer(13)
                .recordCount(70000)
                .orderByFields("OBJECTID ASC")
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(67622))
                .body("features[0].properties.OBJECTID", is(49))
                .body("features[0].properties.name_join", is("Ukraine"));
    }

    @Test
    public void testDataSourcesWithCrossProductJoin() {
        postForResponse(
            requestWithLayer(14)
                .orderByFields("OBJECTID ASC")
                .toString()
        )
            // This is apparently due to the "joinType" being intentionally invalid in the layer.
            // This seems unexpected as the "InvalidJoin" test already tests this.
            .then().statusCode(500);
    }

    @Test
    public void testDataSourcesWithInvalidJoin() {
        postForResponse(
            requestWithLayer(15)
                .orderByFields("OBJECTID ASC")
                .toString()
        )
            // This is apparently due to the "joinType" being intentionally invalid in the layer.
            .then().statusCode(500);
    }

    @Test
    public void testDataSourceWithViewAsRootAndSparqlJoin() {
        postGeoQueryRequest(
            requestWithLayer(7)
                .recordCount(5)
                .orderByFields("OBJECTID ASC")
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features[0].properties.OBJECTID", is(1))
                .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.OBJECT_ID", is(1))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"));
    }

    @Test
    public void testDataSourceWithSparqlAsRoot() {
        postGeoQueryRequest(
            requestWithLayer(8)
                .recordCount(5)
                .orderByFields("OBJECTID ASC")
                .returnGeometry()
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features[0].properties.OBJECTID", is(0))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.DocId", is("/gkg_geojson/gkg_geojson_2017_05_24T02_26_02.zip/gkg_geojson_2017_05_24T02_26_02/0.json"));
    }



    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithFieldsElement() {
        postGeoQueryRequest(
            requestWithLayer(9)
                .recordCount(5)
                .orderByFields("OBJECTID ASC")
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features[0].properties.OBJECTID", is(0))
                .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.OBJECT_ID", is(0))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"));
    }

    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElement() {
        postGeoQueryRequest(
            requestWithLayer(10)
                .recordCount(5)
                .orderByFields("OBJECTID ASC")
        )
                .body(isValidFeatureCollection())
                .body("features.size()", is(5))
                .body("features[0].properties.OBJECT_ID", is(0))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.OBJECTID", is(0))
                .body("features[0].properties.urlpubtimedate", is("2017-05-24T06:00:00Z"))
                .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
                .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.name", is("Tripoli, Tarabulus, Libya"))
                .body("features[0].properties.urltone", is(-3.41f))
                .body("features[0].properties.domain", is("rtbf.be"))
                .body("features[0].properties.urllangcode", is("fra"))
                .body("features[0].properties.geores", is(3));
    }

    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElementStats() {
        postGeoQueryRequest(
            requestWithLayer(11)
                .recordCount(5)
                .groupByFieldsForStatistics("domain")
                .withOutStatistics("[ {\"statisticType\":\"count\",\"onStatisticField\":\"OBJECTID\",\"outStatisticFieldName\":\"objectid_count\"} ]")
        )
                .body(isValidFeatureCollection())
                .body("statistics.size()", is(2))
                .body("statistics.find { it.domain == '4-traders.com' }.objectid_count", is(8))
                .body("statistics.find { it.domain == 'bendigoadvertiser.com.au' }.objectid_count", is(1));
    }
}
