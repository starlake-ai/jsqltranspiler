-- prolog
DROP TABLE IF EXISTS variant_tab;
CREATE TABLE variant_tab (id INTEGER, v VARIANT);
INSERT INTO variant_tab SELECT 1, '{"a":{"b":"x"},"arr":[10,20,30]}'::JSON::VARIANT;

-- provided
SELECT v:a.b AS b FROM variant_tab;

-- expected
SELECT v['a']['b'] AS b FROM variant_tab;

-- result
"b"
"x"

-- epilog
DROP TABLE IF EXISTS variant_tab;


-- prolog
DROP TABLE IF EXISTS variant_tab;
CREATE TABLE variant_tab (id INTEGER, v VARIANT);
INSERT INTO variant_tab SELECT 1, '{"a":{"b":"x"},"arr":[10,20,30]}'::JSON::VARIANT;

-- provided
SELECT v:a.b::string AS s FROM variant_tab;

-- expected
SELECT v['a']['b']::string AS s FROM variant_tab;

-- result
"s"
"x"

-- epilog
DROP TABLE IF EXISTS variant_tab;


-- prolog
DROP TABLE IF EXISTS variant_tab;
CREATE TABLE variant_tab (id INTEGER, v VARIANT);
INSERT INTO variant_tab SELECT 1, '{"a":{"b":"x"},"arr":[10,20,30]}'::JSON::VARIANT;

-- provided
SELECT v:arr[0]::int AS n FROM variant_tab;

-- expected
SELECT v['arr'][1]::int AS n FROM variant_tab;

-- result
"n"
"10"

-- epilog
DROP TABLE IF EXISTS variant_tab;


-- prolog
DROP TABLE IF EXISTS variant_tab;
CREATE TABLE variant_tab (id INTEGER, v VARIANT);
INSERT INTO variant_tab SELECT 1, '{"a":{"b":"x"},"arr":[10,20,30]}'::JSON::VARIANT;

-- provided
SELECT v:a.missing IS NULL AS m FROM variant_tab;

-- expected
SELECT v['a']['missing'] IS NULL AS m FROM variant_tab;

-- result
"m"
"true"

-- epilog
DROP TABLE IF EXISTS variant_tab;


-- prolog
DROP TABLE IF EXISTS variant_tab;
CREATE TABLE variant_tab (id INTEGER, s VARCHAR);
INSERT INTO variant_tab VALUES (1, 'x');

-- provided
SELECT s::VARIANT AS v FROM variant_tab;

-- expected
SELECT s::VARIANT AS v FROM variant_tab;

-- result
"v"
"x"

-- epilog
DROP TABLE IF EXISTS variant_tab;
