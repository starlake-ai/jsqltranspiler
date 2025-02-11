-- provided
(
  SELECT 'kale' as product, 55 AS Q1, 45 AS Q2
  UNION ALL
  SELECT 'apple', 8, 10
)
|> UNPIVOT(sales FOR quarter IN (Q1, Q2))
|> ORDER BY 1
|> SELECT DISTINCT product;

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
;

-- result
"product"
"apple"
"kale"