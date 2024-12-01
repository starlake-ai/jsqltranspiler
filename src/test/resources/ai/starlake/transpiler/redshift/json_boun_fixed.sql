--provided
SELECT JSON_PARSE('[10001,10002,"abc"]') as parse_result;

--expected
SELECT '[10001,10002,"abc"]'::JSON AS parse_result

--output
"parse_result"
"[10001,10002,""abc""]"

--result
"parse_result"
"[10001,10002,""abc""]"

--provided
SELECT JSON_TYPEOF(JSON_PARSE('[10001,10002,"abc"]')) as json_type;

--expected
SELECT JSON_TYPE('[10001,10002,"abc"]'::JSON) AS json_type

--result
"json_type"
"array"

--provided
SELECT CASE
           WHEN CAN_JSON_PARSE('[10001,10002,"abc"]')
               THEN JSON_PARSE('[10001,10002,"abc"]')
           END as 'json_parsable';

--expected
SELECT CASE
        WHEN  Try_Cast( '[10001,10002,"abc"]' AS JSON ) IS NOT NULL
            THEN '[10001,10002,"abc"]'::JSON
        END AS 'json_parsable'
;

--output
"json_parsable"
"[10001,10002,""abc""]"

--result
"json_parsable"
"[10001,10002,""abc""]"


-- provided
SELECT JSON_SERIALIZE(JSON_PARSE('[10001,10002,"abc"]')) as json_string;

-- expected
SELECT Cast('[10001,10002,"abc"]'::JSON AS VARCHAR) AS json_string;

-- result
"json_string"
"[10001,10002,""abc""]"


-- provided
SELECT JSON_SERIALIZE_TO_VARBYTE(JSON_PARSE('[10001,10002,"abc"]')) as json_varbytes;

-- expected
SELECT enocode('[10001,10002,"abc"]'::JSON) AS json_varbytes

-- count
1

--provided
SELECT CAST((JSON_SERIALIZE_TO_VARBYTE(JSON_PARSE('[10001,10002,"abc"]'))) AS VARCHAR) as json_varchars;

--expected
SELECT CAST((JSON_SERIALIZE_TO_VARBYTE('[10001,10002,"abc"]'::JSON)) AS VARCHAR) AS json_varchars

--result
"json_varchars"
"[10001,10002,""abc""]"

--provided
with datum AS(
    select 0 as id, '{"a":2}' as json_strings
    union all
    select 4, '{"a":{"b":{"c":1}}}'
    union all
    select 8, '{"a": [1,2,"b"]}'
    union all
    select 12, '{{}}'
    union all
    select 16, '{1:"a"}'
    union all
    select 20, '[1,2,3]'
)
select id, json_strings, IS_VALID_JSON(json_strings) as is_valid from datum;

--expected
WITH datum AS (SELECT 0 AS id, '{"a":2}' AS json_strings UNION ALL SELECT 4, '{"a":{"b":{"c":1}}}' UNION ALL SELECT 8, '{"a": [1,2,"b"]}' UNION ALL SELECT 12, '{{}}' UNION ALL SELECT 16, '{1:"a"}' UNION ALL SELECT 20, '[1,2,3]') SELECT id, json_strings, Json_Valid(json_strings) AND Json_type(Try_cast(json_strings AS JSON)) <> 'ARRAY' AS is_valid FROM datum


--result
"id","json_strings","is_valid"
"0","{""a"":2}","true"
"4","{""a"":{""b"":{""c"":1}}}","true"
"8","{""a"": [1,2,""b""]}","true"
"12","{{}}","false"
"16","{1:""a""}","false"
"20","[1,2,3]","false"

--provided
with datum AS(
    select '[]' as json_strings
    union all
    select '["a","b"]'
    union all
    select '["a",["b",1,["c",2,3,null]]]'
    union all
    select '{"a":1}'
    union all
    select 'a'
    union all
    select '[1,2,]'
)
select json_strings, IS_VALID_JSON_ARRAY(json_strings) as is_valid from datum;

--expected
WITH datum AS (SELECT '[]' AS json_strings UNION ALL SELECT '["a","b"]' UNION ALL SELECT '["a",["b",1,["c",2,3,null]]]' UNION ALL SELECT '{"a":1}' UNION ALL SELECT 'a' UNION ALL SELECT '[1,2,]') SELECT json_strings, Json_Valid(json_strings) AND Json_type(Try_cast(json_strings AS JSON)) = 'ARRAY' AS is_valid FROM datum;

--result
"json_strings","is_valid"
"[]","true"
"[""a"",""b""]","true"
"[""a"",[""b"",1,[""c"",2,3,null]]]","true"
"{""a"":1}","false"
"a","false"
"[1,2,]","true"

--provided
SELECT JSON_ARRAY_LENGTH('[11,12,13,{"f1":21,"f2":[25,26]},14]') as result;

--expected
SELECT JSON_ARRAY_LENGTH('[11,12,13,{"f1":21,"f2":[25,26]},14]') AS result

--output
"result"
"5"

--result
"result"
"5"

--provided
SELECT JSON_ARRAY_LENGTH('[11,12,13,{"f1":21,"f2":[25,26]},14',true) as result;

--expected
SELECT IF(TRUE AND NOT JSON_VALID('[11,12,13,{"f1":21,"f2":[25,26]},14'),NULL,JSON_ARRAY_LENGTH('[11,12,13,{"f1":21,"f2":[25,26]},14'))AS RESULT;

--result
"result"
"JSQL_NULL"

--provided
SELECT JSON_EXTRACT_ARRAY_ELEMENT_TEXT('[111,112,113]', 2) as result;

--expected
SELECT Try_Cast('[111,112,113]' AS JSON)[2] AS result

--output
"result"
"113"

--result
"result"
"113"

--provided
SELECT JSON_EXTRACT_ARRAY_ELEMENT_TEXT('["a",["b",1,["c",2,3,null,]]]',1,true) as result;

--expected
SELECT JSON_EXTRACT_ARRAY_ELEMENT_TEXT('["a",["b",1,["c",2,3,null,]]]', 1, true) AS result


--result
"result"
"JSQL_NULL"

--provided
SELECT JSON_EXTRACT_PATH_TEXT('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}','f4', 'f6') as result;

--expected
SELECT '{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}'->'f4'->'f6' AS RESULT;

--output
"result"
"""star"""

--result
"result"
"star"

--provided
SELECT JSON_EXTRACT_PATH_TEXT('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}','f4', 'f6',true) as result;

--expected
SELECT '{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}'::JSON->'f4'->'f6'->true AS result

--result
"result"
"JSQL_NULL"

--provided
SELECT JSON_EXTRACT_PATH_TEXT('{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}', 'farm', 'barn', 'color') as result;

--expected
SELECT '{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}'::JSON->'farm'->'barn'->'color' AS result

--output
"result"
"""red"""

--result
"result"
"red"

--provided
SELECT JSON_EXTRACT_PATH_TEXT('{
    "farm": {
        "barn": {}
    }
}', 'farm', 'barn', 'color') as result;

--expected
SELECT '{
    "farm": {
        "barn": {}
    }
}'::JSON->'farm'->'barn'->'color' AS result

--output
"result"
"JSQL_NULL"

--result
"result"
"JSQL_NULL"

--provided
SELECT JSON_EXTRACT_PATH_TEXT('{
  "house": {
    "address": {
      "street": "123 Any St.",
      "city": "Any Town",
      "state": "FL",
      "zip": "32830"
    },
    "bathroom": {
      "color": "green",
      "shower": true
    },
    "appliances": {
      "washing machine": {
        "brand": "Any Brand",
        "color": "beige"
      },
      "dryer": {
        "brand": "Any Brand",
        "color": "white"
      }
    }
  }
}', 'house', 'appliances', 'washing machine', 'brand') as result;

--expected
SELECT '{
  "house": {
    "address": {
      "street": "123 Any St.",
      "city": "Any Town",
      "state": "FL",
      "zip": "32830"
    },
    "bathroom": {
      "color": "green",
      "shower": true
    },
    "appliances": {
      "washing machine": {
        "brand": "Any Brand",
        "color": "beige"
      },
      "dryer": {
        "brand": "Any Brand",
        "color": "white"
      }
    }
  }
}'::JSON->'house'->'appliances'->'washing machine'->'brand' AS result

--output
"result"
"""Any Brand"""

--result
"result"
"Any Brand"

--provided
with datum AS(
    select 1 as id, JSON_PARSE('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}') as json_text
    union all
    select 2, JSON_PARSE('{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}')
)
SELECT * FROM datum;

--expected
WITH datum AS (SELECT 1 AS id, '{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}'::JSON AS json_text UNION ALL SELECT 2, '{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}'::JSON) SELECT * FROM datum

--output
"id","json_text"
"1","{""f2"":{""f3"":1},""f4"":{""f5"":99,""f6"":""star""}}"
"2","{
    ""farm"": {
        ""barn"": {
            ""color"": ""red"",
            ""feed stocked"": true
        }
    }
}"

--result
"id","json_text"
"1","{""f2"":{""f3"":1},""f4"":{""f5"":99,""f6"":""star""}}"
"2","{""farm"":{""barn"":{""color"":""red"",""feed stocked"":true}}}"

--provided
with datum AS(
    select 1 as id, JSON_PARSE('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}') as json_text
    union all
    select 2, JSON_PARSE('{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}')
)
SELECT id, JSON_EXTRACT_PATH_TEXT(JSON_SERIALIZE(json_text), 'f2') FROM json_example;

--expected
WITH datum AS (SELECT 1 AS id, '{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}'::JSON AS json_text UNION ALL SELECT 2, '{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}'::JSON) SELECT id, JSON_SERIALIZE(json_text)::JSON->'f2' FROM json_example

--result
"user_id","rec_1_id","rec_1_name","rec_2_id","rec_2_name"
"1","1","Python","2","SQL"
"2","3","R","2","SQL"
"3","2","SQL","1","Python"
"4","2","SQL","3","R"
"5","3","R","1","Python"

--provided
with datum as (
	select 1 as user_id, '[{"language_id": 1, "language_name": "Python"}, {"language_id": 2, "language_name": "SQL"}]' as recommendations
	union all
	select 2, '[{"language_id": 3, "language_name": "R"}, {"language_id": 2, "language_name": "SQL"}]'
	union all
	select 3, '[{"language_id": 2, "language_name": "SQL"}, {"language_id": 1, "language_name": "Python"}]'
	union all
	select 4, '[{"language_id": 2, "language_name": "SQL"}, {"language_id": 3, "language_name": "R"}]'
	union all
	select 5, '[{"language_id": 3, "language_name": "R"}, {"language_id": 1, "language_name": "Python"}]'
),
recs_super AS (
    SELECT
        user_id,
        JSON_PARSE(recommendations) AS recommendations
    FROM datum
)
SELECT
    user_id,
    recommendations[0].language_id AS rec_1_id,
    recommendations[0].language_name AS rec_1_name,
    recommendations[1].language_id AS rec_2_id,
    recommendations[1].language_name AS rec_2_name
FROM recs_super;


--result
"user_id","rec_1_id","rec_1_name","rec_2_id","rec_2_name"
"1","1","""Python""","2","""SQL"""
"2","3","""R""","2","""SQL"""
"3","2","""SQL""","1","""Python"""
"4","2","""SQL""","3","""R"""
"5","3","""R""","1","""Python"""



