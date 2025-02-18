-- prolog
DROP TABLE IF EXISTS sample_json_table;
CREATE TABLE sample_json_table (ID INTEGER, varchar1 VARCHAR, variant1 BLOB);
INSERT INTO sample_json_table (ID, varchar1) VALUES
    (1, '{"ValidKey1": "ValidValue1"}'),
    (2, '{"Malformed -- Missing value": }'),
    (3, NULL)
    ;
UPDATE sample_json_table SET variant1 = varchar1::BLOB;

-- provided
SELECT ID, CHECK_JSON(varchar1) v, varchar1 FROM sample_json_table ORDER BY ID;

-- expected
SELECT ID, json_valid(varchar1) v, varchar1 FROM sample_json_table ORDER BY ID;

-- result
"ID","v","varchar1"
"1","true","{""ValidKey1"": ""ValidValue1""}"
"2","false","{""Malformed -- Missing value"": }"
"3","JSQL_NULL","JSQL_NULL"

-- epilog
DROP TABLE IF EXISTS sample_json_table;


-- prolog
DROP TABLE IF EXISTS demo1;
CREATE TABLE demo1 (id INTEGER, json_data VARCHAR);
INSERT INTO demo1 SELECT
   1, '{"level_1_key": "level_1_value"}';
INSERT INTO demo1 SELECT
   2, '{"level_1_key": {"level_2_key": "level_2_value"}}';
INSERT INTO demo1 SELECT
   3, '{"level_1_key": {"level_2_key": ["zero", "one", "two"]}}';

-- provided
SELECT
        JSON_EXTRACT_PATH_TEXT(json_data, 'level_1_key')
            AS JSON_EXTRACT_PATH_TEXT
    FROM demo1
    ORDER BY id;


-- expected
SELECT JSON_DATA->>'level_1_key' AS JSON_EXTRACT_PATH_TEXT FROM DEMO1 ORDER BY ID;

-- result
"JSON_EXTRACT_PATH_TEXT"
"level_1_value"
"{""level_2_key"":""level_2_value""}"
"{""level_2_key"":[""zero"",""one"",""two""]}"


-- epilog
DROP TABLE IF EXISTS demo1;


-- prolog
DROP TABLE IF EXISTS vartab;
CREATE OR REPLACE TABLE vartab (ID INTEGER, v VARCHAR);

INSERT INTO vartab (id, v) VALUES
  (1, '[-1, 12, 289, 2188, false,]'),
  (2, '{ "x" : "abc", "y" : false, "z": 10} '),
  (3, '{ "bad" : "json", "missing" : true, "close_brace": 10 ');

-- provided
SELECT ID, TRY_PARSE_JSON(v) j
  FROM vartab
  ORDER BY ID;

-- expected
SELECT ID, Try_Cast(v AS JSON) j
  FROM vartab
  ORDER BY ID;

-- result
"ID","j"
"1","[-1, 12, 289, 2188, false,]"
"2","{ ""x"" : ""abc"", ""y"" : false, ""z"": 10} "
"3","JSQL_NULL"

-- epilog
DROP TABLE IF EXISTS vartab;