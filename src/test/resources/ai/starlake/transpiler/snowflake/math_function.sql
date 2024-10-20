-- provided
SELECT random()  AS rand FROM table(generator(rowCount => 3));

-- expected
SELECT CAST((RANDOM()-0.5)*1E19 AS INT64)AS RAND FROM(SELECT RANGE AS SEQ4 FROM RANGE(0,3))

-- count
3


-- provided
SELECT DIV0(1, 0)  AS div0;

-- expected
SELECT COALESCE(DIVIDE(1,0), 0) AS div0;

-- result
"div0"
"0"


-- provided
SELECT MOD(3, 2) AS mod1, MOD(4.5, 1.2) AS mod2;

-- result
"mod1","mod2"
"1","0.9"

-- provided
SELECT ROUND(2.5, 0) AS r1, ROUND(2.5, 0, 'HALF_TO_EVEN') AS r2;

-- expected
SELECT ROUND(2.5, 0) AS r1, ROUND_EVEN(2.5, 0) AS r2;

-- result
"r1","r2"
"3","2.0"


-- provided
SELECT FACTORIAL(0), FACTORIAL(1), FACTORIAL(5), FACTORIAL(10);

-- result
"factorial(0)","factorial(1)","factorial(5)","factorial(10)"
"1","1","120","3628800"


-- provided
select square(12) AS square;

-- expected
select power(12,2) AS square;

-- returns
"square"
"144.0"
