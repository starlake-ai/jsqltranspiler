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