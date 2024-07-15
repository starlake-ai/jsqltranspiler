-- prolog
CREATE OR REPLACE TABLE aggr(k int, v decimal(10,2), v2 decimal(10, 2));
INSERT INTO aggr VALUES(1, 10, NULL);
INSERT INTO aggr VALUES(2, 10, 11), (2, 20, 22), (2, 25, NULL), (2, 30, 35);

-- provided
SELECT k, CORR(v, v2) AS corr FROM aggr GROUP BY k;

-- result
"k","corr"
"1",""
"2","0.9988445981121534"

-- epilog
drop TABLE aggr;


-- prolog
CREATE TABLE basic_example (i_col INTEGER, j_col INTEGER);
INSERT INTO basic_example VALUES
    (11,101), (11,102), (11,NULL), (12,101), (NULL,101), (NULL,102);

-- provided
SELECT COUNT(*), COUNT(i_col), COUNT(DISTINCT i_col), COUNT(j_col), COUNT(DISTINCT j_col) FROM basic_example;

-- result
"count_star()","count(i_col)","count(DISTINCT i_col)","count(j_col)","count(DISTINCT j_col)"
"6","4","2","5","2"

-- epilog
drop TABLE basic_example;


-- prolog
CREATE TABLE basic_example (i_col INTEGER, j_col INTEGER);
INSERT INTO basic_example VALUES
    (11,101), (11,102), (11,NULL), (12,101), (NULL,101), (NULL,102);

-- provided
SELECT COUNT_IF(i_col IS NOT NULL AND j_col IS NOT NULL) as count FROM basic_example;

-- result
"count"
"3"

-- epilog
drop TABLE basic_example;


-- prolog
CREATE OR REPLACE TABLE aggr(k int, v decimal(10,2), v2 decimal(10, 2));
INSERT INTO aggr VALUES(1, 10, NULL);
INSERT INTO aggr VALUES(2, 10, 11), (2, 20, 22), (2, 25, NULL), (2, 30, 35);

-- provided
SELECT k, COVAR_POP(v, v2) FROM aggr GROUP BY k;

-- result
"k","covar_pop(v, v2)"
"1",""
"2","80.0"

-- epilog
drop TABLE aggr;


-- prolog
CREATE OR REPLACE TABLE aggr(k int, v decimal(10,2), v2 decimal(10, 2));
INSERT INTO aggr VALUES(1, 10, NULL);
INSERT INTO aggr VALUES(2, 10, 11), (2, 20, 22), (2, 25, NULL), (2, 30, 35);

-- provided
SELECT k, COVAR_SAMP(v, v2) FROM aggr GROUP BY k;

-- result
"k","covar_samp(v, v2)"
"1",""
"2","120.0"

-- epilog
drop TABLE aggr;


-- provided
SELECT O_ORDERSTATUS, listagg(O_CLERK, ', '  ORDER BY O_TOTALPRICE DESC)  AS clerks
    FROM orders WHERE O_ORDERKEY IN (41445, 55937, 67781, 80550, 95808, 101700, 103136) GROUP BY O_ORDERSTATUS ORDER BY 1;

-- expected
SELECT O_ORDERSTATUS, listagg(O_CLERK, ', ' ORDER BY O_TOTALPRICE DESC) AS clerks
    FROM orders WHERE O_ORDERKEY IN (41445, 55937, 67781, 80550, 95808, 101700, 103136) GROUP BY O_ORDERSTATUS  ORDER BY 1;

-- result
"o_orderstatus","clerks"
"F","Clerk#000000136, Clerk#000000521, Clerk#000000386, Clerk#000000508"
"O","Clerk#000000220, Clerk#000000411, Clerk#000000114"



-- prolog
create or replace table aggr(k int, v decimal(10,2));
INSERT INTO aggr (k, v) VALUES
    (1, 10),
    (1, 10),
    (1, 10),
    (1, 10),
    (1, 20),
    (1, 21);
INSERT INTO aggr (k, v) VALUES
    (2, 20),
    (2, 20),
    (2, 25),
    (2, 30);

-- provided
select k, v, mode(v) over (partition by k)  AS mode
    from aggr
    order by k, v;

-- result
"k","v","mode"
"1","10.00","10.00"
"1","10.00","10.00"
"1","10.00","10.00"
"1","10.00","10.00"
"1","20.00","10.00"
"1","21.00","10.00"
"2","20.00","20.00"
"2","20.00","20.00"
"2","25.00","20.00"
"2","30.00","20.00"

-- epilog
drop table aggr;


-- prolog
create or replace table aggr(k int, v decimal(10,2));
insert into aggr (k, v) values
    (0,  0),
    (0, 10),
    (0, 20),
    (0, 30),
    (0, 40),
    (1, 10),
    (1, 20),
    (2, 10),
    (2, 20),
    (2, 25),
    (2, 30),
    (3, 60),
    (4, NULL);

-- provided
select k, percentile_cont(0.25) within group (order by v) AS perc
  from aggr
  group by k
  order by k;

-- expected
SELECT K,QUANTILE_CONT(0.25 ORDER BY V)AS PERC FROM AGGR GROUP BY K ORDER BY K;

-- result
"k","perc"
"0","10.00"
"1","12.50"
"2","17.50"
"3","60.00"
"4",""


-- prolog
create or replace table aggr(k int, v decimal(10,2));
insert into aggr (k, v) values
    (0,  0),
    (0, 10),
    (0, 20),
    (0, 30),
    (0, 40),
    (1, 10),
    (1, 20),
    (2, 10),
    (2, 20),
    (2, 25),
    (2, 30),
    (3, 60),
    (4, NULL);

-- provided
select k, percentile_disc(0.25) within group (order by v) as perc
  from aggr
  group by k
  order by k;

-- expected
SELECT K,QUANTILE_DISC(0.25 ORDER BY V)AS PERC FROM AGGR GROUP BY K ORDER BY K;

-- result
"k","perc"
"0","10.00"
"1","10.00"
"2","10.00"
"3","60.00"
"4",""

-- provided
SELECT menu_category, STDDEV(menu_cogs_usd) stddev_cogs, STDDEV(menu_price_usd) stddev_price
  FROM menu_items
  WHERE menu_category='Dessert'
  GROUP BY 1;

-- result
"menu_category","stddev_cogs","stddev_price"
"Dessert","1.005194840151235","1.4719601443879744"



-- prolog
create or replace table test_boolean_agg(
    id integer,
    c1 boolean,
    c2 boolean,
    c3 boolean,
    c4 boolean
    );

insert into test_boolean_agg (id, c1, c2, c3, c4) values
    (1, true, true,  true,  false),
    (2, true, false, false, false),
    (3, true, true,  false, false),
    (4, true, false, false, false);

insert into test_boolean_agg (id, c1, c2, c3, c4) values
    (-4, false, false, false, true),
    (-3, false, true,  true,  true),
    (-2, false, false, true,  true),
    (-1, false, true,  true,  true);

-- provided
select
      id,
      booland_agg(c1) OVER (PARTITION BY (id > 0)) AS a,
      booland_agg(c2) OVER (PARTITION BY (id > 0)) AS b,
      booland_agg(c3) OVER (PARTITION BY (id > 0)) AS c,
      booland_agg(c4) OVER (PARTITION BY (id > 0)) AS d
    from test_boolean_agg
    order by id;

-- expected
select
      id,
      bool_and(c1) OVER (PARTITION BY (id > 0)) AS a,
      bool_and(c2) OVER (PARTITION BY (id > 0)) AS b,
      bool_and(c3) OVER (PARTITION BY (id > 0)) AS c,
      bool_and(c4) OVER (PARTITION BY (id > 0)) AS d
    from test_boolean_agg
    order by id;

-- result
"id","a","b","c","d"
"-4","false","false","false","true"
"-3","false","false","false","true"
"-2","false","false","false","true"
"-1","false","false","false","true"
"1","true","false","false","false"
"2","true","false","false","false"
"3","true","false","false","false"
"4","true","false","false","false"


-- prolog
create or replace table aggr(k int, v decimal(10,2), v2 decimal(10, 2));
insert into aggr values
    (1, 10, null),
    (2, 10, 12),
    (2, 20, 22),
    (2, 25, null),
    (2, 30, 35);

-- provided
select kurtosis(K), kurtosis(V), kurtosis(V2)
    from aggr;

-- result
"kurtosis(K)","kurtosis(V)","kurtosis(V2)"
"4.999999999998991","-2.32421875000009",""

-- prolog
create or replace table aggr(k int, v decimal(10,2), v2 decimal(10, 2));

insert into aggr values
    (1, 10, null),
    (2, 10, null),
    (2, 20, 22),
    (2, 25, null),
    (2, 30, 35);

-- provided
select SKEW(K) AS skew, SKEW(V) AS skew, SKEW(V2) AS skew
    from aggr;

-- expected
select Skewness(K) AS skew, Skewness(V) AS skew, Skewness(V2) AS skew
    from aggr;

-- result
"skew","skew","skew"
"-2.236067977499806","0.052407843222651324",""



-- prolog
create or replace table corn_production (farmer_ID INTEGER, state varchar, bushels float);
insert into corn_production (farmer_ID, state, bushels) values
    (1, 'Iowa', 100),
    (2, 'Iowa', 110),
    (3, 'Kansas', 120),
    (4, 'Kansas', 130);

-- provided
SELECT state, bushels,
        RANK() OVER (PARTITION BY state ORDER BY bushels DESC) AS rank,
        DENSE_RANK() OVER (PARTITION BY state ORDER BY bushels DESC) AS dense_rank
    FROM corn_production;

-- result
"state","bushels","rank","dense_rank"
"Iowa","110.0","1","1"
"Iowa","100.0","2","2"
"Kansas","130.0","1","1"
"Kansas","120.0","2","2"


-- prolog
CREATE OR REPLACE TABLE t1 (col_1 NUMERIC, col_2 NUMERIC);
INSERT INTO t1 VALUES
    (1, 5),
    (2, 4),
    (3, NULL),
    (4, 2),
    (5, NULL),
    (6, NULL),
    (7, 6);

-- provided
SELECT col_1, col_2, LAG(col_2) IGNORE NULLS OVER (ORDER BY col_1)  AS lag
    FROM t1
    ORDER BY col_1;

-- expected
SELECT col_1, col_2, LAG(col_2 IGNORE NULLS)  OVER (ORDER BY col_1) AS lag
    FROM t1
    ORDER BY col_1;

-- result
"col_1","col_2","lag"
"1.000","5.000",""
"2.000","4.000","5.000"
"3.000","","4.000"
"4.000","2.000","4.000"
"5.000","","2.000"
"6.000","","2.000"
"7.000","6.000","2.000"
