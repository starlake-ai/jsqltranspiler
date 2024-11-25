--provided
SELECT JSON_EXTRACT_STRING_ARRAY('}}', '$') AS result;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract('}}', '$') AS result

--output
INVALID_TRANSLATION Invalid Input Error: Malformed JSON at byte 0 of input: unexpected character.  Input: }}

--result
"result"
"[]"


--provided
SELECT JSON_EXTRACT_STRING_ARRAY('{"a": [10, {"b": 20}]', '$.a') AS result;

--expected
--TODO: Regenerate once implemented
SELECT JSon_Extract('{"a": [10, {"b": 20}]', '$.a') AS result

--output
INVALID_TRANSLATION Invalid Input Error: Malformed JSON at byte 21 of input: unexpected end of data.  Input: {"a": [10, {"b": 20}]

--result
"result"
"[]"


--provided
SELECT JSON_VALUE('{"hello": "world"', "$.hello") AS hello;

--expected
SELECT JSon_Extract_String('{"hello": "world"', '$.hello') AS hello

--output
INVALID_TRANSLATION Invalid Input Error: Malformed JSON at byte 17 of input: unexpected end of data.  Input: {"hello": "world"

--result
"hello"
"world"
