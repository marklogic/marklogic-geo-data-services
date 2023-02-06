package com.marklogic.gds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Knows how to build up the JSON for a query to geoQueryService.
 */
public class Query {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private ObjectNode objectNode;
    private ObjectNode queryNode;

    public Query(int layerId) {
        // Seems like most tests use the GDeltGKG feature
        this("GDeltGKG", layerId);
    }

    public Query(String featureId, int layerId) {
        objectNode = objectMapper.createObjectNode();
        objectNode.putObject("params")
            .put("id", featureId)
            .put("layer", layerId)
            .put("method", "query");
        queryNode = objectNode.putObject("query");
    }

    public Query withObjectIds(String... objectIds) {
        queryNode.put("objectIds", String.join(",", objectIds));
        return this;
    }

    public Query returnCountOnly() {
        queryNode.put("returnCountOnly", true);
        return this;
    }

    public Query recordCount(int count) {
        queryNode.put("resultRecordCount", count);
        return this;
    }

    public Query orderByFields(String value) {
        queryNode.put("orderByFields", value);
        return this;
    }

    public Query returnGeometry(int value) {
        queryNode.put("returnGeometry", value);
        return this;
    }

    public Query returnIdsOnly() {
        queryNode.put("returnIdsOnly", true);
        return this;
    }

    public String toString() {
        return objectNode.toString();
    }
}
