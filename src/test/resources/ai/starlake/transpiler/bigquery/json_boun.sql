--provided
SELECT LAX_FLOAT64(JSON '"+1.5"') AS result;

--expected
SELECT Cast(JSON '"+1.5"' AS Double) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "+1.5"

--result
"result"
"1.5"

--provided
SELECT LAX_FLOAT64(JSON '"NaN"') AS result;

--expected
SELECT Cast(JSON '"NaN"' AS Double) AS result

--output
"result"
"NaN"

--result
INVALID_INPUT_QUERY Character N is neither a decimal digit number, decimal point, nor "e" notation exponential mark.


--provided
SELECT LAX_FLOAT64(JSON '"Inf"') AS result;

--expected
SELECT Cast(JSON '"Inf"' AS Double) AS result

--output
"result"
"∞"

--result
INVALID_INPUT_QUERY Character I is neither a decimal digit number, decimal point, nor "e" notation exponential mark.


--provided
SELECT LAX_FLOAT64(JSON '"-InfiNiTY"') AS result;

--expected
SELECT Cast(JSON '"-InfiNiTY"' AS Double) AS result

--output
"result"
"-∞"

--result
INVALID_INPUT_QUERY Character I is neither a decimal digit number, decimal point, nor "e" notation exponential mark.


--provided
SELECT LAX_FLOAT64(JSON '"foo"') AS result;

--expected
SELECT Cast(JSON '"foo"' AS Double) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "foo"

--result
"result"
"JSQL_NULL"


--provided
SELECT LAX_INT64(JSON '1e100') AS result;

--expected
SELECT Cast(JSON '1e100' AS HugeInt) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: 1e100

--result
"result"
"JSQL_NULL"



--provided
SELECT LAX_INT64(JSON '"1.1"') AS result;

--expected
SELECT Cast(JSON '"1.1"' AS HugeInt) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "1.1"

--result
"result"
"1"


--provided
SELECT LAX_INT64(JSON '"1.1e2"') AS result;

--expected
SELECT Cast(JSON '"1.1e2"' AS HugeInt) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "1.1e2"

--result
"result"
"110"


--provided
SELECT LAX_INT64(JSON '"+1.5"') AS result;

--expected
SELECT Cast(JSON '"+1.5"' AS HugeInt) AS result

--output
INVALID_TRANSLATION Conversion Error: Failed to cast value to numerical: "+1.5"

--result
"result"
"2"










