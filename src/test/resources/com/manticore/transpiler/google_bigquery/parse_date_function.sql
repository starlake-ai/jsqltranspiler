-- provided
SELECT PARSE_DATE('%A %b %e %Y', 'Thursday Dec 25 2008')  AS date
;

-- expected
SELECT strptime('Thursday Dec 25 2008', '%A %b %-d %Y') AS date
;

-- count
1

-- result
"date"
"2008-12-25"