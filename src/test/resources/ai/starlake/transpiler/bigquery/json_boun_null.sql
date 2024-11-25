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
SELECT JSON_EXTRACT_ARRAY('{"a": "foo"}', '$.b') AS result;

--expected
SELECT JSon_Extract('{"a": "foo"}', '$.b[*]') AS result

--output
"result"
"JSQL_NULL"

--result
"result"
"[]"




--provided
SELECT JSON_EXTRACT_STRING_ARRAY(NULL, '$') AS result;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract(NULL, '$') AS result

--output
"result"
"JSQL_NULL"

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a": ["foo", "bar", "baz"]}', '$.b') AS result;

--expected
SELECT JSon_Extract('{"a": ["foo", "bar", "baz"]}', '$.b') AS result

--output
"result"
"JSQL_NULL"

--result
"result"
"[]"


--provided
SELECT JSON_QUERY("null", "$") as sql_null

--expected
SELECT JSon_Extract('null', '$') AS sql_null

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
SELECT JSon_Extract('{"class": {"students": [{"name": "John"}, {"name": null}]}}', '$.class.students[1].name') AS second_student

--output
"second_student"
"null"

--result
"second_student"
"JSQL_NULL"


--provided
SELECT JSON_QUERY('{"a": null}', "$.a") as sql_null;

--expected
SELECT JSon_Extract('{"a": null}', '$.a') AS sql_null

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
SELECT LAX_FLOAT64(JSON 'true') AS result;

--expected
SELECT Cast(JSON 'true' AS Double) AS result

--output
"result"
"1.0"

--result
"result"
"JSQL_NULL"

--provided
SELECT LAX_FLOAT64(JSON 'false') AS result;

--expected
SELECT Cast(JSON 'false' AS Double) AS result

--output
"result"
"0.0"

--result
"result"
"JSQL_NULL"

