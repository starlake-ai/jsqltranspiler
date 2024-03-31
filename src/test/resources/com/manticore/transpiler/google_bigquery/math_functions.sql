-- provided
SELECT -25 a, ABS(25) b;

-- result
"a","b"
"-25","25"


-- provided
SELECT acos(-1) a, acos(0) b, acos(1) c;

--result
"a","b","c"
"3.141592653589793","1.5707963267948966","0.0"


-- provided
SELECT asin(-1) a, asin(0) b, asin(1) c;

-- result
"a","b","c"
"-1.5707963267948966","0.0","1.5707963267948966"


-- provided
SELECT atan(-1) a, atan(0) b, atan(1) c;

-- result
"a","b","c"
"-0.7853981633974483","0.0","0.7853981633974483"


-- provided
SELECT atan2(-1, -1) a, atan2(0, 0) b, atan2(1, 1) c;

-- result
"a","b","c"
"-2.356194490192345","0.0","0.7853981633974483"


-- provided
select CBRT(27) a;

-- result
"a"
"3.0000000000000004"


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
"0.5403023058681398","1.0","0.5403023058681398"


-- provided
SELECT COT(1) AS a, SAFE.COT(1) AS b;

-- expected
SELECT COT(1) AS a, /* Approximation: SAFE prefix is not supported*/ COT(1) AS b;

-- result
"a","b"
"0.6420926159343306","0.6420926159343306"


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

