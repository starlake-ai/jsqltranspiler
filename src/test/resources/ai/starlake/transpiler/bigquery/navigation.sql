-- provided
WITH finishers AS
 (SELECT 'Sophia Liu' as name,
  TIMESTAMP '2016-10-18 2:51:45' as finish_time,
  'F30-34' as division
  UNION ALL SELECT 'Lisa Stelzner', TIMESTAMP '2016-10-18 2:54:11', 'F35-39'
  UNION ALL SELECT 'Nikki Leith', TIMESTAMP '2016-10-18 2:59:01', 'F30-34'
  UNION ALL SELECT 'Lauren Matthews', TIMESTAMP '2016-10-18 3:01:17', 'F35-39'
  UNION ALL SELECT 'Desiree Berry', TIMESTAMP '2016-10-18 3:05:42', 'F35-39'
  UNION ALL SELECT 'Suzy Slane', TIMESTAMP '2016-10-18 3:06:24', 'F35-39'
  UNION ALL SELECT 'Jen Edwards', TIMESTAMP '2016-10-18 3:06:36', 'F30-34'
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 3:07:41', 'F30-34'
  UNION ALL SELECT 'Carly Forte', TIMESTAMP '2016-10-18 3:08:58', 'F25-29'
  UNION ALL SELECT 'Lauren Reasoner', TIMESTAMP '2016-10-18 3:10:14', 'F30-34')
SELECT name,
  FORMAT_TIMESTAMP('%X', finish_time) AS finish_time,
  division,
  FORMAT_TIMESTAMP('%X', fastest_time) AS fastest_time,
  TIMESTAMP_DIFF(finish_time, fastest_time, SECOND) AS delta_in_seconds
FROM (
  SELECT name,
  finish_time,
  division,
  FIRST_VALUE(finish_time)
    OVER (PARTITION BY division ORDER BY finish_time ASC
    ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS fastest_time
  FROM finishers)
order by 3,4,1
;


-- expected
WITH finishers AS (
        SELECT  'Sophia Liu' AS name
                , TIMESTAMP '2016-10-18 2:51:45' AS finish_time
                , 'F30-34' AS division
        UNION ALL
        SELECT  'Lisa Stelzner'
                , TIMESTAMP '2016-10-18 2:54:11'
                , 'F35-39'
        UNION ALL
        SELECT  'Nikki Leith'
                , TIMESTAMP '2016-10-18 2:59:01'
                , 'F30-34'
        UNION ALL
        SELECT  'Lauren Matthews'
                , TIMESTAMP '2016-10-18 3:01:17'
                , 'F35-39'
        UNION ALL
        SELECT  'Desiree Berry'
                , TIMESTAMP '2016-10-18 3:05:42'
                , 'F35-39'
        UNION ALL
        SELECT  'Suzy Slane'
                , TIMESTAMP '2016-10-18 3:06:24'
                , 'F35-39'
        UNION ALL
        SELECT  'Jen Edwards'
                , TIMESTAMP '2016-10-18 3:06:36'
                , 'F30-34'
        UNION ALL
        SELECT  'Meghan Lederer'
                , TIMESTAMP '2016-10-18 3:07:41'
                , 'F30-34'
        UNION ALL
        SELECT  'Carly Forte'
                , TIMESTAMP '2016-10-18 3:08:58'
                , 'F25-29'
        UNION ALL
        SELECT  'Lauren Reasoner'
                , TIMESTAMP '2016-10-18 3:10:14'
                , 'F30-34' )
SELECT  name
        , Strftime( finish_time, '%X' ) AS finish_time
        , division
        , Strftime( fastest_time, '%X' ) AS fastest_time
        , Date_Diff( 'SECOND', fastest_time, finish_time ) AS delta_in_seconds
FROM (  SELECT  name
                , finish_time
                , division
                , First(finish_time) OVER (PARTITION BY division ORDER BY finish_time ASC ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS fastest_time
        FROM finishers )
order by 3,4,1
;

-- result
"name","finish_time","division","fastest_time","delta_in_seconds"
"Carly Forte","03:08:58","F25-29","03:08:58","0"
"Jen Edwards","03:06:36","F30-34","02:51:45","891"
"Lauren Reasoner","03:10:14","F30-34","02:51:45","1109"
"Meghan Lederer","03:07:41","F30-34","02:51:45","956"
"Nikki Leith","02:59:01","F30-34","02:51:45","436"
"Sophia Liu","02:51:45","F30-34","02:51:45","0"
"Desiree Berry","03:05:42","F35-39","02:54:11","691"
"Lauren Matthews","03:01:17","F35-39","02:54:11","426"
"Lisa Stelzner","02:54:11","F35-39","02:54:11","0"
"Suzy Slane","03:06:24","F35-39","02:54:11","733"


-- provided
SELECT
  PERCENTILE_CONT(x, 0) OVER() AS min,
  PERCENTILE_CONT(x, 0.01) OVER() AS percentile1,
  PERCENTILE_CONT(x, 0.5) OVER() AS median,
  PERCENTILE_CONT(x, 0.9) OVER() AS percentile90,
  PERCENTILE_CONT(x, 1) OVER() AS max
FROM UNNEST([0, 3, NULL, 1, 2]) AS x LIMIT 1;

-- expected
SELECT  quantile_cont(x, 0) OVER () AS min
        , quantile_cont(x, 0.01) OVER () AS percentile1
        , quantile_cont(x, 0.5) OVER () AS median
        , quantile_cont(x, 0.9) OVER () AS percentile90
        , quantile_cont(x, 1) OVER () AS max
FROM (  SELECT Unnest( [0, 3, NULL, 1, 2] ) AS x  ) AS x
LIMIT 1
;

-- result
"min","percentile1","median","percentile90","max"
"0.0","0.03","1.5","2.7","3.0"


-- provided
SELECT
  x,
  PERCENTILE_DISC(x, 0) OVER() AS min,
  PERCENTILE_DISC(x, 0.5) OVER() AS median,
  PERCENTILE_DISC(x, 1) OVER() AS max
FROM UNNEST(['c', NULL, 'b', 'a']) AS x;

-- expected
SELECT
  x,
  Quantile_Disc(x, 0) OVER() AS min,
  Quantile_Disc(x, 0.5) OVER() AS median,
  Quantile_Disc(x, 1) OVER() AS max
FROM (SELECT UNNEST(['c', NULL, 'b', 'a']) AS x) AS x;

-- result
"x","min","median","max"
"c","a","b","c"
"","a","b","c"
"b","a","b","c"
"a","a","b","c"


-- provided
WITH finishers AS
 (SELECT 'Sophia Liu' as name,
  TIMESTAMP '2016-10-18 2:51:45' as finish_time,
  'F30-34' as division
  UNION ALL SELECT 'Lisa Stelzner', TIMESTAMP '2016-10-18 2:54:11', 'F35-39'
  UNION ALL SELECT 'Nikki Leith', TIMESTAMP '2016-10-18 2:59:01', 'F30-34'
  UNION ALL SELECT 'Lauren Matthews', TIMESTAMP '2016-10-18 3:01:17', 'F35-39'
  UNION ALL SELECT 'Desiree Berry', TIMESTAMP '2016-10-18 3:05:42', 'F35-39'
  UNION ALL SELECT 'Suzy Slane', TIMESTAMP '2016-10-18 3:06:24', 'F35-39'
  UNION ALL SELECT 'Jen Edwards', TIMESTAMP '2016-10-18 3:06:36', 'F30-34'
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 3:07:41', 'F30-34'
  UNION ALL SELECT 'Carly Forte', TIMESTAMP '2016-10-18 3:08:58', 'F25-29'
  UNION ALL SELECT 'Lauren Reasoner', TIMESTAMP '2016-10-18 3:10:14', 'F30-34')
SELECT name,
  finish_time,
  division,
  LAG(name)
    OVER (PARTITION BY division ORDER BY finish_time ASC) AS preceding_runner
FROM finishers
order by 3, 2, 1;

-- result
"name","finish_time","division","preceding_runner"
"Carly Forte","2016-10-18 03:08:58.0","F25-29",""
"Sophia Liu","2016-10-18 02:51:45.0","F30-34",""
"Nikki Leith","2016-10-18 02:59:01.0","F30-34","Sophia Liu"
"Jen Edwards","2016-10-18 03:06:36.0","F30-34","Nikki Leith"
"Meghan Lederer","2016-10-18 03:07:41.0","F30-34","Jen Edwards"
"Lauren Reasoner","2016-10-18 03:10:14.0","F30-34","Meghan Lederer"
"Lisa Stelzner","2016-10-18 02:54:11.0","F35-39",""
"Lauren Matthews","2016-10-18 03:01:17.0","F35-39","Lisa Stelzner"
"Desiree Berry","2016-10-18 03:05:42.0","F35-39","Lauren Matthews"
"Suzy Slane","2016-10-18 03:06:24.0","F35-39","Desiree Berry"


-- provided
WITH finishers AS
 (SELECT 'Sophia Liu' as name,
  TIMESTAMP '2016-10-18 2:51:45' as finish_time,
  'F30-34' as division
  UNION ALL SELECT 'Lisa Stelzner', TIMESTAMP '2016-10-18 2:54:11', 'F35-39'
  UNION ALL SELECT 'Nikki Leith', TIMESTAMP '2016-10-18 2:59:01', 'F30-34'
  UNION ALL SELECT 'Lauren Matthews', TIMESTAMP '2016-10-18 3:01:17', 'F35-39'
  UNION ALL SELECT 'Desiree Berry', TIMESTAMP '2016-10-18 3:05:42', 'F35-39'
  UNION ALL SELECT 'Suzy Slane', TIMESTAMP '2016-10-18 3:06:24', 'F35-39'
  UNION ALL SELECT 'Jen Edwards', TIMESTAMP '2016-10-18 3:06:36', 'F30-34'
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 3:07:41', 'F30-34'
  UNION ALL SELECT 'Carly Forte', TIMESTAMP '2016-10-18 3:08:58', 'F25-29'
  UNION ALL SELECT 'Lauren Reasoner', TIMESTAMP '2016-10-18 3:10:14', 'F30-34')
SELECT name,
  finish_time,
  division,
  LAG(name, 2, 'Nobody')
    OVER (PARTITION BY division ORDER BY finish_time ASC) AS two_runners_ahead
FROM finishers
order by 3,2,1;

-- result
"name","finish_time","division","two_runners_ahead"
"Carly Forte","2016-10-18 03:08:58.0","F25-29","Nobody"
"Sophia Liu","2016-10-18 02:51:45.0","F30-34","Nobody"
"Nikki Leith","2016-10-18 02:59:01.0","F30-34","Nobody"
"Jen Edwards","2016-10-18 03:06:36.0","F30-34","Sophia Liu"
"Meghan Lederer","2016-10-18 03:07:41.0","F30-34","Nikki Leith"
"Lauren Reasoner","2016-10-18 03:10:14.0","F30-34","Jen Edwards"
"Lisa Stelzner","2016-10-18 02:54:11.0","F35-39","Nobody"
"Lauren Matthews","2016-10-18 03:01:17.0","F35-39","Nobody"
"Desiree Berry","2016-10-18 03:05:42.0","F35-39","Lisa Stelzner"
"Suzy Slane","2016-10-18 03:06:24.0","F35-39","Lauren Matthews"

-- provided
WITH finishers AS
 (SELECT 'Sophia Liu' as name,
  TIMESTAMP '2016-10-18 2:51:45' as finish_time,
  'F30-34' as division
  UNION ALL SELECT 'Lisa Stelzner', TIMESTAMP '2016-10-18 2:54:11', 'F35-39'
  UNION ALL SELECT 'Nikki Leith', TIMESTAMP '2016-10-18 2:59:01', 'F30-34'
  UNION ALL SELECT 'Lauren Matthews', TIMESTAMP '2016-10-18 3:01:17', 'F35-39'
  UNION ALL SELECT 'Desiree Berry', TIMESTAMP '2016-10-18 3:05:42', 'F35-39'
  UNION ALL SELECT 'Suzy Slane', TIMESTAMP '2016-10-18 3:06:24', 'F35-39'
  UNION ALL SELECT 'Jen Edwards', TIMESTAMP '2016-10-18 3:06:36', 'F30-34'
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 3:07:41', 'F30-34'
  UNION ALL SELECT 'Carly Forte', TIMESTAMP '2016-10-18 3:08:58', 'F25-29'
  UNION ALL SELECT 'Lauren Reasoner', TIMESTAMP '2016-10-18 3:10:14', 'F30-34')
SELECT name,
  finish_time,
  division,
  LEAD(name, 2, 'Nobody')
    OVER (PARTITION BY division ORDER BY finish_time ASC) AS two_runners_back
FROM finishers
order by 3, 2, 1;

-- result
"name","finish_time","division","two_runners_back"
"Carly Forte","2016-10-18 03:08:58.0","F25-29","Nobody"
"Sophia Liu","2016-10-18 02:51:45.0","F30-34","Jen Edwards"
"Nikki Leith","2016-10-18 02:59:01.0","F30-34","Meghan Lederer"
"Jen Edwards","2016-10-18 03:06:36.0","F30-34","Lauren Reasoner"
"Meghan Lederer","2016-10-18 03:07:41.0","F30-34","Nobody"
"Lauren Reasoner","2016-10-18 03:10:14.0","F30-34","Nobody"
"Lisa Stelzner","2016-10-18 02:54:11.0","F35-39","Desiree Berry"
"Lauren Matthews","2016-10-18 03:01:17.0","F35-39","Suzy Slane"
"Desiree Berry","2016-10-18 03:05:42.0","F35-39","Nobody"
"Suzy Slane","2016-10-18 03:06:24.0","F35-39","Nobody"


-- provided
WITH finishers AS
 (SELECT 'Sophia Liu' as name,
  TIMESTAMP '2016-10-18 2:51:45' as finish_time,
  'F30-34' as division
  UNION ALL SELECT 'Lisa Stelzner', TIMESTAMP '2016-10-18 2:54:11', 'F35-39'
  UNION ALL SELECT 'Nikki Leith', TIMESTAMP '2016-10-18 2:59:01', 'F30-34'
  UNION ALL SELECT 'Lauren Matthews', TIMESTAMP '2016-10-18 3:01:17', 'F35-39'
  UNION ALL SELECT 'Desiree Berry', TIMESTAMP '2016-10-18 3:05:42', 'F35-39'
  UNION ALL SELECT 'Suzy Slane', TIMESTAMP '2016-10-18 3:06:24', 'F35-39'
  UNION ALL SELECT 'Jen Edwards', TIMESTAMP '2016-10-18 3:06:36', 'F30-34'
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 3:07:41', 'F30-34'
  UNION ALL SELECT 'Carly Forte', TIMESTAMP '2016-10-18 3:08:58', 'F25-29'
  UNION ALL SELECT 'Lauren Reasoner', TIMESTAMP '2016-10-18 3:10:14', 'F30-34')
SELECT name,
  FORMAT_TIMESTAMP('%X', finish_time) AS finish_time,
  division,
  FORMAT_TIMESTAMP('%X', fastest_time) AS fastest_time,
  FORMAT_TIMESTAMP('%X', second_fastest) AS second_fastest
FROM (
  SELECT name,
  finish_time,
  division,finishers,
  FIRST_VALUE(finish_time)
    OVER w1 AS fastest_time,
  NTH_VALUE(finish_time, 2)
    OVER w1 as second_fastest
  FROM finishers
  WINDOW w1 AS (
    PARTITION BY division ORDER BY finish_time ASC
    ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING))
order by 3, 2, 1
;

-- expected
WITH finishers AS (
        SELECT  'Sophia Liu' AS name
                , TIMESTAMP '2016-10-18 2:51:45' AS finish_time
                , 'F30-34' AS division
        UNION ALL
        SELECT  'Lisa Stelzner'
                , TIMESTAMP '2016-10-18 2:54:11'
                , 'F35-39'
        UNION ALL
        SELECT  'Nikki Leith'
                , TIMESTAMP '2016-10-18 2:59:01'
                , 'F30-34'
        UNION ALL
        SELECT  'Lauren Matthews'
                , TIMESTAMP '2016-10-18 3:01:17'
                , 'F35-39'
        UNION ALL
        SELECT  'Desiree Berry'
                , TIMESTAMP '2016-10-18 3:05:42'
                , 'F35-39'
        UNION ALL
        SELECT  'Suzy Slane'
                , TIMESTAMP '2016-10-18 3:06:24'
                , 'F35-39'
        UNION ALL
        SELECT  'Jen Edwards'
                , TIMESTAMP '2016-10-18 3:06:36'
                , 'F30-34'
        UNION ALL
        SELECT  'Meghan Lederer'
                , TIMESTAMP '2016-10-18 3:07:41'
                , 'F30-34'
        UNION ALL
        SELECT  'Carly Forte'
                , TIMESTAMP '2016-10-18 3:08:58'
                , 'F25-29'
        UNION ALL
        SELECT  'Lauren Reasoner'
                , TIMESTAMP '2016-10-18 3:10:14'
                , 'F30-34' )
SELECT  name
        , Strftime( finish_time, '%X' ) AS finish_time
        , division
        , Strftime( fastest_time, '%X' ) AS fastest_time
        , Strftime( second_fastest, '%X' ) AS second_fastest
FROM (  SELECT  name
                , finish_time
                , division
                , finishers
                , First(finish_time) OVER w1 AS fastest_time
                , NTH_VALUE(finish_time, 2) OVER w1 AS second_fastest
        FROM finishers
        WINDOW w1 AS (
            PARTITION BY division ORDER BY finish_time ASC
            ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING))
order by 3, 2, 1
;

-- result
"name","finish_time","division","fastest_time","second_fastest"
"Carly Forte","03:08:58","F25-29","03:08:58",""
"Sophia Liu","02:51:45","F30-34","02:51:45","02:59:01"
"Nikki Leith","02:59:01","F30-34","02:51:45","02:59:01"
"Jen Edwards","03:06:36","F30-34","02:51:45","02:59:01"
"Meghan Lederer","03:07:41","F30-34","02:51:45","02:59:01"
"Lauren Reasoner","03:10:14","F30-34","02:51:45","02:59:01"
"Lisa Stelzner","02:54:11","F35-39","02:54:11","03:01:17"
"Lauren Matthews","03:01:17","F35-39","02:54:11","03:01:17"
"Desiree Berry","03:05:42","F35-39","02:54:11","03:01:17"
"Suzy Slane","03:06:24","F35-39","02:54:11","03:01:17"


