package com.marklogic.gds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Knows how to build up the JSON for a request to geoQueryService.
 * <p>
 * Good reference on spatialRel definitions -
 * https://desktop.arcgis.com/en/arcmap/latest/extensions/data-reviewer/types-of-spatial-relationships-that-can-be-validated.htm
 */
public class GeoQueryRequest {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private ObjectNode request;

    /**
     * For a layer query on what seems to be the most commonly used service and layer.
     */
    public GeoQueryRequest() {
        this(0);
    }

    /**
     * For a service-level operation for the given service; e.g. getting the service descriptor.
     *
     * @param serviceName
     */
    public GeoQueryRequest(String serviceName) {
        request = objectMapper.createObjectNode();
        request.putObject("params")
            .put("id", serviceName);
    }

    /**
     * For a query operation on a particular layer
     *
     * @param layerNumber
     */
    public GeoQueryRequest(int layerNumber) {
        // Seems like most tests use the GDeltGKG service
        this("GDeltGKG", layerNumber);
    }

    /**
     * For a query operation on the given service and layer.
     *
     * @param serviceName
     * @param layerNumber
     */
    public GeoQueryRequest(String serviceName, int layerNumber) {
        request = objectMapper.createObjectNode();
        request.putObject("params")
            .put("id", serviceName)
            .put("layer", layerNumber);
    }

    public GeoQueryRequest withObjectIds(String... objectIds) {
        getQueryNode().put("objectIds", String.join(",", objectIds));
        return this;
    }

    public GeoQueryRequest withOutFields(String fields) {
        getQueryNode().put("outFields", fields);
        return this;
    }

    public GeoQueryRequest returnCountOnly() {
        getQueryNode().put("returnCountOnly", true);
        return this;
    }

    public GeoQueryRequest recordCount(int count) {
        getQueryNode().put("resultRecordCount", count);
        return this;
    }

    public GeoQueryRequest offset(int offset) {
        getQueryNode().put("resultOffset", offset);
        return this;
    }

    public GeoQueryRequest orderByFields(String value) {
        getQueryNode().put("orderByFields", value);
        return this;
    }

    public GeoQueryRequest returnGeometry() {
        getQueryNode().put("returnGeometry", 1);
        return this;
    }

    public GeoQueryRequest returnIdsOnly() {
        getQueryNode().put("returnIdsOnly", true);
        return this;
    }

    /**
     * This also supports a "search" JSON object as a child. That is presumably not a standard thing that an ArcGIS
     * Feature Service user can provide. So not sure yet if it's something we'd document as part of GDS - yet.
     *
     * @param whereClause
     * @return
     */
    public GeoQueryRequest where(String whereClause) {
        getQueryNode().put("where", whereClause);
        return this;
    }

    public GeoQueryRequest intersectsEnvelope(double... coordinates) {
        return withEnvelopeQuery(null, coordinates);
    }

    public GeoQueryRequest containsEnvelope(double... coordinates) {
        return withEnvelopeQuery("esriSpatialRelContains", coordinates);
    }

    private GeoQueryRequest withEnvelopeQuery(String spatialRel, double... coordinates) {
        ObjectNode query = getQueryNode();
        query.put("geometryType", "esriGeometryEnvelope");
        if (spatialRel != null) {
            query.put("spatialRel", spatialRel);
        }
        ObjectNode geometry = query.putObject("extension").putObject("geometry");
        geometry.put("type", "Polygon");
        ArrayNode coords = geometry.putArray("coordinates").addArray();
        for (int i = 0; i < coordinates.length; i += 2) {
            ArrayNode pair = coords.addArray();
            pair.add(coordinates[i]).add(coordinates[i + 1]);
        }
        return this;
    }

    public GeoQueryRequest intersectsPolygon(double... coordinates) {
        return withPolygonQuery(null, coordinates);
    }

    public GeoQueryRequest containsPolygon(double... coordinates) {
        return withPolygonQuery("esriSpatialRelContains", coordinates);
    }

    public GeoQueryRequest overlapsPolygon(double... coordinates) {
        return withPolygonQuery("esriSpatialRelOverlaps", coordinates);
    }

    public GeoQueryRequest touchesPolygon(double... coordinates) {
        return withPolygonQuery("esriSpatialRelTouches", coordinates);
    }

    public GeoQueryRequest withinPolygon(double... coordinates) {
        return withPolygonQuery("esriSpatialRelWithin", coordinates);
    }

    public GeoQueryRequest withPolygonQuery(String spatialRel, double... coordinates) {
        ObjectNode query = getQueryNode();
        query.put("geometryType", "esriGeometryPolygon");
        if (spatialRel != null) {
            query.put("spatialRel", spatialRel);
        }
        ObjectNode geometry = query.putObject("extension").putObject("geometry");
        geometry.put("type", "Polygon");
        ArrayNode coords = geometry.putArray("coordinates").addArray();
        for (int i = 0; i < coordinates.length; i += 2) {
            ArrayNode pair = coords.addArray();
            pair.add(coordinates[i]).add(coordinates[i + 1]);
        }
        return this;
    }

    public GeoQueryRequest crossesLineString(double... coordinates) {
        return withLineStringQuery("esriSpatialRelCrosses", coordinates);
    }

    public GeoQueryRequest intersectsLineString(double... coordinates) {
        return withLineStringQuery(null, coordinates);
    }

    private GeoQueryRequest withLineStringQuery(String spatialRel, double... coordinates) {
        ObjectNode query = getQueryNode();
        query.put("geometryType", "esriGeometryPolyline");
        if (spatialRel != null) {
            query.put("spatialRel", spatialRel);
        }
        ObjectNode geometry = query.putObject("extension").putObject("geometry");
//        geometry.putObject("spatialReference").put("wkid", 4326);
        geometry.put("type", "LineString");
        ArrayNode coords = geometry.putArray("coordinates");
        for (int i = 0; i < coordinates.length; i += 2) {
            ArrayNode pair = coords.addArray();
            pair.add(coordinates[i]).add(coordinates[i + 1]);
        }
        return this;
    }

    public GeoQueryRequest containsPoint(double x, double y) {
        return withPointQuery("esriSpatialRelContains", x, y);
    }

    public GeoQueryRequest intersectsPoint(double x, double y) {
        return withPointQuery(null, x, y);
    }

    private GeoQueryRequest withPointQuery(String spatialRel, double x, double y) {
        ObjectNode query = getQueryNode();
        query.put("geometryType", "esriGeometryPoint");
        if (spatialRel != null) {
            query.put("spatialRel", spatialRel);
        }
        ObjectNode geometry = query.putObject("extension").putObject("geometry");
        geometry.put("type", "Point");
        geometry.putArray("coordinates").add(x).add(y);
        return this;
    }

    public GeoQueryRequest withClassificationDef(String value) {
        ObjectNode query = getQueryNode("generateRenderer");
        query.put("classificationDef", value);
        return this;
    }

    public GeoQueryRequest withClassificationDefObject(String method, int breakCount) {
        ObjectNode query = getQueryNode("generateRenderer");
        query.putObject("classificationDef")
            .put("type", "classBreaksDef")
            .put("classificationField", "OBJECTID")
            .put("classificationMethod", method)
            .put("breakCount", breakCount);
        return this;
    }

    public GeoQueryRequest groupByFieldsForStatistics(String value) {
        getQueryNode().put("groupByFieldsForStatistics", value);
        return this;
    }

    public GeoQueryRequest withOutStatistics(String json) {
        getQueryNode().put("outStatistics", json);
        return this;
    }

    public GeoQueryRequest withTimeRange(String startAndEndTimestamps) {
        getQueryNode().put("time", startAndEndTimestamps);
        return this;
    }

    public String toString() {
        return request.toString();
    }

    private ObjectNode getQueryNode() {
        return getQueryNode("query");
    }

    private ObjectNode getQueryNode(String method) {
        if (request.has("query")) {
            return (ObjectNode) request.get("query");
        }
        ObjectNode params = (ObjectNode) request.get("params");
        params.put("method", method);
        return request.putObject("query");
    }
}
