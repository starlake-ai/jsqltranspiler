-- provided
SELECT ARRAY
  (SELECT 1 UNION ALL
   SELECT 2 UNION ALL
   SELECT 3 order by 1) AS new_array;

-- expected
SELECT List_Sort(ARRAY
  (SELECT 1 UNION ALL
   SELECT 2 UNION ALL
   SELECT 3 order by 1)) AS new_array;

-- result
"new_array"
"[1, 2, 3]"


-- provided
SELECT ARRAY_CONCAT([1, 2], [3, 4], [5, 6]) as count_to_six;

-- expected
select [1, 2] || [3, 4] || [5, 6] as count_to_six;

-- result
"count_to_six"
"[1, 2, 3, 4, 5, 6]"


-- provided
WITH items AS
  (SELECT ['coffee', NULL, 'milk' ] as list
  UNION ALL
  SELECT ['cake', 'pie'] as list)
SELECT ARRAY_TO_STRING(list, '++', 'MISSING') AS text
        , ARRAY_LENGTH(list) AS size
FROM items
ORDER BY size DESC;

-- expected
WITH items AS
  (SELECT ['coffee', NULL, 'milk' ] as list
  UNION ALL
  SELECT ['cake', 'pie'] as list)
SELECT Array_To_String( list_transform(list, x -> Coalesce( x, 'MISSING' )), '++' ) AS text
    , ARRAY_LENGTH(list) AS size
FROM items
ORDER BY size DESC;

-- result
"text","size"
"coffee++MISSING++milk","3"
"cake++pie","2"


-- provided
WITH example AS (
  SELECT [1, 2, 3, 1, 2, 3] AS arr UNION ALL
  SELECT [4, 5] AS arr UNION ALL
  SELECT [] AS arr
)
SELECT
  ARRAY_REVERSE(arr) AS reverse_arr
FROM example;

-- result
"reverse_arr"
"[3, 2, 1, 3, 2, 1]"
"[5, 4]"
"[]"


-- provided
SELECT GENERATE_ARRAY(1, 5) AS example_array;

-- expected
SELECT GENERATE_SERIES(1, 5) AS example_array;

-- result
"example_array"
"[1, 2, 3, 4, 5]"


-- provided
SELECT GENERATE_ARRAY(0, 10, 3) AS example_array;

-- expected
SELECT GENERATE_SERIES(0, 10, 3) AS example_array;

-- result
"example_array"
"[0, 3, 6, 9]"


-- provided
SELECT GENERATE_DATE_ARRAY(date_start, date_end, INTERVAL 1 WEEK) AS date_range
FROM (
  SELECT DATE '2016-01-01' AS date_start, DATE '2016-01-31' AS date_end
  UNION ALL SELECT DATE '2016-04-01', DATE '2016-04-30'
  UNION ALL SELECT DATE '2016-07-01', DATE '2016-07-31'
  UNION ALL SELECT DATE '2016-10-01', DATE '2016-10-31'
) AS items;


-- expected
SELECT GENERATE_SERIES(date_start::DATE, date_end::DATE, INTERVAL 1 WEEK::INTERVAL)::DATE[] AS date_range
FROM (
  SELECT DATE '2016-01-01' AS date_start, DATE '2016-01-31' AS date_end
  UNION ALL SELECT DATE '2016-04-01', DATE '2016-04-30'
  UNION ALL SELECT DATE '2016-07-01', DATE '2016-07-31'
  UNION ALL SELECT DATE '2016-10-01', DATE '2016-10-31'
) AS items;

-- result
"date_range"
"[2016-01-01, 2016-01-08, 2016-01-15, 2016-01-22, 2016-01-29]"
"[2016-04-01, 2016-04-08, 2016-04-15, 2016-04-22, 2016-04-29]"
"[2016-07-01, 2016-07-08, 2016-07-15, 2016-07-22, 2016-07-29]"
"[2016-10-01, 2016-10-08, 2016-10-15, 2016-10-22, 2016-10-29]"

-- provided
SELECT GENERATE_DATE_ARRAY('2016-10-05', '2016-10-08') AS example;

-- expected
SELECT GENERATE_SERIES('2016-10-05'::DATE,'2016-10-08'::DATE,INTERVAL 1 DAY)::DATE[]AS EXAMPLE;

-- result
"example"
"[2016-10-05, 2016-10-06, 2016-10-07, 2016-10-08]"

-- provided
SELECT GENERATE_TIMESTAMP_ARRAY(start_timestamp, end_timestamp, INTERVAL 1 HOUR)
  AS timestamp_array
FROM
  (SELECT
    TIMESTAMP '2016-10-05 00:00:00' AS start_timestamp,
    TIMESTAMP '2016-10-05 02:00:00' AS end_timestamp
   UNION ALL
   SELECT
    TIMESTAMP '2016-10-05 12:00:00' AS start_timestamp,
    TIMESTAMP '2016-10-05 14:00:00' AS end_timestamp
   UNION ALL
   SELECT
    TIMESTAMP '2016-10-05 23:59:00' AS start_timestamp,
    TIMESTAMP '2016-10-06 01:59:00' AS end_timestamp);


-- expected
SELECT GENERATE_SERIES(start_timestamp::TIMESTAMP, end_timestamp::TIMESTAMP, INTERVAL 1 HOUR::INTERVAL)
  AS timestamp_array
FROM
  (SELECT
    TIMESTAMP '2016-10-05 00:00:00' AS start_timestamp,
    TIMESTAMP '2016-10-05 02:00:00' AS end_timestamp
   UNION ALL
   SELECT
    TIMESTAMP '2016-10-05 12:00:00' AS start_timestamp,
    TIMESTAMP '2016-10-05 14:00:00' AS end_timestamp
   UNION ALL
   SELECT
    TIMESTAMP '2016-10-05 23:59:00' AS start_timestamp,
    TIMESTAMP '2016-10-06 01:59:00' AS end_timestamp);

-- result
"timestamp_array"
"[2016-10-05 00:00:00.0, 2016-10-05 01:00:00.0, 2016-10-05 02:00:00.0]"
"[2016-10-05 12:00:00.0, 2016-10-05 13:00:00.0, 2016-10-05 14:00:00.0]"
"[2016-10-05 23:59:00.0, 2016-10-06 00:59:00.0, 2016-10-06 01:59:00.0]"

