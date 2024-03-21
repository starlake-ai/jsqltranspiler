-- provided
SELECT DATE_DIFF(DATE '2010-07-07', DATE '2008-12-25', DAY) AS days_diff;

-- expected
SELECT DATE_DIFF('DAY', DATE '2008-12-25', DATE '2010-07-07') AS days_diff;

-- count
1

-- result
"days_diff"
"559"

-- provided
SELECT
  DATE_DIFF(DATE '2017-10-15', DATE '2017-10-14', DAY) AS days_diff,
  DATE_DIFF(DATE '2017-10-15', DATE '2017-10-14', WEEK) AS weeks_diff;


-- expected
SELECT
  DATE_DIFF('DAY', DATE '2017-10-14', DATE '2017-10-15') AS days_diff,
  /*APPROXIMATION: WEEK*/ DATE_DIFF('WEEK', DATE '2017-10-14', DATE '2017-10-15') AS weeks_diff;

-- count
1

-- result
"days_diff","weeks_diff"
"1","0"

-- provided
SELECT
  DATE_DIFF('2017-12-30', '2014-12-30', YEAR) AS year_diff,
  DATE_DIFF('2017-12-30', '2014-12-30', ISOYEAR) AS isoyear_diff;

-- expected
SELECT
  DATE_DIFF('YEAR', DATE '2014-12-30', DATE '2017-12-30' ) AS year_diff,
  DATE_DIFF('ISOYEAR', DATE '2014-12-30', DATE '2017-12-30') AS isoyear_diff;


-- count
1

-- result
"year_diff","isoyear_diff"
"3","2"


-- provided
SELECT
  DATE_DIFF('2017-12-18', '2017-12-17', WEEK) AS week_diff,
  DATE_DIFF('2017-12-18', '2017-12-17', WEEK(MONDAY)) AS week_weekday_diff,
  DATE_DIFF('2017-12-18', '2017-12-17', ISOWEEK) AS isoweek_diff;

-- expected
SELECT
  /*APPROXIMATION: WEEK*/ DATE_DIFF(  'WEEK', DATE '2017-12-17', DATE '2017-12-18' ) AS week_diff,
  /*APPROXIMATION: WEEK*/ DATE_DIFF( 'WEEK', DATE '2017-12-17', DATE '2017-12-18' ) AS week_weekday_diff,
  DATE_DIFF( 'WEEK', DATE '2017-12-17', DATE '2017-12-18' ) AS isoweek_diff;

-- count
1

-- result
"week_diff","week_weekday_diff","isoweek_diff"
"1","1","1"


-- provided
SELECT DATETIME_DIFF(DATETIME '2010-07-07 10:20:00',
    DATETIME '2008-12-25 15:30:00', DAY) as difference;

-- expected
SELECT DATE_DIFF('DAY', DATETIME '2008-12-25 15:30:00', DATETIME '2010-07-07 10:20:00' ) as difference;

-- result
"difference"
"559"

-- provided
SELECT TIME_DIFF(TIME '15:30:00', TIME '14:35:00', MINUTE) as difference;

-- expected
SELECT DATE_DIFF('MINUTE', TIME '14:35:00', TIME '15:30:00' ) as difference;

-- result
"difference"
"55"