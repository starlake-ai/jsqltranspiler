-- provided
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL 5 DAY) AS five_days_later
;

-- expected
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL '5 DAY') AS five_days_later
;

-- count
1

-- result
"five_days_later"
"2008-12-30 00:00:00"


-- provided
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL -5 DAY) AS five_days_ago
;

-- expected
SELECT DATE_ADD(DATE '2008-12-25', INTERVAL '-5 DAY') AS five_days_ago
;

-- count
1

-- result
"five_days_ago"
"2008-12-20 00:00:00"
