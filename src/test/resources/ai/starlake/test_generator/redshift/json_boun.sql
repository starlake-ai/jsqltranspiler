SELECT JSON_PARSE('[10001,10002,"abc"]') as parse_result;

SELECT JSON_TYPEOF(JSON_PARSE('[10001,10002,"abc"]')) as json_type;

SELECT CASE
           WHEN CAN_JSON_PARSE('[10001,10002,"abc"]')
               THEN JSON_PARSE('[10001,10002,"abc"]')
           END as 'json_parsable';

SELECT CASE
           WHEN CAN_JSON_PARSE('This is a string.')
               THEN JSON_PARSE('This is a string.')
           ELSE 'This is not JSON.'
           END as json_parsable;

SELECT JSON_SERIALIZE(JSON_PARSE('[10001,10002,"abc"]')) as json_string;

SELECT JSON_SERIALIZE_TO_VARBYTE(JSON_PARSE('[10001,10002,"abc"]')) as json_varbytes;

SELECT CAST((JSON_SERIALIZE_TO_VARBYTE(JSON_PARSE('[10001,10002,"abc"]'))) AS VARCHAR) as json_varchars;

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

SELECT JSON_ARRAY_LENGTH('[11,12,13,{"f1":21,"f2":[25,26]},14]') as result;

SELECT JSON_ARRAY_LENGTH('[11,12,13,{"f1":21,"f2":[25,26]},14',true) as result;

SELECT JSON_EXTRACT_ARRAY_ELEMENT_TEXT('[111,112,113]', 2) as result;

SELECT JSON_EXTRACT_ARRAY_ELEMENT_TEXT('["a",["b",1,["c",2,3,null,]]]',1,true) as result;

SELECT JSON_EXTRACT_PATH_TEXT('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}','f4', 'f6') as result;

SELECT JSON_EXTRACT_PATH_TEXT('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}','f4', 'f6',true) as result;

SELECT JSON_EXTRACT_PATH_TEXT('{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}', 'farm', 'barn', 'color') as result;

SELECT JSON_EXTRACT_PATH_TEXT('{
    "farm": {
        "barn": {}
    }
}', 'farm', 'barn', 'color') as result;

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
)
SELECT
    user_id,
    JSON_EXTRACT_PATH_TEXT(
            JSON_EXTRACT_ARRAY_ELEMENT_TEXT(
                    recommendations, 0), 'language_id') AS rec_1_id,
    JSON_EXTRACT_PATH_TEXT(
            JSON_EXTRACT_ARRAY_ELEMENT_TEXT(
                    recommendations, 0), 'language_name') AS rec_1_name,
    JSON_EXTRACT_PATH_TEXT(
            JSON_EXTRACT_ARRAY_ELEMENT_TEXT(
                    recommendations, 1), 'language_id') AS rec_2_id,
    JSON_EXTRACT_PATH_TEXT(
            JSON_EXTRACT_ARRAY_ELEMENT_TEXT(
                    recommendations, 1), 'language_name') AS rec_2_name
FROM datum

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
FROM recs_super

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
select r.language_id, max(r.language_name) as language_name, count(*) as nb_recommendations, count(distinct r.language_name) as id_mismatch
FROM recs_super rs, rs.recommendations r
group by r.language_id