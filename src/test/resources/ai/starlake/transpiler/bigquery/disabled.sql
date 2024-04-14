
-- provided
SELECT APPROX_TOP_COUNT(x, 2) as approx_top_count
FROM UNNEST(["apple", "apple", "pear", "pear", "pear", "banana"]) as x;


-- expected
SELECT list( struct_pack( x := x, y := y) ) approx_top_count
from (
    SELECT x, count(x) y
    FROM (SELECT UNNEST(['apple', 'apple', 'pear', 'pear', 'pear', 'banana']) as x)
    group by x
    order by 2 desc
    limit 2
)
;

-- result
"approx_top_count"
"[{x=pear, y=3}, {x=apple, y=2}]"


-- provided
SELECT APPROX_TOP_SUM(x, weight, 2) AS approx_top_sum FROM
UNNEST([
  STRUCT("apple" AS x, 3 AS weight),
  ("pear", 2),
  ("apple", 0),
  ("banana", 5),
  ("pear", 4)
]);


-- expected
SELECT list( struct_pack( x:= x, y:= y) ) approx_top_sum
from (
    SELECT x, sum(weight) y
    FROM (  SELECT Unnest(  [
                                { x:'apple',weight:3 }
                                , ( 'pear', 2 )
                                , ( 'apple', 0 )
                                , ( 'banana', 5 )
                                , ( 'pear', 4 )
                            ], recursive => TRUE ) )
    group by x
    order by 2 desc
    limit 2
)
;

-- result
"approx_top_sum"
"[{x=pear, y=6}, {x=banana, y=5}]"



-- provided
SELECT
  ARRAY
    (SELECT AS STRUCT 1, 2, 3
     UNION ALL SELECT AS STRUCT 4, 5, 6) AS new_array;

-- expected
SELECT
  ARRAY
    (SELECT [1, 2, 3]
     UNION ALL SELECT [4, 5, 6]) AS new_array;

-- result
"new_array"
"[[4, 5, 6], [1, 2, 3]]"



-- provided
select extract(us from time '18:25:33.123456') AS micros;

-- expected
select extract(us from time '18:25:33.123456') % 1000000 AS micros;

-- result
"micros"
"123456"

