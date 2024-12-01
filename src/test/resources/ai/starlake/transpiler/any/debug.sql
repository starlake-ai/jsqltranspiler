--provided
SELECT ARRAY(
           SELECT CAST(integer_element AS INT64)
  FROM UNNEST(
    JSON_EXTRACT_ARRAY('[1,2,3]','$')
  ) AS integer_element
) AS integer_array;

--expected
SELECT List_Sort(Array(SELECT CAST(integer_element AS INT64) FROM (SELECT UNNEST(JSon_Extract('[1,2,3]', '$[*]')) AS integer_element) AS integer_element)) AS integer_array

--result
"integer_array"
"[1, 2, 3]"