-- provided
SELECT PARSE_DATE('%A %b %e %Y', 'Thursday Dec 25 2008')  AS date
;

-- expected
SELECT Cast(strptime('Thursday Dec 25 2008', '%A %b %-d %Y') AS DATE) AS date
;

-- result
"date"
"2008-12-25"


-- provided
SELECT PARSE_DATETIME('%Y-%m-%d %H:%M:%S', '1998-10-18 13:45:55') AS datetime
;

-- expected
SELECT Cast(strptime('1998-10-18 13:45:55', '%Y-%m-%d %H:%M:%S') AS DATETIME) AS datetime
;

-- result
"datetime"
"1998-10-18 13:45:55.0"


-- provided
SELECT PARSE_DATETIME('%m/%d/%Y %I:%M:%S %p', '8/30/2018 2:23:38 pm') AS datetime;

-- expected
SELECT Cast(strptime('8/30/2018 2:23:38 pm', '%m/%d/%Y %I:%M:%S %p') AS DATETIME) AS datetime
;

-- result
"datetime"
"2018-08-30 14:23:38.0"


-- provided
SELECT PARSE_DATETIME('%A, %B %e, %Y','Wednesday, December 19, 2018') AS datetime
;

-- expected
SELECT Cast(strptime('Wednesday, December 19, 2018', '%A, %B %-d, %Y') AS DATETIME) AS datetime
;

-- result
"datetime"
"2018-12-19 00:00:00.0"


-- provided
SELECT PARSE_TIME('%H', '15') as parsed_time;

-- expected
SELECT Cast(strptime('15', '%H') AS TIME) AS parsed_time
;

-- result
"parsed_time"
"15:00:00"


-- provided
SELECT PARSE_TIME('%I:%M:%S %p', '2:23:38 pm') AS parsed_time;

-- expected
SELECT Cast(strptime('2:23:38 pm', '%I:%M:%S %p') AS TIME) AS parsed_time
;

-- result
"parsed_time"
"14:23:38"


-- provided
SELECT PARSE_TIMESTAMP('%c', 'Thu Dec 25 07:30:00 2008') AS parsed;

-- expected
SELECT Cast(strptime('Thu Dec 25 07:30:00 2008', '%a %b %-d %-H:%M:%S %Y') AS TIMESTAMP) AS parsed
;

-- result
"parsed"
"2008-12-25 07:30:00.0"

