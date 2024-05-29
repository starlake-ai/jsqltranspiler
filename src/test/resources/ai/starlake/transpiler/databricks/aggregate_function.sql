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

-- expected
SELECT array_agg(DISTINCT col ORDER BY col) AS arr FROM VALUES (1), (2), (NULL), (1) AS tab(col);

-- result
"arr"
"[1, 2, null]"


-- provided
SELECT avg(col) AS avg FROM VALUES (1), (2), (NULL) AS tab(col);

-- result
"avg"
"1.5"


-- provided
SELECT bit_and(col) FILTER(WHERE col < 6) AS bit_and FROM VALUES (3), (5), (6) AS tab(col);

-- result
"bit_and"
"1"


-- provided
SELECT bit_or(col) FILTER(WHERE col < 8) AS bit_or FROM VALUES (3), (5), (8) AS tab(col);

-- result
"bit_or"
"7"


-- provided
SELECT bit_xor(DISTINCT col) AS bit_xor FROM VALUES (3), (3), (5) AS tab(col);

-- result
"bit_xor"
"6"


-- provided
SELECT bool_and(col) AS bool_and FROM VALUES (true), (false), (true) AS tab(col);

-- result
"bool_and"
"false"


-- provided
SELECT bool_or(col) AS bool_or FROM VALUES (false), (false), (NULL) AS tab(col);

-- result
"bool_or"
"false"


-- provided
SELECT collect_list(DISTINCT col)  AS list FROM VALUES (1), (2), (NULL), (1) AS tab(col);

-- expected
SELECT list(DISTINCT col ORDER BY col) AS list FROM VALUES (1), (2), (NULL), (1) AS tab(col);

-- result
"list"
"[1, 2, null]"


-- provided
SELECT collect_set(col1) FILTER(WHERE col2 = 10) AS list
    FROM VALUES (1, 10), (2, 10), (NULL, 10), (1, 10), (3, 12) AS tab(col1, col2);

-- expected
SELECT list(DISTINCT col1 ORDER BY col1) FILTER(WHERE col2 = 10) AS list
    FROM VALUES (1, 10), (2, 10), (NULL, 10), (1, 10), (3, 12) AS tab(col1, col2);

-- result
"list"
"[1, 2, null]"


-- provided
SELECT corr(DISTINCT c1, c2) FILTER(WHERE c1 != c2) AS corr
    FROM VALUES (3, 2), (3, 3), (3, 3), (6, 4) as tab(c1, c2);

-- result
"corr"
"1.0"


-- provided
SELECT count(DISTINCT col1) AS count
    FROM VALUES (NULL, NULL), (5, NULL), (5, 1), (5, 2), (NULL, 2), (20, 2) AS tab(col1, col2);

-- result
"count"
"2"


-- provided
SELECT count_if(DISTINCT col % 2 = 0) AS count_if FROM VALUES (NULL), (0), (1), (2), (2), (3) AS tab(col);

-- expected
SELECT count_if(col % 2 = 0)  AS count_if FROM VALUES (NULL), (0), (1), (2), (2), (3) AS tab(col);

-- result
"count_if"
"3"


-- provided
 SELECT covar_pop(DISTINCT c1, c2) AS covar_pop FROM VALUES (1,1), (2,2), (2,2), (3,3) AS tab(c1, c2);

-- result
"covar_pop"
"0.6666666666666666"


-- provided
SELECT covar_samp(DISTINCT c1, c2) AS covar_samp FROM VALUES (1,1), (2,2), (2, 2), (3,3) AS tab(c1, c2);

-- result
"covar_samp"
"1.0"


-- provided
SELECT first(col) AS first FROM VALUES (NULL), (5), (20) AS tab(col);

-- result
"first"
""






