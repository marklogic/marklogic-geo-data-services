declare variable $input external; (: expected to be something like "schema.viewName" :)
 
map:map() ! (
for $i in $input
  return if (map:get(., $i)) then () else
  let $schema := fn:substring-before($i, ".")
  let $viewName := fn:substring-after($i, ".")
  let $view := tde:get-view($schema, $viewName)
  let $hash := xdmp:hash64(xdmp:quote($view))
  return map:put(., $i, $hash),
  .
  )