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
SELECT instr('abcbarbar', 'bar' ) AS i1, ifplus( instr(substring('abcbarbar', 5), 'bar'), 0, 5-1) i2;

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


-- provided
SELECT lpad('hi', 5, 'ab') AS padded;

-- expected
SELECT CASE TYPEOF('hi')WHEN 'VARCHAR' THEN LPAD('hi'::VARCHAR,5,'ab')END AS PADDED;

-- result
"padded"
"abahi"


-- provided
SELECT '+' || ltrim('abc', 'acbabSparkSQL   ') || '+' AS trimmed;

-- expected
SELECT '+' || ltrim('acbabSparkSQL   ', 'abc') || '+' AS trimmed;

-- result
"trimmed"
"+SparkSQL   +"


-- provided
SELECT md5('Spark') AS md5;

-- result
"md5"
"8cde774d6f7333752ed72cacddb05126"


-- provided
SELECT octet_length('Spark SQL') as l1, octet_length('서울시') as l2;

-- expected
SELECT OCTET_LENGTH(CASE TYPEOF('Spark SQL')
                    WHEN 'VARCHAR' THEN ENCODE('Spark SQL'::VARCHAR)
                    ELSE ENCODE('Spark SQL')
                    END)AS L1
        ,OCTET_LENGTH(CASE TYPEOF('서울시')
                    WHEN 'VARCHAR' THEN ENCODE('서울시'::VARCHAR)
                    ELSE ENCODE('서울시')
                    END)AS L2;

-- result
"l1","l2"
"9","9"


-- provided
SELECT position('bar', 'abcbarbar') as p1, position('bar' IN 'abcbarbar') AS p2, position('bar', 'abcbarbar', 5) as p3;

-- expected
SELECT INSTR('abcbarbar','bar')AS P1,POSITION('bar' IN 'abcbarbar')AS P2,IFPLUS(INSTR(SUBSTRING('abcbarbar',5),'bar'),0,5-1)AS P3;

-- result
"p1","p2","p3"
"4","4","7"


-- provided
SELECT printf('Hello World %d %s', 100, 'days') AS out;

-- result
"out"
"Hello World 100 days"


-- provided
SELECT regexp_like('%SystemDrive%\\Users\\John', '%SystemDrive%\\\\Users.*') AS b;

-- expected
SELECT REGEXP_MATCHES('%SystemDrive%\Users\John','%SystemDrive%\\Users.*')AS B

-- result
"b"
"true"


-- provided
SELECT r'%SystemDrive%\Users\John' rlike r'%SystemDrive%\\Users.*' AS b;

-- expected
SELECT '%SystemDrive%\Users\John' SIMILAR TO '%SystemDrive%\\Users.*' AS B;

-- result
"b"
"true"


-- provided
SELECT regexp_count('Steven Jones and Stephen Smith are the best players', 'Ste(v|ph)en') AS count;

-- expected
SELECT LENGTH(REGEXP_SPLIT_TO_ARRAY('Steven Jones and Stephen Smith are the best players','Ste(v|ph)en'))-1 AS COUNT;

-- result
"count"
"2"


-- provided
SELECT regexp_extract('100-200', '(\\d+)-(\\d+)', 1) AS s;

-- expected
SELECT REGEXP_EXTRACT('100-200','(\d+)-(\d+)', 1)AS S

-- result
"s"
"100"


-- provided
SELECT regexp_extract_all('100-200, 300-400', '(\\d+)-(\\d+)', 1) AS s;

-- expected
SELECT Regexp_Extract_All( '100-200, 300-400', '(\d+)-(\d+)', 1 ) AS s
;

-- result
"s"
"[100, 300]"


-- provided
SELECT regexp_instr('Steven Jones and Stephen Smith are the best players', 'Ste(v|ph)en') AS s;

-- expected
SELECT CASE
        WHEN LENGTH(REGEXP_SPLIT_TO_ARRAY('Steven Jones and Stephen Smith are the best players','Ste(v|ph)en'))>1
        THEN LENGTH(REGEXP_SPLIT_TO_ARRAY('Steven Jones and Stephen Smith are the best players','Ste(v|ph)en')[1])+1
        ELSE 0
        END AS S;
-- result
"s"
"1"


-- provided
SELECT regexp_replace('100-200', '(\\d+)', 'num') AS s;

-- expected
SELECT regexp_replace('100-200', '(\d+)', 'num', 'g') AS s;

-- result
"s"
"num-num"


-- provided
SELECT regexp_substr('Steven Jones and Stephen Smith are the best players', 'Ste(v|ph)en') AS s;

-- expected
SELECT regexp_extract('Steven Jones and Stephen Smith are the best players', 'Ste(v|ph)en') AS s;

-- result
"s"
"Steven"


-- provided
SELECT repeat('123', 2) AS s;

-- result
"s"
"123123"


-- provided
SELECT replace('ABCabcABCabc', 'abc', 'DEF') AS s;

-- result
"s"
"ABCDEFABCDEF"


-- provided
SELECT reverse('Spark SQL') AS s;

-- result
"s"
"LQS krapS"


-- provided
SELECT right('Spark SQL', 3) AS s;

-- result
"s"
"SQL"


-- provided
SELECT rpad('hi', 5, 'ab') AS s;

-- expected
SELECT CASE TYPEOF('hi')WHEN 'VARCHAR' THEN RPAD('hi'::VARCHAR,5,'ab')END AS S;

-- result
"s"
"hiaba"


-- provided
SELECT sha2('Spark', 256) as s;

-- expected
SELECT sha256('Spark') as s;

-- result
"s"
"529bc3b07127ecb7e53a4dcf1991d9152c24537d919178022b2c42657f79a26b"


-- provided
SELECT space(10) as s;

-- expected
SELECT repeat(' ', 10) as s;

-- result
"s"
"          "


-- provided
SELECT split('oneAtwoBthreeC', '[ABC]') AS s;

-- expected
SELECT regexp_split_to_array('oneAtwoBthreeC', '[ABC]') AS s;

-- result
"s"
"[one, two, three, ]"


-- provided
SELECT '->' || split_part('Hello,world,!', ',', 2) || '<-' AS s;

-- result
"s"
"->world<-"


-- provided
SELECT string(5) AS s;

-- expected
SELECT 5::VARCHAR AS s;

-- result
"s"
"5"


-- provided
SELECT substr('Spark SQL' FROM 5 FOR 1) AS s;

-- expected
SELECT SubString('Spark SQL' FROM 5 FOR 1) AS s;

-- result
"s"
"k"


-- provided
SELECT substring_index('www.apache.org', '.', 2) AS i;

-- expected
select list_aggregate(regexp_split_to_array('www.apache.org', regexp_escape('.'))[1:2], 'string_agg', '.') AS i;

-- result
"i"
"www.apache"


-- provided
SELECT cast(to_binary('537061726B') AS STRING) AS s;

-- expected
SELECT cast(unhex('537061726B') AS String) AS s;

-- result
"s"
"Spark"


-- provided
SELECT cast(to_binary('537061726B', 'hex') AS STRING) AS s;

-- expected
SELECT cast(unhex('537061726B') AS String) AS s;

-- result
"s"
"Spark"

-- provided
SELECT cast(to_binary('U3Bhcms=', 'base64') AS STRING) AS s;

-- expected
SELECT cast(From_Base64('U3Bhcms=') AS STRING) AS s;

-- result
"s"
"Spark"


-- provided
SELECT hex(to_binary('서울시(Seoul)', 'UTF-8')) AS s;

-- expected
SELECT Hex(encode('서울시(Seoul)')) AS s;

-- result
"s"
"EC849CEC9AB8EC8B9C2853656F756C29"


-- provided
SELECT cast(unbase64('U3BhcmsgU1FM') AS STRING) AS s;

-- expected
SELECT cast(From_Base64('U3BhcmsgU1FM') AS STRING) AS s;

-- result
"s"
"Spark SQL"







































































