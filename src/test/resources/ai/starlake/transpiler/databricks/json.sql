-- provided
SELECT c1:price j
    FROM VALUES('{ "price": 5 }') AS T(c1);

-- expected
SELECT c1 -> 'price' j
    FROM VALUES('{ "price": 5 }') AS T(c1);

-- result
"j"
"5"


-- provided
SELECT ( c1:['price'] )::decimal(5,2) j
    FROM VALUES('{ "price": 5 }') AS T(c1);

-- expected
SELECT ( c1 -> 'price' ) ::decimal(5,2) j
    FROM VALUES('{ "price": 5 }') AS T(c1);

-- result
"j"
"5.0"


-- provided
SELECT from_json('{"a":1, "b":0.8}', 'a INT, b DOUBLE') j;

-- expected
SELECT json('{"a":1, "b":0.8}') j;

-- result
"j"
"{""a"":1,""b"":0.8}"


-- provided
SELECT get_json_object('{"a":"b"}', '$.a') v;

-- expected
SELECT json_value('{"a":"b"}', '$.a') v;

-- result
"v"
"""b"""


-- provided
SELECT json_array_length('[1,2,3,{"f1":1,"f2":[5,6]},4]') l;

-- result
"l"
"5"


-- provided
SELECT json_object_keys('{"f1":"abc","f2":{"f3":"a", "f4":"b"}}') k;

-- expected
SELECT json_keys('{"f1":"abc","f2":{"f3":"a", "f4":"b"}}') k;

-- result
"k"
"[f1, f2]"


-- provided
SELECT schema_of_json('[{"col":0}]') s;

-- expected
SELECT json_structure('[{"col":0}]') s;

-- result
"s"
"[{""col"":""UBIGINT""}]"