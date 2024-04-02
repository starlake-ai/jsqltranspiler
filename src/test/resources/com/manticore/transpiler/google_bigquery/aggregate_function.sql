-- provided
SELECT
  fruit,
  ANY_VALUE(fruit) OVER (ORDER BY LENGTH(fruit) ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) AS any_value
from (select UNNEST(['apple', 'banana', 'pear']) as fruit);

-- expected
SELECT  fruit
        , ANY_VALUE(FRUIT)
            OVER (  ORDER BY CASE TYPEOF(FRUIT)
                            WHEN 'VARCHAR' THEN LENGTH(FRUIT::VARCHAR)
                            WHEN 'BLOB' THEN OCTET_LENGTH(FRUIT::BLOB)
                            ELSE -1 END
                    ROWS BETWEEN 1 PRECEDING
                        AND CURRENT ROW) AS any_value
FROM (  SELECT Unnest( ['apple', 'banana', 'pear'] ) AS fruit  )
;

-- result
"fruit","any_value"
"pear","pear"
"apple","pear"
"banana","apple"


-- provided
SELECT ARRAY_AGG(x) AS array_agg FROM UNNEST([2, 1,-2, 3, -2, 1, 2]) AS x;

-- expected
SELECT ARRAY_AGG(x) AS array_agg FROM (select UNNEST([2, 1,-2, 3, -2, 1, 2]) AS x) AS x;

-- result
"array_agg"
"[2, 1, -2, 3, -2, 1, 2]"


-- provided
SELECT ARRAY_AGG(DISTINCT x ORDER BY x) AS array_agg
FROM UNNEST([2, 1, -2, 3, -2, 1, 2]) AS x;

-- expected
SELECT ARRAY_AGG(DISTINCT x ORDER BY x) AS array_agg
FROM (select UNNEST([2, 1, -2, 3, -2, 1, 2]) AS x) as x;

-- result
"array_agg"
"[-2, 1, 2, 3]"


-- provided
SELECT
  x,
  ARRAY_AGG(x) OVER (ORDER BY ABS(x)) AS array_agg
FROM UNNEST([2, 1, -2, 3, -2, 1, 2]) AS x;

-- expected
SELECT
  x,
  ARRAY_AGG(x) OVER (ORDER BY ABS(x)) AS array_agg
FROM (select UNNEST([2, 1, -2, 3, -2, 1, 2]) AS x) AS x;

-- result
"x","array_agg"
"1","[1, 1]"
"1","[1, 1]"
"2","[1, 1, 2, -2, -2, 2]"
"-2","[1, 1, 2, -2, -2, 2]"
"-2","[1, 1, 2, -2, -2, 2]"
"2","[1, 1, 2, -2, -2, 2]"
"3","[1, 1, 2, -2, -2, 2, 3]"


-- provided
SELECT FORMAT('%T', ARRAY_CONCAT_AGG(x)) AS array_concat_agg FROM (
  SELECT [NULL, 1, 2, 3, 4] AS x
  UNION ALL SELECT NULL
  UNION ALL SELECT [5, 6]
  UNION ALL SELECT [7, 8, 9]
);

-- expected
SELECT printf('%s', list_sort(flatten(list(x)), 'ASC', 'NULLS FIRST')) AS array_concat_agg FROM (
  SELECT [NULL, 1, 2, 3, 4] AS x
  UNION ALL SELECT NULL
  UNION ALL SELECT [5, 6]
  UNION ALL SELECT [7, 8, 9]
);

-- result
"array_concat_agg"
"[NULL, 1, 2, 3, 4, 5, 6, 7, 8, 9]"


-- provided
SELECT AVG(DISTINCT x) AS avg
FROM UNNEST([0, 2, 4, 4, 5]) AS x;

-- expected
SELECT AVG(DISTINCT x) AS avg
FROM (SELECT UNNEST([0, 2, 4, 4, 5]) AS x)  AS x;

-- result
"avg"
"2.75"

-- provided
SELECT
  x,
  AVG(x) OVER (ORDER BY x ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) AS avg
FROM UNNEST([0, 2, NULL, 4, 4, 5]) AS x;


-- expected
SELECT
  x,
  AVG(x) OVER (ORDER BY x ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) AS avg
FROM (select UNNEST([0, 2, NULL, 4, 4, 5]) AS x) as x;

-- result
"x","avg"
"0","0.0"
"2","1.0"
"4","3.0"
"4","4.0"
"5","4.5"
"","5.0"


-- provided
SELECT BIT_AND(x) as bit_and FROM UNNEST([0xF001, 0x00A1]) as x;

-- expected
SELECT BIT_AND(x) as bit_and
FROM (SELECT UNNEST([61441, 161]) as x) as x;

-- result
"bit_and"
"1"


-- provided
SELECT BIT_OR(x) as bit_or FROM UNNEST([0xF001, 0x00A1]) as x;

-- expected
SELECT BIT_OR(x) as bit_or
FROM (SELECT UNNEST([61441, 161]) as x) as x;

-- result
"bit_or"
"61601"


-- provided
SELECT BIT_XOR(x) as bit_xor FROM UNNEST([0xF001, 0x00A1]) as x;

-- expected
SELECT BIT_XOR(x) as bit_xor
FROM (SELECT UNNEST([61441, 161]) as x) as x;

-- result
"bit_xor"
"61600"


-- provided
SELECT
  x,
  COUNT(*) OVER (PARTITION BY MOD(x, 3)) AS count_star,
  COUNT(x) OVER (PARTITION BY MOD(x, 3)) AS count_x
FROM UNNEST([1, 4, NULL, 4, 5]) AS x
ORDER BY 1 NULLS FIRST;

-- expected
SELECT
  x,
  COUNT(*) OVER (PARTITION BY MOD(x, 3)) AS count_star,
  COUNT(x) OVER (PARTITION BY MOD(x, 3)) AS count_x
FROM (SELECT UNNEST([1, 4, NULL, 4, 5]) AS x) AS x
ORDER BY 1 NULLS FIRST;

-- result
"x","count_star","count_x"
"","1","0"
"1","3","3"
"4","3","3"
"4","3","3"
"5","1","1"


-- provided
SELECT COUNT(DISTINCT IF(x > 0, x, NULL)) AS distinct_positive
FROM UNNEST([1, -2, 4, 1, -5, 4, 1, 3, -6, 1]) AS x;

-- expected
SELECT COUNT(DISTINCT IF(x > 0, x, NULL)) AS distinct_positive
FROM (SELECT UNNEST([1, -2, 4, 1, -5, 4, 1, 3, -6, 1]) AS x) AS x;

-- result
"distinct_positive"
"3"


-- provided
SELECT COUNTIF(x<0) AS num_negative, COUNTIF(x>0) AS num_positive
FROM UNNEST([5, -2, 3, 6, -10, -7, 4, 0]) AS x;

-- expected
SELECT COUNT(IF(x < 0, x, NULL)) AS num_negative, COUNT(IF(x > 0, x, NULL)) AS num_positive
FROM (SELECT UNNEST([5, -2, 3, 6, -10, -7, 4, 0]) AS x) AS x;

-- result
"num_negative","num_positive"
"3","4"


-- provided
SELECT
  x,
  COUNTIF(x<0) OVER (ORDER BY ABS(x) ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING) AS num_negative
FROM UNNEST([5, -2, 3, 6, -10, NULL, -7, 4, 0]) AS x
order by 1 NULLS FIRST;

-- expected
SELECT
  x,
  /* Approximation: Different NULL handling */ COUNT(IF(x < 0, x, NULL)) OVER (ORDER BY ABS(x) ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING) AS num_negative
FROM (SELECT UNNEST([5, -2, 3, 6, -10, NULL, -7, 4, 0]) AS x) AS x
order by 1 NULLS FIRST;

-- result
"x","num_negative"
"","1"
"-10","2"
"-7","2"
"-2","1"
"0","1"
"3","1"
"4","0"
"5","0"
"6","1"


-- provided
WITH
  Products AS (
    SELECT 'shirt' AS product_type, 't-shirt' AS product_name, 3 AS product_count UNION ALL
    SELECT 'shirt', 't-shirt', 8 UNION ALL
    SELECT 'shirt', 'polo', 25 UNION ALL
    SELECT 'pants', 'jeans', 6
  )
SELECT
  product_type,
  product_name,
  SUM(product_count) AS product_sum,
  GROUPING(product_type) AS product_type_agg,
  GROUPING(product_name) AS product_name_agg,
FROM Products
GROUP BY GROUPING SETS(product_type, product_name, ())
ORDER BY product_name, product_type;

-- result
"product_type","product_name","product_sum","product_type_agg","product_name_agg"
"","jeans","6.00","1","0"
"","polo","25.00","1","0"
"","t-shirt","11.00","1","0"
"pants","","6.00","0","1"
"shirt","","36.00","0","1"
"","","42.00","1","1"
