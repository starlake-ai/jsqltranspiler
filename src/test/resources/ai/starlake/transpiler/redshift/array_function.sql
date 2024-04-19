-- provided
select array(1,50,null,100) AS arr;

-- expected
select [1,50,null,100] AS arr;

-- result
"arr"
"[1, 50, null, 100]"


-- provided
select array_concat( array(1,2),  array(3,4) ) AS arr;

-- expected
select [1,2] ||  [3,4] AS arr;

-- result
"arr"
"[1, 2, 3, 4]"


-- provided
select array_flatten( array(array(1,2),  array(3,4),  array(4,5)) ) AS arr;

-- expected
select flatten( [ [1,2],  [3,4], [4,5] ] ) AS arr;

-- result
"arr"
"[1, 2, 3, 4, 4, 5]"


-- provided
SELECT GET_ARRAY_LENGTH(ARRAY(1,2,3,4,5,6,7,8,9,10)) AS len;

-- expected
SELECT Len([1,2,3,4,5,6,7,8,9,10]) AS len;

-- result
"len"
"10"


-- provided
SELECT SPLIT_TO_ARRAY('12|345|6789', '|') AS arr;

-- expected
SELECT regexp_split_to_array('12|345|6789', regexp_escape('|')) AS arr;

-- result
"arr"
"[12, 345, 6789]"


-- provided
SELECT SUBARRAY(ARRAY('a', 'b', 'c', 'd', 'e', 'f'), 2, 3) AS arr;

-- expected
SELECT list_slice(['a', 'b', 'c', 'd', 'e', 'f'], 2 , 3) AS arr;

-- result
"arr"
"[b, c]"


