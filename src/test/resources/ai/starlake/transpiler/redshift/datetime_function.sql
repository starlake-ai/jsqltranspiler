-- provided
select add_months('2008-01-01 05:07:30', 1) AS added;

-- expected
select date_add(TIMESTAMP '2008-01-01 05:07:30', (1 || ' MONTH')::INTERVAL ) AS added;

-- result
"added"
"2008-02-01 05:07:30.0"


-- provided
SELECT TIMESTAMPTZ '2001-02-16 20:38:40-05' AT TIME ZONE 'MST' tstz;

-- result
"tstz"
"2001-02-16 18:38:40.0"


-- provided
select convert_timezone('America/New_York', '2013-02-01 08:00:00') AS converted;

-- expected
select TIMESTAMP '2013-02-01 08:00:00' AT TIME ZONE 'UTC' AT TIME ZONE 'America/New_York' AS converted;

-- result
"converted"
"2013-02-01 03:00:00.0"


-- provided
select listtime, convert_timezone('US/Pacific', listtime)  AS converted from listing
where listid = 16;

-- expected
select listtime, listtime AT TIME ZONE 'UTC' AT TIME ZONE 'US/Pacific' AS converted
from listing
where listid = 16;

-- result
"listtime","converted"
"2008-08-24 09:36:12.0","2008-08-24 02:36:12.0"


-- provided
select caldate, '2008-01-04',
date_cmp(caldate,'2008-01-04') AS date_cmp
from date
order by dateid
limit 10;

-- expected
SELECT  caldate
        , '2008-01-04'
        , CASE
            WHEN caldate::DATE < DATE '2008-01-04'
                THEN - 1
            WHEN caldate::DATE > DATE '2008-01-04'
                THEN 1
            ELSE 0
            END AS date_cmp
FROM date
ORDER BY dateid
LIMIT 10
;

-- result
"caldate","'2008-01-04'","date_cmp"
"2008-01-01","2008-01-04","-1"
"2008-01-02","2008-01-04","-1"
"2008-01-03","2008-01-04","-1"
"2008-01-04","2008-01-04","0"
"2008-01-05","2008-01-04","1"
"2008-01-06","2008-01-04","1"
"2008-01-07","2008-01-04","1"
"2008-01-08","2008-01-04","1"
"2008-01-09","2008-01-04","1"
"2008-01-10","2008-01-04","1"


-- provided
select listid, '2008-06-18', CAST(listtime AS timestamptz) AS tstz,
date_cmp_timestamptz('2008-06-18', CAST(listtime AS timestamptz))  AS compared
from listing
order by 1, 2, 3, 4
limit 10;

-- expected
SELECT  listid
        , '2008-06-18'
        ,  Cast( listtime AS TIMESTAMPTZ ) AS tstz
        , CASE
            WHEN DATE '2008-06-18' <  Cast( listtime AS TIMESTAMPTZ )
                THEN - 1
            WHEN DATE '2008-06-18' >  Cast( listtime AS TIMESTAMPTZ )
                THEN 1
            ELSE 0
            END AS compared
FROM listing
ORDER BY    1
            , 2
            , 3
            , 4
LIMIT 10
;

-- result
"listid","'2008-06-18'","tstz","compared"
"1","2008-06-18","2008-01-24T06:43:29+07:00","1"
"2","2008-06-18","2008-03-05T12:25:29+07:00","1"
"3","2008-06-18","2008-11-01T07:35:33+07:00","-1"
"4","2008-06-18","2008-05-24T01:18:37+07:00","1"
"5","2008-06-18","2008-05-17T02:29:11+07:00","1"
"6","2008-06-18","2008-08-15T02:08:13+07:00","-1"
"7","2008-06-18","2008-11-15T09:38:15+07:00","-1"
"8","2008-06-18","2008-11-09T05:07:30+07:00","-1"
"9","2008-06-18","2008-09-09T08:03:36+07:00","-1"
"10","2008-06-18","2008-06-17T09:44:54+07:00","1"


-- provided
select dateadd(day,30,caldate) as novplus30
from date
where month='NOV'
order by dateid;


-- expected
select date_add(caldate,  (30 ||' DAY')::INTERVAL) as novplus30
from date
where month='NOV'
order by dateid;

-- result
"novplus30"
"2008-12-01 00:00:00.0"
"2008-12-02 00:00:00.0"
"2008-12-03 00:00:00.0"
"2008-12-04 00:00:00.0"
"2008-12-05 00:00:00.0"
"2008-12-06 00:00:00.0"
"2008-12-07 00:00:00.0"
"2008-12-08 00:00:00.0"
"2008-12-09 00:00:00.0"
"2008-12-10 00:00:00.0"
"2008-12-11 00:00:00.0"
"2008-12-12 00:00:00.0"
"2008-12-13 00:00:00.0"
"2008-12-14 00:00:00.0"
"2008-12-15 00:00:00.0"
"2008-12-16 00:00:00.0"
"2008-12-17 00:00:00.0"
"2008-12-18 00:00:00.0"
"2008-12-19 00:00:00.0"
"2008-12-20 00:00:00.0"
"2008-12-21 00:00:00.0"
"2008-12-22 00:00:00.0"
"2008-12-23 00:00:00.0"
"2008-12-24 00:00:00.0"
"2008-12-25 00:00:00.0"
"2008-12-26 00:00:00.0"
"2008-12-27 00:00:00.0"
"2008-12-28 00:00:00.0"
"2008-12-29 00:00:00.0"
"2008-12-30 00:00:00.0"


-- provided
select datediff(week,'2009-01-01','2009-12-31') as numweeks;

-- expected
select datediff('week',DATE '2009-01-01', DATE '2009-12-31') as numweeks;

-- result
"numweeks"
"52"


-- provided
select datediff(hour, '2023-01-01', '2023-01-03 05:04:03') AS diff;

-- expected
select datediff('HOUR', DATE '2023-01-01',  TIMESTAMP WITHOUT TIME ZONE '2023-01-03T05:04:03.000') AS diff;

-- result
"diff"
"53"


-- provided
SELECT DATE_PART(minute, timestamp '20230104 04:05:06.789') AS part;

-- expected
SELECT DATE_PART('MINUTE', timestamp '2023-01-04T04:05:06.789') AS part;

-- result
"part"
"5"


-- provided
SELECT DATE_PART(minute, timestamp '20230104 04:05:06.789+0700') AS part;

-- expected
SELECT DATE_PART('MINUTE', timestamp with time zone '2023-01-03T21:05:06.789+0000') AS part;

-- result
"part"
"5"

-- provided
SELECT DATE_PART(minute, DATE '20230104 04:05:06.789+0700') AS part;

-- expected
SELECT DATE_PART('MINUTE', timestamp with time zone '2023-01-03T21:05:06.789+0000'::DATE) AS part;

-- result
"part"
"0"


-- provided
SELECT DATE_PART_YEAR(date '20220502 04:05:06.789') AS year;

-- expected
SELECT DATE_PART('YEAR', TIMESTAMP WITHOUT TIME ZONE '2022-05-02T04:05:06.789'::DATE) AS year;

-- result
"year"
"2022"

-- provided
select date_trunc('week', TIMESTAMP '20220430 04:05:06.789') AS truncated;

-- expected
select date_trunc('week', TIMESTAMP '2022-04-30T04:05:06.789') AS truncated;

-- result
"truncated"
"2022-04-25"


-- provided
select extract(ms from timestamp '2009-09-09 12:08:43.101') AS millis;

-- expected
select extract(ms from timestamp '2009-09-09T12:08:43.101') % 1000 AS millis;

-- result
"millis"
"101"


-- provided
select GETDATE();

-- expected
select get_current_timestamp();

-- count
1


-- provided
select interval_cmp(INTERVAL '3 days', INTERVAL '1 year') as compare;

-- expected
select case
        when INTERVAL '3 days' > INTERVAL '1 year' then 1
        when INTERVAL '3 days' < INTERVAL '1 year' then -1
        else 0 end as compare
;

-- result
"compare"
"-1"


-- provided
select datediff(day, saletime, last_day(saletime)) as "Days Remaining", sum(qtysold) AS tally
from sales
where datediff(day, saletime, last_day(saletime)) < 7
group by 1
order by 1;

-- expected
select datediff('DAY', saletime, last_day(saletime)) as "Days Remaining", sum(qtysold) AS tally
from sales
where datediff('DAY', saletime, last_day(saletime)) < 7
group by 1
order by 1;

-- result
"Days Remaining","tally"
"0","10140"
"1","11187"
"2","11515"
"3","11217"
"4","11446"
"5","11708"
"6","10988"

-- provided
select months_between('1969-01-18', '1969-03-18')
as months;

-- expected
select date_diff('MONTH', DATE '1969-03-18', DATE '1969-01-18')
as months;

-- result
"months"
"-2"


-- provided
select sysdate;

-- expected
select current_date;

-- count
1


-- provided
select timeofday();

-- expected
select strftime(Current_TIMESTAMP, '%a %b %-d %H:%M:%S.%n %Y %Z');

-- count
1


-- provided
select listid, listtime,
timestamp_cmp_date(listtime, '2008-06-18')  AS cmp
from listing
order by 1, 2, 3
limit 10;

-- expected
SELECT  listid
        , listtime
        , CASE
            WHEN listtime::TIMESTAMP < DATE '2008-06-18'
                THEN - 1
            WHEN listtime::TIMESTAMP > DATE '2008-06-18'
                THEN 1
            ELSE 0
            END AS cmp
FROM listing
ORDER BY    1
            , 2
            , 3
LIMIT 10
;

-- result
"listid","listtime","cmp"
"1","2008-01-24 06:43:29.0","-1"
"2","2008-03-05 12:25:29.0","-1"
"3","2008-11-01 07:35:33.0","1"
"4","2008-05-24 01:18:37.0","-1"
"5","2008-05-17 02:29:11.0","-1"
"6","2008-08-15 02:08:13.0","1"
"7","2008-11-15 09:38:15.0","1"
"8","2008-11-09 05:07:30.0","1"
"9","2008-09-09 08:03:36.0","1"
"10","2008-06-17 09:44:54.0","-1"


-- provided
select listid, listtime,
timestamp_cmp_date(listtime, '2008-06-18') AS cmp
from listing
order by 1, 2, 3
limit 10;

-- expected
SELECT  listid
        , listtime
        , CASE
            WHEN listtime::TIMESTAMP < DATE '2008-06-18'
                THEN - 1
            WHEN listtime::TIMESTAMP > DATE '2008-06-18'
                THEN 1
            ELSE 0
            END AS cmp
FROM listing
ORDER BY    1
            , 2
            , 3
LIMIT 10
;

-- result
"listid","listtime","cmp"
"1","2008-01-24 06:43:29.0","-1"
"2","2008-03-05 12:25:29.0","-1"
"3","2008-11-01 07:35:33.0","1"
"4","2008-05-24 01:18:37.0","-1"
"5","2008-05-17 02:29:11.0","-1"
"6","2008-08-15 02:08:13.0","1"
"7","2008-11-15 09:38:15.0","1"
"8","2008-11-09 05:07:30.0","1"
"9","2008-09-09 08:03:36.0","1"
"10","2008-06-17 09:44:54.0","-1"



-- provided
SELECT TIMESTAMP_CMP_TIMESTAMPTZ('2008-01-24 06:43:29', '2008-01-24 06:43:29+00') AS cmp1
    , TIMESTAMP_CMP_TIMESTAMPTZ('2008-01-24 06:43:29', '2008-02-18 02:36:48+00')  AS cmp2
    , TIMESTAMP_CMP_TIMESTAMPTZ('2008-02-18 02:36:48', '2008-01-24 06:43:29+00')  AS cmp3;

-- expected
SELECT  CASE
        WHEN TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000' < TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
            THEN - 1
        WHEN TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000' > TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
            THEN 1
        ELSE 0
        END AS cmp1
        , CASE
            WHEN TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000' < TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000'
                THEN - 1
            WHEN TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000' > TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000'
                THEN 1
            ELSE 0
            END AS cmp2
        , CASE
            WHEN TIMESTAMP WITHOUT TIME ZONE '2008-02-18T02:36:48.000' < TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
                THEN - 1
            WHEN TIMESTAMP WITHOUT TIME ZONE '2008-02-18T02:36:48.000' > TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
                THEN 1
            ELSE 0
            END AS cmp3
;

-- result
"cmp1","cmp2","cmp3"
"-1","-1","1"


-- provided
SELECT TIMESTAMPTZ_CMP('2008-01-24 06:43:29+00', '2008-01-24 06:43:29+00')  AS cmp1
    , TIMESTAMPTZ_CMP('2008-01-24 06:43:29+00', '2008-02-18 02:36:48+00')  AS cmp2
    , TIMESTAMPTZ_CMP('2008-02-18 02:36:48+00', '2008-01-24 06:43:29+00')  AS cmp3;

-- expected
SELECT  CASE
        WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' < TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
            THEN - 1
        WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' > TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
            THEN 1
        ELSE 0
        END AS cmp1
        , CASE
            WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' < TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000'
                THEN - 1
            WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' > TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000'
                THEN 1
            ELSE 0
            END AS cmp2
        , CASE
            WHEN TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000' < TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
                THEN - 1
            WHEN TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000' > TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000'
                THEN 1
            ELSE 0
            END AS cmp3
;

-- result
"cmp1","cmp2","cmp3"
"0","-1","1"


-- provided
select listid, CAST(listtime as timestamptz) as tstz,
timestamp_cmp_date(tstz, '2008-06-18') AS cmp
from listing
order by 1, 2, 3
limit 10;

-- expected
SELECT  listid
        ,  Cast( listtime AS TIMESTAMPTZ ) AS tstz
        , CASE
            WHEN tstz::TIMESTAMP < DATE '2008-06-18'
                THEN - 1
            WHEN tstz::TIMESTAMP > DATE '2008-06-18'
                THEN 1
            ELSE 0
            END AS cmp
FROM listing
ORDER BY    1
            , 2
            , 3
LIMIT 10
;

-- result
"listid","tstz","cmp"
"1","2008-01-24T06:43:29+07:00","-1"
"2","2008-03-05T12:25:29+07:00","-1"
"3","2008-11-01T07:35:33+07:00","1"
"4","2008-05-24T01:18:37+07:00","-1"
"5","2008-05-17T02:29:11+07:00","-1"
"6","2008-08-15T02:08:13+07:00","1"
"7","2008-11-15T09:38:15+07:00","1"
"8","2008-11-09T05:07:30+07:00","1"
"9","2008-09-09T08:03:36+07:00","1"
"10","2008-06-17T09:44:54+07:00","-1"



-- provided
SELECT TIMESTAMPTZ_CMP_TIMESTAMP('2008-01-24 06:43:29+00', '2008-01-24 06:43:29')  AS cmp1
, TIMESTAMPTZ_CMP_TIMESTAMP('2008-01-24 06:43:29+00', '2008-02-18 02:36:48')  AS cmp2
, TIMESTAMPTZ_CMP_TIMESTAMP('2008-02-18 02:36:48+00', '2008-01-24 06:43:29')  AS cmp3;

-- expected
SELECT  CASE
        WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' < TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000'
            THEN - 1
        WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' > TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000'
            THEN 1
        ELSE 0
        END AS cmp1
        , CASE
            WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' < TIMESTAMP WITHOUT TIME ZONE '2008-02-18T02:36:48.000'
                THEN - 1
            WHEN TIMESTAMP WITH TIME ZONE '2008-01-24T06:43:29.000+0000' > TIMESTAMP WITHOUT TIME ZONE '2008-02-18T02:36:48.000'
                THEN 1
            ELSE 0
            END AS cmp2
        , CASE
            WHEN TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000' < TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000'
                THEN - 1
            WHEN TIMESTAMP WITH TIME ZONE '2008-02-18T02:36:48.000+0000' > TIMESTAMP WITHOUT TIME ZONE '2008-01-24T06:43:29.000'
                THEN 1
            ELSE 0
            END AS cmp3
;

-- result
"cmp1","cmp2","cmp3"
"1","-1","1"


-- provided
SELECT TIMEZONE('PST', '2008-06-17 09:44:54') as tstz;

-- expected
SELECT TIMESTAMP WITHOUT TIME ZONE '2008-06-17T09:44:54.000' AT TIME ZONE 'PST' as tstz;

-- result
"tstz"
"2008-06-17T23:44:54+07:00"


-- provided
SELECT TIMEZONE('PST', timestamptz '2008-06-17 09:44:54+00' )  as tstz;

-- expected
SELECT TIMESTAMP WITH TIME ZONE '2008-06-17T09:44:54.000+0000' AT TIME ZONE 'PST' as tstz;

-- result
"tstz"
"2008-06-17 02:44:54.0"


-- provided
SELECT TO_TIMESTAMP('2017','YYYY') AS tstz;

-- expected
SELECT strptime('2017', '%Y')::TIMESTAMP AT TIME ZONE 'UTC' AS tstz;

-- result
"tstz"
"2017-01-01T07:00+07:00"


-- provided
SELECT TO_TIMESTAMP('2011-12-18 23:38:15', 'YYYY-MM-DD HH24:MI:SS')  AS tstz;

-- expected
SELECT STRPTIME('2011-12-18 23:38:15','%Y-%m-%d %H:%M:%S')::TIMESTAMP AT TIME ZONE 'UTC' AS TSTZ;

-- result
"tstz"
"2011-12-19T06:38:15+07:00"

