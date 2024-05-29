-- provided
SELECT max_by(x, y) AS max_by FROM VALUES (('a', 10)), (('b', 50)), (('c', 20)) AS tab(x, y);

-- expected
SELECT max_by(x, y) AS max_by FROM VALUES ('a', 10), ('b', 50), ('c', 20) AS tab(x, y);

-- result
"max_by"
"b"