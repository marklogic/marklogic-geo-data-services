xquery version "1.0-ml";

(:
Helper functions for geospatial operations using the MarkLogic Search API.
In this context, "search" = the MarkLogic Search API.
:)

module namespace searchUtil = "http://marklogic.com/geo-data-services/geo-search-util";

import module namespace sut = "http://marklogic.com/rest-api/lib/search-util" at "/MarkLogic/rest-api/lib/search-util.xqy";
import module namespace search = "http://marklogic.com/appservices/search" at "/MarkLogic/appservices/search/search.xqy";
import module namespace ast = "http://marklogic.com/appservices/search-ast" at "/MarkLogic/appservices/search/ast.xqy";

declare private variable $VALUES_OPTION_PREFIX as xs:string := "__generated_values_";

declare function searchUtil:parse-combined(
  $json as item(),
  $options-name as xs:string?
) as cts:query
{
  (: convert to xml :)
  let $search := searchUtil:search-from-json($json)

  (: get options from request body, or fall back to named options :)
  let $resolved-options := searchUtil:resolve-options($search, ($options-name, "all")[1])

  (: include any additional query :)
  let $additional-queries := $resolved-options/search:additional-query/*/cts:query(.)

  (: include qtext :)
  let $text-query :=
    if ($search/search:qtext) then
      search:parse($search/search:qtext, $resolved-options, "cts:query")
    else ()

  (: include structured query :)
  let $structured-query :=
    if ($search/search:query) then
      searchUtil:structured-to-cts($search/search:query, $resolved-options)
    else ()

  (: combine all into large andQuery :)
  return cts:and-query((
    $additional-queries,
    $text-query,
    $structured-query
  ))
};

declare function searchUtil:search-from-json(
  $json as item()
) as element(search:search)
{
  let $json :=
    if ($json instance of json:object) then
      xdmp:to-json($json)
    else
      $json
  return
    sut:search-from-json($json)
};

declare function searchUtil:search-to-json(
  $search as element(search:search)
) as json:object
{
  sut:search-to-json($search)
};

declare function searchUtil:structured-to-cts(
  $sq as element(search:query),
  $options as element(search:options)
) as cts:query?
{
  map:get(ast:to-query($sq, $options), "query")
};

declare function searchUtil:resolve-options(
  $search as element(search:search),
  $options-name as xs:string
) as element(search:options)
{
  let $delta-options := $search/search:options
  let $saved-options := searchUtil:get-search-options($options-name)
  let $empty-options := <options xmlns="http://marklogic.com/appservices/search"/>
  return
    if (exists($delta-options) and exists($saved-options)) then
      sut:merge-options($saved-options, $delta-options)
    else if (empty($delta-options) and empty($saved-options)) then
      $empty-options
    else
      (: only one of these contains a value :)
      ($delta-options, $saved-options)
};

declare function searchUtil:get-search-options(
  $options-name as xs:string
) as element(search:options)?
{
  sut:options(map:entry("options", $options-name))
};

declare function searchUtil:get-search-options-constraints(
  $options-name as xs:string
) as object-node()*
{
  for $constraint in searchUtil:get-search-options($options-name)/search:constraint
    let $json := json:object()
    let $name := $constraint/@name
    let $description := $constraint/search:annotation/description
    return (
      map:put($json, "name", fn:string($name)),
      if (fn:empty($description)) then () else map:put($json, "description", fn:string($description)),
      xdmp:to-json($json)/object-node()
    )
};

declare function searchUtil:get-geometry-type(
  $constraint as element(search:constraint)
) as xs:string*
{
  let $point-types := ("geo-attr-pair", "geo-elem", "geo-elem-pair", "geo-json-property", "geo-json-property-pair", "geo-path")
  let $region-types := ("geo-region-path")
  return if (fn:exists($constraint/*[fn:local-name(.) = $point-types])) then "Point"
  else if (fn:exists($constraint/*[fn:local-name(.) = $region-types])) then "Region"
  else ()
};

declare function searchUtil:create-heatmapped-constraint(
  $base-geo-constraint as element(search:constraint),
  $options as object-node()
) as element(search:constraint)
{
  let $base-geo-constraint-index := $base-geo-constraint/*[1]
  let $values-limit := xs:unsignedLong($options/valuesLimit)
  let $viewport := $options/viewport
  let $lat-per-div := 180.0 div $viewport/maxLatDivs
  let $lon-per-div := 360.0 div $viewport/maxLonDivs
  let $lon-divs := fn:ceiling(fn:max(((($viewport/box/e + fn:abs($viewport/box/w)) div $lon-per-div), $options/defaultMinDivs)))
  let $lat-divs := fn:ceiling(fn:max(((($viewport/box/n + fn:abs($viewport/box/s)) div $lat-per-div), $options/defaultMinDivs)))
  let $exclude-elems := (
    $base-geo-constraint-index/search:heatmap,
    $base-geo-constraint-index/search:facet-option[fn:starts-with(fn:string(.), "limit=")]
  )
  return element search:constraint {
    $base-geo-constraint/@*,
    element { fn:node-name($base-geo-constraint-index) } {
      $base-geo-constraint-index/@*,
      $base-geo-constraint-index/* except $exclude-elems,
      element search:heatmap {
        attribute w { $viewport/box/w },
        attribute s { $viewport/box/s },
        attribute n { $viewport/box/n },
        attribute e { $viewport/box/e },
        attribute latdivs { $lat-divs },
        attribute londivs { $lon-divs }
      },
      if ($values-limit gt 0) then element search:facet-option { fn:concat("limit=", $values-limit) } else ()
    }
  }
};

declare function searchUtil:create-search-criteria(
  $stored-options-name as xs:string,
  $delta-search as element(search:search),
  $geo-constraint-names as xs:string*,
  $options as object-node()
) as element(search:search)
{
  (: for testing only :)
  (:
  let $base-options := xdmp:invoke-function(
    function() { fn:doc("/Default/gds-sample-project/rest-api/options/example-gkg-options.xml") },
    <options xmlns="xdmp:eval"><database>{ xdmp:modules-database() }</database></options>)//search:options
  :)

  let $aggregate-values := $options/aggregateValues eq fn:true()
  let $return-values := $options/returnValues eq fn:true()
  let $values-limit := xs:unsignedLong($options/valuesLimit)
  let $base-options := searchUtil:get-search-options($stored-options-name)

  (: qtext to structured queries :)
  let $qtext-queries := search:parse($options/fullQueryText, $base-options, "search:query")

  let $base-geo-constraints := $base-options/search:constraint[@name = $geo-constraint-names]
  let $new-geo-constraints := for $base-geo-constraint in $base-geo-constraints
    (: get values via <heatmap> :)
    let $use-heatmap := $return-values and $aggregate-values and searchUtil:get-geometry-type($base-geo-constraint) eq "Point"
    return if ($use-heatmap) then searchUtil:create-heatmapped-constraint($base-geo-constraint, $options) else $base-geo-constraint

  let $values-options := for $base-geo-constraint in $base-geo-constraints
    (: get values via <values> :)
    let $use-values := $return-values and (fn:not($aggregate-values) or ($aggregate-values and searchUtil:get-geometry-type($base-geo-constraint) ne "Point"))
    let $base-geo-constraint-index := $base-geo-constraint/*[1]
    return element search:values {
      attribute name { fn:concat($VALUES_OPTION_PREFIX, $base-geo-constraint/@name) },
      $base-geo-constraint-index,
      if ($values-limit gt 0) then element search:values-option { fn:concat("limit=", $values-limit) } else ()
    }

  (: create modified stored options :)
  let $new-options := element search:options {
    $base-options/@*,
    $base-options/* except $base-geo-constraints,
    $new-geo-constraints,
    $values-options
  }

  (: merge with delta options :)
  let $merged-options := sut:merge-options($new-options, $delta-search/search:options)

  (: create search:search :)
  return element search:search {
    element search:query {
      $qtext-queries/*,
      $delta-search/search:query/*
    },
    $merged-options
  }
};

declare function searchUtil:get-search-suggestions(
  $search as element(search:search),
  $qtext as xs:string,
  $limit as xs:unsignedInt?
) as xs:string*
{
  search:suggest(
    $qtext,
    $search/search:options,
    ($limit, 10)[1],
    (),
    (),
    $search/search:query)
};

declare function searchUtil:get-search-results(
  $search as element(search:search),
  $geo-constraint-names as xs:string*,
  $options as object-node()
) as item()
{
  let $start := xs:unsignedLong($options/start)
  let $page-length := xs:unsignedLong($options/pageLength)
  let $return-facets := $options/returnFacets eq fn:true()

  let $search-response := search:resolve($search/search:query, $search/search:options, $start, $page-length)

  let $excluded-response-elems := (
    $search-response/search:boxes[@name = $geo-constraint-names], (: don't include search:boxes returned from altered constraints :)
    if (fn:not($return-facets)) then $search-response/search:facet else ()
  )
  let $stripped-response := element search:response {
    $search-response/@*,
    $search-response/* except $excluded-response-elems
  }

  let $response := xdmp:from-json(sut:response-to-json-object($stripped-response, "all"))
    => searchUtil:add-response-values($search, $search-response, $geo-constraint-names, $options)

  return xdmp:to-json($response)
};

declare private function searchUtil:add-response-values(
  $json as json:object,
  $search as element(search:search),
  $search-response as element(search:response),
  $geo-constraint-names as xs:string*,
  $options as object-node()
) as map:map
{
  let $return-values := $options/returnValues eq fn:true()
  return if ($return-values)
  then
    let $values-object := json:object()
    return (
      for $geo-constraint-name in $geo-constraint-names
        let $constraint-values := json:object()
          => map:with("type", searchUtil:get-geometry-type($search/search:options/search:constraint[@name eq $geo-constraint-name]))
          => searchUtil:add-constraint-clusters($geo-constraint-name, $search-response, $options)
          => searchUtil:add-constraint-values($geo-constraint-name, $search, $options)
        return map:put($values-object, $geo-constraint-name, $constraint-values),
      map:put($json, "values", $values-object),
      $json
    )
  else $json
};

declare private function searchUtil:add-constraint-clusters(
  $json as json:object,
  $geo-constraint-name as xs:string,
  $search-response as element(search:response),
  $options as object-node()
) as map:map
{
  let $aggregate-values := $options/aggregateValues eq fn:true()
  let $geo-constraint-boxes := $search-response/search:boxes[@name = $geo-constraint-name]
  return if ($aggregate-values and $geo-constraint-boxes)
  then
    let $point-clusters := json:array(), $points := json:array()
    let $_ := (
      for $box in $geo-constraint-boxes/search:box
      return if ($box/@s eq $box/@n and $box/@w eq $box/@e)
      then json:array-push($points, object-node {
        "count": number-node { $box/@count },
        "lat": number-node { $box/@n },
        "lon": number-node { $box/@e }
      })
      else json:array-push($point-clusters, object-node {
        "count": number-node { $box/@count },
        "s": number-node { $box/@s },
        "w": number-node { $box/@w },
        "n": number-node { $box/@n },
        "e": number-node { $box/@e }
      }),
      map:put($json, "total", fn:count($geo-constraint-boxes/search:box)),
      if (json:array-size($point-clusters) gt 0) then map:put($json, "pointClusters", $point-clusters) else (),
      if (json:array-size($points) gt 0) then map:put($json, "points", $points) else ()
    )
    return $json
  else $json
};

declare private function searchUtil:add-constraint-values(
  $json as json:object,
  $geo-constraint-name as xs:string,
  $search as element(search:search),
  $options as object-node()
) as map:map
{
  let $geo-constraint-values-name := fn:concat($VALUES_OPTION_PREFIX, $geo-constraint-name)
  let $geo-constraint-values := $search/search:options/search:values[@name = $geo-constraint-values-name]
  let $aggregate-values := $options/aggregateValues eq fn:true()
  return if (fn:not($aggregate-values) and $geo-constraint-values)
  then
    let $is-longlat := fn:not(fn:empty($geo-constraint-values//search:geo-option[fn:string(.) eq "type=long-lat-point"]))
    let $lat-index := if ($is-longlat) then 2 else 1
    let $lon-index := if ($is-longlat) then 1 else 2
    let $values-response := search:values($geo-constraint-values-name, $search/search:options, $search/search:query)
    return $json
      => map:with("total", fn:count($values-response/search:distinct-value))
      => map:with("points", json:to-array(
        for $value in $values-response/search:distinct-value
        let $coords := fn:tokenize($value, ",")
        return object-node {
          "count": number-node { $value/@frequency },
          "lat": number-node { $coords[$lat-index] },
          "lon": number-node { $coords[$lon-index] }
        })
    )
  else $json
};
