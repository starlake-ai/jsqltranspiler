-- provided
SELECT
  fruit,
  ANY_VALUE(fruit) OVER (ORDER BY LENGTH(fruit) ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) AS any_value
from (select UNNEST(['apple', 'banana', 'pear']) as fruit);

-- expected
SELECT  fruit
        , Any_Value( fruit )
                OVER (ORDER BY CASE TYPEOF(FRUIT)
                                WHEN 'VARCHAR'
                                    THEN LENGTH(TRY_CAST(FRUIT AS VARCHAR))
                                WHEN 'BLOB'
                                    THEN OCTET_LENGTH(TRY_CAST(FRUIT AS BLOB))
                                END
                      ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) AS any_value
FROM (  SELECT Unnest(  [ 'apple', 'banana', 'pear'] ) AS fruit  )
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
"",""
"0","0.0"
"2","1.0"
"4","3.0"
"4","4.0"
"5","4.5"


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
"","0"
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
  GROUPING(product_name) AS product_name_agg
FROM Products
GROUP BY GROUPING SETS(product_type, product_name, ())
ORDER BY product_name, product_type;

-- result
"product_type","product_name","product_sum","product_type_agg","product_name_agg"
"","","42","1","1"
"pants","","6","0","1"
"shirt","","36","0","1"
"","jeans","6","1","0"
"","polo","25","1","0"
"","t-shirt","11","1","0"


-- provided
SELECT LOGICAL_AND(x < 3) AS logical_and
FROM UNNEST([1, 2, 4]) AS x;

-- expected
SELECT BOOL_AND(x < 3) AS logical_and
FROM (SELECT UNNEST([1, 2, 4]) AS x) AS x;

-- result
"logical_and"
"false"


-- provided
SELECT LOGICAL_OR(x < 3) AS logical_or FROM UNNEST([1, 2, 4]) AS x;

-- expected
SELECT BOOL_OR(x < 3) AS logical_or
FROM (SELECT UNNEST([1, 2, 4]) AS x) as x;

-- result
"logical_or"
"true"


-- provided
SELECT x, MAX(x) OVER (PARTITION BY MOD(x, 2)) AS max
FROM UNNEST([8, NULL, 37, 55, NULL, 4]) AS x
order by 1 nulls first;

-- expected
SELECT x, MAX(x) OVER (PARTITION BY MOD(x, 2)) AS max
FROM (select UNNEST([8, NULL, 37, 55, NULL, 4]) AS x) as x
order by 1 nulls first;

-- result
"x","max"
"",""
"",""
"4","8"
"8","8"
"37","55"
"55","55"


-- provided
WITH fruits AS (
  SELECT 'apple'  fruit, 3.55 price UNION ALL
  SELECT 'banana'  fruit, 2.10 price UNION ALL
  SELECT 'pear'  fruit, 4.30 price
)
SELECT MAX_BY(fruit, price) as fruit
FROM fruits;

-- result
"fruit"
"pear"


-- provided
SELECT x, MIN(x) OVER (PARTITION BY MOD(x, 2)) AS min
FROM UNNEST([8, NULL, 37, 4, NULL, 55]) AS x
order by 1 nulls first;

-- expected
SELECT x, MIN(x) OVER (PARTITION BY MOD(x, 2)) AS min
FROM (Select UNNEST([8, NULL, 37, 4, NULL, 55]) AS x) as x
order by 1 nulls first;

-- result
"x","min"
"",""
"",""
"4","4"
"8","4"
"37","37"
"55","37"


-- provided
WITH fruits AS (
  SELECT 'apple'  fruit, 3.55 price UNION ALL
  SELECT 'banana'  fruit, 2.10 price UNION ALL
  SELECT 'pear'  fruit, 4.30 price
)
SELECT MIN_BY(fruit, price) as fruit
FROM fruits;

-- result
"fruit"
"banana"


-- provided
SELECT STRING_AGG(fruit, ' & ' ORDER BY LENGTH(fruit)) AS string_agg
FROM UNNEST(['apple', 'pear', 'banana', 'pear']) AS fruit;

-- expected
SELECT STRING_AGG( FRUIT,' & ' ORDER BY CASE TYPEOF(FRUIT)
                                        WHEN 'VARCHAR' THEN LENGTH(TRY_CAST(FRUIT AS VARCHAR))
                                        WHEN 'BLOB' THEN OCTET_LENGTH(TRY_CAST(FRUIT AS BLOB))
                                        END ) AS STRING_AGG
FROM ( SELECT UNNEST( ['apple','pear','banana','pear'] ) AS FRUIT) AS FRUIT;

-- result
"string_agg"
"pear & pear & apple & banana"


-- provided
SELECT
  fruit,
  STRING_AGG(fruit, ' & ') OVER (ORDER BY LENGTH(fruit)) AS string_agg
FROM UNNEST(['apple', NULL, 'pear', 'banana', 'pear']) AS fruit;

-- expected
SELECT FRUIT, STRING_AGG(FRUIT,' & ') OVER( ORDER BY CASE TYPEOF(FRUIT)
                                                        WHEN 'VARCHAR' THEN LENGTH(TRY_CAST(FRUIT AS VARCHAR))
                                                        WHEN 'BLOB' THEN OCTET_LENGTH(TRY_CAST(FRUIT AS BLOB))
                                                        END) AS STRING_AGG
FROM( SELECT UNNEST( ['apple',NULL,'pear','banana','pear'] )AS FRUIT ) AS FRUIT
;

-- result
"fruit","string_agg"
"",""
"pear","pear & pear"
"pear","pear & pear"
"apple","pear & pear & apple"
"banana","pear & pear & apple & banana"


-- provided
SELECT CORR(y, x) AS results
FROM
  UNNEST(
    [
      STRUCT(1.0 AS y, 5.0 AS x),
      (3.0, 9.0),
      (4.0, 7.0)]);

-- expected
SELECT CORR(Y,X)AS RESULTS
FROM( SELECT UNNEST([{Y:1.0,X:5.0},(3.0,9.0),(4.0,7.0)], recursive=>true));

-- result
"results"
"0.6546536707079772"


-- provided
SELECT COVAR_POP(y, x) AS results
FROM
  UNNEST(
    [
      STRUCT(1.0 AS y, 1.0 AS x),
      (2.0, 6.0),
      (9.0, 3.0),
      (2.0, 6.0),
      (9.0, 3.0)])
;

-- expected
SELECT Covar_Pop( y, x ) AS results
FROM (  SELECT Unnest(  [
                            { y:1.0,x:1.0 }
                            , ( 2.0, 6.0 )
                            , ( 9.0, 3.0 )
                            , ( 2.0, 6.0 )
                            , ( 9.0, 3.0 )
                        ], recursive => TRUE ) )
;

-- result
"results"
"-1.6800000000000002"


-- provided
SELECT COVAR_POP(y, x) AS results
FROM UNNEST([STRUCT(1.0 AS y, NULL AS x),(9.0, 3.0)])

;

-- expected
SELECT Covar_Pop( y, x ) AS results
FROM (  SELECT Unnest(  [
                            { y:1.0,x:NULL }
                            , ( 9.0, 3.0 )
                        ], recursive => TRUE ) )
;

-- result
"results"
"0.0"


-- provided
SELECT COVAR_POP(y, x) AS results
FROM
  UNNEST(
    [
      STRUCT(1.0 AS y, 1.0 AS x),
      (2.0, 6.0),
      (9.0, 3.0),
      (2.0, 6.0),
      (CAST('Infinity' as FLOAT64), 3.0)])
;

-- expected
SELECT Covar_Pop( y, x ) AS results
FROM (  SELECT Unnest(  [
                            { y:1.0, x:1.0 }
                            , ( 2.0, 6.0 )
                            , ( 9.0, 3.0 )
                            , ( 2.0, 6.0 )
                            , (  Cast( 'Infinity' AS FLOAT8 ), 3.0 )
                        ], recursive => TRUE ) )
;

-- result
"results"
"NaN"


-- provided
SELECT COVAR_SAMP(y, x) AS results
FROM
  UNNEST(
    [
      STRUCT(1.0 AS y, 1.0 AS x),
      (2.0, 6.0),
      (9.0, 3.0),
      (2.0, 6.0),
      (9.0, 3.0)])
;

-- expected
SELECT Covar_Samp( y, x ) AS results
FROM (  SELECT Unnest(  [
                            { y:1.0,x:1.0 }
                            , ( 2.0, 6.0 )
                            , ( 9.0, 3.0 )
                            , ( 2.0, 6.0 )
                            , ( 9.0, 3.0 )
                        ], recursive => TRUE ) )
;

-- result
"results"
"-2.1"


-- provided
SELECT COVAR_SAMP(y, x) AS results
FROM
  UNNEST(
    [
      STRUCT(1.0 AS y, 1.0 AS x),
      (2.0, 6.0),
      (9.0, 3.0),
      (2.0, 6.0),
      (NULL, 3.0)])
;

-- expected
SELECT Covar_Samp( y, x ) AS results
FROM (  SELECT Unnest(  [
                            { y:1.0,x:1.0 }
                            , ( 2.0, 6.0 )
                            , ( 9.0, 3.0 )
                            , ( 2.0, 6.0 )
                            , ( NULL, 3.0 )
                        ], recursive => TRUE ) )
;

-- result
"results"
"-1.3333333333333333"


-- provided
SELECT STDDEV_SAMP(x) AS results FROM UNNEST([10, 14, 18]) AS x
;

-- expected
SELECT Stddev_Samp( x ) AS results
FROM (  SELECT Unnest(  [ 10, 14, 18] ) AS x  ) AS x
;

-- result
"results"
"4.0"


-- provided
SELECT STDDEV_POP(x) AS results FROM UNNEST([10, 14, 18]) AS x;

-- expected
SELECT STDDEV_POP(x) AS results FROM (SELECT UNNEST([10, 14, 18]) AS x) AS x;

-- result
"results"
"3.265986323710904"


-- provided
SELECT VAR_POP(x) AS results FROM UNNEST([10, 14, 18]) AS x;


-- expected
SELECT VAR_POP(x) AS results FROM (SELECT UNNEST([10, 14, 18]) AS x) AS x;

-- result
"results"
"10.666666666666666"


-- provided
SELECT VAR_SAMP(x) AS results FROM UNNEST([10, 14, 18]) AS x;


-- expected
SELECT VAR_SAMP(x) AS results FROM (SELECT UNNEST([10, 14, 18]) AS x) AS x;

-- result
"results"
"16.0"

