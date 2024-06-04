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

INSERT INTO tmp
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

DELETE FROM tmp
WHERE ( qtysold, sellerid ) IN (    SELECT TOP 10
                                        qtysold
                                        , sellerid
                                    FROM (  SELECT TOP 4
                                                qtysold
                                                , sellerid
                                            FROM sales
                                            ORDER BY    qtysold DESC
                                                        , sellerid ) a
                                    ORDER BY    qtysold DESC
                                                , sellerid )
;

UPDATE tmp
SET qtysold = 0
WHERE ( qtysold, sellerid ) IN (    SELECT TOP 10
                                        qtysold
                                        , sellerid
                                    FROM (  SELECT TOP 4
                                                qtysold
                                                , sellerid
                                            FROM sales
                                            ORDER BY    qtysold DESC
                                                        , sellerid ) a
                                    ORDER BY    qtysold DESC
                                                , sellerid )
;

MERGE INTO tmp
    USING ( SELECT TOP 10
                qtysold
                , sellerid
            FROM (  SELECT TOP 4
                        qtysold
                        , sellerid
                    FROM sales
                    ORDER BY    qtysold DESC
                                , sellerid ) a
            ORDER BY    qtysold DESC
                        , sellerid ) s
        ON ( tmp.sellerid = s.sellerid )
WHEN MATCHED THEN
    UPDATE SET  qtysold = 0
;