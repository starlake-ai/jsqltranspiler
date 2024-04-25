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

