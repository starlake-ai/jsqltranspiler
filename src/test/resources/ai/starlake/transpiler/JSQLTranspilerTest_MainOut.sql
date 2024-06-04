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
FROM (  SELECT  qtysold
                , sellerid
        FROM sales
        ORDER BY    qtysold DESC
                    , sellerid
        LIMIT 4 ) a
ORDER BY    qtysold DESC
            , sellerid
LIMIT 10
;

INSERT INTO tmp
SELECT  qtysold
        , sellerid
FROM (  SELECT  qtysold
                , sellerid
        FROM sales
        ORDER BY    qtysold DESC
                    , sellerid
        LIMIT 4 ) a
ORDER BY    qtysold DESC
            , sellerid
LIMIT 10
;

DELETE FROM tmp
WHERE ( qtysold, sellerid ) IN (    SELECT  qtysold
                                            , sellerid
                                    FROM (  SELECT  qtysold
                                                    , sellerid
                                            FROM sales
                                            ORDER BY    qtysold DESC
                                                        , sellerid
                                            LIMIT 4 ) a
                                    ORDER BY    qtysold DESC
                                                , sellerid
                                    LIMIT 10 )
;

UPDATE tmp
SET qtysold = 0
WHERE ( qtysold, sellerid ) IN (    SELECT  qtysold
                                            , sellerid
                                    FROM (  SELECT  qtysold
                                                    , sellerid
                                            FROM sales
                                            ORDER BY    qtysold DESC
                                                        , sellerid
                                            LIMIT 4 ) a
                                    ORDER BY    qtysold DESC
                                                , sellerid
                                    LIMIT 10 )
;

MERGE INTO tmp
    USING ( SELECT  qtysold
                    , sellerid
            FROM (  SELECT  qtysold
                            , sellerid
                    FROM sales
                    ORDER BY    qtysold DESC
                                , sellerid
                    LIMIT 4 ) a
            ORDER BY    qtysold DESC
                        , sellerid
            LIMIT 10 ) s
        ON ( tmp.sellerid = s.sellerid )
WHEN MATCHED THEN
    UPDATE SET  qtysold = 0
;