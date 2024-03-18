-- provided
SELECT FORMAT_DATE('%b-%d-%Y', DATE '2008-12-25') AS formatted
;

-- expected
SELECT strftime(DATE '2008-12-25', '%b-%d-%Y') AS formatted
;

-- count
1

-- result
"formatted"
"Dec-25-2008"
