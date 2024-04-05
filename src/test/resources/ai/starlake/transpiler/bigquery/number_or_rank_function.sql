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
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 2:59:01', 'F30-34')
SELECT name,
  finish_time,
  division,
  CUME_DIST() OVER (PARTITION BY division ORDER BY finish_time ASC) AS finish_rank
FROM finishers
order by 3, 2, 1
;

-- result
"name","finish_time","division","finish_rank"
"Sophia Liu","2016-10-18 02:51:45","F30-34","0.25"
"Meghan Lederer","2016-10-18 02:59:01","F30-34","0.75"
"Nikki Leith","2016-10-18 02:59:01","F30-34","0.75"
"Jen Edwards","2016-10-18 03:06:36","F30-34","1.0"
"Lisa Stelzner","2016-10-18 02:54:11","F35-39","0.25"
"Lauren Matthews","2016-10-18 03:01:17","F35-39","0.5"
"Desiree Berry","2016-10-18 03:05:42","F35-39","0.75"
"Suzy Slane","2016-10-18 03:06:24","F35-39","1.0"


-- provided
WITH Numbers AS
 (SELECT 1 as x
  UNION ALL SELECT 2
  UNION ALL SELECT 2
  UNION ALL SELECT 5
  UNION ALL SELECT 8
  UNION ALL SELECT 10
  UNION ALL SELECT 10
)
SELECT x,
  DENSE_RANK() OVER (ORDER BY x ASC) AS dense_rank
FROM Numbers
order by 1;

-- result
"x","dense_rank"
"1","1"
"2","2"
"2","2"
"5","3"
"8","4"
"10","5"
"10","5"


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
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 2:59:01', 'F30-34')
SELECT name,
  finish_time,
  division,
  DENSE_RANK() OVER (PARTITION BY division ORDER BY finish_time ASC) AS finish_rank
FROM finishers
order by 3,2,1;

-- result
"name","finish_time","division","finish_rank"
"Sophia Liu","2016-10-18 02:51:45","F30-34","1"
"Meghan Lederer","2016-10-18 02:59:01","F30-34","2"
"Nikki Leith","2016-10-18 02:59:01","F30-34","2"
"Jen Edwards","2016-10-18 03:06:36","F30-34","3"
"Lisa Stelzner","2016-10-18 02:54:11","F35-39","1"
"Lauren Matthews","2016-10-18 03:01:17","F35-39","2"
"Desiree Berry","2016-10-18 03:05:42","F35-39","3"
"Suzy Slane","2016-10-18 03:06:24","F35-39","4"


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
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 2:59:01', 'F30-34')
SELECT name,
  finish_time,
  division,
  PERCENT_RANK() OVER (PARTITION BY division ORDER BY finish_time ASC) AS finish_rank
FROM finishers
order by 3,2,1;

-- result
"name","finish_time","division","finish_rank"
"Sophia Liu","2016-10-18 02:51:45","F30-34","0.0"
"Meghan Lederer","2016-10-18 02:59:01","F30-34","0.3333333333333333"
"Nikki Leith","2016-10-18 02:59:01","F30-34","0.3333333333333333"
"Jen Edwards","2016-10-18 03:06:36","F30-34","1.0"
"Lisa Stelzner","2016-10-18 02:54:11","F35-39","0.0"
"Lauren Matthews","2016-10-18 03:01:17","F35-39","0.3333333333333333"
"Desiree Berry","2016-10-18 03:05:42","F35-39","0.6666666666666666"
"Suzy Slane","2016-10-18 03:06:24","F35-39","1.0"


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
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 2:59:01', 'F30-34')
SELECT name,
  finish_time,
  division,
  RANK() OVER (PARTITION BY division ORDER BY finish_time ASC) AS finish_rank
FROM finishers
order by 3,2,1;

-- result
"name","finish_time","division","finish_rank"
"Sophia Liu","2016-10-18 02:51:45","F30-34","1"
"Meghan Lederer","2016-10-18 02:59:01","F30-34","2"
"Nikki Leith","2016-10-18 02:59:01","F30-34","2"
"Jen Edwards","2016-10-18 03:06:36","F30-34","4"
"Lisa Stelzner","2016-10-18 02:54:11","F35-39","1"
"Lauren Matthews","2016-10-18 03:01:17","F35-39","2"
"Desiree Berry","2016-10-18 03:05:42","F35-39","3"
"Suzy Slane","2016-10-18 03:06:24","F35-39","4"


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
  UNION ALL SELECT 'Meghan Lederer', TIMESTAMP '2016-10-18 2:59:01', 'F30-34')
SELECT name,
  finish_time,
  division,
  ROW_NUMBER() OVER (PARTITION BY division ORDER BY finish_time ASC, name DESC) AS finish_rank
FROM finishers
order by 3,2,1;

-- result
"name","finish_time","division","finish_rank"
"Sophia Liu","2016-10-18 02:51:45","F30-34","1"
"Meghan Lederer","2016-10-18 02:59:01","F30-34","3"
"Nikki Leith","2016-10-18 02:59:01","F30-34","2"
"Jen Edwards","2016-10-18 03:06:36","F30-34","4"
"Lisa Stelzner","2016-10-18 02:54:11","F35-39","1"
"Lauren Matthews","2016-10-18 03:01:17","F35-39","2"
"Desiree Berry","2016-10-18 03:05:42","F35-39","3"
"Suzy Slane","2016-10-18 03:06:24","F35-39","4"


