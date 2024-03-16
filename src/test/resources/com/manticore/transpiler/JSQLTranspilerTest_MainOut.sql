SELECT Coalesce( NULL, 1 ) a
;

SELECT  qtysold
        , sellerid
FROM sales
ORDER BY    qtysold DESC
            , sellerid
LIMIT 10
;

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