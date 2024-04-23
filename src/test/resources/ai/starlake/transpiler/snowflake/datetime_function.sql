-- provided
SELECT DATE_FROM_PARTS(1977, 8, 7) AS date;

-- expected
SELECT MAKE_DATE(1977, 8, 7) AS date;

-- result
"date"
"1977-08-07"


-- provided
select time_from_parts(12, 34, 56, 987654321) AS time;

-- expected
select make_time(12, 34, (56 || '.' || 987654321)::DOUBLE ) AS time;

-- result
"time"
"12:34:56"


-- provided
SELECT TIMESTAMP_LTZ_FROM_PARTS(2013, 4, 5, 12, 00, 00) AS ts;

-- expected
SELECT MAKE_TIMESTAMP(2013, 4, 5, 12, 00, 00) AS ts;

-- result
"ts"
"2013-04-05 12:00:00.0"


-- provided
select timestamp_ntz_from_parts(2013, 4, 5, 12, 00, 00, 987654321) AS ts;

-- expected
select MAKE_TIMESTAMP(2013, 4, 5, 12, 00, (00 || '.' || 987654321)::DOUBLE) AS ts;

-- result
"ts"
"2013-04-05 12:00:00.987"


-- provided
select timestamp_ntz_from_parts(to_date('2013-04-05'), to_time('12:00:00')) AS ts;

-- expected
select ( '2013-04-05'::DATE + '12:00:00'::TIME )::TIMESTAMP AS ts;

-- result
"ts"
"2013-04-05 12:00:00.0"


-- provided
select timestamp_tz_from_parts(2013, 4, 5, 12, 00, 00, 0, 'America/Los_Angeles')  AS tstz;

-- expected
select MAKE_TIMESTAMP(2013, 4, 5, 12, 00, (00 || '.' || 0)::DOUBLE) AT TIME ZONE 'America/Los_Angeles' AS tstz;

-- result
"tstz"
"2013-04-05T19:00Z"




