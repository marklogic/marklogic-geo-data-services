import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class ServiceDescriptorArrayTest extends AbstractFeatureServiceTest {
    @Test
    public void testViewAsRoot() {
        JsonPath postBody = getJson("testViewAsRoot.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("features.size()", is(5))

                .body("features[0].properties.OBJECTID", is(1))
                .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.OBJECT_ID", is(1))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
        ;
    }

    @Test
    public void testDataSourceWithViewAsRoot() {
        JsonPath postBody = getJson("testDataSourceWithViewAsRoot.json");
        postQuery(postBody)

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
                    .body("features[0].properties.geores", is(1))
        ;
    }

    @Test
    public void testDataSourcesWithLeftOuterJoin() {
        JsonPath postBody = getJson("testDataSourcesWithLeftOuterJoin.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("features.size()", is(1502))

                .body("features[0].properties.OBJECTID", is(49))
                .body("features[0].properties.name_join", is("Ukraine"))
        ;
    }

    @Test
    public void testDataSourcesWithFullOuterJoin() {
        JsonPath postBody = getJson("testDataSourcesWithFullOuterJoin.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("features.size()", is(67622))

                .body("features[0].properties.OBJECTID", is(49))
                .body("features[0].properties.name_join", is("Ukraine"))
        ;
    }

    @Test
    public void testDataSourcesWithCrossProductJoin() {
        JsonPath postBody = getJson("testDataSourcesWithCrossProductJoin.json");
        postQueryForError(postBody);
    }

    @Test
    public void testDataSourcesWithInvalidJoin() {
        JsonPath postBody = getJson("testDataSourcesWithInvalidJoin.json");
        postQueryForError(postBody);
    }

    @Test
    public void testDataSourceWithViewAsRootAndSparqlJoin() {
        JsonPath postBody = getJson("testDataSourceWithViewAsRootAndSparqlJoin.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("features.size()", is(5))

                .body("features[0].properties.OBJECTID", is(1))
                .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.OBJECT_ID", is(1))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
        ;
    }

    @Test
    public void testDataSourceWithSparqlAsRoot() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRoot.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("features.size()", is(5))

                .body("features[0].properties.OBJECTID", is(0))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.DocId", is("/gkg_geojson/gkg_geojson_2017_05_24T02_26_02.zip/gkg_geojson_2017_05_24T02_26_02/0.json"))
        ;
    }



    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithFieldsElement() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRootAndViewJoinWithFieldsElement.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("features.size()", is(5))

                .body("features[0].properties.OBJECTID", is(0))
                .body("features[0].properties.url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
                .body("features[0].properties.OBJECT_ID", is(0))
                .body("features[0].properties.sparql_url", is("https://www.rtbf.be/info/monde/detail_le-suspect-de-manchester-un-etudiant-reserve-issu-d-un-quartier-modeste?id=9615589"))
        ;
    }

    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElement() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElement.json");
        postQuery(postBody)

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
                .body("features[0].properties.geores", is(3))
        ;
    }

    @Test
    public void testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElementStats() {
        JsonPath postBody = getJson("testDataSourceWithSparqlAsRootAndViewJoinWithoutFieldsElementStats.json");
        postQuery(postBody)

                .body(isValidFeatureCollection())

                .body("statistics.size()", is(2))
                .body(
                    "statistics.find { it.domain == '4-traders.com' }.objectid_count",
                    is(8)
                )
                .body(
                    "statistics.find { it.domain == 'bendigoadvertiser.com.au' }.objectid_count",
                    is(1)
                )
        ;
    }



}
