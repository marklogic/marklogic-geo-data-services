xquery version "1.0-ml";

module namespace cache = "server-field-cache";

declare variable $DEBUG := fn:true();

declare variable $default-cache-timeout := xs:dayTimeDuration("PT1H");

declare function cache:get-and-reset($key as xs:string) {
	cache:get-and-reset($key, ())
};

(: method used to reset expiration time upon access to a value :)
declare function cache:get-and-reset($key as xs:string, $duration as xs:dayTimeDuration?) as item()* {
	let $key := fn:concat("cache_", $key)
	let $entry := xdmp:get-server-field($key)
	let $sdur := xdmp:get-session-field("credential-timeout")
	let $dur := ($duration, $sdur, $default-cache-timeout)[1]
	return
		if (fn:not(fn:exists($entry))) then
			()
		else if (cache:is-expired($entry)) then
			xdmp:set-server-field($key, ())
		else
			(
				let $val := map:get($entry, "value")
				let $_ := map:put($entry, "expiration", fn:current-dateTime() + $dur)
				let $_ := xdmp:set-server-field($key, $entry) 
				return
					$val
			)
};

declare function cache:get($key as xs:string) as item()* {
    let $_ := if ($DEBUG) then xdmp:trace("udm-cache", ("***INSIDE cache:get")) else ()
    let $_ := if ($DEBUG) then xdmp:trace("udm-cache", (fn:concat("key:", xdmp:quote($key)))) else ()
	let $key := fn:concat("cache_", $key)
	let $entry := xdmp:get-server-field($key)
	let $_ := if ($DEBUG) then xdmp:trace("udm-cache", (fn:concat("entry:", xdmp:quote($entry)))) else ()
	return
		if (fn:exists($entry) and cache:is-expired($entry)) then
			xdmp:set-server-field($key, ())
		else
			map:get($entry, "value")
};

(: returns the time the entry expires :)
declare function cache:get-expiration($key as xs:string) {
	let $key := fn:concat("cache_", $key)
	let $entry := xdmp:get-server-field($key)
	return
		map:get($entry, "expiration")
};

(: returns the number of seconds from a duration :)
declare private function cache:duration-to-seconds($d as xs:dayTimeDuration) as xs:unsignedLong {
	xs:unsignedLong( $d div xs:dayTimeDuration('PT1S') )
};

(: returns the # of seconds the entry is valid for - if expired or does not exist - returns 0 :)
declare function cache:get-expiration-seconds($key as xs:string) {
	let $key := fn:concat("cache_", $key)
	let $entry := xdmp:get-server-field($key)
	return
		if (fn:exists($entry)) then
			let $delta := cache:duration-to-seconds(map:get($entry, "expiration") - fn:current-dateTime())
			return
				if ($delta > 0) then
					$delta
				else 0
		else
			0
};


declare private function cache:is-expired($entry as map:map) as xs:boolean {
	map:get($entry, "expiration") lt fn:current-dateTime()
};

declare function cache:put($key as xs:string, $value as item()*) as item()* {
	cache:put($key, $value, ())
};

declare function cache:put($key as xs:string, $value as item()*, $duration as xs:dayTimeDuration?) as item()* {

    let $_ := if ($DEBUG) then xdmp:trace("udm-cache", ("***INSIDE cache:put")) else ()
    let $_ := if ($DEBUG) then xdmp:trace("udm-cache", (fn:concat("key:", $key))) else ()
    let $_ := if ($DEBUG) then xdmp:trace("udm-cache", (fn:concat("value:", xdmp:quote($value)))) else ()
    let $_ := if ($DEBUG) then xdmp:trace("udm-cache", (fn:concat("duration:", xdmp:quote($duration)))) else ()

	let $key := fn:concat("cache_", $key)
	let $sdur := xdmp:get-session-field("credential-timeout")
	let $dur := ($duration, $sdur, $default-cache-timeout)[1]
	let $entry := map:map()
	let $_ :=	(
					map:put($entry, "expiration", fn:current-dateTime() + $dur),
					map:put($entry, "value", $value),
					xdmp:set-server-field($key, $entry)
					)
	return $value
};

declare function cache:clear($key as xs:string) {
	xdmp:set-server-field(fn:concat("cache_", $key), ())
};

declare function cache:clear-expired() {
	for $field in xdmp:get-server-field-names()[fn:starts-with(., "cache_")]
		let $key := fn:substring-after($field, "cache_")
		let $_ := cache:get($key)
	return
		()
};

(:~
 : Clear all cached values.
 :)
declare function cache:clear() {
	for $i in xdmp:get-server-field-names()[fn:starts-with(., "cache_")]
	return xdmp:set-server-field($i, ())
};

declare function cache:toggleDebug() {
	let $val := cache:get("logger-debug-all")
	return
		if ($val = "true") then	
			cache:put("logger-debug-all", fn:false(), $default-cache-timeout)
		else
			cache:put("logger-debug-all", fn:true(), $default-cache-timeout)
};

declare function cache:getCategoryDebug($category) as xs:boolean {
	(: enable debugging if all is set or security :)
	let $debugall := cache:get("logger-debug-all")
	let $check := if ( $debugall ) then
							()
						else
							cache:put("logger-debug-all", fn:false(), $default-cache-timeout)
	return
		(cache:get("logger-debug-all") or cache:get($category))
};