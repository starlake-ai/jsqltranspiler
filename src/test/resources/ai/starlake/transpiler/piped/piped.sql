-- provided
FROM (SELECT 'apples' AS item, 2 AS sales)
|> SELECT item AS fruit_name;

-- expected
SELECT item AS fruit_name
FROM (SELECT 'apples' AS item, 2 AS sales);

-- results
"fruit_name"
"apples"


-- provided
(
  SELECT 'apples' AS item, 2 AS sales
  UNION ALL
  SELECT 'carrots' AS item, 8 AS sales
)
|> EXTEND item IN ('carrots', 'oranges') AS is_orange;

-- expected
select *, item IN ('carrots', 'oranges') AS is_orange
from (
       SELECT 'apples' AS item, 2 AS sales
       UNION ALL
       SELECT 'carrots' AS item, 8 AS sales
     );

-- result
"item","sales","is_orange"
"apples","2","false"
"carrots","8","true"


-- provided
(
  SELECT 'apples' AS item, 2 AS sales
  UNION ALL
  SELECT 'bananas' AS item, 5 AS sales
  UNION ALL
  SELECT 'carrots' AS item, 8 AS sales
)
|> EXTEND SUM(sales) OVER() AS total_sales;

-- expected
SELECT *, SUM(sales) OVER() AS total_sales
FROM (
       SELECT 'apples' AS item, 2 AS sales
       UNION ALL
       SELECT 'bananas' AS item, 5 AS sales
       UNION ALL
       SELECT 'carrots' AS item, 8 AS sales
     );

-- result
"item","sales","total_sales"
"apples","2","15"
"bananas","5","15"
"carrots","8","15"


-- provided
(
  SELECT 'apples' AS item, 2 AS sales
  UNION ALL
  SELECT 'bananas' AS item, 5 AS sales
)
|> AS produce_sales
|> LEFT JOIN
     (
       SELECT 'apples' AS item, 123 AS id
     ) AS produce_data
   ON produce_sales.item = produce_data.item
|> SELECT produce_sales.item, sales, id;

-- expected
SELECT produce_sales.item, sales, id
FROM (
       SELECT 'apples' AS item, 2 AS sales
       UNION ALL
       SELECT 'bananas' AS item, 5 AS sales
     ) AS produce_sales
     LEFT JOIN
            (
              SELECT 'apples' AS item, 123 AS id
            ) AS produce_data
          ON produce_sales.item = produce_data.item;

-- result
"item","sales","id"
"apples","2","123"
"bananas","5",""


-- provided
(
  SELECT '000123' AS id, 'apples' AS item, 2 AS sales
  UNION ALL
  SELECT '000456' AS id, 'bananas' AS item, 5 AS sales
) AS sales_table
|> AGGREGATE SUM(sales) AS total_sales GROUP BY id, item
|> AS t1
|> JOIN (SELECT 456 AS id, 'yellow' AS color) AS t2
   ON CAST(t1.id AS INT64) = t2.id
|> SELECT t2.id, total_sales, color;

-- expected
SELECT t2.id
       , total_sales
       , color
FROM (  SELECT  Sum( sales ) AS total_sales
                , id
                , item
        FROM (  SELECT  '000123' AS id
                        , 'apples' AS item
                        , 2 AS sales
                UNION ALL
                SELECT  '000456' AS id
                        , 'bananas' AS item
                        , 5 AS sales  ) AS sales_table
        GROUP BY    id
                    , item ) AS t1
    JOIN (  SELECT  456 AS id
                    , 'yellow' AS color  ) AS t2
        ON  Cast( t1.id AS INT64 ) = t2.id
;


-- result
"id","total_sales","color"
"456","5","yellow"


-- provided
(
  SELECT 'apples' AS item, 2 AS sales
  UNION ALL
  SELECT 'bananas' AS item, 5 AS sales
  UNION ALL
  SELECT 'carrots' AS item, 8 AS sales
)
|> WHERE sales >= 3;

-- expected
SELECT *
FROM (
       SELECT 'apples' AS item, 2 AS sales
       UNION ALL
       SELECT 'bananas' AS item, 5 AS sales
       UNION ALL
       SELECT 'carrots' AS item, 8 AS sales
     )
WHERE sales >= 3;

-- result
"item","sales"
"bananas","5"
"carrots","8"


-- provided
(
  SELECT 'apples' AS item, 2 AS sales
  UNION ALL
  SELECT 'bananas' AS item, 5 AS sales
  UNION ALL
  SELECT 'carrots' AS item, 8 AS sales
)
|> ORDER BY item
|> LIMIT 1;

-- expected
SELECT *
FROM (
       SELECT 'apples' AS item, 2 AS sales
       UNION ALL
       SELECT 'bananas' AS item, 5 AS sales
       UNION ALL
       SELECT 'carrots' AS item, 8 AS sales
     )
ORDER BY item
LIMIT 1;

-- result
"item","sales"
"apples","2"

-- provided
(
  SELECT 1 AS x, 11 AS y
  UNION ALL
  SELECT 2 AS x, 22 AS y
)
|> SET x = x * x, y = 3;

-- expected
SELECT * REPLACE ( x * x AS x,  3 AS y )
FROM (
       SELECT 1 AS x, 11 AS y
       UNION ALL
       SELECT 2 AS x, 22 AS y
     );

-- results
"x","y"
"1","3"
"4","3"


-- provided
SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
|> DROP sales, category;

-- expected
select * EXCLUDE(sales, category)
FROM (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
);

-- result
"item"
"apples"


-- provided
FROM (SELECT 1 AS x, 2 AS y) AS t
|> DROP x
|> SELECT y;

-- expected
SELECT y FROM (
    SELECT * EXCLUDE(x)
    FROM (SELECT 1 AS x, 2 AS y) AS t
);

-- result
"y"
"2"


-- provided
SELECT 1 AS one_digit, 10 AS two_digit
|> UNION ALL
    (SELECT 20 AS two_digit, 2 AS one_digit);

-- expected
SELECT *
FROM (
    SELECT 1 AS one_digit, 10 AS two_digit
    UNION ALL
    SELECT 20 AS two_digit, 2 AS one_digit
)
;

-- result
"one_digit","two_digit"
"1","10"
"20","2"


-- provided
SELECT * FROM UNNEST(ARRAY[1, 2, 3, 3, 4]) AS number
|> INTERSECT DISTINCT
    (SELECT * FROM UNNEST(ARRAY[2, 3, 3, 5]) AS number),
    (SELECT * FROM UNNEST(ARRAY[3, 3, 4, 5]) AS number);

-- expected
SELECT *
FROM (  SELECT *
        FROM (  SELECT Unnest(  ARRAY   [ 1, 2, 3, 3, 4] ) AS number  ) AS number
        INTERSECT DISTINCT
        SELECT *
        FROM (  SELECT Unnest(  ARRAY   [ 2, 3, 3, 5] ) AS number  ) AS number
        INTERSECT DISTINCT
        SELECT *
        FROM (  SELECT Unnest(  ARRAY   [ 3, 3, 4, 5] ) AS number  ) AS number  )
;

-- result
"number"
"3"


-- provided
SELECT * FROM UNNEST(ARRAY[1, 2, 3, 3, 4]) AS number
|> EXCEPT DISTINCT
(
  SELECT * FROM UNNEST(ARRAY[1, 2]) AS number
  |> EXCEPT DISTINCT
      (SELECT * FROM UNNEST(ARRAY[1, 4]) AS number)
)
|> ORDER BY 1
;

-- expected
SELECT *
FROM (  SELECT *
        FROM (  SELECT Unnest(  ARRAY   [ 1, 2, 3, 3, 4] ) AS number  ) AS number
        EXCEPT DISTINCT
        SELECT *
        FROM (  SELECT *
                FROM (  SELECT Unnest(  ARRAY   [ 1, 2] ) AS number  ) AS number
                EXCEPT DISTINCT
                SELECT *
                FROM (  SELECT Unnest(  ARRAY   [ 1, 4] ) AS number  ) AS number  ) )
ORDER BY 1
;

-- result
"number"
"1"
"3"
"4"


-- provided
(
  SELECT 'apples' AS item, 2 AS sales
  UNION ALL
  SELECT 'bananas' AS item, 5 AS sales
  UNION ALL
  SELECT 'carrots' AS item, 8 AS sales
)
|> WINDOW SUM(sales) OVER() AS total_sales;


-- expected
SELECT *, SUM(sales) OVER() AS total_sales
FROM (
       SELECT 'apples' AS item, 2 AS sales
       UNION ALL
       SELECT 'bananas' AS item, 5 AS sales
       UNION ALL
       SELECT 'carrots' AS item, 8 AS sales
     )
;

-- result
"item","sales","total_sales"
"apples","2","15"
"bananas","5","15"
"carrots","8","15"


-- provided
(
  SELECT 'kale' AS product, 51 AS sales, 'Q1' AS quarter
  UNION ALL
  SELECT 'kale' AS product, 4 AS sales, 'Q1' AS quarter
  UNION ALL
  SELECT 'kale' AS product, 45 AS sales, 'Q2' AS quarter
  UNION ALL
  SELECT 'apple' AS product, 8 AS sales, 'Q1' AS quarter
  UNION ALL
  SELECT 'apple' AS product, 10 AS sales, 'Q2' AS quarter
)
|> PIVOT(SUM(sales) FOR quarter IN ('Q1', 'Q2'))
|> ORDER BY 1
;

-- expected
SELECT *
FROM (
       SELECT 'kale' AS product, 51 AS sales, 'Q1' AS quarter
       UNION ALL
       SELECT 'kale' AS product, 4 AS sales, 'Q1' AS quarter
       UNION ALL
       SELECT 'kale' AS product, 45 AS sales, 'Q2' AS quarter
       UNION ALL
       SELECT 'apple' AS product, 8 AS sales, 'Q1' AS quarter
       UNION ALL
       SELECT 'apple' AS product, 10 AS sales, 'Q2' AS quarter
     )
PIVOT(SUM(sales) FOR quarter IN ('Q1', 'Q2'))
ORDER BY 1
;

-- result
"product","Q1","Q2"
"apple","8","10"
"kale","55","45"


-- provided
(
  SELECT 'kale' as product, 55 AS Q1, 45 AS Q2
  UNION ALL
  SELECT 'apple', 8, 10
)
|> UNPIVOT(sales FOR quarter IN (Q1, Q2))
|> ORDER BY 1;

-- expected
SELECT *
FROM (
       SELECT 'kale' as product, 55 AS Q1, 45 AS Q2
       UNION ALL
       SELECT 'apple', 8, 10
     )
UNPIVOT(sales FOR quarter IN (Q1, Q2))
ORDER BY 1
;

-- result
"product","quarter","sales"
"apple","Q1","8"
"apple","Q2","10"
"kale","Q1","55"
"kale","Q2","45"


-- provided
FROM sales
|> TABLESAMPLE SYSTEM (0.0 PERCENT);

-- expected
SELECT *
FROM sales
USING SAMPLE SYSTEM (0.0 PERCENT);

-- result
"salesid","listid","sellerid","buyerid","eventid","dateid","qtysold","pricepaid","commission","saletime"


-- provided
(
  SELECT 'kale' as product, 55 AS Q1, 45 AS Q2
  UNION ALL
  SELECT 'apple', 8, 10
)
|> UNPIVOT(sales FOR quarter IN (Q1, Q2))
|> ORDER BY 1
|> SELECT DISTINCT product
|> ORDER BY 1
;

-- expected
SELECT DISTINCT product
FROM (
    SELECT *
    FROM (
           SELECT 'kale' as product, 55 AS Q1, 45 AS Q2
           UNION ALL
           SELECT 'apple', 8, 10
         )
    UNPIVOT(sales FOR quarter IN (Q1, Q2))
    ORDER BY 1
)
ORDER BY 1
;

-- result
"product"
"apple"
"kale"


-- provided
FROM customer
|> LEFT OUTER JOIN orders ON c_custkey = o_custkey AND o_comment NOT LIKE '%unusual%packages%'
|> AGGREGATE COUNT(o_orderkey) c_count GROUP BY c_custkey
|> AGGREGATE COUNT(*) AS custdist GROUP BY c_count
|> ORDER BY custdist DESC, c_count DESC;

-- expected
select COUNT(*) AS custdist, c_count
from (
    select COUNT(o_orderkey) c_count, c_custkey
    FROM customer
    LEFT OUTER JOIN orders ON c_custkey = o_custkey AND o_comment NOT LIKE '%unusual%packages%'
GROUP BY c_custkey
)
GROUP BY c_count
ORDER BY custdist DESC, c_count DESC
;

-- result
"custdist","c_count"
"10002","0"
"1315","10"
"1305","9"
"1247","8"
"1183","11"
"1130","12"
"974","13"
"941","14"
"925","7"
"921","19"
"902","18"
"901","20"
"901","16"
"884","15"
"874","17"
"795","21"
"774","22"
"634","6"
"629","23"
"525","24"
"424","25"
"396","5"
"336","26"
"249","27"
"214","4"
"173","28"
"133","29"
"90","3"
"75","30"
"45","31"
"33","32"
"24","2"
"22","33"
"9","34"
"5","35"
"5","1"
"2","38"
"1","40"
"1","37"
"1","36"


-- provided
with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce;

-- expected
with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
SELECT *
FROM Produce;

-- result
"item","sales","category"
"apples","2","fruit"
"carrots","8","vegetable"
"apples","7","fruit"
"bananas","5","fruit"


-- provided
with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce
|> WHERE item != 'bananas'
     AND category IN ('fruit', 'nut')
|> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales
    GROUP BY item
|> ORDER BY item DESC;

-- expected
with produce
    as (
        SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
        UNION ALL
        SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
        UNION ALL
        SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
        UNION ALL
        SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category )
SELECT COUNT(*) AS num_items, SUM(sales) AS total_sales, item
FROM Produce
WHERE item != 'bananas'
     AND category IN ('fruit', 'nut')
GROUP BY item
ORDER BY item DESC;

-- result
"num_items","total_sales","item"
"2","9","apples"


-- provided
with
    produce as (
        SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
        UNION ALL
        SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
        UNION ALL
        SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
        UNION ALL
        SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
    )
FROM Produce AS p1
  JOIN Produce AS p2
    USING (item)
|> WHERE item = 'bananas'
|> SELECT p1.item, p2.sales;

-- expected
with
    produce as (
        SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
        UNION ALL
        SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
        UNION ALL
        SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
        UNION ALL
        SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
    )
SELECT p1.item, p2.sales
FROM Produce AS p1
  JOIN Produce AS p2
    USING (item)
WHERE item = 'bananas';

-- result
"item","sales"
"bananas","5"


-- provided
FROM (SELECT 'apples' AS item, 2 AS sales)
|> SELECT item AS fruit_name;

-- expected
SELECT item AS fruit_name
FROM (SELECT 'apples' AS item, 2 AS sales)
;

-- result
"fruit_name"
"apples"


-- provided
SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3]) AS number
|> UNION DISTINCT
    (SELECT 1),
    (SELECT 2)
|> ORDER BY 1
;

-- expected
SELECT *
FROM (
    SELECT * FROM ( SELECT UNNEST(ARRAY[1,2,3])AS NUMBER) AS NUMBER
    UNION DISTINCT SELECT 1
    UNION DISTINCT SELECT 2
)
ORDER BY 1
;

-- result
"number"
"1"
"2"
"3"


-- provided
WITH client_info AS (
    WITH client AS (
        SELECT 1 AS client_id
        |> UNION ALL
            ( SELECT 2) ,
            ( SELECT 3)
    ),
    basket AS (
        SELECT 1 AS basket_id, 1 AS client_id
            |> UNION ALL
                ( SELECT 2, 2)
        ),
    basket_item AS (
        SELECT 1 AS item_id, 1 AS basket_id
        |> UNION ALL
            ( SELECT 2, 1),
            ( SELECT 3, 1),
            ( SELECT 4, 2)
        ),
    item AS (
        SELECT 1 AS item_id, 'milk' AS name
        |> UNION ALL
            (SELECT 2, "chocolate"),
            (SELECT 3, "donut"),
            (SELECT 4, "croissant")
        )
    FROM
        client c
            LEFT JOIN basket b USING (client_id)
            LEFT JOIN basket_item bi USING (basket_id)
            LEFT JOIN item i
    ON
        i.item_id = bi.item_id
        |> AGGREGATE COUNT(i.item_id) AS bought_item
           GROUP BY c.client_id, i.item_id, i.name
    )
FROM client_info
|> ORDER BY 1,2,3
;

-- expected
WITH CLIENT_INFO AS (
    WITH CLIENT AS(
            SELECT*FROM(
                SELECT 1 AS CLIENT_ID
                UNION ALL SELECT 2
                UNION ALL SELECT 3)
            )
        , BASKET AS(
            SELECT*FROM(
                SELECT 1 AS BASKET_ID,1 AS CLIENT_ID
                UNION ALL SELECT 2,2)
            )
        , BASKET_ITEM AS(
            SELECT*FROM(
                SELECT 1 AS ITEM_ID,1 AS BASKET_ID
                UNION ALL SELECT 2,1
                UNION ALL SELECT 3,1
                UNION ALL SELECT 4,2)
            )
        , ITEM AS(
            SELECT*FROM(
                SELECT 1 AS ITEM_ID,'milk' AS NAME
                UNION ALL SELECT 2,'chocolate'
                UNION ALL SELECT 3,'donut'
                UNION ALL SELECT 4,'croissant')
            )
        SELECT COUNT(I.ITEM_ID)AS BOUGHT_ITEM,C.CLIENT_ID,I.ITEM_ID,I.NAME
        FROM CLIENT C
            LEFT JOIN BASKET B USING(CLIENT_ID)
            LEFT JOIN BASKET_ITEM BI USING(BASKET_ID)
            LEFT JOIN ITEM I ON I.ITEM_ID=BI.ITEM_ID
        GROUP BY C.CLIENT_ID,I.ITEM_ID,I.NAME
    )
SELECT * FROM CLIENT_INFO
ORDER BY 1,2,3
;

-- result
"bought_item","client_id","item_id","name"
"0","3","",""
"1","1","1","milk"
"1","1","2","chocolate"
"1","1","3","donut"
"1","2","4","croissant"
