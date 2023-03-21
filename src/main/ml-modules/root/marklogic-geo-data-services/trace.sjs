/*
 * Copyright (c) 2023 MarkLogic Corporation
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

const EVENT_INFO = 'GDS-INFO';
const EVENT_WARNING = 'GDS-WARN';
const EVENT_ERROR = 'GDS-ERR';

function trace(severity, msg, loc) {
  xdmp.trace(severity, (loc ? (loc + ':') : '') + msg);
}

exports.info = (msg, loc) => { trace(EVENT_INFO, msg, loc); }
exports.warn = (msg, loc) => { trace(EVENT_WARNING, msg, loc); }
exports.error = (msg, loc) => { trace(EVENT_ERROR, msg, loc); }
