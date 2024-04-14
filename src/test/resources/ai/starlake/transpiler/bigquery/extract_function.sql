-- provided
SELECT EXTRACT(DAY FROM DATE '2013-12-25') AS the_day
;

-- expected
SELECT EXTRACT(DAY FROM DATE '2013-12-25') AS the_day
;

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

-- result
"date","week_sunday","week_monday"
"2017-11-05","44","44"


-- provided
SELECT EXTRACT(HOUR FROM DATETIME(2008, 12, 25, 15, 30, 00)) as hour;

-- expected
SELECT EXTRACT(HOUR FROM Cast(MAKE_DATE(2008, 12, 25) + MAKE_TIME(15, 30, 00) AS DATETIME)) as hour;

-- result
"hour"
"15"


-- provided
WITH Datetimes AS (
  SELECT DATETIME  '2005-01-03 12:34:56' as datetime UNION ALL
  SELECT DATETIME '2007-12-31' UNION ALL
  SELECT DATETIME '2009-01-01' UNION ALL
  SELECT DATETIME '2009-12-31' UNION ALL
  SELECT DATETIME '2017-01-02' UNION ALL
  SELECT DATETIME '2017-05-26'
)
SELECT
  datetime,
  EXTRACT(ISOYEAR FROM datetime) AS isoyear,
  EXTRACT(ISOWEEK FROM datetime) AS isoweek,
  EXTRACT(YEAR FROM datetime) AS year,
  EXTRACT(WEEK FROM datetime) AS week
FROM Datetimes
ORDER BY datetime
;

-- expected
WITH Datetimes AS (
  SELECT DATETIME '2005-01-03 12:34:56' as datetime UNION ALL
  SELECT DATETIME '2007-12-31' UNION ALL
  SELECT DATETIME '2009-01-01' UNION ALL
  SELECT DATETIME '2009-12-31' UNION ALL
  SELECT DATETIME '2017-01-02' UNION ALL
  SELECT DATETIME '2017-05-26'
)
SELECT
  datetime,
  EXTRACT(ISOYEAR FROM datetime) AS isoyear,
  EXTRACT(WEEK FROM datetime) AS isoweek,
  EXTRACT(YEAR FROM datetime) AS year,
  /*APPROXIMATION: WEEK*/ EXTRACT(WEEK FROM datetime) AS week
FROM Datetimes
ORDER BY datetime
;

-- result
"datetime","isoyear","isoweek","year","week"
"2005-01-03 12:34:56","2005","1","2005","1"
"2007-12-31 00:00:00","2008","1","2007","1"
"2009-01-01 00:00:00","2009","1","2009","1"
"2009-12-31 00:00:00","2009","53","2009","53"
"2017-01-02 00:00:00","2017","1","2017","1"
"2017-05-26 00:00:00","2017","21","2017","21"


-- provided
SELECT EXTRACT(HOUR FROM TIME '15:30:00') as hour;

-- expected
SELECT EXTRACT(HOUR FROM TIME '15:30:00.000') as hour;

-- result
"hour"
"15"

-- provided
WITH Input AS (SELECT TIMESTAMP('2008-12-25 05:31:00+00') AS timestamp_value)
SELECT
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'UTC') AS the_day_utc,
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'America/Los_Angeles') AS the_day_california
FROM Input;

-- expected
WITH Input AS (SELECT Cast('2008-12-25 05:31:00+00' AS TIMESTAMPTZ) AS timestamp_value)
SELECT
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'UTC') AS the_day_utc,
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'America/Los_Angeles') AS the_day_california
FROM Input;

-- result
"the_day_utc","the_day_california"
"25","24"

-- provided
WITH Input AS (SELECT TIMESTAMP('2008-12-25 05:30:00+00') AS timestamp_value)
SELECT
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'UTC') AS the_day_utc,
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'America/Los_Angeles') AS the_day_california
FROM Input;

-- expected
WITH Input AS (SELECT Cast('2008-12-25 05:30:00+00' AS TIMESTAMPTZ) AS timestamp_value)
SELECT
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'UTC') AS the_day_utc,
  EXTRACT(DAY FROM timestamp_value AT TIME ZONE 'America/Los_Angeles') AS the_day_california
FROM Input;

-- result
"the_day_utc","the_day_california"
"25","24"

