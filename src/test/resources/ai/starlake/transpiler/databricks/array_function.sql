-- provided
SELECT a[2] AS e FROM VALUES(array(10, 20, 30)) AS T(a);

-- expected
SELECT a[2+1] AS e FROM VALUES([10, 20, 30]) AS T(a);

-- result
"e"
"30"


-- provided
SELECT array(1, 2, 3)  AS arr;

-- expected
SELECT [1, 2, 3] AS arr;

-- result
"arr"
"[1, 2, 3]"


-- provided
SELECT array_append(array(1, 2, 3), 0) AS arr;

-- expected
SELECT [1, 2, 3] || [0] AS arr;

-- result
"arr"
"[1, 2, 3, 0]"


-- provided
SELECT array_compact(array(1, 2, NULL, 3, NULL, 3)) AS arr;

-- expected
SELECT list_filter([1, 2, NULL, 3, NULL, 3], x -> x IS NOT NULL) AS arr;

-- result
"arr"
"[1, 2, 3, 3]"


-- provided
SELECT array_contains([1, 2, 3], 2) AS b;

-- result
"b"
"true"


-- provided
SELECT array_distinct([1, 2, 3, NULL, 3]) AS arr;

-- result
"arr"
"[3, 2, 1]"


-- provided
SELECT array_except([1, 2, 2, 3], [1, 1, 3, 5]) AS arr;

-- expected
SELECT list_distinct(list_filter([1, 2, 2, 3], x -> NOT array_contains(list_intersect([1, 2, 2, 3], [1, 1, 3, 5]), x) )) AS arr;

-- result
"arr"
"[2]"
