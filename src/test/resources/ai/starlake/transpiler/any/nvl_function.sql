-- provided
SELECT Nvl( NULL, 1 ) a
;

-- expected
SELECT Coalesce( NULL, 1 ) a
;

-- count
1

-- results
"a"
"1"