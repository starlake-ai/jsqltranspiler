-- provided
SELECT CONVERT(time, saletime) AS time, salesid
FROM sales order by salesid limit 10;

-- expected
SELECT Cast(saletime AS time) AS time, salesid
FROM sales order by salesid limit 10;

-- result
"time","salesid"
"02:36:48","1"
"05:00:16","2"
"08:26:17","3"
"08:38:52","4"
"09:17:02","5"
"11:59:24","6"
"12:56:06","7"
"02:12:36","8"
"02:23:17","9"
"02:51:55","10"


-- provided
select to_number('-12,454.8', 'S99G999D9')  AS number;

-- expected
SELECT CAST(IF(TYPEOF('-12,454.8')='VARCHAR',LIST_AGGREGATE(REGEXP_EXTRACT_ALL('-12,454.8','[\+|\-\D|\.]'),'string_agg',''),'-12,454.8')AS NUMERIC) AS number;

-- result
"number"
"-12454.80"

-- provided
select to_number('$ 12,454.88', 'L 99G999D99')  AS number;

-- expected
SELECT CAST(IF(TYPEOF('$ 12,454.88')='VARCHAR',LIST_AGGREGATE(REGEXP_EXTRACT_ALL('$ 12,454.88','[\+|\-\D|\.]'),'string_agg',''),'$ 12,454.88')AS NUMERIC)  AS number;

-- result
"number"
"12454.88"

-- provided
select to_number('$ 2,012,454.88', 'L 9,999,999.99')  AS number;

-- expected
SELECT CAST(IF(TYPEOF('$ 2,012,454.88')='VARCHAR',LIST_AGGREGATE(REGEXP_EXTRACT_ALL('$ 2,012,454.88','[\+|\-\D|\.]'),'string_agg',''),'$ 2,012,454.88')AS NUMERIC)  AS number;

-- result
"number"
"2012454.88"