-- provided
SELECT ARRAY
  (SELECT 1 UNION ALL
   SELECT 2 UNION ALL
   SELECT 3 order by 1) AS new_array;

-- expected
SELECT List_Sort(ARRAY
  (SELECT 1 UNION ALL
   SELECT 2 UNION ALL
   SELECT 3 order by 1)) AS new_array;

-- result
"new_array"
"[1, 2, 3]"


-- provided
SELECT ARRAY_CONCAT([1, 2], [3, 4], [5, 6]) as count_to_six;

-- expected
select [1, 2] || [3, 4] || [5, 6] as count_to_six;

-- result
"count_to_six"
"[1, 2, 3, 4, 5, 6]"


-- provided
WITH items AS
  (SELECT ['coffee', NULL, 'milk' ] as list
  UNION ALL
  SELECT ['cake', 'pie'] as list)
SELECT ARRAY_TO_STRING(list, '++', 'MISSING') AS text
        , ARRAY_LENGTH(list) AS size
FROM items
ORDER BY size DESC;

-- expected
WITH items AS
  (SELECT ['coffee', NULL, 'milk' ] as list
  UNION ALL
  SELECT ['cake', 'pie'] as list)
SELECT Array_To_String( list_transform(list, x -> Coalesce( x, 'MISSING' )), '++' ) AS text
    , ARRAY_LENGTH(list) AS size
FROM items
ORDER BY size DESC;

-- result
"text","size"
"coffee++MISSING++milk","3"
"cake++pie","2"


-- provided
WITH example AS (
  SELECT [1, 2, 3, 1, 2, 3] AS arr UNION ALL
  SELECT [4, 5] AS arr UNION ALL
  SELECT [] AS arr
)
SELECT
  ARRAY_REVERSE(arr) AS reverse_arr
FROM example;

-- result
"reverse_arr"
"[3, 2, 1, 3, 2, 1]"
"[5, 4]"
"[]"
