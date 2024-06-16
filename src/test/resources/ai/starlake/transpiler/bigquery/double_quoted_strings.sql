-- provided
WITH Items AS (
  SELECT [] AS numbers, "Empty array in query" AS description UNION ALL
  SELECT CAST(NULL AS ARRAY<INT64>), "NULL array in query")
SELECT numbers, description, numbers IS NULL AS numbers_null
FROM Items;

-- expected
WITH items AS (
        SELECT  [ ] AS numbers
                , 'Empty array in query' AS description
        UNION ALL
        SELECT   Cast( NULL AS INT64[] )
                , 'NULL array in query' )
SELECT  numbers
        , description
        ,
            numbers IS NULL AS numbers_null
FROM items
;

--result
"numbers","description","numbers_null"
"[]","Empty array in query","false"
"","NULL array in query","true"

