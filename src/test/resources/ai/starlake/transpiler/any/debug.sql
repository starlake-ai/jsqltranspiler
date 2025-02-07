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