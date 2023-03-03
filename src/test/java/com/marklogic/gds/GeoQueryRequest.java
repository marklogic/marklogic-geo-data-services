package com.marklogic.gds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Knows how to build up the JSON for a request to geoQueryService.
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

    public GeoQueryRequest where(String whereClause) {
        getQueryNode().put("where", whereClause);
        return this;
    }

    public GeoQueryRequest withEnvelopeIntersectsQuery(String envelope, double... coordinates) {
        return withEnvelopeQuery(null, coordinates);
    }

    public GeoQueryRequest withEnvelopeContainsQuery(double... coordinates) {
        return withEnvelopeQuery("esrispatialrelcontains", coordinates);
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

    public GeoQueryRequest withPolygonQuery(double... coordinates) {
        ObjectNode query = getQueryNode();
        query.put("geometryType", "esriGeometryPolygon");
        ObjectNode geometry = query.putObject("extension").putObject("geometry");
        geometry.putObject("spatialReference").put("wkid", 4326);
        geometry.put("type", "Polygon");
        ArrayNode coords = geometry.putArray("coordinates").addArray();
        for (int i = 0; i < coordinates.length; i += 2) {
            ArrayNode pair = coords.addArray();
            pair.add(coordinates[i]).add(coordinates[i + 1]);
        }
        return this;
    }

    // TODO This redundancy can't be right, can it???
    public GeoQueryRequest withPointQuery(double x, double y) {
        ObjectNode query = getQueryNode();
        query.put("geometryType", "esriGeometryPoint");
//        query.putObject("geometry").put("x", x).put("y", y);
        ObjectNode geometry = query.putObject("extension").putObject("geometry");
//        geometry.put("x", x).put("y", y);
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
