-- provided
SELECT
  DATETIME(2008, 12, 25, 05, 30, 00) as datetime_ymdhms,
  DATETIME(DATE '2016-12-25', TIME '23:59:59') AS datetime_dt,
  DATETIME(TIMESTAMP '2008-12-25 05:30:00+00', 'America/Los_Angeles') as datetime_tstz
;


-- expected
SELECT
  CAST(MAKE_DATE(2008, 12, 25) + MAKE_TIME(05, 30, 00) AS DATETIME) AS datetime_ymdhms,
  CAST(DATE '2016-12-25' + TIME '23:59:59' AS DATETIME) AS datetime_dt,
  CAST(TIMESTAMPTZ '2008-12-25 05:30:00+00' AT TIME ZONE 'America/Los_Angeles' AS DATETIME) AS datetime_tstz
;

-- result
"datetime_ymdhms","datetime_dt","datetime_tstz"
"2008-12-25 05:30:00.0","2016-12-25 23:59:59.0","2008-12-24 21:30:00.0"

