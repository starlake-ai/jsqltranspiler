-- provided
SELECT DATE_TRUNC(DATE '2008-12-25', MONTH) AS month
;

-- expected
SELECT DATE_TRUNC('MONTH', DATE '2008-12-25') AS month
;

-- count
1

-- result
"month"
"2008-12-01"


-- provided
SELECT  date AS original
        , Date_Trunc( date, Week( monday ) ) AS truncated
FROM (  SELECT Date( '2017-11-05' ) AS date  )
;


-- expected
SELECT  date AS original
        , Date_Trunc( 'WEEK', date ) AS truncated
FROM (  SELECT  cast( '2017-11-05' AS DATE ) AS date  )
;

-- count
1

-- result
"original","truncated"
"2017-11-05","2017-10-30"


-- provided
SELECT
  DATETIME '2008-12-25 15:30:00' as original,
  DATETIME_TRUNC(DATETIME '2008-12-25 15:30:00', DAY) as truncated;

-- expected
SELECT
  DATETIME '2008-12-25 15:30:00' as original,
  DATE_TRUNC('DAY', DATETIME '2008-12-25 15:30:00') as truncated;

-- result
"original","truncated"
"2008-12-25 15:30:00","2008-12-25"


-- provided
SELECT
  TIME '15:30:00' as original,
  TIME_TRUNC(TIME '15:30:00', HOUR) as truncated;

-- expected
SELECT
  TIME '15:30:00' as original,
  CAST(DATE_TRUNC('HOUR', CURRENT_DATE() + TIME '15:30:00') AS TIME) as truncated;

-- result
"original","truncated"
"15:30:00","15:00:00"
