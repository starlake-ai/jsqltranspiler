-- prolog
CREATE OR REPLACE TABLE strings (v VARCHAR(50));
INSERT INTO strings (v) VALUES
    ('San Francisco'),
    ('San Jose'),
    ('Santa Clara'),
    ('Sacramento');

-- provided
SELECT v
    FROM strings
    WHERE v REGEXP 'San* [fF].*'
    ORDER BY v;

-- expected
SELECT v
    FROM strings
    WHERE v SIMILAR TO 'San* [fF].*'
    ORDER BY v;

-- result
"v"
"San Francisco"

-- epilog
drop table strings;


-- prolog
create or replace table overlap (id int, a string);
insert into overlap values (1,',abc,def,ghi,jkl,');
insert into overlap values (2,',abc,,def,,ghi,,jkl,');

-- provided
select id, regexp_count(a,'[[:punct:]][[:alnum:]]+[[:punct:]]', 1, 'i') AS count from overlap;

-- expected
SELECT ID,LENGTH(REGEXP_SPLIT_TO_ARRAY(A,'[[:punct:]][[:alnum:]]+[[:punct:]]'))-1 AS COUNT FROM OVERLAP

-- result
"id","count"
"1","2"
"2","4"

-- epilog
drop table overlap;


-- provided
select regexp_extract_all('a1_a2a3_a4A5a6', 'a[[:digit:]]') as matches;

-- result
"matches"
"[a1, a2, a3, a4, a6]"

-- provided
select regexp_substr_all('a1_a2a3_a4A5a6', '(a)([[:digit:]])', 1, 1, 'ie') as matches;

-- expected
select /*Warning: unsupported parameters*/ REGEXP_EXTRACT_ALL('a1_a2a3_a4A5a6', '(a)([[:digit:]])') as matches;

-- result
"matches"
"[a1, a2, a3, a4, a6]"

-- prolog
CREATE TABLE demo1 (id INT, string1 VARCHAR);
INSERT INTO demo1 (id, string1) VALUES
    (1, 'nevermore1, nevermore2, nevermore3.')
    ;

-- provided
select id, string1,
      regexp_substr(string1, 'nevermore\\d') AS "SUBSTRING",
      regexp_instr( string1, 'nevermore\\d') AS "POSITION"
    from demo1
    order by id;

-- expected
SELECT ID
    ,STRING1
    ,REGEXP_EXTRACT(STRING1,'nevermore\d',0)AS "SUBSTRING"
    ,CASE
        WHEN LENGTH(REGEXP_SPLIT_TO_ARRAY(STRING1,'nevermore\d'))>1 THEN LENGTH(REGEXP_SPLIT_TO_ARRAY(STRING1,'nevermore\d')[1])+1
        ELSE 0
        END AS "POSITION"
FROM DEMO1
ORDER BY ID;

-- result
"id","string1","SUBSTRING","POSITION"
"1","nevermore1, nevermore2, nevermore3.","nevermore1","1"


-- prolog
CREATE OR REPLACE TABLE cities(city varchar(20));
INSERT INTO cities VALUES
    ('Sacramento'),
    ('San Francisco'),
    ('San Jose'),
    (null);

-- provided
SELECT city FROM cities WHERE REGEXP_LIKE(city, 'san.*')
UNION ALL
SELECT city FROM cities WHERE REGEXP_LIKE(city, 'san.*', 'i');

-- expected
SELECT city FROM cities WHERE REGEXP_MATCHES(city, 'san.*')
UNION ALL
SELECT city FROM cities WHERE REGEXP_MATCHES(city, 'san.*', 'i');

-- result
"city"
"San Francisco"
"San Jose"

-- epilog
drop table cities;


-- provided
SELECT REGEXP_REPLACE('Customers - (NY)','\\(|\\)','') AS customers;

-- expected
SELECT REGEXP_REPLACE('Customers - (NY)', '\(|\)','', 'g') AS customers;

-- result
"customers"
"Customers - NY"


-- provided
SELECT column1, ASCII(column1)
  FROM (values('!'), ('A'), ('a'), ('bcd'), (''), (null));

-- expected
SELECT COLUMN1,ASCII(COLUMN1)FROM((SELECT UNNEST([{COLUMN1:'!'},{COLUMN1:'A'},{COLUMN1:'a'},{COLUMN1:'bcd'},{COLUMN1:''},{COLUMN1:NULL}],RECURSIVE=>TRUE)));

-- results
"column1","ascii(column1)"
"!","33"
"A","65"
"a","97"
"bcd","98"
"","0"
"",""


-- provided
SELECT BIT_LENGTH(column1) AS string, BIT_LENGTH(column2) AS bytes
FROM VALUES ('abc', NULL)
        , ('\u0394', X'A1B2')
;

-- expected
SELECT
    8 * CASE TYPEOF(COLUMN1)
            WHEN 'VARCHAR' THEN OCTET_LENGTH(ENCODE(TRY_CAST(COLUMN1 AS VARCHAR)))
            ELSE OCTET_LENGTH(TRY_CAST(COLUMN1 AS BLOB)) END AS string
    , 8 * CASE TYPEOF(COLUMN2)
            WHEN 'VARCHAR' THEN OCTET_LENGTH(ENCODE(TRY_CAST(COLUMN2 AS VARCHAR)))
            ELSE OCTET_LENGTH(TRY_CAST(COLUMN2 AS BLOB)) END AS bytes
FROM ( SELECT UNNEST( [
                { COLUMN1 : 'abc' , COLUMN2 : NULL }
                , { COLUMN1 : 'Δ' , COLUMN2 : '\xA1\xB2'::BLOB }
                ] ,RECURSIVE=>TRUE ) );

-- result
"string","bytes"
"24",""
"16","16"


-- provided
SELECT OCTET_LENGTH('abc') AS ascii, OCTET_LENGTH('\u0392') as unicode, OCTET_LENGTH(X'A1B2') as bytes;

-- expected
SELECT  CASE Typeof( 'abc' )
        WHEN 'VARCHAR'
            THEN Octet_Length( Encode(  Try_Cast( 'abc' AS VARCHAR ) ) )
        ELSE Octet_Length( Encode( 'abc' ) )
        END AS ascii
        , CASE Typeof( 'Β' )
            WHEN 'VARCHAR'
                THEN Octet_Length( Encode(  Try_Cast( 'Β' AS VARCHAR ) ) )
            ELSE Octet_Length( Encode( 'Β' ) )
            END AS unicode
        , CASE Typeof( '\xA1\xB2'::BLOB )
            WHEN 'VARCHAR'
                THEN Octet_Length( Encode(  Try_Cast( '\xA1\xB2'::BLOB AS VARCHAR ) ) )
            ELSE Octet_Length(  Try_Cast( '\xA1\xB2'::BLOB AS BLOB ) )
            END AS bytes
;

-- result
"ascii","unicode","bytes"
"3","2","2"

-- provided
SELECT column1, CHR(column1) AS chr
FROM (VALUES(83), (33), (169), (8364), (null));

-- expected
SELECT  column1
        , Chr( column1 ) AS chr
FROM (  (   SELECT Unnest(  [
                                { column1:83 }
                                , { column1:33 }
                                , { column1:169 }
                                , { column1:8364 }
                                , { column1:NULL }
                            ], recursive => True ) ) )
;

-- result
"column1","chr"
"83","S"
"33","!"
"169","©"
"8364","€"
"",""

-- provided
SELECT CONCAT_WS(',', 'one', 'two', 'three') as concat;

-- result
"concat"
"one,two,three"


-- provided
SELECT INSERT('abcdef', 3, 2, 'zzz') as STR;

-- expected
SELECT substr('abcdef',0,3) || 'zzz' ||  substr('abcdef', 3+2)  as STR;

-- result
"STR"
"abzzzef"


-- provided
select column1, len(column1) as len, bit_length(column1) as bits
from values
 ( 'Joyeux Noël')
 ,('Merry Christmas')
 ,('Veselé Vianoce')
 ,('Wesołych Świąt')
 ,('圣诞节快乐')
 ,('')

-- expected
SELECT COLUMN1
    ,CASE TYPEOF(COLUMN1)
        WHEN 'VARCHAR' THEN LENGTH(TRY_CAST(COLUMN1 AS VARCHAR))
        WHEN 'BLOB' THEN OCTET_LENGTH(TRY_CAST(COLUMN1 AS BLOB))END AS LEN
    ,8*CASE TYPEOF(COLUMN1)
        WHEN 'VARCHAR' THEN OCTET_LENGTH(ENCODE(TRY_CAST(COLUMN1 AS VARCHAR)))
        ELSE OCTET_LENGTH(TRY_CAST(COLUMN1 AS BLOB))END AS BITS
FROM ( SELECT UNNEST([
            {COLUMN1:'Joyeux Noël'}
            ,{COLUMN1:'Merry Christmas'}
            ,{COLUMN1:'Veselé Vianoce'}
            ,{COLUMN1:'Wesołych Świąt'}
            ,{COLUMN1:'圣诞节快乐'}
            ,{COLUMN1:''}
            ],RECURSIVE=>TRUE));

-- result
"column1","len","bits"
"Joyeux Noël","11","96"
"Merry Christmas","15","120"
"Veselé Vianoce","14","120"
"Wesołych Świąt","14","136"
"圣诞节快乐","5","120"
"","0","0"


-- provided
SELECT LPAD('123.50', 19, '*_') as padded;

-- expected
SELECT CASE TYPEOF('123.50')WHEN 'VARCHAR' THEN LPAD('123.50'::VARCHAR,19,'*_')END AS PADDED;

-- result
"padded"
"*_*_*_*_*_*_*123.50"


-- provided
SELECT LTRIM('#000000123', '0#') as trimmed;

-- result
"trimmed"
"123"


-- provided
SELECT REPEAT('xy', 5) AS repeat;

-- result
"repeat"
"xyxyxyxyxy"


-- provided
SELECT '2019-05-22'::DATE as date, REVERSE('2019-05-22'::DATE) AS reversed;

-- result
"date","reversed"
"2019-05-22","22-50-9102"


-- provided
SELECT RPAD('123.50', 20, '*-') AS padded FROM dual;

-- expected
SELECT CASE TYPEOF('123.50')WHEN 'VARCHAR' THEN RPAD('123.50'::VARCHAR,20,'*-')END AS PADDED;

-- result
"padded"
"123.50*-*-*-*-*-*-*-"


-- provided
SELECT RTRIM('$125.00', '0.') AS trimmed;

-- result
"trimmed"
"$125"


-- provided
SELECT RTRIMMED_LENGTH(' ABCD ') AS trimmed_len;

-- expected
SELECT LEN(RTRIM(' ABCD ')) AS trimmed_len;

-- result
"trimmed_len"
"5"


-- provided
SELECT SPACE(3) AS space;

-- expected
SELECT repeat(' ', 3) As space;

-- result
"space"
"   "


-- provided
SELECT SPLIT('127.0.0.1', '.') AS split;

-- result
"split"
"[127, 0, 0, 1]"


-- provided
select  0, split_part('11.22.33', '.',  0) AS part UNION
select  1, split_part('11.22.33', '.',  1) UNION
select  2, split_part('11.22.33', '.',  2) UNION
select  3, split_part('11.22.33', '.',  3) UNION
select  4, split_part('11.22.33', '.',  4) UNION
select -1, split_part('11.22.33', '.', -1) UNION
select -2, split_part('11.22.33', '.', -2) UNION
select -3, split_part('11.22.33', '.', -3) UNION
select -4, split_part('11.22.33', '.', -4)
order by 1 ;


-- result
"0","part"
"-4",""
"-3","11"
"-2","22"
"-1","33"
"0",""
"1","11"
"2","22"
"3","33"
"4",""


-- provided
SELECT table1.value
    FROM table(split_to_table('a.b', '.')) AS table1
    ORDER BY table1.value;

-- expected
SELECT TABLE1.VALUE FROM(SELECT 0 AS SEQ,0 AS INDEX,REGEXP_SPLIT_TO_TABLE('a.b',REGEXP_ESCAPE('.')),REGEXP_SPLIT_TO_TABLE('a.b',REGEXP_ESCAPE('.'))AS VALUE)AS TABLE1 ORDER BY TABLE1.VALUE;

-- result
"value"
"a"
"b"


-- provided
SELECT *
    FROM (VALUES ('a b'), ('cde'), ('f|g'), ('')) AS t1, LATERAL STRTOK_SPLIT_TO_TABLE(t1.column1, ' |')
    ORDER BY SEQ, INDEX, value;


-- expected
SELECT *
FROM (  (   SELECT Unnest(  [
                                { COLUMN1:'a b' }
                                , { COLUMN1:'cde' }
                                , { COLUMN1:'f|g' }
                                , { COLUMN1:'' }
                            ], RECURSIVE => TRUE ) ) ) AS t1
    , LATERAL ( SELECT  0 AS seq
                , 0 AS index
                , Regexp_Split_To_Table( t1.column1, List_Aggregate( List_Transform( Str_Split_Regex( '|', '' ), X -> REGEXP_ESCAPE(X) ), 'string_agg', '|' ) ) AS value  )
ORDER BY    seq
            , index
            , value
;

-- result
"column1","seq","index","value"
"","0","0",""
"a b","0","0","a"
"a b","0","0","b"
"cde","0","0","cde"
"f|g","0","0","f"
"f|g","0","0","g"


-- provided
SELECT STRTOK('user@snowflake.com', '@.', 1) AS tk1
       , STRTOK('user@snowflake.com', '@.', 2) AS tk2
       , STRTOK('user@snowflake.com', '@.', 3) AS tk3;

-- expected
SELECT Str_Split_Regex('user@snowflake.com', List_Aggregate( List_Transform( Str_Split_Regex( '@.', '' ), X -> REGEXP_ESCAPE(X) ), 'string_agg', '|' ))[1] AS tk1
       , Str_Split_Regex('user@snowflake.com', List_Aggregate( List_Transform( Str_Split_Regex( '@.', '' ), X -> REGEXP_ESCAPE(X) ), 'string_agg', '|' ))[2] AS tk2
       , Str_Split_Regex('user@snowflake.com', List_Aggregate( List_Transform( Str_Split_Regex( '@.', '' ), X -> REGEXP_ESCAPE(X) ), 'string_agg', '|' ))[3] AS tk3
;

-- result
"tk1","tk2","tk3"
"user","snowflake","com"


-- provided
SELECT STRTOK_TO_ARRAY('user@snowflake.com', '.@') AS tokens;


-- expected
SELECT Str_Split_Regex('user@snowflake.com', List_Aggregate( List_Transform( Str_Split_Regex( '.@', '' ), X -> REGEXP_ESCAPE(X) ), 'string_agg', '|' )) AS tokens
;

-- result
"tokens"
"[user, snowflake, com]"

-- provided
SELECT TRANSLATE('peña','ñ','n') AS translated;

-- result
"translated"
"pena"


-- provided
SELECT TRIM('❄-❄ABC-❄-', '❄-') AS trimmed;

-- result
"trimmed"
"ABC"


-- provided
SELECT column1, UNICODE(column1) AS unicode, CHAR(UNICODE(column1)) AS char
FROM values('a'), ('\u2744'), ('cde'), (''), (null);

-- expected
SELECT  column1
        , If( Length( column1 ) = 0, 0, Unicode( column1 ) ) AS unicode
        , Chr( If( Length( column1 ) = 0, 0, Unicode( column1 ) ) ) AS char
FROM (  SELECT Unnest(  [
                            { column1:'a' }
                            , { column1:'❄' }
                            , { column1:'cde' }
                            , { column1:'' }
                            , { column1:NULL }
                        ], recursive => True ) )
;

-- result
"column1","unicode","char"
"a","97","a"
"❄","10052","❄"
"cde","99","c"
"","0"," "
"","",""


-- provided
select UUID_STRING() AS uuid;

-- expected
select uuid() AS uuid;

-- count
1


-- provided
select charindex('an', 'banana', 1) AS pos;

-- expected
select instr(substr('banana', 1), 'an') AS pos;

-- result
"pos"
"2"


-- provided
select * from values ('coffee'), ('ice tea') where CONTAINS(column1, 'te');

-- expected
SELECT *
FROM (  SELECT Unnest(  [
                            { column1:'coffee' }
                            , { column1:'ice tea' }
                        ], recursive => True ) )
WHERE Contains( column1, 'te' )
;

-- result
"column1"
"ice tea"


-- provided
SELECT EDITDISTANCE('future', 'past', 2)  AS dist

-- expected
SELECT Least(editdist3('future', 'past'), 2) AS dist
;

-- result
"dist"
"2"


-- provided
SELECT ENDSWITH('latte', 'te')  AS ends;

-- expected
SELECT ENDS_WITH('latte', 'te') AS ends
;

-- result
"ends"
"true"


-- provided
SELECT STARTSWITH('latte', 'la')  AS starts;

-- expected
SELECT STARTS_WITH('latte', 'la') AS starts
;

-- result
"starts"
"true"


-- prolog
CREATE OR REPLACE TABLE like_example(name VARCHAR(20));
INSERT INTO like_example VALUES
    ('John  Dddoe'),
    ('Joe   Doe'),
    ('John_do%wn'),
    ('Joe down'),
    ('Tom   Doe'),
    ('Tim down'),
    (null);

-- provided
SELECT *
  FROM like_example
  WHERE name LIKE ALL ('%Jo%oe%','J%e')
  ORDER BY name;

-- expected
SELECT *
  FROM like_example
  WHERE name LIKE '%Jo%oe%' AND name LIKE 'J%e'
  ORDER BY name;

-- result
"name"
"Joe   Doe"
"John  Dddoe"


-- provided
SELECT *
  FROM like_example
  WHERE name LIKE ANY ('%Jo%oe%','T%e')
  ORDER BY name;


-- expected
SELECT *
  FROM like_example
  WHERE name LIKE '%Jo%oe%' OR name LIKE 'T%e'
  ORDER BY name;

-- result
"name"
"Joe   Doe"
"John  Dddoe"
"Tom   Doe"




