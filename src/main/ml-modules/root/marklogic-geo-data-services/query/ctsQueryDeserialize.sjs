/*
 * Copyright 2016-2019 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';


var deserializer = {
qd:{
  query(query) {
    // BUGTRACK: https://bugtrack.marklogic.com/52601
    // first test if it's really a serialized query; if it's a normal in-memory
    // query, just return it
    let funcName = query.constructor.name.substring(4);
 //   xdmp.log(deserializer.cts[funcName]);

    if (deserializer.cts[funcName]) return query;

    let q = cts.query(query);
    let func = Object.keys(query)[0];
    xdmp.trace("GDS-DEBUG", func);
    return deserializer.cts[func](q);
  }
},
cts:{
andNotQuery(obj) {
    return cts.andNotQuery(
        cts.andNotQueryPositiveQuery(obj),
        cts.andNotQueryNegativeQuery(obj)
        );
    },
andQuery(obj) {
    return cts.andQuery(
        cts.andQueryQueries(obj),
        cts.andQueryOptions(obj)
        );
    },
boostQuery(obj) {
    return cts.boostQuery(
        cts.boostQueryMatchingQuery(obj),
        cts.boostQueryBoostingQuery(obj)
        );
    },
box(obj) {
    return cts.box(
        cts.boxSouth(obj),
        cts.boxWest(obj),
        cts.boxNorth(obj),
        cts.boxEast(obj)
        );
    },
circle(obj) {
    return cts.circle(
        cts.circleRadius(obj),
        cts.circleCenter(obj)
        );
    },
collectionQuery(obj) {
    return cts.collectionQuery(
        cts.collectionQueryUris(obj)
        );
    },
collectionReference(obj) {
    return cts.collectionReference(
        (cts.referenceNullable(obj) === true) ? ["nullable"] : []
        );
    },
directoryQuery(obj) {
    return cts.directoryQuery(
        cts.directoryQueryUris(obj),
        cts.directoryQueryDepth(obj)
        );
    },
documentFragmentQuery(obj) {
    return cts.documentFragmentQuery(
        cts.documentFragmentQueryQuery(obj)
        );
    },
documentQuery(obj) {
    return cts.documentQuery(
        cts.documentQueryUris(obj)
        );
    },
elementAttributePairGeospatialQuery(obj) {
    return cts.elementAttributePairGeospatialQuery(
        cts.elementAttributePairGeospatialQueryElementName(obj),
        cts.elementAttributePairGeospatialQueryLatitudeName(obj),
        cts.elementAttributePairGeospatialQueryLongitudeName(obj),
        cts.elementAttributePairGeospatialQueryRegion(obj),
        cts.elementAttributePairGeospatialQueryOptions(obj),
        cts.elementAttributePairGeospatialQueryWeight(obj)
        );
    },
elementAttributeRangeQuery(obj) {
    return cts.elementAttributeRangeQuery(
        cts.elementAttributeRangeQueryElementName(obj),
        cts.elementAttributeRangeQueryAttributeName(obj),
        cts.elementAttributeRangeQueryOperator(obj),
        cts.elementAttributeRangeQueryValue(obj),
        cts.elementAttributeRangeQueryOptions(obj),
        cts.elementAttributeRangeQueryWeight(obj)
        );
    },
elementAttributeReference(obj) {
    const container = xdmp.toJSON(obj).root.elementAttributeReference;
    return cts.elementAttributeReference(
        fn.QName(fn.string(container.parentNamespaceUri), fn.string(container.parentLocalname)),
        fn.QName(fn.string(container.namespaceUri), fn.string(container.localname)),
        cts.exportCtsReferenceOptions(obj)
        );
    },
elementAttributeValueQuery(obj) {
    return cts.elementAttributeValueQuery(
        cts.elementAttributeValueQueryElementName(obj),
        cts.elementAttributeValueQueryAttributeName(obj),
        cts.elementAttributeValueQueryText(obj),
        cts.elementAttributeValueQueryOptions(obj),
        cts.elementAttributeValueQueryWeight(obj)
        );
    },
elementAttributeWordQuery(obj) {
    return cts.elementAttributeValueQuery(
        cts.elementAttributeWordQueryElementName(obj),
        cts.elementAttributeWordQueryAttributeName(obj),
        cts.elementAttributeWordQueryText(obj),
        cts.elementAttributeWordQueryOptions(obj),
        cts.elementAttributeWordQueryWeight(obj)
        );
    },
elementChildGeospatialQuery(obj) {
    return cts.elementChildGeospatialQuery(
        cts.elementChildGeospatialQueryElementName(obj),
        cts.elementChildGeospatialQueryChildName(obj),
        cts.elementChildGeospatialQueryRegion(obj),
        cts.elementChildGeospatialQueryOptions(obj),
        cts.elementChildGeospatialQueryWeight(obj)
        );
    },
elementGeospatialQuery(obj) {
    return cts.elementGeospatialQuery(
        cts.elementGeospatialQueryElementName(obj),
        cts.elementGeospatialQueryRegion(obj),
        cts.elementGeospatialQueryOptions(obj),
        cts.elementGeospatialQueryWeight(obj)
        );
    },
elementPairGeospatialQuery(obj) {
    return cts.elementPairGeospatialQuery(
        cts.elementPairGeospatialQueryElementName(obj),
        cts.elementPairGeospatialQueryLatitudeName(obj),
        cts.elementPairGeospatialQueryLongitudeName(obj),
        cts.elementPairGeospatialQueryRegion(obj),
        cts.elementPairGeospatialQueryOptions(obj),
        cts.elementPairGeospatialQueryWeight(obj)
        );
    },
elementQuery(obj) {
    return cts.elementQuery(
        cts.elementQueryElementName(obj),
        cts.elementQueryQuery(obj)
        );
    },
elementRangeQuery(obj) {
    return cts.elementRangeQuery(
        cts.elementRangeQueryElementName(obj),
        cts.elementRangeQueryOperator(obj),
        cts.elementRangeQueryValue(obj),
        cts.elementRangeQueryOptions(obj),
        cts.elementRangeQueryWeight(obj)
        );
    },
elementReference(obj) {
    const container = xdmp.toJSON(obj).root.elementReference;
    return cts.elementReference(
        fn.QName(fn.string(container.namespaceUri), fn.string(container.localname)),
        cts.exportCtsReferenceOptions(obj)
        );
    },
elementValueQuery(obj) {
    return cts.elementValueQuery(
        cts.elementValueQueryElementName(obj),
        cts.elementValueQueryText(obj),
        cts.elementValueQueryOptions(obj),
        cts.elementValueQueryWeight(obj)
        );
    },
elementWordQuery(obj) {
    return cts.elementWordQuery(
        cts.elementWordQueryElementName(obj),
        cts.elementWordQueryText(obj),
        cts.elementWordQueryOptions(obj),
        cts.elementWordQueryWeight(obj)
        );
    },
falseQuery() {
    return cts.falseQuery();
    },
fieldRangeQuery(obj) {
    return cts.fieldRangeQuery(
        cts.fieldRangeQueryFieldName(obj),
        cts.fieldRangeQueryOperator(obj),
        cts.fieldRangeQueryValue(obj),
        cts.fieldRangeQueryOptions(obj),
        cts.fieldRangeQueryWeight(obj)
        );
    },
fieldReference(obj) {
    const container = xdmp.toJSON(obj).root.fieldReference;
    return cts.fieldReference(
        fn.string(container.fieldName),
        cts.exportCtsReferenceOptions(obj)
        );
    },
fieldValueQuery(obj) {
    return cts.fieldValueQuery(
        cts.fieldValueQueryFieldName(obj),
        cts.fieldValueQueryText(obj),
        cts.fieldValueQueryOptions(obj),
        cts.fieldValueQueryWeight(obj)
        );
    },
fieldWordQuery(obj) {
    return cts.fieldWordQuery(
        cts.fieldWordQueryFieldName(obj),
        cts.fieldWordQueryText(obj),
        cts.fieldWordQueryOptions(obj),
        cts.fieldWordQueryWeight(obj)
        );
    },
jsonPropertyChildGeospatialQuery(obj) {
    return cts.jsonPropertyChildGeospatialQuery(
        cts.jsonPropertyChildGeospatialQueryPropertyName(obj),
        cts.jsonPropertyChildGeospatialQueryChildName(obj),
        cts.jsonPropertyChildGeospatialQueryRegion(obj),
        cts.jsonPropertyChildGeospatialQueryOptions(obj),
        cts.jsonPropertyChildGeospatialQueryWeight(obj)
        );
    },
jsonPropertyGeospatialQuery(obj) {
    return cts.jsonPropertyGeospatialQuery(
        cts.jsonPropertyGeospatialQueryPropertyName(obj),
        cts.jsonPropertyGeospatialQueryRegion(obj),
        cts.jsonPropertyGeospatialQueryOptions(obj),
        cts.jsonPropertyGeospatialQueryWeight(obj)
        );
    },
jsonPropertyPairGeospatialQuery(obj) {
    return cts.jsonPropertyPairGeospatialQuery(
        cts.jsonPropertyPairGeospatialQueryPropertyName(obj),
        cts.jsonPropertyPairGeospatialQueryLatitudeName(obj),
        cts.jsonPropertyPairGeospatialQueryLongitudeName(obj),
        cts.jsonPropertyPairGeospatialQueryRegion(obj),
        cts.jsonPropertyPairGeospatialQueryOptions(obj),
        cts.jsonPropertyPairGeospatialQueryWeight(obj)
        );
    },
jsonPropertyRangeQuery(obj) {
    return cts.jsonPropertyRangeQuery(
        cts.jsonPropertyRangeQueryPropertyName(obj),
        cts.jsonPropertyRangeQueryOperator(obj),
        cts.jsonPropertyRangeQueryValue(obj),
        cts.jsonPropertyRangeQueryOptions(obj),
        cts.jsonPropertyRangeQueryWeight(obj)
        );
    },
jsonPropertyReference(obj) {
    const container = xdmp.toJSON(obj).root.jsonPropertyReference;
    return cts.jsonPropertyReference(
        fn.string(container.property),
        cts.exportCtsReferenceOptions(obj)
        );
    },
jsonPropertyScopeQuery(obj) {
    return cts.jsonPropertyScopeQuery(
        cts.jsonPropertyScopeQueryPropertyName(obj),
        cts.jsonPropertyScopeQueryQuery(obj)
        );
    },
jsonPropertyValueQuery(obj) {
    return cts.jsonPropertyValueQuery(
        cts.jsonPropertyValueQueryPropertyName(obj),
        cts.jsonPropertyValueQueryValue(obj),
        cts.jsonPropertyValueQueryOptions(obj),
        cts.jsonPropertyValueQueryWeight(obj)
        );
    },
jsonPropertyWordQuery(obj) {
    return cts.jsonPropertyWordQuery(
        cts.jsonPropertyWordQueryPropertyName(obj),
        cts.jsonPropertyWordQueryText(obj),
        cts.jsonPropertyWordQueryOptions(obj),
        cts.jsonPropertyWordQueryWeight(obj)
        );
    },
locksFragmentQuery(obj) {
    return cts.locksFragmentQuery(
        cts.locksFragmentQueryQuery(obj)
        );
    },
lsqtQuery(obj) {
    return cts.lsqtQuery(
        cts.lsqtQueryTemporalCollection(obj),
        cts.lsqtQueryTimestamp(obj),
        cts.lsqtQueryOptions(obj),
        cts.lsqtQueryWeight(obj)
        );
    },
nearQuery(obj) {
    return cts.nearQuery(
        cts.nearQueryQueries(obj),
        cts.nearQueryDistance(obj),
        cts.nearQueryOptions(obj),
        cts.nearQueryWeight(obj)
        );
    },
notInQuery(obj) {
    return cts.notInQuery(
        cts.notInQueryPositiveQuery(obj),
        cts.notInQueryNegativeQuery(obj)
        );
    },
notQuery(obj) {
    return cts.notQuery(
        cts.notQueryQuery(obj)
        );
    },
orQuery(obj) {
    return cts.orQuery(
        cts.orQueryQueries(obj),
        cts.orQueryOptions(obj)
        );
    },
pathGeospatialQuery(obj) {
    return cts.pathGeospatialQuery(
        cts.pathGeospatialQueryPathExpression(obj),
        cts.pathGeospatialQueryRegion(obj),
        cts.pathGeospatialQueryOptions(obj),
        cts.pathGeospatialQueryWeight(obj)
        );
    },
pathRangeQuery(obj) {
    return cts.pathRangeQuery(
        cts.pathRangeQueryPathName(obj),
        cts.pathRangeQueryOperator(obj),
        cts.pathRangeQueryValue(obj),
        cts.pathRangeQueryOptions(obj),
        cts.pathRangeQueryWeight(obj)
        );
    },
pathReference(obj) {
    const container = xdmp.toJSON(obj).root.pathReference;
    return cts.pathReference(
        fn.string(container.pathExpression),
        cts.exportCtsReferenceOptions(obj)
        );
    },
period(obj) {
    return cts.period("period", fn.head(xdmp.xqueryEval(
        `declare variable $obj as cts:period external;
        document{$obj}/cts:period/json:to-array((
            xs:dateTime(fn:string(cts:period-start)),
            xs:dateTime(fn:string(cts:period-end))
            ))`,
            {obj:obj}
            ))
        );
    },
periodCompareQuery(obj) {
    return cts.periodCompareQuery(
        cts.periodCompareQueryAxis1(obj),
        cts.periodCompareQueryOperator(obj),
        cts.periodCompareQueryAxis2(obj),
        cts.periodCompareQueryOptions(obj)
        );
    },
periodRangeQuery(obj) {
    return cts.periodRangeQuery(
        cts.periodRangeQueryAxis(obj),
        cts.periodRangeQueryOperator(obj),
        cts.periodRangeQueryPeriod(obj),
        cts.periodRangeQueryOptions(obj)
        );
    },
point(obj) {
    return cts.point(
        cts.pointLatitude(obj),
        cts.pointLongitude(obj)
        );
    },
polygon(obj) {
    return cts.polygon(
        cts.polygonVertices(obj)
        );
    },
propertiesFragmentQuery(obj) {
    return cts.propertiesFragmentQuery(
        cts.propertiesFragmentQueryQuery(obj)
        );
    },
rangeQuery(obj) {
    return cts.rangeQuery(
        cts.rangeQueryIndex(obj),
        cts.rangeQueryOperator(obj),
        cts.rangeQueryValue(obj),
        cts.rangeQueryOptions(obj),
        cts.rangeQueryWeight(obj)
    );
},
tripleRangeQuery(obj) {
    return cts.tripleRangeQuery(
        cts.tripleRangeQuerySubject(obj),
        cts.tripleRangeQueryPredicate(obj),
        cts.tripleRangeQueryObject(obj),
        cts.tripleRangeQueryOperator(obj),
        cts.tripleRangeQueryOptions(obj),
        cts.tripleRangeQueryWeight(obj)
        );
    },
trueQuery() {
    return cts.trueQuery();
    },
uriReference() {
    return cts.uriReference();
    },
wordQuery(obj) {
    return cts.wordQuery(
        cts.wordQueryText(obj),
        cts.wordQueryOptions(obj),
        cts.wordQueryWeight(obj)
        );
    }}
};
module.exports = deserializer;

// circular dependency for recursive export
const ob = require('/MarkLogic/optic/optic-base.sjs');