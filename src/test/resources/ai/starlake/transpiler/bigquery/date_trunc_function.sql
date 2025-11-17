-- provided
SELECT DATE_TRUNC(DATE '2008-12-25', MONTH) AS month
;

-- expected
SELECT DATE_TRUNC('MONTH', DATE '2008-12-25') AS month
;

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
"2008-12-25 15:30:00.0","2008-12-25"


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

-- provided
SELECT
  TIMESTAMP_TRUNC(TIMESTAMP '2008-12-25 15:30:00+00', DAY) AS utc,
  TIMESTAMP_TRUNC(TIMESTAMP '2008-12-25 15:30:00+00', DAY) AS la;

-- expected
SELECT
  CAST(DATE_TRUNC('DAY', TIMESTAMPTZ '2008-12-25 15:30:00+00') AS TIMESTAMP) AS utc,
  CAST(DATE_TRUNC('DAY', TIMESTAMPTZ '2008-12-25 15:30:00+00') AS TIMESTAMP) AS la;

-- result
"utc","la"
"2008-12-25 00:00:00.0","2008-12-25 00:00:00.0"

-- provided
SELECT
  TIMESTAMP_TRUNC(TIMESTAMP '2008-12-25 15:30:00+00', DAY, 'UTC') AS utc,
  TIMESTAMP_TRUNC(TIMESTAMP '2008-12-25 15:30:00+00', DAY, 'America/Los_Angeles') AS la;

-- expected
SELECT
  CAST(DATE_TRUNC('DAY', TIMESTAMPTZ '2008-12-25 15:30:00+00' AT TIME ZONE 'UTC') AS TIMESTAMPTZ) AS utc,
  CAST(DATE_TRUNC('DAY', TIMESTAMPTZ '2008-12-25 15:30:00+00' AT TIME ZONE 'America/Los_Angeles') AS TIMESTAMPTZ) AS la;

-- result
"utc","la"
"2008-12-25T00:00+07:00","2008-12-25T00:00+07:00"

