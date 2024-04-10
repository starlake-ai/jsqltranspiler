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



