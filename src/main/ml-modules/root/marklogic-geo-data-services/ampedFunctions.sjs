/**
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
 */

/**
 * Intended to define amped functions around MarkLogic functions that require privileges that we don't want to force
 * user roles to inherit.
 */

'use strict';

function invokeFunction(theFunction, options) {
    return xdmp.invokeFunction(theFunction, options);
}

function invoke(path, vars, options) {
    return xdmp.invoke(path, vars, options);
}

module.exports = {
    invoke: module.amp(invoke),
    invokeFunction: module.amp(invokeFunction)
}
