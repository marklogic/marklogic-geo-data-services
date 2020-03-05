xquery version "1.0-ml";

module namespace gsu = "http://marklogic.com/geo-data-services/geo-search-util";

import module namespace sut = "http://marklogic.com/rest-api/lib/search-util" at "/MarkLogic/rest-api/lib/search-util.xqy";
import module namespace search = "http://marklogic.com/appservices/search" at "/MarkLogic/appservices/search/search.xqy";

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
  $heatmap as map:map
) as element(search:search)
{
  (: for testing only :)
  (:
  let $base-options := xdmp:invoke-function(
    function() { fn:doc("/Default/gds-sample-project/rest-api/options/example-gkg-options.xml") },
    <options xmlns="xdmp:eval"><database>{ xdmp:modules-database() }</database></options>)//search:options
  :)

  let $base-options := sut:options(map:entry("options", $stored-options-name))
  let $base-geo-constraint := $base-options/search:constraint[@name = $geo-constraint-name]
  
  (: replicate geo constraint and add <heatmap> :)
  let $base-geo-constraint-index := $base-geo-constraint/*[1]
  let $geo-constraint := element search:constraint {
    $base-geo-constraint/@*,
    element { fn:node-name($base-geo-constraint-index) } {
      $base-geo-constraint-index/@*,
      $base-geo-constraint-index/* except $base-geo-constraint-index/*[fn:local-name() = "heatmap"],
      element search:heatmap {
        attribute w { map:get($heatmap, "w") },
        attribute s { map:get($heatmap, "s") },
        attribute n { map:get($heatmap, "n") },
        attribute e { map:get($heatmap, "e") },
        attribute latdivs { map:get($heatmap, "latdivs") },
        attribute londivs { map:get($heatmap, "londivs") }
      }
    }
  }
  
  (: create modified stored options :)
  let $new-options := element search:options {
    $base-options/@*,
    $base-geo-constraint/preceding-sibling::*,
    $geo-constraint,
    $base-geo-constraint/following-sibling::*
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
  $options as map:map
) as item()
{
  let $start := xs:unsignedLong(map:get($options, "start"))
  let $page-length := xs:unsignedLong(map:get($options, "pageLength"))
  let $aggregate-values := xs:unsignedLong(map:get($options, "aggregate-values"))

  let $search-response := search:resolve($search/search:query, $search/search:options, $start, $page-length)

  (: find results for geo constraint :)
  let $geo-boxes := $search-response/search:boxes[@name = $geo-constraint-name]

  let $stripped-response := element search:response {
    $search-response/@*,
    $search-response/* except $geo-boxes
  }

  let $response := xdmp:from-json(sut:response-to-json-object($stripped-response, "all"))
    => map:with("values", map:map()
      => map:with($geo-constraint-name, object-node {
        "type": "Point",
        "pointClusters": json:to-array(
          for $box in $geo-boxes/search:box
          return object-node {
            "count": number-node { $box/@count },
            "s": number-node { $box/@s },
            "w": number-node { $box/@w },
            "n": number-node { $box/@n },
            "e": number-node { $box/@e }
          })
      })
    )

  return xdmp:to-json($response)
};