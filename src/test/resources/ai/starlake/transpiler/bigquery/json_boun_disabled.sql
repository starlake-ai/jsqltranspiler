/* Invalid Json */

--provided
SELECT JSON_EXTRACT_STRING_ARRAY('}}', '$') AS result;

--expected
SELECT JSON_EXTRACT_STRING('}}','$[*]')AS RESULT;

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a": [10, {"b": 20}]', '$.a') AS result;

--expected
SELECT JSON_EXTRACT_STRING('{"a":[10,{"b":20}]','$.a[*]')AS RESULT;

--result
"result"
"[]"

--provided
SELECT JSON_VALUE('{"hello": "world"', "$.hello") AS hello;

--expected
SELECT JSon_Extract_String('{"hello": "world"', '$.hello') AS hello

--result
"hello"
"world"


-- ---------------------------------------------------------------------------------------------------------------------
-- JSON NULL vs. NULL

--provided
SELECT JSON_QUERY('{"a": null}', "$.a") as sql_null;

--expected
SELECT JSON_EXTRACT('{"a":null}','$.a[*]')AS SQL_NULL;

--output
"sql_null"
"null"

--result
"sql_null"
"JSQL_NULL"


--provided
SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "John"}, {"name": null}]}}',
            '$.class.students[1].name') AS second_student;

--expected
SELECT JSON_EXTRACT('{"class":{"students":[{"name":"John"},{"name":null}]}}','$.class.students[1].name[*]')AS SECOND_STUDENT;

--output
"second_student"
"null"

--result
"second_student"
"JSQL_NULL"


--provided
SELECT JSON_QUERY("null", "$") as sql_null;

--expected
SELECT JSON_EXTRACT('null','$[*]')AS SQL_NULL;

--output
"sql_null"
"null"

--result
"sql_null"
"JSQL_NULL"


--provided
SELECT JSON_QUERY_ARRAY('{"a": "foo"}', '$.b') AS result;

--expected
SELECT JSon_Extract('{"a": "foo"}', '$.b[*]') AS result

--output
"result"
"JSQL_NULL"

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT('{"a": null}', "$.a") as str_null;

--expected
SELECT JSON_EXTRACT('{"a": null}', '$.a') AS str_null

--output
"str_null"
"null"

--result
"str_null"
"JSQL_NULL"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY(NULL, '$') AS result;

--expected
SELECT JSON_EXTRACT_STRING(NULL,'$[*]')AS RESULT;

--output
"result"
"JSQL_NULL"

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": null}]}}',
               '$.class.students[1].name') AS second_student;

--expected
SELECT JSON_EXTRACT('{"class": {"students": [{"name": "John"}, {"name": null}]}}', '$.class.students[1].name') AS second_student

--output
"second_student"
"null"

--result
"second_student"
"JSQL_NULL"


--provided
SELECT JSON_EXTRACT("null", "$") as str_null

--expected
SELECT JSON_EXTRACT('null', '$') AS str_null

--output
"str_null"
"null"

--result
"str_null"
"JSQL_NULL"


--provided
WITH t AS (
    SELECT '{"name": null}' AS json_string, JSON '{"name": null}' AS json)
SELECT JSON_QUERY(json_string, "$.name") AS name_string,
       JSON_QUERY(json_string, "$.name") IS NULL AS name_string_is_null,
       JSON_QUERY(json, "$.name") AS name_json,
       JSON_QUERY(json, "$.name") IS NULL AS name_json_is_null
FROM t;

--expected
WITH t AS (
        SELECT  '{"name":null}' AS json_string
                , JSON '{"name":null}' AS json  )
SELECT  Json_Extract( json_string, '$.name[*]' ) AS name_string
        , Json_Extract( json_string, '$.name[*]' ) IS NULL AS name_string_is_null
        , Json_Extract( json, '$.name[*]' ) AS name_json
        , Json_Extract( json, '$.name[*]' ) IS NULL AS name_json_is_null
FROM t
;

--output
"name_string","name_string_is_null","name_json","name_json_is_null"
"null","false","null","false"

--result
"name_string","name_string_is_null","name_json","name_json_is_null"
"JSQL_NULL","true","null","false"


-- ---------------------------------------------------------------------------------------------------------------------
-- JSON_KEYS works recursively on BigQuery

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