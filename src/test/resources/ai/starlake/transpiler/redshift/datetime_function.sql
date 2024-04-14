-- provided
select add_months('2008-01-01 05:07:30', 1) AS added;

-- expected
select date_add(TIMESTAMP '2008-01-01 05:07:30', (1 || ' MONTH')::INTERVAL ) AS added;

-- result
"added"
"2008-02-01 05:07:30"


-- provided
SELECT TIMESTAMPTZ '2001-02-16 20:38:40-05' AT TIME ZONE 'MST' tstz;

-- result
"tstz"
"2001-02-16 18:38:40"


-- provided
select convert_timezone('America/New_York', '2013-02-01 08:00:00') AS converted;

-- expected
select TIMESTAMP '2013-02-01 08:00:00' AT TIME ZONE 'UTC' AT TIME ZONE 'America/New_York' AS converted;

-- result
"converted"
"2013-02-01 03:00:00"


-- provided
select listtime, convert_timezone('US/Pacific', listtime)  AS converted from listing
where listid = 16;

-- expected
select listtime, listtime AT TIME ZONE 'UTC' AT TIME ZONE 'US/Pacific' AS converted
from listing
where listid = 16;

-- result
"listtime","converted"
"2008-08-24 09:36:12","2008-08-24 02:36:12"


-- provided
select caldate, '2008-01-04',
date_cmp(caldate,'2008-01-04') AS date_cmp
from date
order by dateid
limit 10;

-- expected
select caldate, '2008-01-04',
case when caldate < '2008-01-04'::DATE then -1 when caldate > '2008-01-04'::DATE then 1 else 0 end AS date_cmp
from date
order by dateid
limit 10;

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
            WHEN '2008-06-18' <  Cast( listtime AS TIMESTAMPTZ )::TIMESTAMPTZ
                THEN - 1
            WHEN '2008-06-18' >  Cast( listtime AS TIMESTAMPTZ )::TIMESTAMPTZ
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
"1","2008-06-18","2008-01-23T23:43:29Z","1"
"2","2008-06-18","2008-03-05T05:25:29Z","1"
"3","2008-06-18","2008-11-01T00:35:33Z","-1"
"4","2008-06-18","2008-05-23T18:18:37Z","1"
"5","2008-06-18","2008-05-16T19:29:11Z","1"
"6","2008-06-18","2008-08-14T19:08:13Z","-1"
"7","2008-06-18","2008-11-15T02:38:15Z","-1"
"8","2008-06-18","2008-11-08T22:07:30Z","-1"
"9","2008-06-18","2008-09-09T01:03:36Z","-1"
"10","2008-06-18","2008-06-17T02:44:54Z","1"


-- provided
select dateadd(day,30,caldate) as novplus30
from date
where month='NOV'
order by dateid;


-- expected
select date_add(caldate,  (30 ||' day')::INTERVAL) as novplus30
from date
where month='NOV'
order by dateid;

-- result
"novplus30"
"2008-12-01 00:00:00"
"2008-12-02 00:00:00"
"2008-12-03 00:00:00"
"2008-12-04 00:00:00"
"2008-12-05 00:00:00"
"2008-12-06 00:00:00"
"2008-12-07 00:00:00"
"2008-12-08 00:00:00"
"2008-12-09 00:00:00"
"2008-12-10 00:00:00"
"2008-12-11 00:00:00"
"2008-12-12 00:00:00"
"2008-12-13 00:00:00"
"2008-12-14 00:00:00"
"2008-12-15 00:00:00"
"2008-12-16 00:00:00"
"2008-12-17 00:00:00"
"2008-12-18 00:00:00"
"2008-12-19 00:00:00"
"2008-12-20 00:00:00"
"2008-12-21 00:00:00"
"2008-12-22 00:00:00"
"2008-12-23 00:00:00"
"2008-12-24 00:00:00"
"2008-12-25 00:00:00"
"2008-12-26 00:00:00"
"2008-12-27 00:00:00"
"2008-12-28 00:00:00"
"2008-12-29 00:00:00"
"2008-12-30 00:00:00"


-- provided
select datediff(week,'2009-01-01','2009-12-31') as numweeks;

-- expected
select date_diff('week',DATE '2009-01-01', DATE '2009-12-31') as numweeks;

-- result
"numweeks"
"52"


