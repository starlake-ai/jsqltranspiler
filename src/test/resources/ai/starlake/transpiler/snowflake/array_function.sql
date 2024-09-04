-- provided
SELECT ARRAY_AGG(O_ORDERKEY) WITHIN GROUP (ORDER BY O_ORDERKEY ASC)  AS ARR
  FROM orders
  WHERE O_TOTALPRICE > 450000;

-- expected
SELECT ARRAY_AGG(O_ORDERKEY ORDER BY O_ORDERKEY ASC)AS ARR FROM ORDERS WHERE O_TOTALPRICE>450000;

-- result
"ARR"
"[29158, 59873, 89924, 279812, 343430, 555331, 926213, 1083941, 1105571]"


-- provided
SELECT ARRAY_AGG(DISTINCT O_ORDERSTATUS) WITHIN GROUP (ORDER BY O_ORDERSTATUS ASC)  AS ARR
  FROM orders
  WHERE O_TOTALPRICE > 450000;

-- expected
SELECT ARRAY_AGG(DISTINCT O_ORDERSTATUS ORDER BY O_ORDERSTATUS ASC)AS ARR
FROM ORDERS
WHERE O_TOTALPRICE>450000;

-- result
"ARR"
"[F, O, P]"


-- provided
SELECT
    O_ORDERSTATUS,
    ARRAYAGG(O_CLERK) WITHIN GROUP (ORDER BY O_TOTALPRICE DESC) AS ARR
  FROM orders
  WHERE O_TOTALPRICE > 450000
  GROUP BY O_ORDERSTATUS
  ORDER BY O_ORDERSTATUS DESC;

-- expected
SELECT O_ORDERSTATUS,ARRAY_AGG(O_CLERK ORDER BY O_TOTALPRICE DESC)AS ARR
FROM ORDERS
WHERE O_TOTALPRICE>450000
GROUP BY O_ORDERSTATUS
ORDER BY O_ORDERSTATUS DESC
;

-- result
"o_orderstatus","ARR"
"P","[Clerk#000000967, Clerk#000000734]"
"O","[Clerk#000000525, Clerk#000000752, Clerk#000000397, Clerk#000000792]"
"F","[Clerk#000000037, Clerk#000000081, Clerk#000000235]"


-- provided
SELECT ARRAY_APPEND(ARRAY_CONSTRUCT(1, 2, 3), 'HELLO')  AS array;

-- expected
SELECT ARRAY_APPEND(ARRAY[1, 2, 3], 'HELLO') AS array;

-- result
"array"
"[1, 2, 3, HELLO]"


-- provided
SELECT ARRAY_CAT(ARRAY_CONSTRUCT(1, 2), ARRAY_CONSTRUCT(3, 4)) AS array;

-- expected
SELECT ARRAY_CAT(ARRAY[1, 2], ARRAY[3, 4]) AS array;

-- result
"array"
"[1, 2, 3, 4]"


-- provided
select ARRAY_COMPACT(ARRAY_CONSTRUCT(10, NULL, 30)) AS array;

-- expected
select list_filter(ARRAY[10, NULL, 30], x -> x is not null) AS array;

-- result
"array"
"[10, 30]"


-- provided
SELECT ARRAY_CONSTRUCT_COMPACT(null,'hello',3::double,4,5) AS array;

-- expected
select list_filter(ARRAY[null,'hello',3::double,4,5], x -> x is not null) AS array;

-- result
"array"
"[hello, 3.0, 4, 5]"


-- provided
SELECT ARRAY_CONTAINS('hello'::variant, array_construct('hello', 'hi'))  AS contains;

-- expected
SELECT ARRAY_CONTAINS( array['hello', 'hi'], 'hello'::varchar) AS contains;

-- result
"contains"
"true"


-- provided
SELECT ARRAY_DISTINCT(['A', 'A', 'B', NULL, NULL]) AS arr;

-- expected
SELECT LIST_SORT(ARRAY_DISTINCT(['A', 'A', 'B', NULL, NULL])) AS arr;

-- result
"arr"
"[A, B]"


-- provided
SELECT ARRAY_EXCEPT(['A', 'B', 'C'], ['B', 'C']) AS filtered;

-- expected
SELECT list_filter(['A', 'B', 'C'], x -> not list_contains(['B', 'C'],x) ) AS filtered;

-- result
"filtered"
"[A]"


-- provided
SELECT ARRAY_FLATTEN([[1, 2, 3], [4], [5, 6]]) AS flat;

-- expected
SELECT FLATTEN([[1, 2, 3], [4], [5, 6]]) AS flat;

-- result
"flat"
"[1, 2, 3, 4, 5, 6]"


-- provided
SELECT ARRAY_GENERATE_RANGE(5, 25, 10) AS range;

-- expected
SELECT RANGE(5, 25, 10) AS range;

-- result
"range"
"[5, 15]"


-- provided
SELECT ARRAY_INSERT(ARRAY_CONSTRUCT(0,1,2,3),2,'hello') AS array;

-- expected
SELECT ARRAY[0,1,2,3][0:2] || ['hello'] || ARRAY[0,1,2,3][2+1:]  AS array;

-- result
"array"
"[0, 1, hello, 2, 3]"


-- provided
SELECT array_intersection(ARRAY_CONSTRUCT('A', 'B', 'C'),
                          ARRAY_CONSTRUCT('B', 'C')) AS array;

-- expected
SELECT array_intersect(ARRAY['A', 'B', 'C'],
                          ARRAY['B', 'C'])  AS array;

-- result
"array"
"[B, C]"


-- provided
SELECT ARRAY_MAX([20, 0, NULL, 10, NULL])  AS max;

-- expected
SELECT list_reverse_sort(list_filter([20, 0, NULL, 10, NULL], x -> x is not null))[1] AS max;

-- result
"max"
"20"


-- provided
SELECT ARRAY_MIN([20, 0, NULL, 10, NULL])  AS max;

-- expected
SELECT list_sort(list_filter([20, 0, NULL, 10, NULL], x -> x is not null))[1] AS max;

-- result
"max"
"0"


-- provided
SELECT ARRAY_POSITION('hi'::variant, array_construct('hello', 'hi')) AS pos;

-- expected
SELECT nullif(ARRAY_POSITION(array['hello', 'hi'], 'hi'::varchar)-1, -1) AS pos;

-- result
"pos"
"1"


-- provided
SELECT ARRAY_PREPEND(ARRAY_CONSTRUCT(0,1,2,3),'hello')  AS arr;

-- expected
SELECT ARRAY_PREPEND('hello', ARRAY[0,1,2,3]) AS arr;

-- result
"arr"
"[hello, 0, 1, 2, 3]"


-- provided
SELECT ARRAY_REMOVE(
  [1, 5, 5.00, 5.00::DOUBLE, '5', 5, NULL],
  5) AS arr;

-- expected
SELECT list_filter(
  [1, 5, 5.00, 5.00::DOUBLE, '5', 5, NULL], x -> x<>5) AS arr;

-- result
"arr"
"[1.0]"


-- provided
SELECT ARRAY_REMOVE_AT(
  [2, 5, 7],
  0) AS arr;

-- expected
SELECT [2, 5, 7][:0] || [2, 5, 7][0+2:] AS arr;

-- result
"arr"
"[5, 7]"


-- provided
SELECT ARRAY_SIZE(ARRAY_CONSTRUCT(1, 2, 3)) AS SIZE;

-- expected
SELECT len(ARRAY[1, 2, 3]) AS SIZE;

-- result
"SIZE"
"3"


-- provided
SELECT ARRAY_SLICE(ARRAY_CONSTRUCT('foo','snow','flake','bar'), 1, 3) AS slice;

-- expected
SELECT ARRAY_SLICE(ARRAY['foo','snow','flake','bar'], 1+1, 3) AS slice;

-- result
"slice"
"[snow, flake]"


-- provided
SELECT ARRAY_SORT([20, 0, NULL, 10], TRUE, TRUE) AS sorted;

-- expected
SELECT ARRAY_SORT([20,0,NULL,10],IF(TRUE,'ASC','DESC'),IF(TRUE,'NULLS FIRST','NULLS LAST'))AS SORTED;

-- result
"sorted"
"[null, 0, 10, 20]"


-- provided
SELECT array_to_string([1, 2, 3], '-') AS str;

-- result
"str"
"1-2-3"


-- provided
SELECT ARRAYS_OVERLAP(array_construct('hello', 'aloha'),
                      array_construct('hello', 'hi', 'hey'))
  AS Overlap;

-- expected
SELECT len(Array_Intersect(ARRAY['hello', 'aloha'],
                      array['hello', 'hi', 'hey']))>0
  AS Overlap;

-- result
"Overlap"
"true"
