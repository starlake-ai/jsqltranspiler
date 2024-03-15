-- provided
SELECT TOP 10
        qtysold
        , sellerid
FROM sales
ORDER BY    qtysold DESC
            , sellerid
;

-- expected
SELECT  qtysold
        , sellerid
FROM sales
ORDER BY    qtysold DESC
            , sellerid
LIMIT 10
;

-- count
10

-- results
"qtysold","sellerid"
"8","518"
"8","520"
"8","574"
"8","718"
"8","868"
"8","2663"
"8","3396"
"8","3726"
"8","5250"
"8","6216"