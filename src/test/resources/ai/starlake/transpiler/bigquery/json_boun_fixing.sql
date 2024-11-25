--provided
SELECT SAFE.BOOL(JSON '123') AS result;

--expected
SELECT TRY_CAST(TRY_CAST((JSON '123') AS STRING) AS BOOLEAN) AS result;

--result
"result"
"JSQL_NULL"

--provided
SELECT SAFE.FLOAT64(JSON '"strawberry"') AS result;

--expected
SELECT TRY_CAST(TRY_CAST((JSON '"strawberry"') AS STRING) AS DOUBLE) AS result;

--result
"result"
"JSQL_NULL"

--provided
SELECT SAFE.INT64(JSON '"strawberry"') AS result;

--expected
SELECT TRY_CAST(TRY_CAST((JSON '"strawberry"') AS STRING) AS HugeInt) AS result;

--result
"result"
"JSQL_NULL"

--provided
SELECT JSON_ARRAY(STRUCT(10 AS a, 'foo' AS b)) AS json_data

--expected
SELECT JSON_ARRAY({'a': 10, 'b': 'foo'})  AS json_data

--result
"json_data"
"[{""a"":10,""b"":""foo""}]"


--provided
SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "Jane"}]}}',
               "$.class['students']") AS student_names;

--expected
UNSUPPORTEDnet.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "("
    at line 1, column 20.

Was expecting one of:

    <EOF>
    <ST_SEMICOLON>


--output
NOT TRANSPILED

--result
"student_names"
"[{""name"":""Jane""}]"

--provided
SELECT JSON_EXTRACT(
               '{"class": {"students": []}}',
               "$.class['students']") AS student_names;

--expected
UNSUPPORTEDnet.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "("
    at line 1, column 20.

Was expecting one of:

    <EOF>
    <ST_SEMICOLON>


--output
NOT TRANSPILED

--result
"student_names"
"[]"

--provided
SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
               "$.class['students']") AS student_names;

--expected
UNSUPPORTEDnet.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "("
    at line 1, column 20.

Was expecting one of:

    <EOF>
    <ST_SEMICOLON>


--output
NOT TRANSPILED

--result
"student_names"
"[{""name"":""John""},{""name"":""Jamie""}]"


--provided
SELECT JSON_EXTRACT_ARRAY('[1,2,3]') AS string_array;

--expected
SELECT JSon_Extract('[1,2,3]', '$[*]') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: No function matches the given name and argument types 'json_extract(STRING_LITERAL)'. You might need to add explicit type casts.

--result
"string_array"
"[1, 2, 3]"


--provided
SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_EXTRACT_ARRAY('[1,2,3]','$')
  ) AS integer_element
) AS integer_array;

--expected
SELECT List_Sort(Array(SELECT CAST(integer_element AS INT64) FROM (SELECT UNNEST(JSon_Extract('[1,2,3]', '$[*]')) AS integer_element) AS integer_element)) AS integer_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: UNNEST() for correlated expressions is not supported yet

--result
"integer_array"
"[1, 2, 3]"


--provided
SELECT ARRAY(
           SELECT JSON_EXTRACT_SCALAR(string_element, '$')
  FROM UNNEST(JSON_EXTRACT_ARRAY('["apples","oranges","grapes"]','$')) AS string_element
) AS string_array;

--expected
SELECT List_Sort(Array(SELECT JSon_Extract_String(string_element, '$') FROM (SELECT UNNEST(JSon_Extract('["apples","oranges","grapes"]', '$[*]')) AS string_element) AS string_element)) AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: UNNEST() for correlated expressions is not supported yet

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_EXTRACT_ARRAY('{"a.b": {"c": ["world"]}}', "$['a.b'].c") AS hello;

--expected
UNSUPPORTEDnet.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "("
    at line 1, column 26.

Was expecting one of:

    <EOF>
    <ST_SEMICOLON>


--output
NOT TRANSPILED

--result
"hello"
"[""world""]"


--provided
SELECT JSON_EXTRACT_SCALAR('{"a.b": {"c": "world"}}', "$['a.b'].c") AS hello;

--expected
UNSUPPORTEDnet.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "("
    at line 1, column 27.

Was expecting one of:

    <EOF>
    <ST_SEMICOLON>


--output
NOT TRANSPILED

--result
"hello"
"world"


--provided
SELECT JSON_EXTRACT_ARRAY('["apples", "oranges"]') AS json_array,
       JSON_EXTRACT_STRING_ARRAY('["apples", "oranges"]') AS string_array;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract('["apples", "oranges"]') AS json_array, JSon_Extract('["apples", "oranges"]') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: No function matches the given name and argument types 'json_extract(STRING_LITERAL)'. You might need to add explicit type casts.

--result
"json_array","string_array"
"[""apples"", ""oranges""]","[apples, oranges]"


--provided
SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_EXTRACT_STRING_ARRAY('[1, 2, 3]', '$')
  ) AS integer_element
) AS integer_array;

--expected
--TODO: Regenerate once implemented
SELECT List_Sort(Array(SELECT CAST(integer_element AS INT64) FROM (SELECT UNNEST(JSon_Extract('[1, 2, 3]', '$')) AS integer_element) AS integer_element)) AS integer_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: UNNEST() for correlated expressions is not supported yet

--result
"integer_array"
"[1, 2, 3]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a.b": {"c": ["world"]}}', "$['a.b'].c") AS hello;

--expected
--TODO: Regenerate once implemented
UNSUPPORTEDnet.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "("
    at line 1, column 33.

Was expecting one of:

    <EOF>
    <ST_SEMICOLON>


--output
NOT TRANSPILED

--result
"hello"
"[world]"


--provided
WITH Items AS (SELECT 'hello' AS key, 'world' AS value)
SELECT JSON_OBJECT(key, value) AS json_data FROM Items

--expected
UNSUPPORTEDnet.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "," ","
    at line 2, column 23.

Was expecting one of:

    <S_CHAR_LITERAL>


--output
NOT TRANSPILED

--result
"json_data"
"{""hello"":""world""}"


--provided
SELECT JSON_QUERY_ARRAY('[1, 2, 3]') AS string_array;

--expected
SELECT JSon_Extract('[1, 2, 3]', '$[*]') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: No function matches the given name and argument types 'json_extract(STRING_LITERAL)'. You might need to add explicit type casts.

--result
"string_array"
"[1, 2, 3]"


--provided
SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_QUERY_ARRAY('[1, 2, 3]','$')
  ) AS integer_element
) AS integer_array;

--expected
SELECT List_Sort(Array(SELECT CAST(integer_element AS INT64) FROM (SELECT UNNEST(JSon_Extract('[1, 2, 3]', '$')) AS integer_element) AS integer_element)) AS integer_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: UNNEST() for correlated expressions is not supported yet

--result
"integer_array"
"[1, 2, 3]"


--provided
SELECT ARRAY(
           SELECT JSON_VALUE(string_element, '$')
  FROM UNNEST(JSON_QUERY_ARRAY('["apples", "oranges", "grapes"]', '$')) AS string_element
) AS string_array;

--expected
SELECT List_Sort(Array(SELECT JSon_Extract_String(string_element, '$') FROM (SELECT UNNEST(JSon_Extract('["apples", "oranges", "grapes"]', '$[*]')) AS string_element) AS string_element)) AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: UNNEST() for correlated expressions is not supported yet

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_VALUE_ARRAY(
               JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits'
       ) AS string_array;

--expected
SELECT JSON_VALUE_ARRAY(JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_QUERY_ARRAY('["apples", "oranges"]') AS json_array,
       JSON_VALUE_ARRAY('["apples", "oranges"]') AS string_array;

--expected
SELECT JSon_Extract('["apples", "oranges"]') AS json_array, JSON_VALUE_ARRAY('["apples", "oranges"]') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: No function matches the given name and argument types 'json_extract(STRING_LITERAL)'. You might need to add explicit type casts.

--result
"json_array","string_array"
"[""apples"", ""oranges""]","[apples, oranges]"


--provided
SELECT JSON_VALUE_ARRAY('["foo", "bar", "baz"]', '$') AS string_array;

--expected
SELECT JSON_VALUE_ARRAY('["foo", "bar", "baz"]', '$') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"string_array"
"[foo, bar, baz]"


--provided
SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_VALUE_ARRAY('[1, 2, 3]', '$')
  ) AS integer_element
) AS integer_array;

--expected
SELECT List_Sort(Array(SELECT CAST(integer_element AS INT64) FROM (SELECT UNNEST(JSON_VALUE_ARRAY('[1, 2, 3]', '$')) AS integer_element) AS integer_element)) AS integer_array

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"integer_array"
"[1, 2, 3]"


--provided
SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

--expected
SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$."fruits"') AS string_array;

--expected
SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$."fruits"') AS string_array

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_VALUE_ARRAY('{"a.b": {"c": ["world"]}}', '$."a.b".c') AS hello;

--expected
SELECT JSON_VALUE_ARRAY('{"a.b": {"c": ["world"]}}', '$."a.b".c') AS hello

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"hello"
"[world]"


--provided
SELECT JSON_VALUE_ARRAY('}}', '$') AS result;

--expected
SELECT JSON_VALUE_ARRAY('}}', '$') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY(NULL, '$') AS result;

--expected
SELECT JSON_VALUE_ARRAY(NULL, '$') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": ["foo", "bar", "baz"]}', '$.b') AS result;

--expected
SELECT JSON_VALUE_ARRAY('{"a": ["foo", "bar", "baz"]}', '$.b') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": "foo"}', '$') AS result;

--expected
SELECT JSON_VALUE_ARRAY('{"a": "foo"}', '$') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c": 2}], "d": "baz"}', '$.a') AS result;

--expected
SELECT JSON_VALUE_ARRAY('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c": 2}], "d": "baz"}', '$.a') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": [10, {"b": 20}]', '$.a') AS result;

--expected
SELECT JSON_VALUE_ARRAY('{"a": [10, {"b": 20}]', '$.a') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"result"
"[]"

--provided
SELECT JSON_VALUE_ARRAY('{"a": "foo", "b": []}', '$.b') AS result;

--expected
SELECT JSON_VALUE_ARRAY('{"a": "foo", "b": []}', '$.b') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name json_value_array does not exist!

--result
"result"
"[]"


--provided
SELECT LAX_BOOL(JSON '"true "') AS result;

--expected
SELECT Cast(JSON '"true "' AS Boolean) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "true "

--result
"result"
"JSQL_NULL"


--provided
SELECT LAX_BOOL(JSON '"foo"') AS result;

--expected
SELECT Cast(JSON '"foo"' AS Boolean) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "foo"


--provided
SELECT TO_JSON(9007199254740993, stringify_wide_numbers=>TRUE) as stringify_on;

--expected
SELECT TO_JSON(9007199254740993, stringify_wide_numbers => true) AS stringify_on

--output
INVALID_TRANSLATION java.sql.SQLException: Invalid Input Error: to_json() takes exactly one argument

--result
"stringify_on"
"""9007199254740993"""


--provided
SELECT TO_JSON(9007199254740993, stringify_wide_numbers=>FALSE) as stringify_off;

--expected
SELECT TO_JSON(9007199254740993, stringify_wide_numbers => false) AS stringify_off

--output
INVALID_TRANSLATION java.sql.SQLException: Invalid Input Error: to_json() takes exactly one argument

--result
"stringify_off"
"9007199254740993"


--provided
With T1 AS (
    (SELECT 9007199254740993 AS id) UNION ALL
    (SELECT 2 AS id))
SELECT TO_JSON(t, stringify_wide_numbers=>TRUE) AS json_objects
FROM T1 AS t;

--expected
WITH T1 AS ((SELECT 9007199254740993 AS id) UNION ALL (SELECT 2 AS id)) SELECT TO_JSON(t, stringify_wide_numbers => true) AS json_objects FROM T1 AS t

--output
INVALID_TRANSLATION java.sql.SQLException: Invalid Input Error: to_json() takes exactly one argument

--result
"json_objects"
"{""id"":""9007199254740993""}"
"{""id"":2}"


--provided
With T1 AS (
    (SELECT 9007199254740993 AS id) UNION ALL
    (SELECT 2.1 AS id))
SELECT TO_JSON(t, stringify_wide_numbers=>TRUE) AS json_objects
FROM T1 AS t;

--expected
WITH T1 AS ((SELECT 9007199254740993 AS id) UNION ALL (SELECT 2.1 AS id)) SELECT TO_JSON(t, stringify_wide_numbers => true) AS json_objects FROM T1 AS t

--output
INVALID_TRANSLATION java.sql.SQLException: Invalid Input Error: to_json() takes exactly one argument

--result
"json_objects"
"{""id"":9.007199254740992e+15}"
"{""id"":2.1}"


--provided
SELECT SAFE.STRING(JSON '123') AS result;

--expected
SELECT /* Approximation: SAFE prefix is not supported. */ STRING(JSON '123') AS result

--output
INVALID_TRANSLATION java.sql.SQLException: Catalog Error: Scalar Function with name string does not exist!

--result
"result"
"JSQL_NULL"


--provided
SELECT LAX_INT64(JSON '"1e100"') AS result;

--expected
SELECT Cast(JSON '"1e100"' AS HugeInt) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "1e100"

--result
"result"
"JSQL_NULL"


--provided
SELECT LAX_INT64(JSON '"foo"') AS result;

--expected
SELECT Cast(JSON '"foo"' AS HugeInt) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "foo"

--result
"result"
"JSQL_NULL"