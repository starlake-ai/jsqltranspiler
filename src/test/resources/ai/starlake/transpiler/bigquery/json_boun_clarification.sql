--provided
SELECT JSON_EXTRACT_ARRAY('{"a": "foo"}', '$.a') AS result;

--expected
SELECT JSon_Extract('{"a": "foo"}', '$.a[*]') AS result

--output
"result"
"""foo"""

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract,
       JSON_EXTRACT_SCALAR('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract_scalar;

--expected
--TODO: Regenerate once implemented
SELECT JSON_EXTRACT('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract, JSon_Extract_String('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract_scalar

--output
"json_extract","json_extract_scalar"
"[""apple"",""banana""]","[""apple"",""banana""]"

--result
"json_extract","json_extract_scalar"
"[""apple"",""banana""]","JSQL_NULL"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY(
               JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits'
       ) AS string_array;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract(JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array

--output
"string_array"
"[""apples"",""oranges"",""grapes""]"

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('["foo", "bar", "baz"]', '$') AS string_array;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract('["foo", "bar", "baz"]', '$') AS string_array

--output
"string_array"
"[""foo"",""bar"",""baz""]"

--result
"string_array"
"[foo, bar, baz]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$[fruits]') AS string_array;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract('{"fruits": ["apples", "oranges", "grapes"]}', '/fruits') AS string_array

--output
"string_array"
"[""apples"",""oranges"",""grapes""]"

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array

--output
"string_array"
"[""apples"",""oranges"",""grapes""]"

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a": "foo"}', '$') AS result;

--expected
SELECT JSon_Extract('{"a": "foo"}', '$') AS result

--output
"result"
"{""a"":""foo""}"

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c":2}], "d": "baz"}', '$.a') AS result;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c":2}], "d": "baz"}', '$.a') AS result

--output
"result"
"[{""b"":""foo"",""c"":1},{""b"":""bar"",""c"":2}]"

--result
"result"
"[]"


--provided
SELECT JSON_KEYS(JSON '{"a": {"b":1}}') AS json_keys

--expected
SELECT JSON_KEYS(JSON '{"a": {"b":1}}') AS json_keys

--output
"json_keys"
"[a]"

--result
"json_keys"
"[a, a.b]"


--provided
SELECT JSON_KEYS(JSON '{"a": {"b":1}}', 1) AS json_keys

--expected
SELECT JSON_KEYS(JSON '{"a": {"b":1}}', 1) AS json_keys

--output
"json_keys"
"JSQL_NULL"

--result
"json_keys"
"[a]"


--provided
SELECT JSON_KEYS(
               JSON '{"a":[{"b":1}, {"c":2}], "d":3}',
               mode => "lax") as json_keys

--expected
SELECT JSON_KEYS(JSON '{"a":[{"b":1}, {"c":2}], "d":3}', mode => 'lax') AS json_keys

--output
"json_keys"
"JSQL_NULL"

--result
"json_keys"
"[a, a.b, a.c, d]"


--provided
SELECT JSON_KEYS(JSON '{"a":[[{"b":1}]]}', mode => "lax") as json_keys

--expected
SELECT JSON_KEYS(JSON '{"a":[[{"b":1}]]}', mode => 'lax') AS json_keys

--output
"json_keys"
"JSQL_NULL"

--result
"json_keys"
"[a]"


--provided
SELECT JSON_KEYS(JSON '{"a":[[{"b":1}]]}', mode => "lax recursive") as json_keys

--expected
SELECT JSON_KEYS(JSON '{"a":[[{"b":1}]]}', mode => 'lax recursive') AS json_keys

--output
"json_keys"
"JSQL_NULL"

--result
"json_keys"
"[a, a.b]"


--provided
SELECT JSON_KEYS(JSON '{"a":[{"b":[{"c":1}]}]}', mode => "lax") as json_keys

--expected
SELECT JSON_KEYS(JSON '{"a":[{"b":[{"c":1}]}]}', mode => 'lax') AS json_keys

--output
"json_keys"
"JSQL_NULL"

--result
"json_keys"
"[a, a.b, a.b.c]"


--provided
SELECT JSON_KEYS(JSON '{"a":[{"b":[[{"c":1}]]}]}', mode => "lax") as json_keys

--expected
SELECT JSON_KEYS(JSON '{"a":[{"b":[[{"c":1}]]}]}', mode => 'lax') AS json_keys

--output
"json_keys"
"JSQL_NULL"

--result
"json_keys"
"[a, a.b]"


--provided
SELECT JSON_KEYS(
               JSON '{"a":[{"b":[[{"c":1}]]}]}', mode => "lax recursive") as json_keys

--expected
SELECT JSON_KEYS(JSON '{"a":[{"b":[[{"c":1}]]}]}', mode => 'lax recursive') AS json_keys

--output
"json_keys"
"JSQL_NULL"

--result
"json_keys"
"[a, a.b, a.b.c]"


--provided
SELECT JSON_QUERY_ARRAY('{"a": "foo"}', '$.a') AS result;

--expected
SELECT JSon_Extract('{"a": "foo"}', '$.a[*]') AS result

--output
"result"
"""foo"""

--result
"result"
"[]"


--provided
SELECT JSON_QUERY('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_query,
       JSON_VALUE('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_value;

--expected
SELECT JSon_Extract('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_query, JSon_Extract_String('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_value

--output
"json_query","json_value"
"[""apple"",""banana""]","[""apple"",""banana""]"

--result
"json_query","json_value"
"[""apple"",""banana""]","JSQL_NULL"



--provided
SELECT JSON_QUERY(JSON '{"key": 1, "key": 2}', "$") AS json;

--expected
SELECT JSon_Extract(JSON '{"key": 1, "key": 2}', '$') AS json

--output
"json"
"{""key"":1,""key"":2}"

--result
"json"
"{""key"":1}"


--provided
WITH t AS (
    SELECT '{"name": null}' AS json_string, JSON '{"name": null}' AS json)
SELECT JSON_QUERY(json_string, "$.name") AS name_string,
       JSON_QUERY(json_string, "$.name") IS NULL AS name_string_is_null,
       JSON_QUERY(json, "$.name") AS name_json,
       JSON_QUERY(json, "$.name") IS NULL AS name_json_is_null
FROM t;

--expected
WITH t AS (SELECT '{"name": null}' AS json_string, JSON '{"name": null}' AS json) SELECT JSon_Extract(json_string, '$.name') AS name_string, JSon_Extract(json_string, '$.name') IS NULL AS name_string_is_null, JSon_Extract(json, '$.name') AS name_json, JSon_Extract(json, '$.name') IS NULL AS name_json_is_null FROM t

--output
"name_string","name_string_is_null","name_json","name_json_is_null"
"null","false","null","false"

--result
"name_string","name_string_is_null","name_json","name_json_is_null"
"JSQL_NULL","true","null","false"