-- provided
SELECT commission, TRUNC(commission) AS trunc
FROM sales WHERE salesid=784;

-- result
"commission","trunc"
"111.15","111"

-- provided
select format('{:CC}', DATE '2023-12-31') century;

-- expected
"century"
"21"

