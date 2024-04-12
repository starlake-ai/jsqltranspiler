-- provided
SELECT (firstname || ' ' || lastname) as fullname
FROM users
ORDER BY 1
LIMIT 10;

-- result
"fullname"
"Aaron Banks"
"Aaron Booth"
"Aaron Browning"
"Aaron Burnett"
"Aaron Casey"
"Aaron Cash"
"Aaron Castro"
"Aaron Dickerson"
"Aaron Dixon"
"Aaron Dotson"

-- provided
SELECT ASCII('amazon') ascii;

-- result
"ascii"
"97"

-- provided
SELECT userid, firstname, lastname, BPCHARCMP(firstname, lastname) bpcharcmp
FROM users
ORDER BY 1, 2, 3, 4
LIMIT 10;

-- expected
SELECT  userid
        , firstname
        , lastname
        , CASE
                WHEN firstname > lastname
                    THEN 1
                WHEN firstname < lastname
                    THEN - 1
                ELSE 0
            END bpcharcmp
FROM users
ORDER BY    1
            , 2
            , 3
            , 4
LIMIT 10
;

-- result
"userid","firstname","lastname","BPCHARCMP"
"1","Rafael","Taylor","-1"
"2","Vladimir","Humphrey","1"
"3","Lars","Ratliff","-1"
"4","Barry","Roy","-1"
"5","Reagan","Hodge","1"
"6","Victor","Hernandez","1"
"7","Tamekah","Juarez","1"
"8","Colton","Roy","-1"
"9","Mufutau","Watkins","-1"
"10","Naida","Calderon","1"


-- provided
SELECT  'xyzaxyzbxyzcxyz' AS untrim
        , Btrim( 'xyzaxyzbxyzcxyz', 'xyz' ) AS trimmed
;

-- expected
SELECT  'xyzaxyzbxyzcxyz' AS untrim
        , Trim( 'xyzaxyzbxyzcxyz', 'xyz' ) AS trimmed
;

-- result
"untrim","trimmed"
"xyzaxyzbxyzcxyz","axyzbxyzc"


-- provided
SELECT LENGTH(CAST('français' AS VARBYTE)) as bytes, LENGTH('français') as chars;

-- expected
SELECT  CASE Typeof( Encode( 'français' ) )
            WHEN 'VARCHAR'
                THEN Length(  Try_Cast( Encode( 'français' ) AS VARCHAR ) )
            WHEN 'BLOB'
                THEN Octet_Length(  Try_Cast( Encode( 'français' ) AS BLOB ) )
            END AS bytes
        , CASE Typeof( 'français' )
            WHEN 'VARCHAR'
                THEN Length(  Try_Cast( 'français' AS VARCHAR ) )
            WHEN 'BLOB'
                THEN Octet_Length(  Try_Cast( 'français' AS BLOB ) )
            END as chars
;

-- result
"bytes","chars"
"9","8"


-- provided
SELECT DISTINCT CHARINDEX('.', commission) AS charindex, COUNT (CHARINDEX('.', commission)) AS count
FROM sales
WHERE CHARINDEX('.', commission) > 4
GROUP BY CHARINDEX('.', commission)
ORDER BY 1,2;

-- expected
SELECT DISTINCT instr(commission::VARCHAR, '.' ) AS charindex, COUNT (instr(commission::VARCHAR, '.')) AS count
FROM sales
WHERE instr(commission::VARCHAR, '.') > 4
GROUP BY instr(commission::VARCHAR, '.')
ORDER BY 1,2
;

-- result
"charindex","count"
"5","629"


-- provided
SELECT DISTINCT POSITION('.' IN commission::VARCHAR) AS position, COUNT (POSITION('.' IN commission::VARCHAR)) AS count
FROM sales
WHERE POSITION('.' IN commission::VARCHAR) > 4
GROUP BY POSITION('.' IN commission::VARCHAR)
ORDER BY 1,2;

-- result
"position","count"
"5","629"


-- provided
SELECT DISTINCT
    eventname
FROM event
WHERE Substring( eventname, 1, 1 ) = Chr( 65 )
ORDER BY 1
LIMIT 5
;

-- result
"eventname"
"A Bronx Tale"
"A Catered Affair"
"A Chorus Line"
"A Christmas Carol"
"A Doll's House"


-- provided
WITH Words AS (
  SELECT
    COLLATE('a', 'case_insensitive') AS char1,
    COLLATE('A', 'case_insensitive') AS char2
)
SELECT ( Words.char1 = Words.char2 ) AS same
FROM Words;

-- expected
WITH Words AS (
  SELECT
    ICU_SORT_KEY('a', 'und:ci') AS char1,
    ICU_SORT_KEY('A', 'und:ci') AS char2
)
SELECT ( Words.char1 = Words.char2 ) AS same
FROM Words;
-- result
"same"
"true"


-- provided
SELECT CONCAT('December 25, ', '2008') concat;

-- result
"concat"
"December 25, 2008"


-- provided
SELECT eventid, eventname,
LEFT(eventname,5) AS left_5,
RIGHT(eventname,5) AS right_5
FROM event
WHERE eventid BETWEEN 1000 AND 1005
ORDER BY 1;

-- result
"eventid","eventname","left_5","right_5"
"1000","Gypsy","Gypsy","Gypsy"
"1001","Chicago","Chica","icago"
"1002","The King and I","The K","and I"
"1003","Pal Joey","Pal J"," Joey"
"1004","Grease","Greas","rease"
"1005","Chicago","Chica","icago"


-- provided
SELECT  catname
        , Lower( catname ) AS lower
FROM category
ORDER BY    1
            , 2
;

-- result
"catname","lower"
"Classical","classical"
"Jazz","jazz"
"MLB","mlb"
"MLS","mls"
"Musicals","musicals"
"NBA","nba"
"NFL","nfl"
"NHL","nhl"
"Opera","opera"
"Plays","plays"
"Pop","pop"


-- provided
SELECT LPAD(eventname, 20) AS lpad FROM event
WHERE eventid BETWEEN 1 AND 5 ORDER BY 1;

-- expected
SELECT CASE TYPEOF(EVENTNAME) WHEN 'VARCHAR' THEN LPAD(EVENTNAME::VARCHAR,20,' ') END AS LPAD
FROM EVENT
WHERE EVENTID BETWEEN 1 AND 5 ORDER BY 1;

-- result
"lpad"
"              Salome"
"        Il Trovatore"
"       Boris Godunov"
"     Gotterdammerung"
"La Cenerentola (Cind"


-- provided
SELECT RPAD(eventname, 20,'0123456789') AS rpad FROM event
WHERE eventid BETWEEN 1 AND 5
ORDER BY 1;

-- expected
SELECT CASE TYPEOF(EVENTNAME) WHEN 'VARCHAR' THEN RPAD(EVENTNAME::VARCHAR,20,'0123456789') END AS RPAD
FROM EVENT
WHERE EVENTID BETWEEN 1 AND 5
ORDER BY 1;

-- result
"rpad"
"Boris Godunov0123456"
"Gotterdammerung01234"
"Il Trovatore01234567"
"La Cenerentola (Cind"
"Salome01234567890123"


-- provided
SELECT  listid
        , listtime
        , Ltrim( listtime::VARCHAR, '2008-' ) AS ltrim
FROM listing
ORDER BY    1
            , 2
            , 3
LIMIT 10
;

-- result
"listid","listtime","ltrim"
"1","2008-01-24 06:43:29","1-24 06:43:29"
"2","2008-03-05 12:25:29","3-05 12:25:29"
"3","2008-11-01 07:35:33","11-01 07:35:33"
"4","2008-05-24 01:18:37","5-24 01:18:37"
"5","2008-05-17 02:29:11","5-17 02:29:11"
"6","2008-08-15 02:08:13","15 02:08:13"
"7","2008-11-15 09:38:15","11-15 09:38:15"
"8","2008-11-09 05:07:30","11-09 05:07:30"
"9","2008-09-09 08:03:36","9-09 08:03:36"
"10","2008-06-17 09:44:54","6-17 09:44:54"


-- provided
SELECT OCTETINDEX('Redshift', 'Amazon Redshift') AS index;

-- expected
SELECT octet_length(encode(substr('Amazon Redshift',0 , instr('Amazon Redshift', 'Redshift')+1))) as index;

-- result
"index"
"8"


-- provided
SELECT OCTETINDEX('Redshift', 'Άμαζον Amazon Redshift') as index;

-- expected
SELECT octet_length(encode(substr('Άμαζον Amazon Redshift',0 , instr('Άμαζον Amazon Redshift', 'Redshift')+1))) as index;

-- result
"index"
"21"


-- provided
SELECT OCTET_LENGTH('français') AS bytes, LEN('français') AS chars;

-- expected
SELECT  Octet_Length( CASE Typeof( 'français' )
                        WHEN 'VARCHAR'
                            THEN Encode( 'français'::VARCHAR )
                        ELSE Encode( 'français' )
                        END ) AS bytes
        , CASE Typeof( 'français' )
            WHEN 'VARCHAR'
                THEN Length(  Try_Cast( 'français' AS VARCHAR ) )
            WHEN 'BLOB'
                THEN Octet_Length(  Try_Cast( 'français' AS BLOB ) )
            END AS chars
;

-- result
"bytes","chars"
"9","8"


-- provided
SELECT REGEXP_COUNT('abcdefghijklmnopqrstuvwxyz', '[a-z]{3}') AS count;

-- expected
SELECT Length(regexp_split_to_array('abcdefghijklmnopqrstuvwxyz', '[a-z]{3}'))-1 AS count;

-- result
"count"
"8"


-- provided
SELECT email, REGEXP_COUNT(email,'@[^.]*\\.(org|edu)') AS count FROM users
ORDER BY userid LIMIT 4;

-- expected
SELECT email,  Length(regexp_split_to_array(email,'@[^.]*\.(org|edu)'))-1 AS count  FROM users
ORDER BY userid LIMIT 4;

-- result
"email","count"
"Etiam.laoreet.libero@sodalesMaurisblandit.edu","1"
"Suspendisse.tristique@nonnisiAenean.edu","1"
"amet.faucibus.ut@condimentumegetvolutpat.ca","0"
"sed@lacusUtnec.ca","0"


-- provided
SELECT venuename, REGEXP_INSTR(venuename,'[cC]ent(er|re)$') AS pos
FROM venue
WHERE REGEXP_INSTR(venuename,'[cC]ent(er|re)$') > 0
ORDER BY venueid LIMIT 4;

-- expected
SELECT venuename, case when len(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$'))>1 then len(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$')[1])+1 else 0 end AS pos
FROM venue
WHERE case when len(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$'))>1 then len(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$')[1])+1 else 0 end > 0
ORDER BY venueid LIMIT 4;

-- result
"venuename","pos"
"The Home Depot Center","16"
"Izod Center","6"
"Wachovia Center","10"
"Air Canada Centre","12"

