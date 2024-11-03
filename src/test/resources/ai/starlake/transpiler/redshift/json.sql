-- provided
SELECT JSON_PARSE('[10001,10002,"abc"]') j;

-- expected
SELECT '[10001,10002,"abc"]'::JSON  j;


-- result
"j"
"[10001,10002,""abc""]"


-- provided
SELECT CASE
            WHEN CAN_JSON_PARSE('[10001,10002,"abc"]')
            THEN JSON_PARSE('[10001,10002,"abc"]')
        END t;

-- expected
SELECT CASE
            WHEN try_cast('[10001,10002,"abc"]' AS JSON) is not null
            THEN '[10001,10002,"abc"]'::JSON
        END t;


-- result
"t"
"[10001,10002,""abc""]"


-- provided
SELECT CAN_JSON_PARSE('This is a string.') t;

-- provided
SELECT try_cast('This is a string.' AS JSON) is not null t;

-- result
"t"
"false"

-- prolog
drop table if exists test_json;
CREATE TABLE test_json(id int primary key, json_strings VARCHAR);

INSERT INTO test_json VALUES
(1, '{"a":2}'),
(2, '{"a":{"b":{"c":1}}}'),
(3, '{"a": [1,2,"b"]}');

INSERT INTO test_json VALUES
(4, '{{}}'),
(5, '{1:"a"}'),
(6, '[1,2,3]');

-- provided
SELECT id, json_strings, IS_VALID_JSON(json_strings) v
FROM test_json
ORDER BY id;

-- expected
SELECT id, json_strings, json_valid(json_strings) AND json_type(try_cast(json_strings AS JSON))<>'ARRAY' v
FROM test_json
ORDER BY id;

-- result
"id","json_strings","v"
"1","{""a"":2}","true"
"2","{""a"":{""b"":{""c"":1}}}","true"
"3","{""a"": [1,2,""b""]}","true"
"4","{{}}","false"
"5","{1:""a""}","false"
"6","[1,2,3]","false"

-- epilog
drop table if exists test_json;


-- prolog
DROP TABLE IF EXISTS test_json_arrays;
CREATE TABLE test_json_arrays(id int primary key, json_arrays VARCHAR);

INSERT INTO test_json_arrays
VALUES(1, '[]'),
(2, '["a","b"]'),
(3, '["a",["b",1,["c",2,3,null]]]'),
(4, '{"a":1}'),
(5, 'a'),
(6, '[1,2,]');

-- provided
SELECT json_arrays, IS_VALID_JSON_ARRAY(json_arrays) v
FROM test_json_arrays ORDER BY id;

-- expected
SELECT json_arrays, json_valid(json_arrays) AND json_type(try_cast(json_arrays AS JSON))='ARRAY' v
FROM test_json_arrays ORDER BY id;

-- result
"json_arrays","v"
"[]","true"
"[""a"",""b""]","true"
"[""a"",[""b"",1,[""c"",2,3,null]]]","true"
"{""a"":1}","false"
"a","false"
"[1,2,]","true"

-- epilog
DROP TABLE IF EXISTS test_json_arrays;


-- provided
SELECT JSON_ARRAY_LENGTH('[11,12,13,{"f1":21,"f2":[25,26]},14]') l;

-- expected
"l"
"5.00"


-- provided
SELECT JSON_EXTRACT_ARRAY_ELEMENT_TEXT('[111,112,113]', 2) e;

-- expected
SELECT Try_Cast('[111,112,113]' AS JSON)[2] e;

-- result
"e"
"113"


-- provided
SELECT JSON_EXTRACT_PATH_TEXT('{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}','f4', 'f6') e;

-- expected
SELECT  '{"f2":{"f3":1},"f4":{"f5":99,"f6":"star"}}'::JSON -> 'f4' -> 'f6' e;

-- expected
"e"
"""star"""
