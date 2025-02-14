-- provided
FROM (SELECT 1 AS x, 2 AS y) AS t
|> DROP x
|> SELECT t.x AS original_x, y;

-- expected
SELECT t.x AS original_x, y FROM (
    SELECT * EXCLUDE(x)
    FROM (SELECT 1 AS x, 2 AS y) AS t
);

-- result
""