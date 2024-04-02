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

