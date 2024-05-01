-- prolog
CREATE OR REPLACE TABLE avg_example(int_col int, d decimal(10,5), s1 varchar(10), s2 varchar(10));
INSERT INTO avg_example VALUES
    (1, 1.1, '1.1','one'),
    (1, 10, '10','ten'),
    (2, 2.4, '2.4','two'),
    (2, NULL, NULL, 'NULL'),
    (3, NULL, NULL, 'NULL'),
    (NULL, 9.9, '9.9','nine');

-- provided
SELECT
       int_col,
       AVG(int_col) OVER(PARTITION BY int_col) AS avg
    FROM avg_example
    ORDER BY int_col;

-- result
"int_col","avg"
"1","1.0"
"1","1.0"
"2","2.0"
"2","2.0"
"3","3.0"
"",""

-- epilog
drop TABLE avg_example;


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
CREATE OR REPLACE TABLE sample_table(k CHAR(4), d CHAR(4));

INSERT INTO sample_table VALUES
    ('1', '1'), ('1', '5'), ('1', '3'),
    ('2', '2'), ('2', NULL),
    ('3', NULL),
    (NULL, '7'), (NULL, '1');

-- provided
SELECT k, d, MAX(d) OVER (ORDER BY k, d ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) AS max
  FROM sample_table
  ORDER BY k, d;

-- result
"k","d","max"
"1","1","1"
"1","3","3"
"1","5","5"
"2","2","5"
"2","","2"
"3","",""
"","1","1"
"","7","7"

-- epilog
drop TABLE sample_table;

