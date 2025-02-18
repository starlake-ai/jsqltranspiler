-- provided
SELECT BOOL(JSON 'true') AS vacancy;

-- expected
SELECT Cast(JSON 'true' AS BOOLEAN) AS vacancy;

-- result
"vacancy"
"true"


-- provided
SELECT FLOAT64(JSON '9.8') AS velocity;

-- expected
SELECT Cast(JSON '9.8' AS Double) AS velocity;

-- result
"velocity"
"9.8"


-- provided
SELECT FLOAT64(JSON_QUERY(JSON '{"vo2_max": 39.1, "age": 18}', "$.vo2_max")) AS vo2_max;

-- expected
SELECT Cast(JSon_Extract(JSON '{"vo2_max": 39.1, "age": 18}', '$.vo2_max') AS Double) AS vo2_max;

-- result
"vo2_max"
"39.1"


-- provided
SELECT INT64(JSON '2005') AS flight_number;

-- expected
SELECT Cast(JSON '2005' AS HugeInt) AS flight_number;

-- result
"flight_number"
"2005"


-- provided
SELECT INT64(JSON_QUERY(JSON '{"gate": "A4", "flight_number": 2005}', "$.flight_number")) AS flight_number;

-- expected
SELECT Cast(JSon_Extract(JSON '{"gate": "A4", "flight_number": 2005}', '$.flight_number') AS HugeInt) AS flight_number;

-- result
"flight_number"
"2005"


-- provided
SELECT JSON_ARRAY(10, 'foo', NULL) AS json_data;

-- result
"json_data"
"[10,""foo"",null]"


-- provided
SELECT
  JSON_EXTRACT(JSON '{"class": {"students": [{"id": 5}, {"id": 12}]}}', '$.class')
  AS json_data;

-- result
"json_data"
"{""students"":[{""id"":5},{""id"":12}]}"


-- provided
SELECT JSON_EXTRACT(
  '{"class": {"students": [{"name": "Jane"}]}}',
  '$') AS json_text_string;

-- result
"json_text_string"
"{""class"":{""students"":[{""name"":""Jane""}]}}"


-- provided
SELECT JSON_EXTRACT(
  '{"class": {"students": [{"name": "Jane"}]}}',
  '$.class.students[0]') AS first_student;

-- result
"first_student"
"{""name"":""Jane""}"


-- provided
SELECT JSON_EXTRACT_ARRAY(
  JSON '{"fruits":["apples","oranges","grapes"]}','$.fruits'
  ) AS json_array;

-- expected
SELECT JSON_EXTRACT(JSON '{"fruits":["apples","oranges","grapes"]}','$.fruits[*]')AS JSON_ARRAY;

-- result
"json_array"
"[""apples"", ""oranges"", ""grapes""]"


-- provided
SELECT JSON_EXTRACT_ARRAY(
  '{"fruit": [{"apples": 5, "oranges": 10}, {"apples": 2, "oranges": 4}], "vegetables": [{"lettuce": 7, "kale": 8}]}',
  '$.fruit'
) AS string_array;

-- expected
SELECT JSON_EXTRACT('{"fruit":[{"apples":5,"oranges":10},{"apples":2,"oranges":4}],"vegetables":[{"lettuce":7,"kale":8}]}','$.fruit[*]')AS STRING_ARRAY;


-- result
"string_array"
"[{""apples"":5,""oranges"":10}, {""apples"":2,""oranges"":4}]"


-- provided
SELECT JSON_EXTRACT_SCALAR(JSON '{"name": "Jakob", "age": "6" }', '$.age') AS scalar_age;

-- expected
SELECT CASE
        WHEN Json_Type( Json_Extract( JSON '{"name":"Jakob","age":"6"}', '$.age' ) ) IN (   'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                            , 'UBIGINT', 'BIGINT' )
            THEN Json_Extract_String( JSON '{"name":"Jakob","age":"6"}', '$.age' )
        ELSE Json_Value( JSON '{"name":"Jakob","age":"6"}', '$.age' )
        END AS scalar_age
;

-- result
"scalar_age"
"6"


-- provided
SELECT JSON_VALUE(JSON '{"name": "Jakob", "age": "6" }', '$.age') AS scalar_age;

-- expected
SELECT CASE
        WHEN Json_Type( Json_Extract( JSON '{"name":"Jakob","age":"6"}', '$.age' ) ) IN (   'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                            , 'UBIGINT', 'BIGINT' )
            THEN Json_Extract_String( JSON '{"name":"Jakob","age":"6"}', '$.age' )
        ELSE Json_Value( JSON '{"name":"Jakob","age":"6"}', '$.age' )
        END AS scalar_age
;

-- result
"scalar_age"
"6"


-- provided
SELECT JSON_QUERY('{"name": "Jakob", "age": "6"}', '$.name') AS json_name,
  JSON_VALUE('{"name": "Jakob", "age": "6"}', '$.name') AS scalar_name,
  JSON_QUERY('{"name": "Jakob", "age": "6"}', '$.age') AS json_age,
  JSON_VALUE('{"name": "Jakob", "age": "6"}', '$.age') AS scalar_age;

-- expected
SELECT  Json_Extract( '{"name":"Jakob","age":"6"}', '$.name' ) AS json_name
        , CASE
            WHEN Json_Type( Json_Extract( '{"name":"Jakob","age":"6"}', '$.name' ) ) IN (   'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                            , 'UBIGINT', 'BIGINT' )
                THEN Json_Extract_String( '{"name":"Jakob","age":"6"}', '$.name' )
            ELSE Json_Value( '{"name":"Jakob","age":"6"}', '$.name' )
            END AS scalar_name
        , Json_Extract( '{"name":"Jakob","age":"6"}', '$.age' ) AS json_age
        , CASE
            WHEN Json_Type( Json_Extract( '{"name":"Jakob","age":"6"}', '$.age' ) ) IN (    'VARCHAR', 'DOUBLE', 'BOOLEAN'
                                                                                            , 'UBIGINT', 'BIGINT' )
                THEN Json_Extract_String( '{"name":"Jakob","age":"6"}', '$.age' )
            ELSE Json_Value( '{"name":"Jakob","age":"6"}', '$.age' )
            END AS scalar_age
;

-- result
"json_name","scalar_name","json_age","scalar_age"
"""Jakob""","Jakob","""6""","6"


-- provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$[fruits]') AS string_array;

-- expected
SELECT JSON_EXTRACT_STRING('{"fruits":["apples","oranges","grapes"]}','$.fruits[*]')AS STRING_ARRAY;

-- result
"string_array"
"[apples, oranges, grapes]"


-- provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"fruits": ["apples", "oranges", "grapes"]}', '$.fruits') AS string_array;

-- expected
SELECT Json_Extract_String( '{"fruits":["apples","oranges","grapes"]}', '$.fruits[*]' ) AS string_array;

-- result
"string_array"
"[apples, oranges, grapes]"


-- provided
SELECT JSON_OBJECT('foo', 10, 'bar', TRUE) AS json_data;

-- result
"json_data"
"{""foo"":10,""bar"":true}"


-- provided
SELECT JSON_OBJECT('foo', 10, 'bar', ['a', 'b']) AS json_data;

-- result
"json_data"
"{""foo"":10,""bar"":[""a"",""b""]}"


-- provided
SELECT JSON_OBJECT(['a', 'b'], [JSON '10', JSON '"foo"']) AS json_data

-- result
"json_data"
"{""[a, b]"":[10,""foo""]}"

-- provided
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

-- expected
SELECT  json_val
        , Json_Type( json_val ) AS type
FROM (  SELECT Unnest(  [ JSON '"apple"', JSON '10', JSON '3.14', JSON 'null', JSON '{"city":"New York","State":"NY"}', JSON '["apple","banana"]', JSON 'false'] ) AS json_val  ) AS json_val
;

-- result
"json_val","type"
"""apple""","VARCHAR"
"10","UBIGINT"
"3.14","DOUBLE"
"null","NULL"
"{""city"": ""New York"", ""State"": ""NY""}","OBJECT"
"[""apple"", ""banana""]","ARRAY"
"false","BOOLEAN"


-- provided
SELECT STRING(JSON '"purple"') AS color;

-- expected
SELECT CASE Typeof( JSON '"purple"' )
        WHEN 'JSON'
            THEN  Try_Cast( Json_Extract_String( JSON '"purple"', '$' ) AS TEXT )
        WHEN 'DATE'
            THEN Strftime(  Try_Cast( JSON '"purple"' AS DATE ), '%c%z' )
        WHEN 'TIMESTAMP'
            THEN Strftime(  Try_Cast( JSON '"purple"' AS TIMESTAMP ), '%c%z' )
        WHEN 'TIMESTAMPTZ'
            THEN Strftime(  Try_Cast( JSON '"purple"' AS TIMESTAMPTZ ), '%c%z' )
        ELSE  Try_Cast( JSON '"purple"' AS TEXT )
        END AS color
;

-- result
"color"
"purple"


-- provided
SELECT PARSE_JSON('{"coordinates": [10, 20], "id": 1}') AS json_data;

-- expected
SELECT '{"coordinates":[10,20],"id":1}'::JSON AS JSON_DATA;

-- result
"json_data"
"{""coordinates"": [10, 20], ""id"": 1}"


-- provided
With CoordinatesTable AS (
    (SELECT 1 AS id, [10, 20] AS coordinates) UNION ALL
    (SELECT 2 AS id, [30, 40] AS coordinates) UNION ALL
    (SELECT 3 AS id, [50, 60] AS coordinates))
SELECT TO_JSON(t) AS json_objects
FROM CoordinatesTable AS t;

-- expected
With CoordinatesTable AS (
    (SELECT 1 AS id, [10, 20] AS coordinates) UNION ALL
    (SELECT 2 AS id, [30, 40] AS coordinates) UNION ALL
    (SELECT 3 AS id, [50, 60] AS coordinates))
SELECT TO_JSON(t) AS json_objects
FROM CoordinatesTable AS t;

-- result
"json_objects"
"{""id"":1,""coordinates"":[10,20]}"
"{""id"":2,""coordinates"":[30,40]}"
"{""id"":3,""coordinates"":[50,60]}"


-- provided
SELECT TO_JSON_STRING(STRUCT(1 AS id, [10,20] AS coordinates)) AS json_data;

-- expected
SELECT TO_JSON({ID:1,COORDINATES:[10,20]})::TEXT AS JSON_DATA;

-- result
"json_data"
"{""id"":1,""coordinates"":[10,20]}"



