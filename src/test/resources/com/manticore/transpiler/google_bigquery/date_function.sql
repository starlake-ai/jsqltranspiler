-- provided
SELECT
  DATE(2016, 12, 25) AS date_ymd,
  DATE(DATETIME '2016-12-25 23:59:59') AS date_dt,
  DATE(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz;

-- expected
SELECT
  MAKE_DATE(2016, 12, 25) AS date_ymd,
  CAST(DATETIME '2016-12-25 23:59:59' AS DATE) AS date_dt,
  /*APPROXIMATION: timezone not supported*/ CAST(TIMESTAMP '2016-12-25 05:30:00+07' AS DATE) AS date_tstz;

-- count
1

-- result
"date_ymd","date_dt","date_tstz"
"2016-12-25","2016-12-25","2016-12-24"

