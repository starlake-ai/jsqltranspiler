-- provided
SELECT TOP 10
        qtysold
        , sellerid
FROM (  SELECT TOP 4
                qtysold
                , sellerid
        FROM sales
        ORDER BY    qtysold DESC
                    , sellerid ) a
ORDER BY    qtysold DESC
            , sellerid
;

-- expected
SELECT  qtysold
        , sellerid
FROM (  SELECT qtysold
                , sellerid
        FROM sales
        ORDER BY    qtysold DESC
                    , sellerid
        LIMIT 4 ) a
ORDER BY    qtysold DESC
            , sellerid
LIMIT 10
;

-- count
4

-- results
"qtysold","sellerid"
"8","518"
"8","520"
"8","574"
"8","718"

