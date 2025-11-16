-- provided
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL 5 DAY) AS five_days_later
;

-- expected
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL 5 DAY) AS five_days_later
;

-- count
1

-- result
"five_days_later"
"2008-12-30 00:00:00.0"


-- provided
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL -5 DAY) AS five_days_ago
;

-- expected
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL '-5' DAY) AS five_days_ago
;

-- count
1

-- result
"five_days_ago"
"2008-12-20 00:00:00.0"

-- provided
SELECT
  DATETIME '2008-12-25 15:30:00' as original_date,
  DATETIME_ADD(DATETIME '2008-12-25 15:30:00', INTERVAL 10 MINUTE) as later;

-- expected
SELECT
  DATETIME '2008-12-25 15:30:00' as original_date,
  DATE_ADD(DATETIME '2008-12-25 15:30:00', INTERVAL 10 MINUTE) as later;

-- result
"original_date","later"
"2008-12-25 15:30:00.0","2008-12-25 15:40:00.0"


-- provided
SELECT
  TIME '15:30:00' as original_time,
  TIME_ADD(TIME '15:30:00', INTERVAL 10 MINUTE) as later;

-- expected
SELECT
  TIME '15:30:00' as original_time,
  DATE_ADD(TIME '15:30:00', INTERVAL 10 MINUTE) as later;

-- result
"original_time","later"
"15:30:00","15:40:00"


-- provided
SELECT
  TIMESTAMP('2008-12-25 15:30:00+00') AS original_time,
  TIMESTAMP_ADD(TIMESTAMP '2008-12-25 15:30:00+00', INTERVAL 10 MINUTE) AS later;

-- expected
SELECT
  CAST('2008-12-25 15:30:00+00' AS TIMESTAMPTZ) as original_time,
  DATE_ADD(TIMESTAMPTZ '2008-12-25 15:30:00+00', INTERVAL 10 MINUTE) as later;

-- result
"original_time","later"
"2008-12-25T15:30Z","2008-12-25T15:40Z"


