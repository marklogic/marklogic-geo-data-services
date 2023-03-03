import com.marklogic.gds.GeoQueryRequest;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class ClassificationDefTest extends AbstractFeatureServiceTest{

    @Test
    public void testGkgToneStandardDeviation0() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"OBJECTID\",\"classificationMethod\":\"esriClassifyStandardDeviation\",\"standardDeviationInterval\":5}")
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-13340.0579808794f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(3183.36201912057f))
                //TODO .body("minValue", is(-13340.0579808794f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(5))
            ;
    }

    @Test
    public void testGkgToneStandardDeviation1() {
        postGeoQueryRequest(
            new GeoQueryRequest(1)
                .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"OBJECTID\",\"classificationMethod\":\"esriClassifyStandardDeviation\",\"standardDeviationInterval\":10}")
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-61804.899857712f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-44763.169857712f))
                //TODO .body("minValue", is(-61804.899857712f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(10))
            ;
    }

    @Test
    public void testGkgToneStandardDeviation2() {
        postGeoQueryRequest(
            new GeoQueryRequest(1)
                .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"OBJECTID\",\"classificationMethod\":\"esriClassifyStandardDeviation\",\"standardDeviationInterval\":20}")
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-141140.379126623f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-124345.619126623f))
                //TODO .body("minValue", is(-141140.379126623f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(20))
            ;
    }

    @Test
    public void gkgObjectIdGeometricInterval0() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"OBJECTID\",\"classificationMethod\":\"esriClassifyGeometricalInterval\",\"breakCount\":10}")
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(1))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(2.99261807854486f))
                //TODO .body("minValue", is(1))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(10))

            ;
    }

    @Test
    public void gkgObjectIdGeometricInterval1() {
        postGeoQueryRequest(
            new GeoQueryRequest(1)
                .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"OBJECTID\",\"classificationMethod\":\"esriClassifyGeometricalInterval\",\"breakCount\":5}")
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(37))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(160.936078759448f))
                //TODO .body("minValue", is(37))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(5))
            ;
    }

    @Test
    public void gkgObjectIdGeometricInterval2() {
        postGeoQueryRequest(
            new GeoQueryRequest(1)
                .withClassificationDefObject("esriClassifyGeometricalInterval", 5)
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(37))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(160.936078759448f))
                //TODO .body("minValue", is(37))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(5))
            ;
    }

     @Test
    public void gkgObjectIdQuantile0 () {
         postGeoQueryRequest(
             new GeoQueryRequest(0)
                 .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"OBJECTID\",\"classificationMethod\":\"esriClassifyQuantile\",\"breakCount\":10}")
         )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(1))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(6437))
                //TODO .body("minValue", is(1))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(10))
            ;
    }


    @Test
    public void gkgObjectIdQuantile1 () {
        JsonPath postBody = getJson("gkgObjectIdQuantile1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(37))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(7805))
                //TODO .body("minValue", is(37))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(5))
            ;
    }

    @Test
    public void gkgObjectIdQuantile2 () {
        JsonPath postBody = getJson("gkgObjectIdQuantile2.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(0))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(3042))
                //TODO .body("minValue", is(0))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(20))
            ;
    }

    @Test
    public void gkgToneEqualInterval0() {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"urltone\",\"classificationMethod\":\"esriClassifyEqualInterval\",\"breakCount\":5}")
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-21.77f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-14.17f))
                //TODO .body("minValue", is(-21.77f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(5))
            ;
    }


    @Test
    public void gkgToneEqualInterval1 () {
        JsonPath postBody = getJson("gkgToneEqualInterval1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-15.72f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-13.206f))
                //TODO .body("minValue", is(-15.72f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(10))
            ;
    }

    @Test
    public void gkgToneEqualInterval2 () {
        JsonPath postBody = getJson("gkgToneEqualInterval2.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-21.77f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-19.87f))
                //TODO .body("minValue", is(-21.77f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(20))
            ;
    }

    @Test
    public void gkgToneQuantile0 () {
        postGeoQueryRequest(
            new GeoQueryRequest(0)
                .withClassificationDef("{\"type\":\"classBreaksDef\",\"classificationField\":\"urltone\",\"classificationMethod\":\"esriClassifyQuantile\",\"breakCount\":10}")
        )
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-21.77f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-8.48f))
                //TODO .body("minValue", is(-21.77f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(10))
            ;
    }


    @Test
    public void gkgToneQuantile1 () {
        JsonPath postBody = getJson("gkgToneQuantile1.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-15.72f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-4.64f))
                //TODO .body("minValue", is(-15.72f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(5))
            ;
    }


    @Test
    public void gkgToneQuantile2 () {
        JsonPath postBody = getJson("gkgToneQuantile2.json");
        postQuery(postBody)
                .body(isValidFeatureCollection())
                //TODO .body("statistics.classBreak[0].classMinValue", is(-21.77f))
                //TODO .body("statistics.classBreak[0].classMaxValue", is(-10.82f))
                //TODO .body("minValue", is(-21.77f))
                //TODO .body("type", is("classBreaks"))   maybe change to hasProperty()
                .body("statistics.classBreaks.size()", is(20))
            ;
    }
}
