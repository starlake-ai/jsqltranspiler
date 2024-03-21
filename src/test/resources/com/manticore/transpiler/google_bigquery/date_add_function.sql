-- provided
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL 5 DAY) AS five_days_later
;

-- expected
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL '5 DAY') AS five_days_later
;

-- count
1

-- result
"five_days_later"
"2008-12-30 00:00:00"


-- provided
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL -5 DAY) AS five_days_ago
;

-- expected
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL '-5 DAY') AS five_days_ago
;

-- count
1

-- result
"five_days_ago"
"2008-12-20 00:00:00"

-- provided
SELECT
  DATETIME '2008-12-25 15:30:00' as original_date,
  DATETIME_ADD(DATETIME '2008-12-25 15:30:00', INTERVAL 10 MINUTE) as later;

-- expected
SELECT
  DATETIME '2008-12-25 15:30:00' as original_date,
  DATE_ADD(DATETIME '2008-12-25 15:30:00', INTERVAL '10 MINUTE') as later;

-- result
"original_date","later"
"2008-12-25 15:30:00","2008-12-25 15:40:00"


-- provided
SELECT
  TIME '15:30:00' as original_time,
  TIME_ADD(TIME '15:30:00', INTERVAL 10 MINUTE) as later;

-- expected
SELECT
  TIME '15:30:00' as original_time,
  DATE_ADD(TIME '15:30:00', INTERVAL '10 MINUTE') as later;

-- result
"original_time","later"
"15:30:00","15:40:00"

