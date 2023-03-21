(:
 Copyright (c) 2023 MarkLogic Corporation

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
:)

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
