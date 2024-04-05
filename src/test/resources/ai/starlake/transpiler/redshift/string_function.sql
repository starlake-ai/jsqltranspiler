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

