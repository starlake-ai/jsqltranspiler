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