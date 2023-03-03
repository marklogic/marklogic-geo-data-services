import com.marklogic.gds.GeoQueryRequest;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class AliasTest extends AbstractFeatureServiceTest {

    @Test
    public void testFieldAlias() {
        postGeoQueryRequest(
            new GeoQueryRequest(4)
                .recordCount(5)
                .orderByFields("name")
                .returnGeometry()
        )
            .body(isValidFeatureCollection())

            .body("features.size()", is(5))

            .body("features[0].properties.OBJECTID", is(20643))
            .body("features[0].properties.urlpubtimedate", is("2017-05-24T09:00:00Z"))
            .body("features[0].properties.urlpubdate", is("2017-05-24Z"))
            .body("features[0].properties.url", is("http://satnews.com/story.php?number=1191513746"))
            .body("features[0].properties.name", is("Aalborg, Nordjylland, Denmark"))

            .body("metadata.fields.find {it.name == 'OBJECTID'}.type", is("Integer"))
            .body("metadata.fields.find {it.name == 'OBJECTID'} .alias", is(IsNull.nullValue()))
            .body("metadata.fields.find {it.name == 'urlpubtimedate'}.type", is("Date"))
            .body("metadata.fields.find {it.name == 'urlpubtimedate'} .alias", is("pubtime"))
            .body("metadata.fields.find {it.name == 'urlpubdate'}.type", is("Date"))
            .body("metadata.fields.find {it.name == 'urlpubdate'} .alias", is("pubdate"))
            .body("metadata.fields.find {it.name == 'url'}.type", is("String"))
            .body("metadata.fields.find {it.name == 'url'} .alias", is("doc_url"))
            .body("metadata.fields.find {it.name == 'name'}.type", is("String"))
            .body("metadata.fields.find {it.name == 'name'} .alias", is("Location"));
    }
}
