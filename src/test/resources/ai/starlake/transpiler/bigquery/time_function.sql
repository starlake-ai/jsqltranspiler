-- provided

SELECT
  TIME(15, 30, 00) as time_hms,
  TIME(DATETIME '2008-12-25 15:30:00') AS time_dt,
  TIME(TIMESTAMP '2008-12-25 15:30:00+08', 'America/Los_Angeles') as time_tstz;

-- expected
SELECT
  MAKE_TIME(15, 30, 00) as time_hms,
  CAST(DATETIME '2008-12-25 15:30:00' AS TIME) AS time_dt,
  CAST(TIMESTAMPTZ '2008-12-25 15:30:00+08' AT TIME ZONE 'America/Los_Angeles' AS TIME) as time_tstz;

-- result
"time_hms","time_dt","time_tstz"
"15:30:00","15:30:00","23:30:00"

