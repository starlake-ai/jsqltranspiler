-- provided
SELECT t, len, LPAD(t, len) AS padded FROM UNNEST([
  STRUCT<t VARCHAR, len integer>('abc', 5 ),
  ('abc', 2),
  ('例子', 4)
]);

-- expected
SELECT t, len, CASE TYPEOF(T) WHEN 'VARCHAR' THEN LPAD(T::VARCHAR, LEN,' ') END AS padded from (
select Unnest([
  { t:'abc', len:5 }::STRUCT(t VARCHAR, len integer),
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
