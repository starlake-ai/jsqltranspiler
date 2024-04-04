-- provided
SELECT LAST_DAY(DATE '2008-11-25', MONTH) AS last_day
;

-- expected
SELECT LAST_DAY(DATE '2008-11-25') AS last_day
;

-- count
1

-- result
"last_day"
"2008-11-30"

-- provided
SELECT LAST_DAY(DATE '2008-11-25') AS last_day
;

-- expected
SELECT LAST_DAY(DATE '2008-11-25') AS last_day
;

-- count
1

-- result
"last_day"
"2008-11-30"

