-- provided
SELECT ASCII('abcd') as A, ASCII('a') as B, ASCII('') as C, ASCII(NULL) as D;

-- result
"A","B","C","D"
"97","97","0",""


-- provided
WITH example AS
  (SELECT 'абвгд' AS characters, b'абвгд' AS bytes)
SELECT
  characters,
  BYTE_LENGTH(characters) AS string_example,
  BYTE_LENGTH(bytes) AS bytes_example
FROM example;

-- expected
WITH example AS (
        SELECT  'абвгд' AS characters
                , Coalesce( try_cast( 'абвгд' AS BLOB ), Encode( 'абвгд' ) ) AS bytes  )
SELECT  characters
        , Octet_Length( Coalesce(  try_cast( characters AS BLOB ), Encode(  try_cast( characters AS VARCHAR ) ) ) ) AS string_example
        , Octet_Length( Coalesce(  try_cast( bytes AS BLOB ), Encode(  try_cast( bytes AS VARCHAR ) ) ) ) AS bytes_example
FROM example
;

-- result
"characters","string_example","bytes_example"
"абвгд","10","10"


-- provided
WITH example AS
  (SELECT 'абвгд' AS characters)
SELECT
  characters,
  CHAR_LENGTH(characters) AS char_length_example,
  CHARACTER_LENGTH(characters) AS character_length_example
FROM example;


-- expected
WITH example AS
  (SELECT 'абвгд' AS characters)
SELECT
  characters,
  Length(characters) AS char_length_example,
  Length(characters) AS character_length_example
FROM example;

-- result
"characters","char_length_example","character_length_example"
"абвгд","5","5"


-- provided
SELECT CHR(65) AS A, CHR(255) AS B, CHR(513) AS C, CHR(1024)  AS D;

-- result
"A","B","C","D"
"A","ÿ","ȁ","Ѐ"


-- provided
SELECT CODE_POINTS_TO_BYTES(ARRAY [65, 98, 67, 100]) AS bytes;

-- expected
SELECT (select encode(string_agg(a, '')) as bytes from (select chr(unnest(ARRAY[65, 98, 67, 100])) as a)) AS bytes;

-- count
1


-- provided
SELECT CODE_POINTS_TO_STRING(ARRAY [65, 255, 513, 1024]) AS string;

-- expected
SELECT (select string_agg(a, '') as characters from (select chr(unnest(ARRAY[65, 255, 513, 1024])) as a)) AS string;

-- result
"string"
"AÿȁЀ"


-- provided
SELECT CONCAT('T.P.', ' ', 'Bar') as author;

-- result
"author"
"T.P. Bar"

-- provided
SELECT CONCAT('Summer', ' ', 1923) as release_date;

-- result
"release_date"
"Summer 1923"


-- provided
WITH Words AS (
  SELECT
    COLLATE('a', 'und:ci') AS char1,
    COLLATE('Z', 'und:ci') AS char2
)
SELECT ( Words.char1 < Words.char2 ) AS a_less_than_Z
FROM Words;

-- expected
WITH Words AS (
  SELECT
    ICU_SORT_KEY('a', 'und:ci') AS char1,
    ICU_SORT_KEY('Z', 'und:ci') AS char2
)
SELECT ( Words.char1 < Words.char2 ) AS a_less_than_Z
FROM Words;

-- result
"a_less_than_Z"
"True"


-- provided
SELECT CONTAINS_SUBSTR('the blue house', 'Blue house') AS result;

-- expected
SELECT nfc_normalize('the blue house') ILIKE nfc_normalize('%' || 'Blue house' || '%') AS result;

-- result
"result"
"true"


-- provided
SELECT CONTAINS_SUBSTR('the blue house', CONCAT('Blue ', 'house')) AS result;

-- expected
SELECT nfc_normalize('the blue house') ILIKE nfc_normalize('%' || CONCAT('Blue ', 'house') || '%') AS result;

-- result
"result"
"true"


-- provided
SELECT '\u2168 day' AS a, 'IX' AS b, CONTAINS_SUBSTR('\u2168', 'IX') AS result;

-- expected
SELECT 'Ⅸ day' AS a, 'IX' AS b, nfc_normalize('Ⅸ') ILIKE nfc_normalize('%' || 'IX' || '%') AS result;

--@todo: fix the unicode comparison: "Ⅸ day","IX","true"
-- result
"a","b","result"
"Ⅸ day","IX","false"

-- provided
SELECT '\u00ea day' AS a, '\u0065\u0302' AS b, CONTAINS_SUBSTR('\u00ea day', '\u0065\u0302') AS result;

-- expected
SELECT 'ê day' AS a, 'ê' AS b, nfc_normalize('ê day') ILIKE nfc_normalize('%' || 'ê' || '%') AS result;

-- result
"a","b","result"
"ê day","ê","true"


-- provided
SELECT EDIT_DISTANCE('aa', 'b') AS results;

-- expected
SELECT levenshtein('aa', 'b') AS results;

--result
"results"
"2"

-- provided
WITH items AS
  (SELECT 'apple' as item
  UNION ALL
  SELECT 'banana' as item
  UNION ALL
  SELECT 'orange' as item)

SELECT
  ENDS_WITH(item, 'e') as example
FROM items;

-- result
"example"
"True"
"False"
"True"


-- provided
SELECT FORMAT('date: %s!', FORMAT_DATE('%B %d, %Y', date '2015-01-02')) AS formatted;

-- expected
SELECT printf('date: %s!', STRFTIME(DATE '2015-01-02','%B %d, %Y'))  AS formatted;

-- result
"formatted"
"date: January 02, 2015!"


-- provided
SELECT FROM_BASE64('/+A=') AS byte_data;

-- tally
1


-- provided
WITH Input AS (
  SELECT '00010203aaeeefff' AS hex_str UNION ALL
  SELECT '0AF' UNION ALL
  SELECT '666f6f626172'
)
SELECT hex_str, FROM_HEX(hex_str) AS bytes_str
FROM Input;

-- tally
3

-- provided
WITH example AS (
        SELECT  'banana' AS value
                , 'an' AS subvalue
        UNION ALL
        SELECT  'banana' AS value
                , 'ann' AS subvalue
        UNION ALL
        SELECT  'helloooo' AS value
                , 'oo' AS subvalue
                 )
SELECT  value
        , subvalue
        , Instr( value, subvalue ) AS instr
FROM example
;

-- result
"value","subvalue","instr"
"banana","an","2"
"banana","ann","0"
"helloooo","oo","5"


-- provided
WITH examples AS
(SELECT 'apple' as example
UNION ALL
SELECT 'banana' as example
UNION ALL
SELECT 'абвгд' as example
)
SELECT example, LEFT(example, 3) AS left_example
FROM examples;

-- result
"example","left_example"
"apple","app"
"banana","ban"
"абвгд","абв"


-- provided
WITH example AS
  (SELECT 'абвгд' AS characters, b'абвгд' AS bytes)
SELECT
  characters,
  LENGTH(characters) AS string_example,
  LENGTH(bytes) AS bytes_example
FROM example;

-- expected
WITH example AS (
        SELECT  'абвгд' AS characters
                , Coalesce(  try_cast( 'абвгд' AS BLOB ), Encode( 'абвгд' ) ) AS bytes  )
SELECT  characters
        , CASE TYPEOF(characters)
                WHEN 'VARCHAR'
                    THEN Length( characters::VARCHAR )
                WHEN 'BLOB'
                    THEN Octet_Length( characters::BLOB )
                ELSE - 1
            END AS string_example
        , CASE TYPEOF(bytes)
                WHEN 'VARCHAR'
                    THEN Length( bytes::VARCHAR )
                WHEN 'BLOB'
                    THEN Octet_Length( bytes::BLOB )
                ELSE - 1
            END AS bytes_example
FROM example
;

-- result
"characters","string_example","bytes_example"
"абвгд","5","10"

-- provided
WITH items AS
  (SELECT
    'FOO' as item
  UNION ALL
  SELECT
    'BAR' as item
  UNION ALL
  SELECT
    'BAZ' as item)
SELECT
  LOWER(item) AS example
FROM items;

-- result
"example"
"foo"
"bar"
"baz"


-- provided
SELECT FORMAT('%s', LPAD('abc', 5)) AS padded;

-- expected
select printf('%s', case typeof('abc') when 'VARCHAR' then LPAD('abc'::VARCHAR, 5, ' ') end) AS padded;

-- result
"padded"
"  abc"


-- provided
WITH items AS
  (SELECT '   apple   ' as item
  UNION ALL
  SELECT '   banana   ' as item
  UNION ALL
  SELECT '   orange   ' as item)
SELECT
  CONCAT('#', LTRIM(item), '#') as example
FROM items;

-- result
"example"
"#apple   #"
"#banana   #"
"#orange   #"


-- provided
WITH items AS
  (SELECT '***apple***' as item
  UNION ALL
  SELECT '***banana***' as item
  UNION ALL
  SELECT '***orange***' as item)
SELECT
  LTRIM(item, '*') as example
FROM items;

-- result
"example"
"apple***"
"banana***"
"orange***"


-- provided
SELECT a, b, a = b as normalized
FROM (SELECT NORMALIZE('\u00ea') as a, NORMALIZE('\u0065\u0302') as b);

-- expected
SELECT A,B,A=B AS NORMALIZED
FROM(SELECT NFC_NORMALIZE('ê')AS A,NFC_NORMALIZE('ê')AS B);

-- result
"a","b","normalized"
"ê","ê","true"


-- provided
WITH Strings AS (
  SELECT '\u2168' AS a, 'IX' AS b UNION ALL
  SELECT '\u0041\u030A', '\u00C5'
)
SELECT a, b,
  NORMALIZE_AND_CASEFOLD(a, NFD)=NORMALIZE_AND_CASEFOLD(b, NFD) AS nfd,
  NORMALIZE_AND_CASEFOLD(a, NFC)=NORMALIZE_AND_CASEFOLD(b, NFC) AS nfc,
  NORMALIZE_AND_CASEFOLD(a, NFKD)=NORMALIZE_AND_CASEFOLD(b, NFKD) AS nkfd,
  NORMALIZE_AND_CASEFOLD(a, NFKC)=NORMALIZE_AND_CASEFOLD(b, NFKC) AS nkfc
FROM Strings;

-- expected
WITH STRINGS AS (
    SELECT 'Ⅸ' AS A,'IX' AS B
    UNION ALL
    SELECT 'Å','Å'
)
SELECT a, b,
  NFC_NORMALIZE(Lower(a))=NFC_NORMALIZE(Lower(b)) AS nfd,
  NFC_NORMALIZE(Lower(a))=NFC_NORMALIZE(Lower(b)) AS nfc,
  NFC_NORMALIZE(Lower(a))=NFC_NORMALIZE(Lower(b)) AS nkfd,
  NFC_NORMALIZE(Lower(a))=NFC_NORMALIZE(Lower(b)) AS nkfc
FROM Strings;

--@todo: the first line is incorrect and should return: | Ⅸ | IX | false | false | true | true |
-- result
"A","B","NFD","NFC","NKFD","NKFC"
"Ⅸ","IX","false","false","false","false"
"Å","Å","true","true","true","true"

-- provided
SELECT
  email,
  REGEXP_CONTAINS(email, r'@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+') AS is_valid
FROM
  UNNEST(['foo@example.com', 'bar@example.org', 'www.example.net']) AS email;

-- expected
SELECT
  email,
  REGEXP_MATCHES(email, '@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+') AS is_valid
FROM
  (SELECT UNNEST(['foo@example.com', 'bar@example.org', 'www.example.net']) AS email) AS email;

-- result
"email","is_valid"
"foo@example.com","true"
"bar@example.org","true"
"www.example.net","false"


-- provided
WITH email_addresses AS
  (SELECT 'foo@example.com' as email
  UNION ALL
  SELECT 'bar@example.org' as email
  UNION ALL
  SELECT 'baz@example.net' as email)
SELECT
  REGEXP_EXTRACT(email, r'^[a-zA-Z0-9_.+-]+')
  AS user_name
FROM email_addresses;

-- expected
WITH email_addresses AS
  (SELECT 'foo@example.com' as email
  UNION ALL
  SELECT 'bar@example.org' as email
  UNION ALL
  SELECT 'baz@example.net' as email)
SELECT
  REGEXP_EXTRACT(email, '^[a-zA-Z0-9_.+-]+')
  AS user_name
FROM email_addresses;

-- result
"user_name"
"foo"
"bar"
"baz"


-- provided
WITH code_markdown AS
  (SELECT 'Try `function(x)` or `function(y)`' as code)

SELECT
  REGEXP_EXTRACT_ALL(code, '`(.+?)`') AS example
FROM code_markdown;

-- result
"example"
"[`function(x)`, `function(y)`]"


-- provided
WITH example AS (
  SELECT 'ab@cd-ef' AS source_value, '@[^-]*' AS reg_exp UNION ALL
  SELECT 'ab@d-ef', '@[^-]*' UNION ALL
  SELECT 'abc@cd-ef', '@[^-]*' UNION ALL
  SELECT 'abc-ef', '@[^-]*')
SELECT source_value, reg_exp, REGEXP_INSTR(source_value, reg_exp) AS instr
FROM example;


-- expected
WITH example AS (
        SELECT  'ab@cd-ef' AS source_value
                , '@[^-]*' AS reg_exp
        UNION ALL
        SELECT  'ab@d-ef'
                , '@[^-]*'
        UNION ALL
        SELECT  'abc@cd-ef'
                , '@[^-]*'
        UNION ALL
        SELECT  'abc-ef'
                , '@[^-]*' )
SELECT  source_value
        , reg_exp
        , CASE
                WHEN Regexp_Matches( source_value, reg_exp )
                    THEN Instr( source_value, Regexp_Extract( source_value, reg_exp ) )
                ELSE 0
            END AS instr
FROM example
;

-- result
"source_value","reg_exp","instr"
"ab@cd-ef","@[^-]*","3"
"ab@d-ef","@[^-]*","3"
"abc@cd-ef","@[^-]*","4"
"abc-ef","@[^-]*","0"


-- provided
WITH markdown AS
  (SELECT '# Heading' as heading
  UNION ALL
  SELECT '# Another heading' as heading)
SELECT
  REGEXP_REPLACE(heading, r'^# ([a-zA-Z0-9\s]+$)', r'<h1>\1</h1>')
  AS html
FROM markdown;

-- expected
WITH markdown AS
  (SELECT '# Heading' as heading
  UNION ALL
  SELECT '# Another heading' as heading)
SELECT
  REGEXP_REPLACE(heading, '^# ([a-zA-Z0-9\s]+$)', '<h1>\1</h1>')
  AS html
FROM markdown;

-- result
"html"
"<h1>Heading</h1>"
"<h1>Another heading</h1>"


-- provided
WITH example AS
(SELECT 'Hello World Helloo' AS value, 'H?ello+' AS regex, 1 AS position, 1 AS
occurrence
)
SELECT value, regex, position, occurrence, REGEXP_SUBSTR(value, regex,
position, occurrence) AS regexp_value FROM example;

-- expected
WITH example AS
(SELECT 'Hello World Helloo' AS value, 'H?ello+' AS regex, 1 AS position, 1 AS
occurrence
)
SELECT value, regex, position, occurrence, REGEXP_EXTRACT(value, regex) AS regexp_value FROM example;

-- result
"value","regex","position","occurrence","regexp_value"
"Hello World Helloo","H?ello+","1","1","Hello"


-- provided
Select repeat('abc', 3) as repeated;

-- result
"repeated"
"abcabcabc"


-- provided
WITH desserts AS
  (SELECT 'apple pie' as dessert
  UNION ALL
  SELECT 'blackberry pie' as dessert
  UNION ALL
  SELECT 'cherry pie' as dessert)
SELECT
  REPLACE (dessert, 'pie', 'cobbler') as example
FROM desserts;

-- result
"example"
"apple cobbler"
"blackberry cobbler"
"cherry cobbler"


-- provided
WITH example AS (
  SELECT 'foo' AS sample_string UNION ALL
  SELECT 'абвгд' AS sample_string
)
SELECT
  sample_string,
  REVERSE(sample_string) AS reverse_string
FROM example;

-- result
"sample_string","reverse_string"
"foo","oof"
"абвгд","дгвба"


-- provided
WITH examples AS
(SELECT 'apple' as example
UNION ALL
SELECT 'banana' as example
UNION ALL
SELECT 'абвгд' as example
)
SELECT example, RIGHT(example, 3) AS right_example
FROM examples;

-- result
"example","right_example"
"apple","ple"
"banana","ana"
"абвгд","вгд"


-- provided
SELECT t, len, FORMAT('%T', RPAD(t, len)) AS RPAD FROM UNNEST([
  STRUCT('abc' AS t, 5 AS len),
  ('abc', 2),
  ('例子', 4)
]);

-- expected
SELECT t, len, PRINTF('%s',CASE TYPEOF(T) WHEN 'VARCHAR' THEN RPAD(T::VARCHAR,LEN,' ') END) AS RPAD FROM (select UNNEST([
  { t:'abc', len:5 },
  ('abc', 2),
  ('例子', 4)
], recursive => true));

-- result
"t","len","RPAD"
"abc","5","abc  "
"abc","2","ab"
"例子","4","例子  "


-- provided
WITH items AS
  (SELECT '***apple***' as item
  UNION ALL
  SELECT '***banana***' as item
  UNION ALL
  SELECT '***orange***' as item)
SELECT
  RTRIM(item, '*') as example
FROM items;

-- result
"example"
"***apple"
"***banana"
"***orange"


-- provided
WITH items AS
  (SELECT 'applexxx' as item
  UNION ALL
  SELECT 'bananayyy' as item
  UNION ALL
  SELECT 'orangezzz' as item
  UNION ALL
  SELECT 'pearxyz' as item)
SELECT
  RTRIM(item, 'xyz') as example
FROM items;

-- result
"example"
"apple"
"banana"
"orange"
"pear"


-- provided
SELECT SAFE_CONVERT_BYTES_TO_STRING(b'\x61') as safe_convert
;

-- expected
SELECT DECODE(COALESCE(TRY_CAST('\x61' AS BLOB),ENCODE('\x61')))AS SAFE_CONVERT
;

-- result
"safe_convert"
"a"


-- provided
WITH letters AS
  (SELECT '' as letter_group
  UNION ALL
  SELECT 'a' as letter_group
  UNION ALL
  SELECT 'b c d' as letter_group)
SELECT SPLIT(letter_group, ' ') as example
FROM letters;

-- result
"example"
"[]"
"[a]"
"[b, c, d]"


-- provided
WITH items AS
  (SELECT 'foo' as item
  UNION ALL
  SELECT 'bar' as item
  UNION ALL
  SELECT 'baz' as item)
SELECT
  STARTS_WITH(item, 'b') as example
FROM items;

-- result
"example"
"false"
"true"
"true"


-- provided
WITH email_addresses AS
  (SELECT
    'foo@example.com' AS email_address
  UNION ALL
  SELECT
    'foobar@example.com' AS email_address
  UNION ALL
  SELECT
    'foobarbaz@example.com' AS email_address
  UNION ALL
  SELECT
    'quxexample.com' AS email_address)
SELECT
  STRPOS(email_address, '@') AS example
FROM email_addresses;

-- result
"example"
"4"
"7"
"10"
"0"


-- provided
WITH items AS
  (SELECT 'apple' as item
  UNION ALL
  SELECT 'banana' as item
  UNION ALL
  SELECT 'orange' as item)
SELECT
  SUBSTR(item, 2) as example
FROM items;

-- result
"example"
"pple"
"anana"
"range"


-- provided
WITH items AS
  (SELECT 'apple' as item
  UNION ALL
  SELECT 'banana' as item
  UNION ALL
  SELECT 'orange' as item)

SELECT
  SUBSTRING(item, 123, 5) as example
FROM items;

-- result
"example"
""
""
""


-- provided
SELECT TO_BASE64(b'\x377\x340') AS base64_string;

-- expected
SELECT TO_BASE64( Coalesce(Try_CAST('\x377\x340' AS BLOB), Encode('\x377\x340'))) AS base64_string;

--@todo: verify if this example is correct and makes sense
--result
"base64_string"
"Nzc0MA=="

-- provided
SELECT word, TO_CODE_POINTS(word) AS code_points
FROM UNNEST(['foo', 'bar', 'baz', 'giraffe', 'llama']) AS word;

-- expected
SELECT word, list_transform( split(word, ''),  x -> unicode(x) ) as code_points
FROM (select UNNEST(['foo', 'bar', 'baz', 'giraffe', 'llama']) AS word) AS word;

-- result
"word","code_points"
"foo","[102, 111, 111]"
"bar","[98, 97, 114]"
"baz","[98, 97, 122]"
"giraffe","[103, 105, 114, 97, 102, 102, 101]"
"llama","[108, 108, 97, 109, 97]"