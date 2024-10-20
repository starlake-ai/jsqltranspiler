-- provide
SELECT TO_VARCHAR('2024-04-03'::DATE) AS s;

-- expected
SELECT '2024-04-03'::DATE::VARCHAR AS s;

-- result
"s"
"2024-04-03"

-- provide
SELECT TO_VARCHAR('2024-04-03'::DATE, 'yyyy.mm.dd') AS s;

-- expected
SELECT STRFTIME('2024-04-03'::DATE,'%Y.%m.%d') AS s;

-- result
"s"
"2024.04.03"


-- prolog
CREATE OR REPLACE TABLE number_conv(expr VARCHAR);
INSERT INTO number_conv VALUES ('12.3456'), ('98.76546');

-- provide
SELECT expr,
       TO_NUMBER(expr) AS N1,
       TO_NUMBER(expr, 10, 1) AS N2,
       TO_NUMBER(expr, 10, 8) AS N3
  FROM number_conv;

-- expected
SELECT  expr
        ,  Cast( If( Typeof( expr ) = 'VARCHAR', List_Aggregate( Regexp_Extract_All( expr, '[\+|\-\D|\.]' ), 'string_agg', '' ), expr ) AS DECIMAL (12, 0) ) AS n1
        ,  Cast( If( Typeof( expr ) = 'VARCHAR', List_Aggregate( Regexp_Extract_All( expr, '[\+|\-\D|\.]' ), 'string_agg', '' ), expr ) AS DECIMAL (10, 1) ) AS n2
        ,  Cast( If( Typeof( expr ) = 'VARCHAR', List_Aggregate( Regexp_Extract_All( expr, '[\+|\-\D|\.]' ), 'string_agg', '' ), expr ) AS DECIMAL (10, 8) ) AS n3
FROM number_conv
;

-- result
"expr","N1","N2","N3"
"12.3456","12","12.3","12.3456000"
"98.76546","99","98.8","98.7654600"


-- prolog
CREATE TABLE double_demo (d DECIMAL(7, 2), v VARCHAR, o NUMERIC);
INSERT INTO double_demo (d, v, o) SELECT 1.1, '2.2', 3.14;

-- provided
SELECT TO_DOUBLE(d) AS d, TO_DOUBLE(v) AS v, TO_DOUBLE(o) AS o FROM double_demo;

-- expected
SELECT CAST(D AS DOUBLE)AS D,CAST(V AS DOUBLE)AS V,CAST(O AS DOUBLE)AS O FROM DOUBLE_DEMO;

-- result
"d","v","o"
"1.1","2.2","3.14"

-- prolog
CREATE OR REPLACE TABLE test_boolean(
   b BOOLEAN,
   n NUMERIC,
   s STRING);
INSERT INTO test_boolean VALUES (true, 1, 'yes'), (false, 0, 'no'), (null, null, null);

-- provided
SELECT s, TRY_TO_BOOLEAN(s) AS b1, n, TO_BOOLEAN(n) AS b2 FROM test_boolean;

-- expected
SELECT S,TRY_CAST(S AS BOOLEAN)AS B1,N,CAST(N AS BOOLEAN)AS B2 FROM TEST_BOOLEAN;

-- result
"s","b1","n","b2"
"yes","true","1.00","true"
"no","false","0.00","false"
"","","",""