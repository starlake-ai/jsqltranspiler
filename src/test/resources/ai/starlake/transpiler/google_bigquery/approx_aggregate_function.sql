-- provided
SELECT APPROX_COUNT_DISTINCT(x) as approx_distinct
FROM UNNEST([0, 1, 1, 2, 3, 5]) as x;

-- expected
SELECT Approx_Count_Distinct( x ) AS approx_distinct
FROM (  SELECT Unnest(  [ 0, 1, 1, 2, 3, 5] ) AS x  ) AS x
;

-- result
"approx_distinct"
"5"

