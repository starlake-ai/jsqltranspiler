-- provided
SELECT UNIX_DATE(DATE '2008-12-25') AS days_from_epoch
;

-- expected
SELECT DATE_DIFF('DAY', DATE '1970-01-01', DATE '2008-12-25') AS days_from_epoch
;

-- count
1

-- result
"days_from_epoch"
"14238"


-- provided
SELECT DATE_FROM_UNIX_DATE(14238) AS date_from_epoch
;

-- expected
SELECT DATE_ADD(DATE '1970-01-01', INTERVAL '14238 DAY') AS date_from_epoch
;

-- count
1

-- result
"date_from_epoch"
"2008-12-25"
