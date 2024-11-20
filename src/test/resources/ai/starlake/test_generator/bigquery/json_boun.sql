SELECT BOOL(JSON 'true') AS vacancy;


SELECT BOOL(JSON_QUERY(JSON '{"hotel class": "5-star", "vacancy": true}', "$.vacancy")) AS vacancy;

SELECT SAFE.BOOL(JSON '123') AS result;

SELECT FLOAT64(JSON '9.8') AS velocity;

SELECT FLOAT64(JSON_QUERY(JSON '{"vo2_max": 39.1, "age": 18}', "$.vo2_max")) AS vo2_max;

SELECT FLOAT64(JSON '18446744073709551615', wide_number_mode=>'round') as result;

SELECT FLOAT64(JSON '18446744073709551615') as result;

SELECT SAFE.FLOAT64(JSON '"strawberry"') AS result;

SELECT INT64(JSON '2005') AS flight_number;

SELECT INT64(JSON_QUERY(JSON '{"gate": "A4", "flight_number": 2005}', "$.flight_number")) AS flight_number;

SELECT INT64(JSON '10.0') AS score;

SELECT SAFE.INT64(JSON '"strawberry"') AS result;

SELECT JSON_ARRAY(10) AS json_data

SELECT JSON_ARRAY([]) AS json_data

SELECT JSON_ARRAY(10, 'foo', NULL) AS json_data

SELECT JSON_ARRAY(STRUCT(10 AS a, 'foo' AS b)) AS json_data

SELECT JSON_ARRAY(10, ['foo', 'bar'], [20, 30]) AS json_data

SELECT JSON_ARRAY(10, [JSON '20', JSON '"foo"']) AS json_data

SELECT JSON_ARRAY() AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(JSON '["a", "b", "c"]', '$', 1) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(JSON '["a", "b", "c"]', '$', [1, 2]) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(
               JSON '["a", "b", "c"]',
               '$', [1, 2],
               append_each_element=>FALSE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(
               JSON '["a", ["b"], "c"]',
               '$[1]', [1, 2],
               '$[1][1]', [3, 4],
               append_each_element=>FALSE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(
               JSON '["a", ["b"], "c"]',
               '$[1]', [1, 2],
               '$[1][1]', [3, 4]) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(JSON '{"a": [1]}', '$.a', 2) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(JSON '{"a": null}', '$.a', 10)

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(JSON '{"a": 1}', '$.a', 2) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_APPEND(JSON '{"a": 1}', '$.b', 2) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '["a", ["b", "c"], "d"]', '$[1]', 1) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '["a", ["b", "c"], "d"]', '$[1][0]', 1) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '["a", "b", "c"]', '$[1]', [1, 2]) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(
               JSON '["a", "b", "c"]',
               '$[1]', [1, 2],
               insert_each_element=>FALSE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '["a", "b", "c", "d"]', '$[7]', "e") AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '{"a": {}}', '$.a[0]', 2) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '[1, 2]', '$', 3) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '{"a": null}', '$.a[2]', 10) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_ARRAY_INSERT(JSON '1', '$[0]', 'r1') AS json_data

SELECT JSON_EXTRACT("null", "$") as str_null

SELECT JSON_EXTRACT(JSON 'null', "$") as json_null

SELECT
    JSON_EXTRACT(JSON '{"class": {"students": [{"id": 5}, {"id": 12}]}}', '$.class')
        AS json_data;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "Jane"}]}}',
               '$') AS json_text_string;

SELECT JSON_EXTRACT(
               '{"class": {"students": []}}',
               '$') AS json_text_string;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
               '$') AS json_text_string;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "Jane"}]}}',
               '$.class.students[0]') AS first_student;

SELECT JSON_EXTRACT(
               '{"class": {"students": []}}',
               '$.class.students[0]') AS first_student;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
               '$.class.students[0]') AS first_student;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "Jane"}]}}',
               '$.class.students[1].name') AS second_student;

SELECT JSON_EXTRACT(
               '{"class": {"students": []}}',
               '$.class.students[1].name') AS second_student;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": null}]}}',
               '$.class.students[1].name') AS second_student;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
               '$.class.students[1].name') AS second_student;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "Jane"}]}}',
               "$.class['students']") AS student_names;

SELECT JSON_EXTRACT(
               '{"class": {"students": []}}',
               "$.class['students']") AS student_names;

SELECT JSON_EXTRACT(
               '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
               "$.class['students']") AS student_names;

SELECT JSON_EXTRACT('{"a": null}', "$.a") as str_null;

SELECT JSON_EXTRACT('{"a": null}', "$.b") as str_null;

SELECT JSON_EXTRACT(JSON '{"a": null}', "$.a") as json_null;

SELECT JSON_EXTRACT(JSON '{"a": null}', "$.b") as sql_null;

SELECT JSON_EXTRACT_ARRAY(
               JSON '{"fruits":["apples","oranges","grapes"]}','$.fruits'
       ) AS json_array;

SELECT JSON_EXTRACT_ARRAY('[1,2,3]') AS string_array;

SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_EXTRACT_ARRAY('[1,2,3]','$')
  ) AS integer_element
) AS integer_array;

SELECT JSON_EXTRACT_ARRAY('["apples", "oranges", "grapes"]', '$') AS string_array;

SELECT ARRAY(
           SELECT JSON_EXTRACT_SCALAR(string_element, '$')
  FROM UNNEST(JSON_EXTRACT_ARRAY('["apples","oranges","grapes"]','$')) AS string_element
) AS string_array;

SELECT JSON_EXTRACT_ARRAY(
               '{"fruit": [{"apples": 5, "oranges": 10}, {"apples": 2, "oranges": 4}], "vegetables": [{"lettuce": 7, "kale": 8}]}',
               '$.fruit'
       ) AS string_array;

SELECT JSON_EXTRACT_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$[fruits]') AS string_array;

SELECT JSON_EXTRACT_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

SELECT JSON_EXTRACT_ARRAY('{"a.b": {"c": ["world"]}}', "$['a.b'].c") AS hello;

SELECT JSON_EXTRACT_ARRAY('{"a": "foo"}', '$.a') AS result;

SELECT JSON_EXTRACT_ARRAY('{"a": "foo"}', '$.b') AS result;

SELECT JSON_EXTRACT_ARRAY('{"a": "foo", "b": []}', '$.b') AS result;

SELECT JSON_EXTRACT_SCALAR(JSON '{"name": "Jakob", "age": "6" }', '$.age') AS scalar_age;

SELECT JSON_EXTRACT('{"name": "Jakob", "age": "6" }', '$.name') AS json_name,
       JSON_EXTRACT_SCALAR('{"name": "Jakob", "age": "6" }', '$.name') AS scalar_name,
       JSON_EXTRACT('{"name": "Jakob", "age": "6" }', '$.age') AS json_age,
       JSON_EXTRACT_SCALAR('{"name": "Jakob", "age": "6" }', '$.age') AS scalar_age;

SELECT JSON_EXTRACT('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract,
       JSON_EXTRACT_SCALAR('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_extract_scalar;

SELECT JSON_EXTRACT_SCALAR('{"a.b": {"c": "world"}}', "$['a.b'].c") AS hello;

SELECT JSON_EXTRACT_STRING_ARRAY(
               JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits'
       ) AS string_array;

SELECT JSON_EXTRACT_ARRAY('["apples", "oranges"]') AS json_array,
       JSON_EXTRACT_STRING_ARRAY('["apples", "oranges"]') AS string_array;

SELECT JSON_EXTRACT_STRING_ARRAY('["foo", "bar", "baz"]', '$') AS string_array;

SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_EXTRACT_STRING_ARRAY('[1, 2, 3]', '$')
  ) AS integer_element
) AS integer_array;

SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$[fruits]') AS string_array;

SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

SELECT JSON_EXTRACT_STRING_ARRAY('{"a.b": {"c": ["world"]}}', "$['a.b'].c") AS hello;

SELECT JSON_EXTRACT_STRING_ARRAY('}}', '$') AS result;

SELECT JSON_EXTRACT_STRING_ARRAY(NULL, '$') AS result;

SELECT JSON_EXTRACT_STRING_ARRAY('{"a": ["foo", "bar", "baz"]}', '$.b') AS result;

SELECT JSON_EXTRACT_STRING_ARRAY('{"a": "foo"}', '$') AS result;

SELECT JSON_EXTRACT_STRING_ARRAY('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c":2}], "d": "baz"}', '$.a') AS result;

SELECT JSON_EXTRACT_STRING_ARRAY('{"a": [10, {"b": 20}]', '$.a') AS result;

SELECT JSON_EXTRACT_STRING_ARRAY('{"a": "foo", "b": []}', '$.b') AS result;

SELECT JSON_KEYS(JSON '{"a": {"b":1}}') AS json_keys

SELECT JSON_KEYS(JSON '{"a": {"b":1}}', 1) AS json_keys

SELECT JSON_KEYS(JSON '{"a":[{"b":1}, {"c":2}], "d":3}') AS json_keys

SELECT JSON_KEYS(
               JSON '{"a":[{"b":1}, {"c":2}], "d":3}',
               mode => "lax") as json_keys

SELECT JSON_KEYS(JSON '{"a":[[{"b":1}]]}', mode => "lax") as json_keys

SELECT JSON_KEYS(JSON '{"a":[[{"b":1}]]}', mode => "lax recursive") as json_keys

SELECT JSON_KEYS(JSON '{"a":[{"b":[{"c":1}]}]}', mode => "lax") as json_keys

SELECT JSON_KEYS(JSON '{"a":[{"b":[[{"c":1}]]}]}', mode => "lax") as json_keys

SELECT JSON_KEYS(
               JSON '{"a":[{"b":[[{"c":1}]]}]}', mode => "lax recursive") as json_keys

SELECT JSON_OBJECT() AS json_data

SELECT JSON_OBJECT('foo', 10, 'bar', TRUE) AS json_data

SELECT JSON_OBJECT('foo', 10, 'bar', ['a', 'b']) AS json_data

SELECT JSON_OBJECT('a', NULL, 'b', JSON 'null') AS json_data

--unsupported: Duckdb doesn't create valid json since it can contains duplicate keys.
SELECT JSON_OBJECT('a', 10, 'a', 'foo') AS json_data

WITH Items AS (SELECT 'hello' AS key, 'world' AS value)
SELECT JSON_OBJECT(key, value) AS json_data FROM Items

--unsupported: Duckdb doesn't handle zip of two array as key value of struct
SELECT JSON_OBJECT(CAST([] AS ARRAY<STRING>), []) AS json_data

--unsupported: Duckdb doesn't handle zip of two array as key value of struct
SELECT JSON_OBJECT(['a', 'b'], [10, NULL]) AS json_data

--unsupported: Duckdb doesn't handle zip of two array as key value of struct
SELECT JSON_OBJECT(['a', 'b'], [JSON '10', JSON '"foo"']) AS json_data

--unsupported: Duckdb doesn't handle zip of two array as key value of struct
SELECT
    JSON_OBJECT(
        ['a', 'b'],
        [STRUCT(10 AS id, 'Red' AS color), STRUCT(20 AS id, 'Blue' AS color)])
        AS json_data

--unsupported: Duckdb doesn't handle zip of two array as key value of struct
SELECT
    JSON_OBJECT(
        ['a', 'b'],
        [TO_JSON(10), TO_JSON(['foo', 'bar'])])
        AS json_data

--unsupported: Duckdb doesn't handle zip of two array as key value of struct
WITH
  Fruits AS (
    SELECT 0 AS id, 'color' AS json_key, 'red' AS json_value UNION ALL
    SELECT 0, 'fruit', 'apple' UNION ALL
    SELECT 1, 'fruit', 'banana' UNION ALL
    SELECT 1, 'ripe', 'true'
  )
SELECT JSON_OBJECT(ARRAY_AGG(json_key), ARRAY_AGG(json_value)) AS json_data
FROM Fruits
GROUP BY id

SELECT JSON_QUERY("null", "$") as sql_null

SELECT JSON_QUERY(JSON 'null', "$") as json_null

SELECT
    JSON_QUERY(
            JSON '{"class": {"students": [{"id": 5}, {"id": 12}]}}',
            '$.class') AS json_data;

SELECT
    JSON_QUERY('{"class": {"students": [{"name": "Jane"}]}}', '$') AS json_text_string;

SELECT JSON_QUERY('{"class": {"students": []}}', '$') AS json_text_string;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "John"},{"name": "Jamie"}]}}',
            '$') AS json_text_string;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "Jane"}]}}',
            '$.class.students[0]') AS first_student;

SELECT
    JSON_QUERY('{"class": {"students": []}}', '$.class.students[0]') AS first_student;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
            '$.class.students[0]') AS first_student;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "Jane"}]}}',
            '$.class.students[1].name') AS second_student;

SELECT
    JSON_QUERY(
            '{"class": {"students": []}}',
            '$.class.students[1].name') AS second_student;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "John"}, {"name": null}]}}',
            '$.class.students[1].name') AS second_student;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
            '$.class.students[1].name') AS second_student;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "Jane"}]}}',
            '$.class."students"') AS student_names;

SELECT
    JSON_QUERY(
            '{"class": {"students": []}}',
            '$.class."students"') AS student_names;

SELECT
    JSON_QUERY(
            '{"class": {"students": [{"name": "John"}, {"name": "Jamie"}]}}',
            '$.class."students"') AS student_names;

SELECT
    JSON_QUERY(
            JSON '{"class": {"students": [{"name": "Jane"}]}}',
            'lax $.class.students.name') AS student_names_lax;

SELECT
    JSON_QUERY(
            JSON '[{"class": {"students": [{"name": "Joe"}, {"name": "Jamie"}]}}]',
            'lax $.class.students.name') AS student_names_lax;

SELECT
    JSON_QUERY(
            JSON '{"class": {"students": [[{"name": "John"}], {"name": "Jamie"}]}}',
            'lax $.class.students.name') AS student_names_lax;

SELECT
    JSON_QUERY(
            JSON '{"class": {"students": [{"name": "Jane"}]}}',
            'lax recursive $.class.students.name') AS student_names_lax_recursive;

SELECT
    JSON_QUERY(
            JSON '[[{"class": {"students": [{"name": "Joe"}, {"name": "Jamie"}]}}]]',
            'lax recursive $.class.students.name') AS student_names_lax_recursive;

SELECT
    JSON_QUERY(
            JSON '{"class": {"students": [[{"name": "John"}], {"name": "Jamie"}]}}',
            'lax recursive $.class.students.name') AS student_names_lax_recursive;

SELECT
    JSON_QUERY(
            JSON '{"class": {"students": {"name": "Jane"}}}',
            'lax $.class[0].students[0].name') AS student_names_lax,
    JSON_QUERY(
            JSON '{"class": {"students": {"name": "Jane"}}}',
            'lax recursive $.class[0].students[0].name') AS student_names_lax_recursive;

SELECT
    JSON_QUERY(
            JSON '[{"class": {"students": [{"name": "Joe"}, {"name": "Jamie"}]}}]',
            'lax $.class[0].students[0].name') AS student_names_lax,
    JSON_QUERY(
            JSON '[{"class": {"students": [{"name": "Joe"}, {"name": "Jamie"}]}}]',
            'lax recursive $.class[0].students[0].name') AS student_names_lax_recursive;

SELECT
    JSON_QUERY(
            JSON '{"class": {"students": [[{"name": "John"}], {"name": "Jamie"}]}}',
            'lax $.class[0].students[0].name') AS student_names_lax,
    JSON_QUERY(
            JSON '{"class": {"students": [[{"name": "John"}], {"name": "Jamie"}]}}',
            'lax recursive $.class[0].students[0].name') AS student_names_lax_recursive;

SELECT JSON_QUERY('{"a": null}', "$.a") as sql_null;

SELECT JSON_QUERY('{"a": null}', "$.b") as sql_null;

SELECT JSON_QUERY_ARRAY(
               JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits'
       ) AS json_array;

SELECT JSON_QUERY_ARRAY('[1, 2, 3]') AS string_array;

SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_QUERY_ARRAY('[1, 2, 3]','$')
  ) AS integer_element
) AS integer_array;

SELECT JSON_QUERY_ARRAY('["apples", "oranges", "grapes"]', '$') AS string_array;

SELECT ARRAY(
           SELECT JSON_VALUE(string_element, '$')
  FROM UNNEST(JSON_QUERY_ARRAY('["apples", "oranges", "grapes"]', '$')) AS string_element
) AS string_array;

SELECT JSON_QUERY_ARRAY(
               '{"fruit": [{"apples": 5, "oranges": 10}, {"apples": 2, "oranges": 4}], "vegetables": [{"lettuce": 7, "kale": 8}]}',
               '$.fruit'
       ) AS string_array;

SELECT JSON_QUERY_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

SELECT JSON_QUERY_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$."fruits"') AS string_array;

SELECT JSON_QUERY_ARRAY('{"a.b": {"c": ["world"]}}', '$."a.b".c') AS hello;

SELECT JSON_QUERY_ARRAY('{"a": "foo"}', '$.a') AS result;

SELECT JSON_QUERY_ARRAY('{"a": "foo"}', '$.b') AS result;

SELECT JSON_QUERY_ARRAY('{"a": "foo", "b": []}', '$.b') AS result;

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON '["a", ["b", "c"], "d"]', '$[1]') AS json_data

--unsupported: Duckdb doesn't have equivalent
WITH T AS (SELECT JSON '{"a": {"b": 10, "c": 20}}' AS data)
SELECT JSON_REMOVE(data.a, '$.b') AS json_data FROM T

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON '["a", ["b", "c"], "d"]', '$[1]', '$[1]') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON '["a", ["b", "c"], "d"]', '$[1]', '$[1]', '$[0]') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON '{"a": {"b": {"c": "d"}}}', '$.a.b.c') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON '{"a": {"b": {"c": "d"}}}', '$.a.b') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON '{"a": 1}', '$.b') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON '{"a": [1, 2, 3]}', '$.a[0]', '$.a.b', '$.b', '$.a[0]') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_REMOVE(JSON 'null', '$.a.b') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '{"a": 1}', '$', JSON '{"b": 2, "c": 3}') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(
               JSON '{"a": 1}',
               "$.b", 999,
               create_if_missing => false) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(
               JSON '{"a": 1}',
               "$.a", 999,
               create_if_missing => false) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '{"a": {}}', '$.a.b', 100) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON 'null', '$.a.b', 100) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(
               JSON '{"a": 1}',
               '$.b', 2,
               '$.a.c', 100,
               '$.d', 3) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(
               JSON '{"a": 1}',
               '$.a[2]', 100,
               '$.b', 2) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '["a", ["b", "c"], "d"]', '$[1]', "foo") AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '["a", ["b", "c"], "d"]', '$[1][0]', "foo") AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON 'null', '$[0][3]', "foo")

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '["a", ["b", "c"], "d"]', '$[1][4]', "foo") AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '["a", ["b", "c"], "d"]', '$[1][0][0]', "foo") AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '["a", ["b", "c"], "d"]', '$[1][2][1]', "foo") AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(JSON '{}', '$.b[2].d', 100) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_SET(
               JSON '{"a": 1, "b": {"c":3}, "d": [4]}',
               '$.a', 'v1',
               '$.b.e', 'v2',
               '$.d[2]', 'v3') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(JSON '{"a": null, "b": "c"}') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(JSON '[1, null, 2, null]') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(JSON '[1, null, 2, null]', include_arrays=>FALSE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(JSON '[1, null, 2, null, [null]]') AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(
               JSON '[1, null, 2, null, [null]]',
               remove_empty=>TRUE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(JSON '{"a": null}', remove_empty=>TRUE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(JSON '{"a": [null]}', remove_empty=>TRUE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(
               JSON '{"a": {"b": {"c": null}}, "d": [null], "e": [], "f": 1}',
               include_arrays=>FALSE,
               remove_empty=>TRUE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(
               JSON '{"a": {"b": {"c": null}}, "d": [null], "e": [], "f": 1}',
               remove_empty=>TRUE) AS json_data

--unsupported: Duckdb doesn't have equivalent
SELECT JSON_STRIP_NULLS(JSON 'null') AS json_data

SELECT json_val, JSON_TYPE(json_val) AS type
FROM
    UNNEST(
        [
      JSON '"apple"',
            JSON '10',
            JSON '3.14',
            JSON 'null',
            JSON '{"city": "New York", "State": "NY"}',
            JSON '["apple", "banana"]',
            JSON 'false'
    ]
    ) AS json_val;

SELECT JSON_VALUE(JSON '{"name": "Jakob", "age": "6" }', '$.age') AS scalar_age;

SELECT JSON_QUERY('{"name": "Jakob", "age": "6"}', '$.name') AS json_name,
       JSON_VALUE('{"name": "Jakob", "age": "6"}', '$.name') AS scalar_name,
       JSON_QUERY('{"name": "Jakob", "age": "6"}', '$.age') AS json_age,
       JSON_VALUE('{"name": "Jakob", "age": "6"}', '$.age') AS scalar_age;

SELECT JSON_QUERY('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_query,
       JSON_VALUE('{"fruits": ["apple", "banana"]}', '$.fruits') AS json_value;

SELECT JSON_VALUE('{"a.b": {"c": "world"}}', '$."a.b".c') AS hello;

SELECT JSON_VALUE_ARRAY(
               JSON '{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits'
       ) AS string_array;

SELECT JSON_QUERY_ARRAY('["apples", "oranges"]') AS json_array,
       JSON_VALUE_ARRAY('["apples", "oranges"]') AS string_array;

SELECT JSON_VALUE_ARRAY('["foo", "bar", "baz"]', '$') AS string_array;

SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_VALUE_ARRAY('[1, 2, 3]', '$')
  ) AS integer_element
) AS integer_array;

SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

SELECT JSON_VALUE_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$."fruits"') AS string_array;

SELECT JSON_VALUE_ARRAY('{"a.b": {"c": ["world"]}}', '$."a.b".c') AS hello;

SELECT JSON_VALUE_ARRAY('}}', '$') AS result;

SELECT JSON_VALUE_ARRAY(NULL, '$') AS result;

SELECT JSON_VALUE_ARRAY('{"a": ["foo", "bar", "baz"]}', '$.b') AS result;

SELECT JSON_VALUE_ARRAY('{"a": "foo"}', '$') AS result;

SELECT JSON_VALUE_ARRAY('{"a": [{"b": "foo", "c": 1}, {"b": "bar", "c": 2}], "d": "baz"}', '$.a') AS result;

SELECT JSON_VALUE_ARRAY('{"a": [10, {"b": 20}]', '$.a') AS result;

SELECT JSON_VALUE_ARRAY('{"a": "foo", "b": []}', '$.b') AS result;

SELECT LAX_BOOL(JSON 'true') AS result;

SELECT LAX_BOOL(JSON '"true"') AS result;

SELECT LAX_BOOL(JSON '"true "') AS result;

SELECT LAX_BOOL(JSON '"foo"') AS result;

SELECT LAX_BOOL(JSON '10') AS result;

SELECT LAX_BOOL(JSON '0') AS result;

SELECT LAX_BOOL(JSON '0.0') AS result;

SELECT LAX_BOOL(JSON '-1.1') AS result;

SELECT LAX_FLOAT64(JSON '9.8') AS result;

SELECT LAX_FLOAT64(JSON '9') AS result;

SELECT LAX_FLOAT64(JSON '9007199254740993') AS result;

SELECT LAX_FLOAT64(JSON '1e100') AS result;

SELECT LAX_FLOAT64(JSON 'true') AS result;

SELECT LAX_FLOAT64(JSON 'false') AS result;

SELECT LAX_FLOAT64(JSON '"10"') AS result;

SELECT LAX_FLOAT64(JSON '"1.1"') AS result;

SELECT LAX_FLOAT64(JSON '"1.1e2"') AS result;

SELECT LAX_FLOAT64(JSON '"9007199254740993"') AS result;

SELECT LAX_FLOAT64(JSON '"+1.5"') AS result;

SELECT LAX_FLOAT64(JSON '"NaN"') AS result;

SELECT LAX_FLOAT64(JSON '"Inf"') AS result;

SELECT LAX_FLOAT64(JSON '"-InfiNiTY"') AS result;

SELECT LAX_FLOAT64(JSON '"foo"') AS result;

SELECT LAX_INT64(JSON '10') AS result;

SELECT LAX_INT64(JSON '10.0') AS result;

SELECT LAX_INT64(JSON '1.1') AS result;

SELECT LAX_INT64(JSON '3.5') AS result;

SELECT LAX_INT64(JSON '1.1e2') AS result;

SELECT LAX_INT64(JSON '1e100') AS result;

SELECT LAX_INT64(JSON 'true') AS result;

SELECT LAX_INT64(JSON 'false') AS result;

SELECT LAX_INT64(JSON '"10"') AS result;

SELECT LAX_INT64(JSON '"1.1"') AS result;

SELECT LAX_INT64(JSON '"1.1e2"') AS result;

SELECT LAX_INT64(JSON '"+1.5"') AS result;

SELECT LAX_INT64(JSON '"1e100"') AS result;

SELECT LAX_INT64(JSON '"foo"') AS result;

SELECT LAX_STRING(JSON '"purple"') AS result;

SELECT LAX_STRING(JSON '"10"') AS result;

SELECT LAX_STRING(JSON 'true') AS result;

SELECT LAX_STRING(JSON 'false') AS result;

SELECT LAX_STRING(JSON '10.0') AS result;

SELECT LAX_STRING(JSON '10') AS result;

SELECT LAX_STRING(JSON '1e100') AS result;

SELECT PARSE_JSON('{"coordinates": [10, 20], "id": 1}') AS json_data;

SELECT PARSE_JSON('{"id": 922337203685477580701}', wide_number_mode=>'round') AS json_data;

SELECT PARSE_JSON('6') AS json_data;

SELECT PARSE_JSON('"red"') AS json_data;

SELECT STRING(JSON '"purple"') AS color;

SELECT STRING(JSON_QUERY(JSON '{"name": "sky", "color": "blue"}', "$.color")) AS color;

SELECT SAFE.STRING(JSON '123') AS result;

With CoordinatesTable AS (
    (SELECT 1 AS id, [10, 20] AS coordinates) UNION ALL
    (SELECT 2 AS id, [30, 40] AS coordinates) UNION ALL
    (SELECT 3 AS id, [50, 60] AS coordinates))
SELECT TO_JSON(t) AS json_objects
FROM CoordinatesTable AS t;

SELECT TO_JSON(9007199254740993, stringify_wide_numbers=>TRUE) as stringify_on;

SELECT TO_JSON(9007199254740993, stringify_wide_numbers=>FALSE) as stringify_off;

SELECT TO_JSON(9007199254740993) as stringify_off;

With T1 AS (
    (SELECT 9007199254740993 AS id) UNION ALL
    (SELECT 2 AS id))
SELECT TO_JSON(t, stringify_wide_numbers=>TRUE) AS json_objects
FROM T1 AS t;

With T1 AS (
    (SELECT 9007199254740993 AS id) UNION ALL
    (SELECT 2.1 AS id))
SELECT TO_JSON(t, stringify_wide_numbers=>TRUE) AS json_objects
FROM T1 AS t;

SELECT TO_JSON_STRING(STRUCT(1 AS id, [10,20] AS coordinates)) AS json_data

SELECT TO_JSON_STRING(STRUCT(1 AS id, [10,20] AS coordinates), true) AS json_data

SELECT JSON_VALUE('{"hello": "world"', "$.hello") AS hello;

SELECT JSON_QUERY('{"key": 1, "key": 2}', "$") AS string;

SELECT JSON_QUERY(JSON '{"key": 1, "key": 2}', "$") AS json;

WITH t AS (
    SELECT '{"name": null}' AS json_string, JSON '{"name": null}' AS json)
SELECT JSON_QUERY(json_string, "$.name") AS name_string,
       JSON_QUERY(json_string, "$.name") IS NULL AS name_string_is_null,
       JSON_QUERY(json, "$.name") AS name_json,
       JSON_QUERY(json, "$.name") IS NULL AS name_json_is_null
FROM t;
