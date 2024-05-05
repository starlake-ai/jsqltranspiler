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

--@todo: fix the translation and the result. It should return True.

-- result
"same"
"false"


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
        , Ltrim( listtime, '2008-' ) AS ltrim
FROM listing
ORDER BY    1
            , 2
            , 3
LIMIT 10
;

-- result
"listid","listtime","ltrim"
"1","2008-01-24 06:43:29.0","1-24 06:43:29"
"2","2008-03-05 12:25:29.0","3-05 12:25:29"
"3","2008-11-01 07:35:33.0","11-01 07:35:33"
"4","2008-05-24 01:18:37.0","5-24 01:18:37"
"5","2008-05-17 02:29:11.0","5-17 02:29:11"
"6","2008-08-15 02:08:13.0","15 02:08:13"
"7","2008-11-15 09:38:15.0","11-15 09:38:15"
"8","2008-11-09 05:07:30.0","11-09 05:07:30"
"9","2008-09-09 08:03:36.0","9-09 08:03:36"
"10","2008-06-17 09:44:54.0","6-17 09:44:54"


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
SELECT venuename, case when LENGTH(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$'))>1 then LENGTH(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$')[1])+1 else 0 end AS pos
FROM venue
WHERE case when LENGTH(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$'))>1 then LENGTH(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$')[1])+1 else 0 end > 0
ORDER BY venueid LIMIT 4;

-- result
"venuename","pos"
"The Home Depot Center","16"
"Izod Center","6"
"Wachovia Center","10"
"Air Canada Centre","12"


-- provided
SELECT REGEXP_REPLACE('the fox', 'FOX', 'quick brown fox', 1, 'i') AS replaced;

-- expected
SELECT REGEXP_REPLACE('the fox', 'FOX', 'quick brown fox', 'i')  AS replaced;

-- result
"replaced"
"the quick brown fox"


-- provided
SELECT email, REGEXP_REPLACE(email, '@.*\\.(org|gov|com|edu|ca)$') AS replaced
FROM users
ORDER BY userid LIMIT 4;

-- expected
SELECT email, REGEXP_REPLACE(email, '@.*\.(org|gov|com|edu|ca)$', '', 'g') AS replaced
FROM users
ORDER BY userid LIMIT 4;

-- result
"email","replaced"
"Etiam.laoreet.libero@sodalesMaurisblandit.edu","Etiam.laoreet.libero"
"Suspendisse.tristique@nonnisiAenean.edu","Suspendisse.tristique"
"amet.faucibus.ut@condimentumegetvolutpat.ca","amet.faucibus.ut"
"sed@lacusUtnec.ca","sed"


-- provided
SELECT email, regexp_substr(email,'@[^.]*') AS extract
FROM users
ORDER BY userid LIMIT 4;

-- expected
SELECT email, regexp_extract(email,'@[^.]*', 0) AS extract
FROM users
ORDER BY userid LIMIT 4;

-- result
"email","extract"
"Etiam.laoreet.libero@sodalesMaurisblandit.edu","@sodalesMaurisblandit"
"Suspendisse.tristique@nonnisiAenean.edu","@nonnisiAenean"
"amet.faucibus.ut@condimentumegetvolutpat.ca","@condimentumegetvolutpat"
"sed@lacusUtnec.ca","@lacusUtnec"


-- provided
SELECT regexp_substr('the fox', 'FOX', 1, 1, 'i')  AS extract
;

-- expected
SELECT regexp_extract('the fox', 'FOX', 0, 'i')  AS extract
;

-- result
"extract"
"fox"


-- provide
SELECT catid, REPEAT(catid,3) AS repeat
FROM category
ORDER BY 1,2;

-- result
"catid","repeat"
"1","111"
"2","222"
"3","333"
"4","444"
"5","555"
"6","666"
"7","777"
"8","888"
"9","999"
"10","101010"
"11","111111"


-- provide
SELECT catid, catgroup, REPLACE(catgroup, 'Shows', 'Theatre') AS replace
FROM category
ORDER BY 1,2,3;

-- result
"catid","catgroup","replace"
"1","Sports","Sports"
"2","Sports","Sports"
"3","Sports","Sports"
"4","Sports","Sports"
"5","Sports","Sports"
"6","Shows","Theatre"
"7","Shows","Theatre"
"8","Shows","Theatre"
"9","Concerts","Concerts"
"10","Concerts","Concerts"
"11","Concerts","Concerts"


-- provide
SELECT salesid, REVERSE(salesid) AS reverse
FROM sales
ORDER BY salesid DESC LIMIT 5;

-- result
"salesid","reverse"
"172456","654271"
"172455","554271"
"172454","454271"
"172453","354271"
"172452","254271"


-- provide
select venueid, venuename, rtrim(venuename, 'Park') trimmed
from venue
order by 1, 2, 3
limit 10;

-- result
"venueid","venuename","trimmed"
"1","Toyota Park","Toyota "
"2","Columbus Crew Stadium","Columbus Crew Stadium"
"3","RFK Stadium","RFK Stadium"
"4","CommunityAmerica Ballpark","CommunityAmerica Ballp"
"5","Gillette Stadium","Gillette Stadium"
"6","New York Giants Stadium","New York Giants Stadium"
"7","BMO Field","BMO Field"
"8","The Home Depot Center","The Home Depot Cente"
"9","Dick's Sporting Goods Park","Dick's Sporting Goods "
"10","Pizza Hut Park","Pizza Hut "


-- provide
select listtime, split_part(listtime,'-',1) as year,
split_part(listtime,'-',2) as month,
split_part(split_part(listtime,'-',3),' ',1) as day
from listing order by 1 limit 5;

-- result
"listtime","year","month","day"
"2008-01-01 01:03:11.0","2008","01","01"
"2008-01-01 01:03:16.0","2008","01","01"
"2008-01-01 01:03:17.0","2008","01","01"
"2008-01-01 01:03:21.0","2008","01","01"
"2008-01-01 01:03:53.0","2008","01","01"


-- provide
select split_part(listtime,'-',2) as month, count(*) AS count
from listing
group by split_part(listtime,'-',2)
order by 1, 2;

-- result
"month","count"
"01","18543"
"02","16620"
"03","17594"
"04","16822"
"05","17618"
"06","17158"
"07","17626"
"08","17881"
"09","17378"
"10","17756"
"11","12912"
"12","4589"


-- provide
select listid, listtime,
substring(listtime, 6, 2) as month
from listing
order by 1, 2, 3
limit 10;

-- result
"listid","listtime","month"
"1","2008-01-24 06:43:29.0","01"
"2","2008-03-05 12:25:29.0","03"
"3","2008-11-01 07:35:33.0","11"
"4","2008-05-24 01:18:37.0","05"
"5","2008-05-17 02:29:11.0","05"
"6","2008-08-15 02:08:13.0","08"
"7","2008-11-15 09:38:15.0","11"
"8","2008-11-09 05:07:30.0","11"
"9","2008-09-09 08:03:36.0","09"
"10","2008-06-17 09:44:54.0","06"



-- provide
select listid, listtime,
substring(listtime from 6 for 2) as month
from listing
order by 1, 2, 3
limit 10;

-- result
"listid","listtime","month"
"1","2008-01-24 06:43:29.0","01"
"2","2008-03-05 12:25:29.0","03"
"3","2008-11-01 07:35:33.0","11"
"4","2008-05-24 01:18:37.0","05"
"5","2008-05-17 02:29:11.0","05"
"6","2008-08-15 02:08:13.0","08"
"7","2008-11-15 09:38:15.0","11"
"8","2008-11-09 05:07:30.0","11"
"9","2008-09-09 08:03:36.0","09"
"10","2008-06-17 09:44:54.0","06"


-- provide
SELECT TRANSLATE('mint tea', 'inea', 'osin') AS translated;

-- result
"translated"
"most tin"


-- provide
SELECT TRIM(BOTH FROM '    dog    ') AS trimmed;

-- result
"trimmed"
"dog"


-- provide
SELECT venueid, venuename, TRIM('CDG' FROM venuename) AS trimmed
FROM venue
WHERE venuename LIKE '%Park'
ORDER BY 2
LIMIT 7;

-- result
"venueid","venuename","trimmed"
"121","AT&T Park","AT&T Park"
"109","Citizens Bank Park","itizens Bank Park"
"102","Comerica Park","omerica Park"
"9","Dick's Sporting Goods Park","ick's Sporting Goods Park"
"97","Fenway Park","Fenway Park"
"112","Great American Ball Park","reat American Ball Park"
"114","Miller Park","Miller Park"


-- provide
SELECT catname, UPPER(catname) AS upper
FROM category
ORDER BY 1,2;

-- result
"catname","upper"
"Classical","CLASSICAL"
"Jazz","JAZZ"
"MLB","MLB"
"MLS","MLS"
"Musicals","MUSICALS"
"NBA","NBA"
"NFL","NFL"
"NHL","NHL"
"Opera","OPERA"
"Plays","PLAYS"
"Pop","POP"









