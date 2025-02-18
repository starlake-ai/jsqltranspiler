-- provided
SELECT -25 a, ABS(25) b;

-- result
"a","b"
"-25","25"


-- provided
SELECT acos(-1) a, acos(0) b, acos(1) c;

--result
"a","b","c"
"3.141592654","1.570796327","0.0"


-- provided
SELECT asin(-1) a, asin(0) b, asin(1) c;

-- result
"a","b","c"
"-1.570796327","0.0","1.570796327"


-- provided
SELECT atan(-1) a, atan(0) b, atan(1) c;

-- result
"a","b","c"
"-0.785398163","0.0","0.785398163"


-- provided
SELECT atan2(-1, -1) a, atan2(0, 0) b, atan2(1, 1) c;

-- result
"a","b","c"
"-2.35619449","0.0","0.785398163"


-- provided
select CBRT(27) a;

-- result
"a"
"3.0"


-- provided
SELECT Ceil( x ) a, Ceiling ( x ) b
FROM (  SELECT Unnest(  [ 2.0, 2.3, 2.8, 2.5, - 2.3, - 2.8, - 2.5, 0] ) x  )
;

-- result
"a","b"
"2","2"
"3","3"
"3","3"
"3","3"
"-2","-2"
"-2","-2"
"-2","-2"
"0","0"


-- provided
SELECT cos(-1) a, cos(0) b, cos(1) c;

-- result
"a","b","c"
"0.540302306","1.0","0.540302306"


-- provided
SELECT COT(1) AS a, SAFE.COT(1) AS b;

-- expected
SELECT COT(1) AS a, /* Approximation: SAFE prefix is not supported*/ COT(1) AS b;

-- result
"a","b"
"0.642092616","0.642092616"


-- provided
select x, y, div(x, y) res
from unnest( [
        STRUCT( 20 as x, 4 as y)
        , (12, -7)
        , (20,  3)
        , (0,20)
        ] );

-- expected
SELECT  x
        , y
        , Divide( x, y ) res
FROM (  SELECT Unnest(  [
                            { x:20, y:4 }
                            , ( 12, - 7 )
                            , ( 20, 3 )
                            , ( 0, 20 )
                        ], recursive => TRUE ) )
;

-- result
"x","y","res"
"20","4","5"
"12","-7","-1"
"20","3","6"
"0","20","0"


-- provided
select exp(2.7) exp;

-- result
"exp"
"14.879731725"


-- provided
SELECT Floor( Unnest( [2.0, 2.3, 2.8, 2.5, -2.3, -2.8, -2.5, 0] ) ) floor
;

-- result
"floor"
"2"
"2"
"2"
"2"
"-3"
"-3"
"-3"
"0"

-- provided
SELECT Greatest(3, 5, 1) greatest;

-- result
"greatest"
"5"


-- provided
SELECT Is_Inf(25) a, Is_Inf('-inf'::FLOAT) b, Is_Inf('+inf'::FLOAT) c;

-- expected
SELECT IsInf(25) a, IsInf('-Infinity'::FLOAT) b, IsInf('+Infinity'::FLOAT) c;

-- result
"a","b","c"
"false","true","true"


-- provided
SELECT Is_Nan(25) a, Is_Nan( 'Nan'::FLOAT) b;

-- expected
SELECT IsNan(25) a, IsNan( 'Nan'::FLOAT) b;

-- result
"a","b"
"false","true"


-- provided
SELECT Least(3, 5, 1) least;

-- result
"least"
"1"


-- provided
SELECT Ln(2.7) ln;

-- result
"ln"
"0.993251773"


-- provided
SELECT Log( 100,  10 ) log;

-- expected
SELECT Divide( Ln( 100 ), Ln( 10 ) ) log;

-- result
"log"
"2.0"

-- provided
SELECT Log10( 100 ) log;

-- result
"log"
"2.0"


-- provided
SELECT Mod( 25, 12) mod;

-- result
"mod"
"1"


-- provided
SELECT Pow( 2, 3) a, Pow( 1, 'Nan'::FLOAT) b, Pow( 1, '-inf'::FLOAT) c, Pow( 0.1, '-inf'::FLOAT) d;

-- expected
SELECT Pow( 2, 3) a, Pow( 1, 'Nan'::FLOAT) b, Pow( 1, '-Infinity'::FLOAT) c, Pow( 0.1, '-Infinity'::FLOAT) d;

-- result
"a","b","c","d"
"8.0","1.0","1.0","∞"


-- provided
Select Rand() rand;

-- expected
Select Random() rand;

-- tally
1


-- provided
select RANGE_BUCKET(20, [0, 10, 20, 30, 40]) a -- 3 is return value
       , RANGE_BUCKET(20, [0, 10, 20, 20, 40, 40]) b -- 4 is return value
       , RANGE_BUCKET(25, [0, 10, 20, 30, 40]) c -- 3 is return value
       , RANGE_BUCKET(-10, [5, 10, 20, 30, 40]) d -- 0 is return value
;

-- expected
SELECT  Len( List_Filter( [0, 10, 20, 30, 40], x -> x <= 20 ) ) a
        , Len( List_Filter( [0, 10, 20, 20, 40, 40], x -> x <= 20 ) ) b
        , Len( List_Filter( [0, 10, 20, 30, 40], x -> x <= 25 ) ) c
        , Len( List_Filter( [5, 10, 20, 30, 40], x -> x <= -10 ) ) d
;

-- result
"a","b","c","d"
"3","4","3","0"


-- provided
WITH students AS
(
  SELECT 9 AS age UNION ALL
  SELECT 20 AS age UNION ALL
  SELECT 25 AS age UNION ALL
  SELECT 31 AS age UNION ALL
  SELECT 32 AS age UNION ALL
  SELECT 33 AS age
)
SELECT RANGE_BUCKET(age, [10, 20, 30]) AS age_group, COUNT(*) AS count
FROM students
GROUP BY 1
ORDER BY 1
;

-- expected
WITH students AS
(
  SELECT 9 AS age UNION ALL
  SELECT 20 AS age UNION ALL
  SELECT 25 AS age UNION ALL
  SELECT 31 AS age UNION ALL
  SELECT 32 AS age UNION ALL
  SELECT 33 AS age
)
SELECT  Len( List_Filter( [10, 20, 30], x -> x <= age ) ) AS age_group
        , Count( * ) AS count
FROM students
GROUP BY 1
ORDER BY 1
;

-- result
"age_group","count"
"0","1"
"2","2"
"3","3"


-- provided
Select ROUND(2.0) i, 2.0 o
UNION ALL SELECT ROUND(2.3),  2.0
UNION ALL SELECT ROUND(2.8),  3.0
UNION ALL SELECT ROUND(2.5),  3.0
UNION ALL SELECT ROUND(-2.3),     -2.0
UNION ALL SELECT ROUND(-2.8),     -3.0
UNION ALL SELECT ROUND(-2.5),     -3.0
UNION ALL SELECT ROUND(123.7, -1),    120.0
UNION ALL SELECT ROUND(1.235, 2),     1.24
UNION ALL SELECT ROUND('2.25'::NUMERIC, 1, 'ROUND_HALF_EVEN'),     2.2
UNION ALL SELECT ROUND('2.35'::NUMERIC, 1, 'ROUND_HALF_EVEN'),     2.4
UNION ALL SELECT ROUND('2.251'::NUMERIC, 1, 'ROUND_HALF_EVEN'),    2.3
UNION ALL SELECT ROUND('-2.5'::NUMERIC, 0, 'ROUND_HALF_EVEN'),     -2
UNION ALL SELECT ROUND('2.5'::NUMERIC, 0, 'ROUND_HALF_AWAY_FROM_ZERO'),    3
UNION ALL SELECT ROUND('-2.5'::NUMERIC, 0, 'ROUND_HALF_AWAY_FROM_ZERO'),   -3
;

-- expected
Select ROUND(2.0) i,     2.0 o
UNION ALL SELECT ROUND(2.3),  2.0
UNION ALL SELECT ROUND(2.8),  3.0
UNION ALL SELECT ROUND(2.5),  3.0
UNION ALL SELECT ROUND(-2.3),     -2.0
UNION ALL SELECT ROUND(-2.8),     -3.0
UNION ALL SELECT ROUND(-2.5),     -3.0
UNION ALL SELECT ROUND(123.7, -1),    120.0
UNION ALL SELECT ROUND(1.235, 2),     1.24
UNION ALL SELECT ROUND_EVEN('2.25'::NUMERIC, 1),     2.2
UNION ALL SELECT ROUND_EVEN('2.35'::NUMERIC, 1),     2.4
UNION ALL SELECT ROUND_EVEN('2.251'::NUMERIC, 1),    2.3
UNION ALL SELECT ROUND_EVEN('-2.5'::NUMERIC, 0),     -2
UNION ALL SELECT ROUND('2.5'::NUMERIC, 0),    3
UNION ALL SELECT ROUND('-2.5'::NUMERIC, 0),   -3
;

-- result
"i","o"
"2.0","2.0"
"2.0","2.0"
"3.0","3.0"
"3.0","3.0"
"-2.0","-2.0"
"-3.0","-3.0"
"-3.0","-3.0"
"120.0","120.0"
"1.24","1.24"
"2.2","2.2"
"2.4","2.4"
"2.3","2.3"
"-2.0","-2.0"
"3.0","3.0"
"-3.0","-3.0"


-- provided
SELECT SAFE_ADD(5, 4) a;

-- expected
SELECT /* Approximation: SAFE variant not supported */ ADD(5, 4) a;

-- result
"a"
"9"


-- provided
SELECT SAFE_DIVIDE(5, 4) a;

-- expected
SELECT IF(4=0.0 OR 4 IS NULL OR 5 IS NULL,NULL,5/4)A;

-- result
"a"
"1.25"


-- provided
SELECT SAFE_MULTIPLY(5, 4) a;

-- expected
SELECT /* Approximation: SAFE variant not supported */ MULTIPLY(5, 4) a;

-- result
"a"
"20"


-- provided
SELECT SAFE_NEGATE(5) a;

-- expected
SELECT /* Approximation: SAFE variant not supported */ MULTIPLY(5, -1) a;

-- result
"a"
"-5"


-- provided
SELECT SAFE_SUBTRACT(5, 4) a;

-- expected
SELECT /* Approximation: SAFE variant not supported */ SUBTRACT(5, 4) a;

-- result
"a"
"1"


-- provided
SELECT Sign(-5) sign;

-- result
"sign"
"-1"


-- provided
SELECT Sin(3.124) sin;

-- result
"sin"
"0.017591746"


-- provided
SELECT SQRT(25) sqrt;

-- result
"sqrt"
"5.0"


-- provided
SELECT Tan(25) tan;

-- result
"tan"
"-0.133526407"


-- provided
SELECT Trunc(2.3) a, Trunc(2.3, 1) b;

-- expected
SELECT Trunc(2.3) a, Round(2.3, 1) b;

-- result
"a","b"
"2","2.3"


