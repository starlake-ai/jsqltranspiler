-- provided
select safe_cast('_' as FLOAT64) as r;

-- expected
select try_cast('_' as FLOAT8) as r;

-- result
"r"
""
