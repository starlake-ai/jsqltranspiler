-- provided
SELECT DATE_SUB(DATE '2008-12-25', INTERVAL 5 DAY) AS five_days_ago
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
SELECT DATE_SUB(DATE '2008-12-25', INTERVAL -5 DAY) AS five_days_later
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
SELECT
  DATETIME '2008-12-25 15:30:00' as original_date,
  DATETIME_SUB(DATETIME '2008-12-25 15:30:00', INTERVAL 10 MINUTE) as earlier
;

-- expected
SELECT
  DATETIME '2008-12-25 15:30:00' as original_date,
  DATE_ADD(DATETIME '2008-12-25 15:30:00', INTERVAL '-10' MINUTE) as earlier
;

--result
"original_date","earlier"
"2008-12-25 15:30:00.0","2008-12-25 15:20:00.0"

-- provided
SELECT TIME_SUB(TIME '15:30:00', INTERVAL 10 MINUTE) as earlier;

-- expected
SELECT DATE_ADD(TIME '15:30:00', INTERVAL '-10' MINUTE) as earlier;

-- result
"earlier"
"15:20:00"


-- provided
SELECT
  TIMESTAMP('2008-12-25 15:30:00+00') AS original,
  TIMESTAMP_SUB(TIMESTAMP '2008-12-25 15:30:00+00', INTERVAL 10 MINUTE) AS earlier;

-- expected
SELECT
  Cast('2008-12-25 15:30:00+00' AS TIMESTAMPTZ) AS original,
  DATE_ADD(TIMESTAMPTZ '2008-12-25 15:30:00+00', INTERVAL '-10' MINUTE) AS earlier;

-- result
"original","earlier"
"2008-12-25T22:30+07:00","2008-12-25T22:20+07:00"

