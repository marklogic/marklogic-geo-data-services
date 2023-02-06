package com.marklogic.gds;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public GeoQueryRequest returnGeometry(int value) {
        getQueryNode().put("returnGeometry", value);
        return this;
    }

    public GeoQueryRequest returnIdsOnly() {
        getQueryNode().put("returnIdsOnly", true);
        return this;
    }

    public String toString() {
        return request.toString();
    }

    private ObjectNode getQueryNode() {
        if (request.has("query")) {
            return (ObjectNode) request.get("query");
        }
        ObjectNode params = (ObjectNode) request.get("params");
        params.put("method", "query");
        return request.putObject("query");
    }
}
