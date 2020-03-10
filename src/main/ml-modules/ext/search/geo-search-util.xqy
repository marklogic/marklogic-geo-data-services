xquery version "1.0-ml";

module namespace gsu = "http://marklogic.com/geo-data-services/geo-search-util";

import module namespace sut = "http://marklogic.com/rest-api/lib/search-util" at "/MarkLogic/rest-api/lib/search-util.xqy";
import module namespace search = "http://marklogic.com/appservices/search" at "/MarkLogic/appservices/search/search.xqy";

declare private variable $VALUES_OPTION_PREFIX as xs:string := "__generated_values_";

declare function gsu:search-from-json(
  $json as node()
) as element(search:search)
{
  sut:search-from-json($json)
};

declare function gsu:search-to-json(
  $search as element(search:search)
) as json:object
{
  sut:search-to-json($search)
};

declare function gsu:create-search-criteria(
  $stored-options-name as xs:string,
  $delta-search as element(search:search),
  $geo-constraint-name as xs:string,
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
  let $heatmap := xdmp:from-json($options/heatmap)

  let $base-options := sut:options(map:entry("options", $stored-options-name))
  let $base-geo-constraint := $base-options/search:constraint[@name = $geo-constraint-name]
  
  (: Add <heatmap> or <values> to the options if we'll be returning values :)
  let $use-aggregation := $aggregate-values (: TODO: include check geometry type; anything but points should make this false :)
  let $use-heatmap := $return-values and $use-aggregation
  let $use-values := $return-values and fn:not($use-aggregation)
  let $base-geo-constraint-index := $base-geo-constraint/*[1]

  (: <heatmap> option :)
  let $geo-constraint := if ($use-heatmap)
  then 
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
        attribute w { map:get($heatmap, "w") },
        attribute s { map:get($heatmap, "s") },
        attribute n { map:get($heatmap, "n") },
        attribute e { map:get($heatmap, "e") },
        attribute latdivs { map:get($heatmap, "latdivs") },
        attribute londivs { map:get($heatmap, "londivs") }
      },
      element search:facet-option { fn:concat("limit=", $values-limit) }
    }
  }
  else $base-geo-constraint

  (: <values> option :)
  let $values-option := if ($use-values)
  then element search:values {
    attribute name { fn:concat($VALUES_OPTION_PREFIX, $base-geo-constraint/@name) },
    $base-geo-constraint-index,
    element search:values-option { fn:concat("limit=", $values-limit) }
  }
  else ()
  
  (: create modified stored options :)
  let $new-options := element search:options {
    $base-options/@*,
    $base-geo-constraint/preceding-sibling::*,
    $geo-constraint,
    $base-geo-constraint/following-sibling::*,
    $values-option
  }
  
  (: merge with delta options :)
  let $merged-options := sut:merge-options($new-options, $delta-search/search:options)
  
  (: create search:search :)
  return element search:search {
    $delta-search/search:query,
    $merged-options
  }
};

declare function gsu:get-search-results(
  $search as element(search:search),
  $geo-constraint-name as xs:string,
  $options as object-node()
) as item()
{
  let $start := xs:unsignedLong($options/start)
  let $page-length := xs:unsignedLong($options/pageLength)
  let $aggregate-values := $options/aggregateValues eq fn:true()
  let $return-facets := $options/returnFacets eq fn:true()
  let $return-values := $options/returnValues eq fn:true()

  let $search-response := search:resolve($search/search:query, $search/search:options, $start, $page-length)

  (: prepare results for geo constraint :)
  let $geo-constraint-boxes := $search-response/search:boxes[@name = $geo-constraint-name]
  let $geometry-type := "Point" (: TODO: replace with function that determines type :)
  let $geo-constraint-values-node := json:object()
    => map:with("type", $geometry-type)
    => gsu:make-constraint-clusters($geo-constraint-boxes)
    => gsu:make-constraint-values($geo-constraint-name, $search)

  let $excluded-response-elems := (
    $geo-constraint-boxes, (: don't include search:boxes returned from altered constraints :)
    if (fn:not($return-facets)) then $search-response/search:facet else ()
  )
  let $stripped-response := element search:response {
    $search-response/@*,
    $search-response/* except $excluded-response-elems
  }

  (: TODO: this will be multiple later:)
  let $constraint-values := map:map()
    => map:with($geo-constraint-name, $geo-constraint-values-node)

  let $response := xdmp:from-json(sut:response-to-json-object($stripped-response, "all"))
    => gsu:make-response-values($constraint-values)

  return xdmp:to-json($response)
};

declare private function gsu:make-response-values(
  $json as json:object,
  $constraint-values as map:map
) as map:map
{
  if (map:count($constraint-values) gt 0)
  then map:with($json, "values", $constraint-values)
  else $json
};

declare private function gsu:make-constraint-clusters(
  $json as json:object,
  $geo-constraint-boxes as element(search:boxes)*
) as map:map
{
  if ($geo-constraint-boxes)
  then map:with($json, "pointClusters", json:to-array(
    for $box in $geo-constraint-boxes/search:box
    return object-node {
      "count": number-node { $box/@count },
      "s": number-node { $box/@s },
      "w": number-node { $box/@w },
      "n": number-node { $box/@n },
      "e": number-node { $box/@e }
    })
  )
  else $json
};

declare private function gsu:make-constraint-values(
  $json as json:object,
  $geo-constraint-name as xs:string,
  $search as element(search:search)
) as map:map
{
  let $geo-constraint-values-name := fn:concat($VALUES_OPTION_PREFIX, $geo-constraint-name)
  let $geo-constraint-values := $search/search:options/search:values[@name = $geo-constraint-values-name]
  
  return if ($geo-constraint-values)
  then
    let $is-longlat := fn:not(fn:empty($geo-constraint-values//search:geo-option[fn:string(.) eq "type=long-lat-point"]))
    let $values-response := search:values($geo-constraint-values-name, $search/search:options, $search/search:query)
    return map:with($json, "points", json:to-array(
      for $value in $values-response/search:distinct-value
      let $coords := fn:tokenize($value, ",")
      return object-node {
        "count": number-node { $value/@frequency },
        "lat": number-node { $coords[if ($is-longlat) then 2 else 1] },
        "lon": number-node { $coords[if ($is-longlat) then 1 else 2] }
      })
    )
  else $json
};