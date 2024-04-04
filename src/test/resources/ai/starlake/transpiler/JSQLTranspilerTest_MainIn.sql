SELECT Nvl( NULL, 1 ) a
;

SELECT TOP 10
        qtysold
        , sellerid
FROM sales
ORDER BY    qtysold DESC
            , sellerid
;

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