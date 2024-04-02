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

