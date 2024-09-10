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
SELECT JSON_EXTRACT(
  JSON '{"fruits":["apples","oranges","grapes"]}','$.fruits'
  ) AS json_array;

-- result
"json_array"
"[""apples"",""oranges"",""grapes""]"


-- provided
SELECT JSON_EXTRACT_ARRAY(
  '{"fruit": [{"apples": 5, "oranges": 10}, {"apples": 2, "oranges": 4}], "vegetables": [{"lettuce": 7, "kale": 8}]}',
  '$.fruit'
) AS string_array;

-- expected
SELECT JSON_EXTRACT(
  '{"fruit": [{"apples": 5, "oranges": 10}, {"apples": 2, "oranges": 4}], "vegetables": [{"lettuce": 7, "kale": 8}]}',
  '$.fruit'
) AS string_array;


-- result
"string_array"
"[{""apples"":5,""oranges"":10},{""apples"":2,""oranges"":4}]"

