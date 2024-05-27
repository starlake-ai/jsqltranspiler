-- provided
SELECT any(col) AS bool FROM VALUES (true), (false), (false) AS tab(col);

-- expected
SELECT any_value(col) AS bool FROM VALUES (true), (false), (false) AS tab(col);

-- result
"bool"
"true"


-- provided
SELECT any_value(col) IGNORE NULLS FROM VALUES (NULL), (5), (20) AS tab(col);

-- expected
select any_value(col) FROM VALUES (NULL), (5), (20) AS tab(col);

-- count
1


-- provided
SELECT approx_count_distinct(col1) FILTER(WHERE col2 = 10) AS count
    FROM VALUES (1, 10), (1, 10), (2, 10), (2, 10), (3, 10), (1, 12) AS tab(col1, col2);

-- result
"count"
"3"



-- provided
SELECT approx_percentile(DISTINCT col, 0.5, 100)  AS percentile FROM VALUES (0), (6), (6), (7), (9), (10) AS tab(col);

-- expected
SELECT approx_quantile(DISTINCT col, 0.5) AS percentile FROM VALUES (0), (6), (6), (7), (9), (10) AS tab(col);

-- result
"percentile"
"7"


-- provided
SELECT array_agg(DISTINCT col) AS arr FROM VALUES (1), (2), (NULL), (1) AS tab(col);

-- result
"arr"
"[1, null, 2]"


-- provided
SELECT avg(col) AS avg FROM VALUES (1), (2), (NULL) AS tab(col);

-- result
"avg"
"1.50"
