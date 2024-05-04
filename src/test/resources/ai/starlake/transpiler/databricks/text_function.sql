-- provided
SELECT ascii('234') AS ascii;

-- result
"ascii"
"50"

-- provided
SELECT base64('Spark SQL') AS b;

-- result
"b"
"U3BhcmsgU1FM"


-- provided
SELECT bin(13) AS bits;

-- result
"bits"
"1101"

-- provided
SELECT binary('Spark SQL') AS binary;

-- expected
SELECT encode('Spark SQL') AS binary;

-- count
1


-- provided
SELECT bit_length('Spark SQL') AS b1, bit_length('北京') AS b2;

-- result
"b1","b2"
"72","48"

-- provided
SELECT bitmap_count(X'7700CC') AS bits;

-- expected
SELECT BIT_COUNT(7798988::BIT)AS BITS

-- result
"bits"
"10"

-- provided
SELECT 'X' || btrim('    SparkSQL   ') || 'X' AS trimmed;

-- expected
SELECT 'X' || trim('    SparkSQL   ') || 'X' AS trimmed;

-- result
"trimmed"
"XSparkSQLX"


-- provided
SELECT btrim('abcaabaSparkSQLabcaaba', 'abc') AS trimmed;

-- expected
SELECT trim('abcaabaSparkSQLabcaaba', 'abc') AS trimmed;

-- result
"trimmed"
"SparkSQL"


-- provided
SELECT char(65) AS char;

-- expected
SELECT chr(65) AS char;

-- result
"char"
"A"


-- provided
SELECT char_length('Spark SQL ') AS l1, char_length('床前明月光') AS l2;

-- expected
SELECT len('Spark SQL ') AS l1, len('床前明月光') AS l2;

-- result
"l1","l2"
"10","5"


-- provided
SELECT charindex('bar', 'abcbarbar') AS i1, charindex('bar', 'abcbarbar', 5) i2;

-- expected
SELECT instr('abcbarbar', 'bar' ) AS i1, ifplus( instr(substr('abcbarbar', 5), 'bar'), 0, 5-1) i2;

-- result
"i1","i2"
"4","7"


-- provided
SELECT chr(65) AS CHR;

-- result
"CHR"
"A"


-- provided
SELECT concat('Spark', 'SQL') AS concat;

-- result
"concat"
"SparkSQL"


-- provided
SELECT concat_ws(' ', 'Spark', 'SQL') AS concat;

-- result
"concat"
"Spark SQL"


-- provided
SELECT contains('SparkSQL', 'ark') as b;

-- result
"b"
"true"


-- provided
SELECT endswith('SparkSQL', 'SQL') AS b;

-- expected
SELECT ends_with('SparkSQL', 'SQL') AS b;

-- result
"b"
"true"


-- provided
SELECT instr('SparkSQL', 'SQL') AS p;

-- result
"p"
"6"


-- provided
SELECT find_in_set('ab','abc,b,ab,c,def') AS p;

-- expected
SELECT list_position(str_split_regex('abc,b,ab,c,def', ','), 'ab') AS p;

-- result
"p"
"3"


-- provided
SELECT hex('Spark SQL') AS hex;

-- result
"hex"
"537061726B2053514C"


-- provided
SELECT '%SystemDrive%/Users/John' like '/%SystemDrive/%//Users%' ESCAPE '/' AS b;

-- result
"b"
"true"


-- provided
SELECT lcase('LowerCase') AS lcase;

-- result
"lcase"
"lowercase"


-- provided
SELECT left('Spark SQL', 3) AS l;

-- result
"l"
"Spa"


-- provided
SELECT len('Spark SQL ') AS l1, len('床前明月光') AS l2;

-- expected
SELECT CASE TYPEOF('Spark SQL ')WHEN 'VARCHAR' THEN LENGTH(TRY_CAST('Spark SQL ' AS VARCHAR))WHEN 'BLOB' THEN OCTET_LENGTH(TRY_CAST('Spark SQL ' AS BLOB))END AS L1,CASE TYPEOF('床前明月光')WHEN 'VARCHAR' THEN LENGTH(TRY_CAST('床前明月光' AS VARCHAR))WHEN 'BLOB' THEN OCTET_LENGTH(TRY_CAST('床前明月光' AS BLOB))END AS L2;

-- result
"l1","l2"
"10","5"

-- provided
SELECT levenshtein('kitten', 'sitting', 4) AS l;

-- expected
SELECT Least(levenshtein('kitten', 'sitting'), 4) AS l;

-- result
"l"
"3"















































































