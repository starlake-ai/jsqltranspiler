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
"[null, 1, 2]"


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
"[null, 1, 2]"


-- provided
SELECT collect_set(col1) FILTER(WHERE col2 = 10) AS list
    FROM VALUES (1, 10), (2, 10), (NULL, 10), (1, 10), (3, 12) AS tab(col1, col2);

-- expected
SELECT list(DISTINCT col1 ORDER BY col1) FILTER(WHERE col2 = 10) AS list
    FROM VALUES (1, 10), (2, 10), (NULL, 10), (1, 10), (3, 12) AS tab(col1, col2);

-- result
"list"
"[null, 1, 2]"


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
"0.666666667"


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


-- provided
SELECT max_by(x, y) AS max_by FROM VALUES (('a', 10)), (('b', 50)), (('c', 20)) AS tab(x, y);

-- expected
SELECT max_by(x, y) AS max_by FROM VALUES ('a', 10), ('b', 50), ('c', 20) AS tab(x, y);

-- result
"max_by"
"b"


-- provided
SELECT mean(DISTINCT col) AS mean FROM VALUES (1), (1), (2), (NULL) AS tab(col);

-- result
"mean"
"1.5"


-- provided
SELECT median(DISTINCT col) AS median FROM VALUES (1), (2), (2), (3), (4), (NULL) AS tab(col);

-- result
"median"
"2.5"


-- provided
SELECT min_by(x, y) AS min_by FROM VALUES (('a', 10)), (('b', 50)), (('c', 20)) AS tab(x, y);

-- expected
SELECT min_by(x, y) AS min_by FROM VALUES ('a', 10), ('b', 50), ('c', 20) AS tab(x, y);

-- result
"min_by"
"a"


-- provided
SELECT mode(col) AS mode FROM VALUES (1), (1), (2), (2), (3) AS tab(col);

-- result
"mode"
"1"


-- provided
SELECT percentile(col, array(0.25, 0.75)) AS percentile FROM VALUES (0), (10) AS tab(col);

-- expected
SELECT quantile_cont(col, [0.25, 0.75]) AS percentile FROM VALUES (0), (10) AS tab(col);

-- result
"percentile"
"[2.5, 7.5]"


-- provided
SELECT percentile_approx(DISTINCT col, 0.5, 100) AS percentile
    FROM VALUES (0), (6), (7), (9), (10), (10), (10) AS tab(col);

-- expected
SELECT approx_quantile(DISTINCT col, 0.5) AS percentile
    FROM VALUES (0), (6), (7), (9), (10), (10), (10) AS tab(col);

-- result
"percentile"
"7"


-- provided
 SELECT percentile_cont(array(0.5, 0.4, 0.1)) WITHIN GROUP (ORDER BY col) AS percentile
    FROM VALUES (0), (1), (2), (10) AS tab(col);

-- expected
 SELECT QUANTILE_CONT([0.5,0.4,0.1]ORDER BY COL)AS PERCENTILE FROM VALUES(0),(1),(2),(10)AS TAB(COL);

-- result
"percentile"
"[1.5, 1.2000000000000002, 0.30000000000000004]"


-- provided
SELECT percentile_disc(array(0.5, 0.4, 0.1)) WITHIN GROUP (ORDER BY col) AS percentile
    FROM VALUES (0), (1), (2), (10) AS tab(col);

-- expected
SELECT QUANTILE_DISC([0.5,0.4,0.1]ORDER BY COL)AS PERCENTILE FROM VALUES(0),(1),(2),(10)AS TAB(COL);

-- result
"percentile"
"[1, 1, 0]"


-- provided
SELECT regr_avgx(y, x) AS regr_avgx FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_avgx"
"2.666666667"


-- provided
SELECT regr_avgy(y, x)  AS regr_avgy FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_avgy"
"1.666666667"


-- provided
SELECT regr_count(y, x) AS regr_count FROM VALUES (1, 2), (2, NULL), (2, 3), (2, 4) AS t(y, x);

-- result
"regr_count"
"3"


-- provided
SELECT regr_intercept(y, x) AS regr_intercept FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_intercept"
"-1.0"


-- provided
SELECT regr_r2(y, x) AS regr_r2 FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_r2"
"1.0"


-- provided
SELECT regr_slope(y, x) AS regr_slope FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_slope"
"1.0"


-- provided
SELECT regr_sxx(y, x) AS regr_sxx FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_sxx"
"0.666666667"


-- provided
SELECT regr_sxy(y, x) AS regr_sxy FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_sxy"
"0.666666667"


-- provided
SELECT regr_syy(y, x) AS regr_syy FROM VALUES (1, 2), (2, 3), (2, 3), (null, 4), (4, null) AS T(y, x);

-- result
"regr_syy"
"0.666666667"


-- provided
SELECT skewness(DISTINCT col) AS skewness FROM VALUES (-10), (-20), (100), (1000), (1000) AS tab(col);

-- result
"skewness"
"1.928752451"


-- provided
SELECT std(DISTINCT col) AS stddev FROM VALUES (1), (2), (3), (3) AS tab(col);

-- expected
SELECT stddev(DISTINCT col) AS stddev FROM VALUES (1), (2), (3), (3) AS tab(col);

-- result
"stddev"
"1.0"


-- provided
SELECT stddev_pop(DISTINCT col) AS stddev_pop FROM VALUES (1), (2), (3), (3) AS tab(col);

-- result
"stddev_pop"
"0.816496581"


-- provided
SELECT a, b, cume_dist() OVER (PARTITION BY a ORDER BY b) AS cume_dist
    FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b)
    ORDER BY 1,2;

-- result
"a","b","cume_dist"
"A1","1","0.666666667"
"A1","1","0.666666667"
"A1","2","1.0"
"A2","3","1.0"


-- provided
SELECT a, b, lag(b) OVER (PARTITION BY a ORDER BY b) AS lag
    FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b)
    ORDER BY 1,2;

-- result
"a","b","lag"
"A1","1",""
"A1","1","1"
"A1","2","1"
"A2","3",""


-- provided
SELECT a, b, lead(b) OVER (PARTITION BY a ORDER BY b) AS lead
    FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b)
    ORDER BY 1,2
    ;

-- result
"a","b","lead"
"A1","1","1"
"A1","1","2"
"A1","2",""
"A2","3",""


-- provided
SELECT a, b, nth_value(b, 2) OVER (PARTITION BY a ORDER BY b) AS nth_value
    FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b)
ORDER BY 1,2;

-- result
"a","b","nth_value"
"A1","1","1"
"A1","1","1"
"A1","2","1"
"A2","3",""


-- provided
SELECT a,
         b,
         dense_rank() OVER(PARTITION BY a ORDER BY b) AS dense_rank,
         rank() OVER(PARTITION BY a ORDER BY b) AS rank,
         row_number() OVER(PARTITION BY a ORDER BY b) AS row_number
    FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b)
ORDER BY 1,2,3;

-- result
"a","b","dense_rank","rank","row_number"
"A1","1","1","1","1"
"A1","1","1","1","2"
"A1","2","2","3","3"
"A2","3","1","1","1"

-- provided
 SELECT a, b, ntile(2) OVER (PARTITION BY a ORDER BY b) AS ntile
 FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b)
 ORDER BY 1,2;

-- result
"a","b","ntile"
"A1","1","1"
"A1","1","1"
"A1","2","2"
"A2","3","1"


-- provided
SELECT a, b, percent_rank(b) OVER (PARTITION BY a ORDER BY b) AS percent_rank
    FROM VALUES ('A1', 2), ('A1', 1), ('A1', 3), ('A1', 6), ('A1', 7), ('A1', 7), ('A2', 3), ('A1', 1) tab(a, b)
    ORDER BY 1,2;

-- expected
SELECT a, b, percent_rank() OVER (PARTITION BY a ORDER BY b) AS percent_rank
    FROM VALUES ('A1', 2), ('A1', 1), ('A1', 3), ('A1', 6), ('A1', 7), ('A1', 7), ('A2', 3), ('A1', 1) tab(a, b)
    ORDER BY 1,2;


-- result
"a","b","percent_rank"
"A1","1","0.0"
"A1","1","0.0"
"A1","2","0.333333333"
"A1","3","0.5"
"A1","6","0.666666667"
"A1","7","0.833333333"
"A1","7","0.833333333"
"A2","3","0.0"