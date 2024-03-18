-- provided
SELECT EXTRACT(DAY FROM DATE '2013-12-25') AS the_day
;

-- expected
SELECT EXTRACT(DAY FROM DATE '2013-12-25') AS the_day
;

-- count
1

-- result
"the_day"
"25"

-- provided
WITH t AS (SELECT DATE('2017-11-05') AS date)
SELECT
  date,
  EXTRACT(WEEK FROM date) AS week_sunday,
  EXTRACT(ISOWEEK FROM date) AS week_monday FROM t
;

-- expected
WITH t AS (SELECT CAST('2017-11-05' AS DATE) AS date)
SELECT
  date,
  EXTRACT(WEEK FROM date) AS week_sunday,
  EXTRACT(WEEK FROM date) AS week_monday FROM t
;

-- count
1

-- result
"date","week_sunday","week_monday"
"2017-05-11","44","44"
