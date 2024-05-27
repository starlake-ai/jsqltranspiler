-- provided
SELECT add_months('2016-08-31', 1) AS month;

-- expected
SELECT DATE_ADD(TIMESTAMPTZ '2016-08-31',(1||' MONTH')::INTERVAL) AS month;

-- result
"month"
"2016-09-29T17:00Z"


-- provided
SELECT curdate();

-- expect
SELECT CURRENT_DATE;

-- count
1


-- provided
SELECT current_date();

-- expected
SELECT CURRENT_DATE;

-- count
1


-- provided
SELECT current_timestamp();

-- expected
SELECT CURRENT_TIMESTAMP;

-- count
1

-- provided
SELECT current_timezone() as tz;

-- expected
SELECT strftime( current_timestamp, '%Z') as tz;

-- result
"tz"
"Asia/Bangkok"


-- provided
SELECT date('2021-03-21') as date;

-- expected
SELECT CAST('2021-03-21' AS DATE)AS DATE;

-- result
"date"
"2021-03-21"


-- provided
SELECT date_add('2016-07-30', 1) AS date;

-- expected
SELECT date_add(DATE '2016-07-30', 1) AS date;

-- result
"date"
"2016-07-31"


-- provided
SELECT date_add(MICROSECOND, 5, TIMESTAMP'2022-02-28 00:00:00') AS date;

-- expected
SELECT DATE_ADD(TIMESTAMP '2022-02-28T00:00:00.000',(5||'MICROSECOND')::INTERVAL)AS DATE;

-- result
"date"
"2022-02-28 00:00:00.0"


-- provided
SELECT date_diff(MONTH, TIMESTAMP'2021-02-28 12:00:00', TIMESTAMP'2021-03-28 11:59:59') as diff;

-- expected
SELECT DATE_DIFF('MONTH',TIMESTAMP '2021-02-28T12:00:00.000',TIMESTAMP '2021-03-28T11:59:59.000')AS DIFF;

-- result
"diff"
"1"


-- provided
SELECT date_from_unix_date(1) AS d;

-- expected
SELECT DATE '1970-01-01'+(1||'DAY')::INTERVAL AS D;

-- result
"d"
"1970-01-02 00:00:00.0"


-- provided
SELECT date_part('YEAR', TIMESTAMP'2019-08-12 01:00:00.123456') AS part;

-- expected
SELECT DATE_PART('YEAR',TIMESTAMP '2019-08-12T01:00:00.000')AS PART;

-- result
"part"
"2019"


-- provided
SELECT date_sub('2016-07-30', 1) AS d;

-- expected
SELECT date_add(DATE '2016-07-30', -1*1) AS d;

-- result
"d"
"2016-07-29"


-- provided
SELECT datediff('2009-07-31', '2009-07-30') AS diff;

-- expected
SELECT DATE '2009-07-31' - DATE '2009-07-30' AS diff;

-- result
"diff"
"1"


-- provided
SELECT day('2009-07-30') AS d;

-- expected
SELECT day(DATE '2009-07-30') AS d;

-- result
"d"
"30"

-- provided
SELECT dayofmonth('2009-07-30') AS d;

-- expected
SELECT dayofmonth(DATE '2009-07-30') AS d;

-- result
"d"
"30"


-- provided
SELECT dayofweek('2009-07-30') AS d;

-- expected
SELECT dayofweek(DATE '2009-07-30') AS d;

-- result
"d"
"4"


-- provided
SELECT dayofyear('2009-07-30') AS d;

-- expected
SELECT dayofyear(DATE '2009-07-30') AS d;

-- result
"d"
"211"


-- provided
SELECT dayofyear('2009-07-30') AS d;

-- expected
SELECT dayofyear(DATE '2009-07-30') AS d;

-- result
"d"
"211"


-- provided
SELECT hour('2009-07-30 8:00:00') AS d;

-- expected
SELECT HOUR(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:00:00.000')AS D;

-- result
"d"
"8"


-- provided
SELECT last_day('2009-07-30') AS d;

-- expected
SELECT last_day(DATE '2009-07-30') AS d;

-- result
"d"
"2009-07-31"


-- provided
SELECT minute('2009-07-30 8:21:00') AS d;

-- expected
SELECT minute(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:21:00.000')AS D;

-- result
"d"
"21"


-- provided
SELECT month('2009-07-30 8:21:00') AS d;

-- expected
SELECT month(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:21:00.000')AS D;

-- result
"d"
"7"


-- provided
SELECT quarter('2009-07-30 8:21:00') AS d;

-- expected
SELECT quarter(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:21:00.000')AS D;

-- result
"d"
"3"


-- provided
SELECT second('2009-07-30 8:21:53') AS d;

-- expected
SELECT second(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:21:53.000')AS D;

-- result
"d"
"53"


-- provided
SELECT weekday('2009-07-30 8:21:53') AS d;

-- expected
SELECT weekday(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:21:53.000')AS D;

-- result
"d"
"4"


-- provided
SELECT weekofyear('2009-07-30 8:21:53') AS d;

-- expected
SELECT weekofyear(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:21:53.000')AS D;

-- result
"d"
"31"


-- provided
SELECT year('2009-07-30 8:21:53') AS d;

-- expected
SELECT year(TIMESTAMP WITHOUT TIME ZONE '2009-07-30T08:21:53.000')AS D;

-- result
"d"
"2009"


-- provided
SELECT from_unixtime(0, 'yyyy-MM-dd HH:mm:ss') AS D;

-- expected
SELECT TIMESTAMP '1969-12-31T16:00:00.000'+(0||'SECOND')::INTERVAL AS D;

-- result
"d"
"1969-12-31 16:00:00.0"


-- provided
SELECT to_unix_timestamp('2016-04-08', 'yyyy-MM-dd') AS s;

-- expected
SELECT epoch(DATE '2016-04-08') AS s;

-- result
"s"
"1.4600736E9"


-- provided
SELECT extract(YEAR FROM TIMESTAMP '2019-08-12 01:00:00.123456') AS year;

-- expected
SELECT EXTRACT(YEAR FROM TIMESTAMP '2019-08-12T01:00:00.000') AS YEAR;

-- result
"year"
"2019"


-- provide
SELECT getdate();

-- expected
SELECT CURRENT_DATE;

-- count
1


-- provide
SELECT now();

-- expected
SELECT CURRENT_TIMESTAMP;

-- count
1


-- provided
SELECT make_date(2013, 7, 15) AS d;

-- result
"d"
"2013-07-15"


-- provided
SELECT make_timestamp(2014, 12, 28, 6, 30, 45.887, 'CET') AS d;

-- expected
SELECT make_timestamp(2014, 12, 28, 6, 30, 45.887) AT TIME ZONE 'CET' AS d;

-- result
"d"
"2014-12-28T05:30:45.887Z"

-- provided
SELECT make_timestamp(2019, 6, 30, 23, 59, 60) AS d;

-- result
"d"
"2019-07-01 00:00:00.0"