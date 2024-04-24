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

-- provided
SELECT DATE_PART(QUARTER, '2013-05-08'::DATE)  AS part;

-- expected
SELECT DATE_PART('QUARTER', '2013-05-08'::DATE) AS part;

-- result
"part"
"2"

-- provided
SELECT DAYNAME(TO_DATE('2015-05-01')) AS DAY;

-- expected
SELECT STRFTIME('2015-05-01'::DATE,'%a')AS DAY;

-- result
"DAY"
"Fri"


-- provided
SELECT EXTRACT(YEAR FROM TO_TIMESTAMP('2013-05-08T23:39:20.123-07:00')) AS v
    FROM (values(1)) v1;

-- expected
SELECT EXTRACT(YEAR FROM '2013-05-08T23:39:20.123-07:00'::TIMESTAMP WITH TIME ZONE) AS v
    FROM (values(1)) v1;

-- result
"v"
"2013"


-- provided
SELECT '2013-05-08T23:39:20.123-07:00'::TIMESTAMP AS TSTAMP,
         HOUR(tstamp) AS "HOUR",
         MINUTE(tstamp) AS "MINUTE",
         SECOND(tstamp) AS "SECOND";

-- expected
SELECT '2013-05-08T23:39:20.123-07:00'::TIMESTAMPTZ AS TSTAMP,
         HOUR(tstamp) AS "HOUR",
         MINUTE(tstamp) AS "MINUTE",
         SECOND(tstamp) AS "SECOND";

-- result
"TSTAMP","HOUR","MINUTE","SECOND"
"2013-05-09T06:39:20.123Z","13","39","20"


-- provided
SELECT TO_DATE('2015-05-08T23:39:20.123-07:00') AS "DATE",
       LAST_DAY("DATE") AS "LAST DAY OF MONTH";

-- expected
SELECT '2015-05-08T23:39:20.123-07:00'::DATE AS "DATE",
       LAST_DAY("DATE") AS "LAST DAY OF MONTH";

-- result
"DATE","LAST DAY OF MONTH"
"2015-05-08","2015-05-31"


-- provided
SELECT MONTHNAME(TO_TIMESTAMP('2015-04-03 10:00:00')) AS MONTH;

-- expected
SELECT strftime('2015-04-03 10:00:00'::TIMESTAMP, '%b') AS MONTH;

-- result
"MONTH"
"Apr"


-- provided
SELECT
       '2013-05-08T23:39:20.123-07:00'::TIMESTAMP AS tstamp,
       YEAR(tstamp) AS "YEAR",
       QUARTER(tstamp) AS "QUARTER OF YEAR",
       MONTH(tstamp) AS "MONTH",
       DAY(tstamp) AS "DAY",
       DAYOFMONTH(tstamp) AS "DAY OF MONTH",
       DAYOFYEAR(tstamp) AS "DAY OF YEAR";

-- expected
SELECT
       '2013-05-08T23:39:20.123-07:00'::TIMESTAMPTZ AS tstamp,
       YEAR(tstamp) AS "YEAR",
       QUARTER(tstamp) AS "QUARTER OF YEAR",
       MONTH(tstamp) AS "MONTH",
       DAY(tstamp) AS "DAY",
       DAYOFMONTH(tstamp) AS "DAY OF MONTH",
       DAYOFYEAR(tstamp) AS "DAY OF YEAR";

-- result
"tstamp","YEAR","QUARTER OF YEAR","MONTH","DAY","DAY OF MONTH","DAY OF YEAR"
"2013-05-09T06:39:20.123Z","2013","2","5","9","9","129"


-- provided
SELECT ADD_MONTHS('2016-05-15'::timestamp_ntz, 2) AS RESULT;

-- expected
SELECT DATE_ADD('2016-05-15'::TIMESTAMP,(2||' MONTH')::INTERVAL)AS RESULT;

-- result
"RESULT"
"2016-07-15 00:00:00.0"


-- provided
SELECT TO_DATE('2013-05-08') AS v1, DATEADD(year, 2, TO_DATE('2013-05-08')) AS v;

-- expected
SELECT '2013-05-08'::DATE AS v1, DATE_ADD('2013-05-08'::DATE, (  2 || 'year' )::INTERVAL ) AS v;

-- result
"v1","v"
"2013-05-08","2015-05-08 00:00:00.0"


-- provided
SELECT DATEDIFF(year, '2010-04-09 14:39:20'::TIMESTAMP,
                      '2013-05-08 23:39:20'::TIMESTAMP)
               AS diff_years;

-- expected
SELECT DATEDIFF('year','2010-04-09T14:39:20.000'::TIMESTAMP,'2013-05-08T23:39:20.000'::TIMESTAMP)AS DIFF_YEARS;

-- result
"diff_years"
"3"


-- provided
SELECT column1 date_1, column2 date_2,
      DATEDIFF(year, column1, column2) diff_years,
      DATEDIFF(month, column1, column2) diff_months,
      DATEDIFF(day, column1, column2) diff_days,
      column2::DATE - column1::DATE AS diff_days_via_minus
    FROM VALUES
      ('2015-12-30', '2015-12-31'),
      ('2015-12-31', '2016-01-01'),
      ('2016-01-01', '2017-12-31'),
      ('2016-08-23', '2016-09-07');

-- expected
SELECT  column1 date_1
        , column2 date_2
        , Datediff( 'year', column1, column2 ) diff_years
        , Datediff( 'month', column1, column2 ) diff_months
        , Datediff( 'day', column1, column2 ) diff_days
        , column2::DATE - column1::DATE AS diff_days_via_minus
FROM (  SELECT Unnest(  [
                    { COLUMN1:DATE '2015-12-30',COLUMN2:DATE '2015-12-31' }
                    , { COLUMN1:DATE '2015-12-31',COLUMN2:DATE '2016-01-01' }
                    , { COLUMN1:DATE '2016-01-01',COLUMN2:DATE '2017-12-31' }
                    , { COLUMN1:DATE '2016-08-23',COLUMN2:DATE '2016-09-07' }
                ], RECURSIVE => TRUE ) )
;

-- result
"date_1","date_2","diff_years","diff_months","diff_days","diff_days_via_minus"
"2015-12-30","2015-12-31","0","0","1","1"
"2015-12-31","2016-01-01","1","1","1","1"
"2016-01-01","2017-12-31","1","23","730","730"
"2016-08-23","2016-09-07","0","1","15","15"


-- provided
SELECT '2019-02-28'::DATE AS "DATE",
       TIME_SLICE("DATE", 4, 'MONTH', 'START') AS "START OF SLICE",
       TIME_SLICE("DATE", 4, 'MONTH', 'END') AS "END OF SLICE";

-- expected
SELECT  '2019-02-28'::DATE AS "DATE"
        , Time_Bucket( ( 4 || 'MONTH' )::INTERVAL, "DATE" ) AS "START OF SLICE"
        , Time_Bucket( ( 4 || 'MONTH' )::INTERVAL, "DATE" )
                 + ( 4 || 'MONTH' )::INTERVAL AS "END OF SLICE"
;

-- result
"DATE","START OF SLICE","END OF SLICE"
"2019-02-28","2019-01-01","2019-05-01 00:00:00.0"


-- provided
SELECT '2019-02-28T01:23:45.678'::TIMESTAMP_NTZ AS "TIMESTAMP 1",
       '2019-02-28T12:34:56.789'::TIMESTAMP_NTZ AS "TIMESTAMP 2",
       TIME_SLICE("TIMESTAMP 1", 8, 'HOUR') AS "SLICE FOR TIMESTAMP 1",
       TIME_SLICE("TIMESTAMP 2", 8, 'HOUR') AS "SLICE FOR TIMESTAMP 2";

-- expected
SELECT  '2019-02-28T01:23:45.678'::TIMESTAMP AS "TIMESTAMP 1"
        , '2019-02-28T12:34:56.789'::TIMESTAMP AS "TIMESTAMP 2"
        , Time_Bucket( ( 8 || 'HOUR' )::INTERVAL, "TIMESTAMP 1" ) AS "SLICE FOR TIMESTAMP 1"
        , Time_Bucket( ( 8 || 'HOUR' )::INTERVAL, "TIMESTAMP 2" ) AS "SLICE FOR TIMESTAMP 2"
;

-- result
"TIMESTAMP 1","TIMESTAMP 2","SLICE FOR TIMESTAMP 1","SLICE FOR TIMESTAMP 2"
"2019-02-28 01:23:45.678","2019-02-28 12:34:56.789","2019-02-28 00:00:00.0","2019-02-28 08:00:00.0"


-- prolog
DROP TABLE IF EXISTS accounts ;
CREATE TABLE accounts (ID INT, billing_date DATE, balance_due NUMERIC(11, 2));
INSERT INTO accounts (ID, billing_date, balance_due) VALUES
    (1, '2018-07-31', 100.00),
    (2, '2018-08-01', 200.00),
    (3, '2018-08-25', 400.00);

-- provided
SELECT
       TIME_SLICE(billing_date, 2, 'WEEK', 'START') AS "START OF SLICE",
       TIME_SLICE(billing_date, 2, 'WEEK', 'END')   AS "END OF SLICE",
       COUNT(*) AS "NUMBER OF LATE BILLS",
       SUM(balance_due) AS "SUM OF MONEY OWED"
    FROM accounts
    WHERE balance_due > 0    -- bill hasn't yet been paid
    GROUP BY "START OF SLICE", "END OF SLICE"
    order by 1;

-- expected
SELECT
    TIME_BUCKET((2||'WEEK')::INTERVAL,BILLING_DATE) AS "START OF SLICE"
    , TIME_BUCKET((2||'WEEK')::INTERVAL,BILLING_DATE)+(2||'WEEK')::INTERVAL AS "END OF SLICE"
    , COUNT(*) AS "NUMBER OF LATE BILLS"
    , SUM(BALANCE_DUE) AS "SUM OF MONEY OWED"
FROM ACCOUNTS WHERE BALANCE_DUE>0
GROUP BY "START OF SLICE","END OF SLICE"
ORDER BY 1;

-- result
"START OF SLICE","END OF SLICE","NUMBER OF LATE BILLS","SUM OF MONEY OWED"
"2018-07-23","2018-08-06 00:00:00.0","2","300.00"
"2018-08-20","2018-09-03 00:00:00.0","1","400.00"

-- epilog
DROP TABLE IF EXISTS accounts ;




















