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

xquery version "1.0-ml";

module namespace trans = "http://marklogic.com/rest-api/transform/default-geo-data-services-transform";


declare function trans:transform($context as map:map, $params as map:map, $content as document-node())
as document-node()
{
  document {
    <html>
      <head>
        <style>
        body
        &#123;
          font-family: Verdana, Arial;
          font-size: 12px;
          background-color: #f4fbff;
        &#125;

        .group
        &#123;
          list-style-type: none;
        &#125;

        .name
        &#123;
          font-weight: bold;
          padding: 2px;
        &#125;

        .name::after
        &#123;
          content: ': ';
        &#125;

        .value
        &#123;
          padding: 5px;
          word-wrap: break-word;
        &#125;
        </style>
      </head>
      <body>{ trans:transform-node($content) }</body>
    </html>
  }
};

declare function trans:transform-node(
  $input-node as node()
) as node()*
{
  element ul
  {
    attribute class { "group" },
    for $node in ($input-node/@*, $input-node/*)
    let $node-kind := xdmp:node-kind($node)
    return element li {
      attribute node-kind { $node-kind },
      element span {
        attribute class { "name" },
        fn:local-name-from-QName(fn:node-name($node))
      },
      if ($node-kind = "object" or ($node-kind eq "element" and fn:not(fn:empty(($node/@*, $node/*)))))
      then
        trans:transform-node($node)
      else
        element span {
          attribute class { "value" },
          fn:string($node)
        }
    }
  }
};
