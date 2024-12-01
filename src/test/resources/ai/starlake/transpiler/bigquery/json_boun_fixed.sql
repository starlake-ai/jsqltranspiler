-- provided
SELECT SAFE.BOOL(JSON '123') AS result;

-- expected
SELECT TRY_CAST(JSON '123' AS BOOLEAN)AS RESULT;

-- output
"result"
"JSQL_NULL"

-- result
"result"
"true"


-- provided
SELECT SAFE.FLOAT64(JSON '"strawberry"') AS result;

--expected
SELECT TRY_CAST(JSON '"strawberry"' AS DOUBLE) AS result;

-- result
"result"
"JSQL_NULL"


-- provided
SELECT SAFE.INT64(JSON '"strawberry"') AS result;

-- expected
SELECT TRY_CAST(JSON '"strawberry"' AS HugeInt) AS result;

--result
"result"
"JSQL_NULL"


--provided
SELECT JSON_ARRAY(STRUCT(10 AS a, 'foo' AS b)) AS json_data;

--expected
SELECT JSON_ARRAY({A:10,B:'foo'})AS JSON_DATA;

--result
"JSON_DATA"
"[{""A"":10,""B"":""foo""}]"


--provided
SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "Jane"}]}}',
               "$.class['students']") AS student_names;

--expected
SELECT JSON_EXTRACT('{"class":{"students":[{"name":"Jane"}]}}','$.class."students"')AS STUDENT_NAMES;

--result
"student_names"
"[{""name"":""Jane""}]"

--provided
SELECT JSON_EXTRACT(
               '{"class": {"students": []}}',
               "$.class['students']") AS student_names;

--expected
SELECT JSON_EXTRACT('{"class":{"students":[]}}','$.class."students"')AS STUDENT_NAMES;

--result
"student_names"
"[]"

--provided
SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
               "$.class['students']") AS student_names;

--expected
SELECT JSON_EXTRACT('{"class":{"students":[{"name":"John"},{"name":"Jamie"}]}}','$.class."students"')AS STUDENT_NAMES

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
  FROM UNNEST(JSON_EXTRACT_ARRAY('["apples","grapes", "oranges"]','$')) AS string_element
) AS string_array;

--expected
SELECT List_Sort( Array( SELECT CASE
                                WHEN Json_Type( Json_Extract( string_element, '$' ) ) IN (  'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                            , 'UBIGINT', 'BIGINT' )
                                    THEN Json_Extract_String( string_element, '$' )
                                ELSE Json_Value( string_element, '$' )
                                END
        FROM (  SELECT Unnest( Json_Extract( '["apples","grapes","oranges"]', '$[*]' ) ) AS string_element  ) AS string_element ) ) AS string_array
;

--result
"string_array"
"[apples, grapes, oranges]"


--provided
SELECT JSON_EXTRACT_ARRAY('{"a.b": {"c": ["world"]}}', "$['a.b'].c") AS hello;

--expected
SELECT JSON_EXTRACT('{"a.b":{"c":["world"]}}','$."a.b".c[*]')AS HELLO;

--result
"hello"
"[""world""]"


--provided
SELECT JSON_EXTRACT_SCALAR('{"a.b": {"c": "world"}}', "$['a.b'].c") AS hello;

--expected
SELECT CASE
        WHEN Json_Type( Json_Extract( '{"a.b":{"c":"world"}}', '$."a.b".c' ) ) IN ( 'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                    , 'UBIGINT', 'BIGINT' )
            THEN Json_Extract_String( '{"a.b":{"c":"world"}}', '$."a.b".c' )
        ELSE Json_Value( '{"a.b":{"c":"world"}}', '$."a.b".c' )
        END AS hello
;

--result
"hello"
"world"


--provided
SELECT JSON_EXTRACT_ARRAY('["apples", "oranges"]') AS json_array,
       JSON_EXTRACT_STRING_ARRAY('["apples", "oranges"]') AS string_array;

--expected
SELECT JSON_EXTRACT('["apples","oranges"]','$[*]')AS JSON_ARRAY,JSON_EXTRACT_STRING('["apples","oranges"]','$[*]')AS STRING_ARRAY;

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
SELECT LIST_SORT(ARRAY(SELECT CAST(INTEGER_ELEMENT AS INT64)FROM(SELECT UNNEST(JSON_EXTRACT_STRING('[1,2,3]','$[*]'))AS INTEGER_ELEMENT)AS INTEGER_ELEMENT))AS INTEGER_ARRAY;

--output
INVALID_TRANSLATION java.sql.SQLException: Binder Error: UNNEST() for correlated expressions is not supported yet

--result
"integer_array"
"[1, 2, 3]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a.b": {"c": ["world"]}}', "$['a.b'].c") AS hello;

--expected
SELECT JSON_EXTRACT_STRING('{"a.b":{"c":["world"]}}','$."a.b".c[*]')AS HELLO;

--result
"hello"
"[world]"


--provided
WITH Items AS (SELECT 'hello' AS key, 'world' AS value)
SELECT JSON_OBJECT(key, value) AS json_data FROM Items

--expected
WITH ITEMS AS(SELECT 'hello' AS KEY,'world' AS VALUE)SELECT JSON_OBJECT(KEY,VALUE)AS JSON_DATA FROM ITEMS;

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
SELECT LIST_SORT(ARRAY(SELECT CAST(INTEGER_ELEMENT AS INT64)FROM(SELECT UNNEST(JSON_EXTRACT('[1,2,3]','$[*]'))AS INTEGER_ELEMENT)AS INTEGER_ELEMENT))AS INTEGER_ARRAY;

--result
"integer_array"
"[1, 2, 3]"


--provided
SELECT ARRAY(
           SELECT JSON_VALUE(string_element, '$')
  FROM UNNEST(JSON_QUERY_ARRAY('["apples", "grapes", "oranges"]', '$')) AS string_element
) AS string_array;

--expected
SELECT List_Sort( Array( SELECT CASE
                                WHEN Json_Type( Json_Extract( string_element, '$' ) ) IN (  'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                            , 'UBIGINT', 'BIGINT' )
                                    THEN Json_Extract_String( string_element, '$' )
                                ELSE Json_Value( string_element, '$' )
                                END
        FROM (  SELECT Unnest( Json_Extract( '["apples","grapes","oranges"]', '$[*]' ) ) AS string_element  ) AS string_element ) ) AS string_array
;

--result
"string_array"
"[apples, grapes, oranges]"


--provided
SELECT JSON_VALUE_ARRAY(
               JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits'
       ) AS string_array;

--expected
SELECT IF(JSON_VALID(JSON '{"fruits":["apples","oranges","grapes"]}'),JSON_EXTRACT_STRING(JSON '{"fruits":["apples","oranges","grapes"]}','$.fruits[*]'),[])AS STRING_ARRAY;

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_QUERY_ARRAY('["apples", "oranges"]') AS json_array,
       JSON_VALUE_ARRAY('["apples", "oranges"]') AS string_array;

--expected
SELECT JSON_EXTRACT('["apples","oranges"]','$[*]')AS JSON_ARRAY,IF(JSON_VALID('["apples","oranges"]'),JSON_EXTRACT_STRING('["apples","oranges"]','$[*]'),[])AS STRING_ARRAY;

--result
"json_array","string_array"
"[""apples"", ""oranges""]","[apples, oranges]"


--provided
SELECT JSON_VALUE_ARRAY('["foo", "bar", "baz"]', '$') AS string_array;

--expected
SELECT IF(JSON_VALID('["foo","bar","baz"]'),JSON_EXTRACT_STRING('["foo","bar","baz"]','$[*]'),[])AS STRING_ARRAY;

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
SELECT LIST_SORT(ARRAY(SELECT CAST(INTEGER_ELEMENT AS INT64)FROM(SELECT UNNEST(IF(JSON_VALID('[1,2,3]'),JSON_EXTRACT_STRING('[1,2,3]','$[*]'),[]))AS INTEGER_ELEMENT)AS INTEGER_ELEMENT))AS INTEGER_ARRAY;

--result
"integer_array"
"[1, 2, 3]"


--provided
SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

--expected
SELECT IF(JSON_VALID('{"fruits":["apples","oranges","grapes"]}'),JSON_EXTRACT_STRING('{"fruits":["apples","oranges","grapes"]}','$.fruits[*]'),[])AS STRING_ARRAY;

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$."fruits"') AS string_array;

--expected
SELECT IF(JSON_VALID('{"fruits":["apples","oranges","grapes"]}'),JSON_EXTRACT_STRING('{"fruits":["apples","oranges","grapes"]}','$."fruits"[*]'),[])AS STRING_ARRAY;

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_VALUE_ARRAY('{"a.b": {"c": ["world"]}}', '$."a.b".c') AS hello;

--expected
SELECT IF(JSON_VALID('{"a.b":{"c":["world"]}}'),JSON_EXTRACT_STRING('{"a.b":{"c":["world"]}}','$."a.b".c[*]'),[])AS HELLO;

--result
"hello"
"[world]"


--provided
SELECT JSON_VALUE_ARRAY('}}', '$') AS result;

--expected
SELECT if(json_valid('}}'), JSON_EXTRACT_STRING('}}', '$[*]'), [])AS result;

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY(NULL, '$') AS result;

--expected
SELECT if(json_valid(NULL), JSON_EXTRACT_STRING(NULL, '$[*]'), []) AS result;

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": ["foo", "bar", "baz"]}', '$.b') AS result;

--expected
SELECT if(json_valid('{"a": ["foo", "bar", "baz"]}'), JSON_EXTRACT_STRING('{"a": ["foo", "bar", "baz"]}', '$.b[*]'), []) AS result;

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": "foo"}', '$') AS result;

--expected
SELECT if(json_valid('{"a": "foo"}'), JSON_EXTRACT_STRING('{"a": "foo"}', '$[*]'), []) AS result;

--result
"result"
"[]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c": 2}], "d": "baz"}', '$.a') AS result;

--expected
SELECT IF(JSON_VALID('{"a":[{"b":"foo","c":1},{"b":"bar","c":2}],"d":"baz"}'),JSON_EXTRACT_STRING('{"a":[{"b":"foo","c":1},{"b":"bar","c":2}],"d":"baz"}','$.a[*]'),[])AS RESULT;

--result
"RESULT"
"[{""b"":""foo"",""c"":1}, {""b"":""bar"",""c"":2}]"


--provided
SELECT JSON_VALUE_ARRAY('{"a": [10, {"b": 20}]', '$.a') AS result;

--expected
SELECT IF(JSON_VALID('{"a":[10,{"b":20}]'),JSON_EXTRACT_STRING('{"a":[10,{"b":20}]','$.a[*]'),[])AS RESULT;

--result
"result"
"[]"

--provided
SELECT JSON_VALUE_ARRAY('{"a": "foo", "b": []}', '$.b') AS result;

--expected
SELECT IF(JSON_VALID('{"a":"foo","b":[]}'),JSON_EXTRACT_STRING('{"a":"foo","b":[]}','$.b[*]'),[])AS RESULT;

--result
"result"
"[]"


--provided
SELECT LAX_BOOL(JSON '"true "') AS result;

--expected
SELECT CASE Typeof( JSON '"true "' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"true "', '$') ) AS BOOLEAN )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"true "' ) AS BOOLEAN )
        ELSE  Try_Cast( JSON '"true "' AS BOOLEAN )
        END AS result
;

--result
"result"
"true"


--provided
SELECT LAX_BOOL(JSON '"foo"') AS result;

--expected
SELECT Cast(JSON '"foo"' AS Boolean) AS result;

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "foo"


--provided
SELECT TO_JSON(9007199254740993, stringify_wide_numbers=>TRUE) as stringify_on;

--expected
SELECT TO_JSON(9007199254740993)AS STRINGIFY_ON;

--result
"stringify_on"
"9007199254740993"


--provided
SELECT TO_JSON(9007199254740993, stringify_wide_numbers=>FALSE) as stringify_off;

--expected
SELECT TO_JSON(9007199254740993)AS stringify_off;

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
WITH T1 AS((SELECT 9007199254740993 AS ID)UNION ALL(SELECT 2 AS ID))SELECT TO_JSON(T)AS JSON_OBJECTS FROM T1 AS T;

--result
"json_objects"
"{""id"":9007199254740993}"
"{""id"":2}"


--provided
With T1 AS (
    (SELECT 9007199254740993 AS id) UNION ALL
    (SELECT 2.1 AS id))
SELECT TO_JSON(t, stringify_wide_numbers=>TRUE) AS json_objects
FROM T1 AS t;

--expected
WITH T1 AS((SELECT 9007199254740993 AS ID)UNION ALL(SELECT 2.1 AS ID))SELECT TO_JSON(T)AS JSON_OBJECTS FROM T1 AS T;

--result
"json_objects"
"{""id"":9007199254740992.0}"
"{""id"":2.1}"


--provided
SELECT SAFE.STRING(JSON '123') AS result;

--expected
SELECT CASE Typeof( JSON '123' )
        WHEN 'JSON'
            THEN  Try_Cast( Json_Extract_String( JSON '123', '$' ) AS TEXT )
        WHEN 'DATE'
            THEN Strftime(  Try_Cast( JSON '123' AS DATE ), '%c%z' )
        WHEN 'TIMESTAMP'
            THEN Strftime(  Try_Cast( JSON '123' AS TIMESTAMP ), '%c%z' )
        WHEN 'TIMESTAMPTZ'
            THEN Strftime(  Try_Cast( JSON '123' AS TIMESTAMPTZ ), '%c%z' )
        ELSE  Try_Cast( JSON '123' AS TEXT )
        END AS result
;

--result
"result"
"123"


-- provided
SELECT LAX_FLOAT64(JSON '"+1.5"') AS result;

-- expected
SELECT CASE Typeof( JSON '"+1.5"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"+1.5"', '$') ) AS FLOAT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"+1.5"' ) AS FLOAT )
        ELSE  Try_Cast( JSON '"+1.5"' AS FLOAT )
        END AS result
;

-- result
"result"
"1.5"


-- provided
SELECT LAX_FLOAT64(JSON '"NaN"') AS result;

-- expected
SELECT CASE Typeof( JSON '"NaN"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"NaN"', '$') ) AS FLOAT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"NaN"' ) AS FLOAT )
        ELSE  Try_Cast( JSON '"NaN"' AS FLOAT )
        END AS result
;

-- result
"RESULT"
"NaN"



--provided
SELECT LAX_FLOAT64(JSON '"Inf"') AS result;

--expected
SELECT CASE Typeof( JSON '"Inf"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"Inf"', '$') ) AS FLOAT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"Inf"' ) AS FLOAT )
        ELSE  Try_Cast( JSON '"Inf"' AS FLOAT )
        END AS result
;

-- result
"result"
"∞"



--provided
SELECT LAX_FLOAT64(JSON '"-InfiNiTY"') AS result;

--expected
SELECT CASE Typeof( JSON '"-InfiNiTY"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"-InfiNiTY"', '$') ) AS FLOAT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"-InfiNiTY"' ) AS FLOAT )
        ELSE  Try_Cast( JSON '"-InfiNiTY"' AS FLOAT )
        END AS result
;

-- result
"result"
"-∞"


--provided
SELECT LAX_FLOAT64(JSON '"foo"') AS result;

--expected
SELECT CASE Typeof( JSON '"foo"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"foo"', '$') ) AS FLOAT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"foo"' ) AS FLOAT )
        ELSE  Try_Cast( JSON '"foo"' AS FLOAT )
        END AS result
;

--result
"result"
"JSQL_NULL"


--provided
SELECT LAX_INT64(JSON '1e100') AS result;

--expected
SELECT CASE Typeof( JSON '1e100' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '1e100', '$') ) AS HUGEINT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '1e100' ) AS HUGEINT )
        ELSE  Try_Cast( JSON '1e100' AS HUGEINT )
        END AS result
;

--result
"result"
"JSQL_NULL"


--provided
SELECT LAX_INT64(JSON '"1.1"') AS result;

--expected
SELECT CASE Typeof( JSON '"1.1"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"1.1"', '$') ) AS HUGEINT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"1.1"' ) AS HUGEINT )
        ELSE  Try_Cast( JSON '"1.1"' AS HUGEINT )
        END AS result
;

--result
"result"
"1"


--provided
SELECT LAX_INT64(JSON '"1.1e2"') AS result;

--expected
SELECT CASE Typeof( JSON '"1.1e2"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"1.1e2"', '$') ) AS HUGEINT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"1.1e2"' ) AS HUGEINT )
        ELSE  Try_Cast( JSON '"1.1e2"' AS HUGEINT )
        END AS result
;

--result
"result"
"110"


--provided
SELECT LAX_INT64(JSON '"+1.5"') AS result;

--expected
SELECT CASE Typeof( JSON '"+1.5"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"+1.5"', '$') ) AS HUGEINT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"+1.5"' ) AS HUGEINT )
        ELSE  Try_Cast( JSON '"+1.5"' AS HUGEINT )
        END AS result
;

--result
"result"
"2"


--provided
SELECT LAX_INT64(JSON '"1e100"') AS result;

--expected
SELECT CASE Typeof( JSON '"1e100"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"1e100"', '$') ) AS HUGEINT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"1e100"' ) AS HUGEINT )
        ELSE  Try_Cast( JSON '"1e100"' AS HUGEINT )
        END AS result
;

--result
"result"
"JSQL_NULL"


--provided
SELECT LAX_INT64(JSON '"foo"') AS result;

--expected
SELECT CASE Typeof( JSON '"foo"' )
        WHEN 'JSON'
            THEN  Try_Cast( Trim( JSON_EXTRACT_STRING(JSON '"foo"', '$') ) AS HUGEINT )
        WHEN 'TEXT'
            THEN  Try_Cast( Trim( JSON '"foo"' ) AS HUGEINT )
        ELSE  Try_Cast( JSON '"foo"' AS HUGEINT )
        END AS result
;

--result
"result"
"JSQL_NULL"


--provided
SELECT JSON_EXTRACT_ARRAY('{"a": "foo"}', '$.a') AS result;

--expected
SELECT JSon_Extract('{"a": "foo"}', '$.a[*]') AS result

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract,
       JSON_EXTRACT_SCALAR('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract_scalar;

--expected
SELECT  Json_Extract( '{"fruits":["apple","banana"]}', '$.fruits' ) AS json_extract
        , CASE
            WHEN Json_Type( Json_Extract( '{"fruits":["apple","banana"]}', '$.fruits' ) ) IN (  'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                                , 'UBIGINT', 'BIGINT' )
                THEN Json_Extract_String( '{"fruits":["apple","banana"]}', '$.fruits' )
            ELSE Json_VALUE( '{"fruits":["apple","banana"]}', '$.fruits' )
            END AS json_extract_scalar
;

--result
"json_extract","json_extract_scalar"
"[""apple"",""banana""]","JSQL_NULL"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY(
               JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits'
       ) AS string_array;

--expected
SELECT JSON_EXTRACT_STRING(JSON '{"fruits":["apples","oranges","grapes"]}','$.fruits[*]')AS STRING_ARRAY

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('["foo", "bar", "baz"]', '$') AS string_array;

--expected
SELECT JSON_EXTRACT_STRING('["foo","bar","baz"]','$[*]')AS STRING_ARRAY;

--result
"string_array"
"[foo, bar, baz]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$[fruits]') AS string_array;

--expected
SELECT JSON_EXTRACT_STRING('{"fruits":["apples","oranges","grapes"]}','$.fruits[*]')AS STRING_ARRAY;

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

--expected
SELECT JSON_EXTRACT_STRING('{"fruits":["apples","oranges","grapes"]}','$.fruits[*]')AS STRING_ARRAY;

--result
"string_array"
"[apples, oranges, grapes]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a": "foo"}', '$') AS result;

--expected
SELECT JSON_EXTRACT_STRING('{"a":"foo"}','$[*]')AS RESULT;

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c":2}], "d": "baz"}', '$.a') AS result;

--expected
SELECT JSON_EXTRACT_STRING('{"a":[{"b":"foo","c":1},{"b":"bar","c":2}],"d":"baz"}','$.a[*]')AS RESULT;

--result
"result"
"[{""b"":""foo"",""c"":1}, {""b"":""bar"",""c"":2}]"


--provided
SELECT JSON_QUERY_ARRAY('{"a": "foo"}', '$.a') AS result;

--expected
SELECT JSon_Extract('{"a": "foo"}', '$.a[*]') AS result

--result
"result"
"[]"


--provided
SELECT JSON_QUERY('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_query,
       JSON_VALUE('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_value;

--expected
SELECT  Json_Extract( '{"fruits":["apple","banana"]}', '$.fruits' ) AS json_query
        , CASE
            WHEN Json_Type( Json_Extract( '{"fruits":["apple","banana"]}', '$.fruits' ) ) IN (  'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                                , 'UBIGINT', 'BIGINT' )
                THEN Json_Extract_String( '{"fruits":["apple","banana"]}', '$.fruits' )
            ELSE Json_Value( '{"fruits":["apple","banana"]}', '$.fruits' )
            END AS json_value
;

--result
"json_query","json_value"
"[""apple"",""banana""]","JSQL_NULL"


--provided
SELECT JSON_QUERY(JSON '{"key": 1, "key": 2}', "$") AS json;

--expected
SELECT JSON_EXTRACT(JSON '{"key":1,"key":2}','$')AS JSON;

--result
"json"
"{""key"":1,""key"":2}"


