-- provided
SELECT t, len, LPAD(t, len) AS padded FROM UNNEST([
  STRUCT<t string, len integer>('abc', 5 ),
  ('abc', 2),
  ('例子', 4)
]);

-- expected
SELECT t, len, CASE TYPEOF(T) WHEN 'VARCHAR' THEN LPAD(T::VARCHAR, LEN,' ') END AS padded from (
select Unnest([
  { t:'abc', len:5 }::STRUCT(t string, len integer),
  ('abc', 2),
  ('例子', 4)
], recursive => true)
);

-- result
"t","len","padded"
"abc","5","  abc"
"abc","2","ab"
"例子","4","  例子"


-- provided
SELECT t, len, LPAD(t, len) AS padded FROM UNNEST([
  STRUCT('abc' AS t, 5 AS len),
  ('abc', 2),
  ('例子', 4)
]);

-- expected
SELECT t, len, CASE TYPEOF(T) WHEN 'VARCHAR' THEN LPAD(T::VARCHAR, LEN,' ') END AS padded from (
select Unnest([
  { t:'abc', len:5 },
  ('abc', 2),
  ('例子', 4)
], recursive => true)
);

-- result
"t","len","padded"
"abc","5","  abc"
"abc","2","ab"
"例子","4","  例子"


-- provided
SELECT AS VALUE STRUCT(1 AS a, 2 AS b) xyz;

-- expected
SELECT xyz.* FROM (SELECT { a:1,b:2 } xyz);

-- result
"a","b"
"1","2"


-- provided
SELECT AS STRUCT 1 a, 2 b;

-- expected
SELECT {a:1, b:2} AS value_table;

-- result
"value_table"
"{a=1, b=2}"

