--provided
SELECT CASE
           WHEN CAN_JSON_PARSE('This is a string.')
               THEN JSON_PARSE('This is a string.')
           ELSE 'This is not JSON.'
           END as json_parsable;

--expected
SELECT CASE WHEN Try_Cast('This is a string.' AS JSON) IS NOT NULL THEN 'This is a string.'::JSON ELSE 'This is not JSON.' END AS json_parsable

--result
"json_parsable"
"""This is not JSON."""


--expected
WITH datum AS (SELECT 1 AS id, '{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}'::JSON AS json_text UNION ALL SELECT 2, '{
    "farm": {
        "barn": {
            "color": "red",
            "feed stocked": true
        }
    }
}'::JSON) SELECT id, JSON_SERIALIZE(json_text)::JSON->'f2' FROM json_example


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
WITH DATUM AS(SELECT 1 AS ID,JSON('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}')AS JSON_TEXT UNION ALL SELECT 2,JSON('{"farm":{"barn":{"color":"red","feed stocked":true}}}'))SELECT ID,CAST(JSON_TEXT AS TEXT)->>'f2' FROM JSON_EXAMPLE
;

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

-- ---------------------------------------------------------------------------------------------------------------------
-- JSON Table function

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
select r.language_id, max(r.language_name) as language_name, count(*) as nb_recommendations, count(distinct r.language_name) as id_mismatch
FROM recs_super rs, rs.recommendations r
group by r.language_id

--expected
WITH datum AS (SELECT 1 AS user_id, '[{"language_id": 1, "language_name": "Python"}, {"language_id": 2, "language_name": "SQL"}]' AS recommendations UNION ALL SELECT 2, '[{"language_id": 3, "language_name": "R"}, {"language_id": 2, "language_name": "SQL"}]' UNION ALL SELECT 3, '[{"language_id": 2, "language_name": "SQL"}, {"language_id": 1, "language_name": "Python"}]' UNION ALL SELECT 4, '[{"language_id": 2, "language_name": "SQL"}, {"language_id": 3, "language_name": "R"}]' UNION ALL SELECT 5, '[{"language_id": 3, "language_name": "R"}, {"language_id": 1, "language_name": "Python"}]'), recs_super AS (SELECT user_id, recommendations::JSON AS recommendations FROM datum) SELECT r.language_id, max(r.language_name) AS language_name, count(*) AS nb_recommendations, count(DISTINCT r.language_name) AS id_mismatch FROM recs_super rs, rs.recommendations r GROUP BY r.language_id


--result
"language_id","language_name","nb_recommendations","id_mismatch"
"1","""Python""","3","1"
"3","""R""","3","1"
"2","""SQL""","4","1"