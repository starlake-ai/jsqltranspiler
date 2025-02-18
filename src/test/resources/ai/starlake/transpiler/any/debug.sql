-- provided
FROM customer
|> LEFT OUTER JOIN orders ON c_custkey = o_custkey AND o_comment NOT LIKE '%unusual%packages%'
|> AGGREGATE COUNT(o_orderkey) c_count GROUP BY c_custkey
|> AGGREGATE COUNT(*) AS custdist GROUP BY c_count
|> ORDER BY custdist DESC, c_count DESC;

-- expected
select COUNT(*) AS custdist, c_count
from (
    select COUNT(o_orderkey) c_count, c_custkey
    FROM customer
    LEFT OUTER JOIN orders ON c_custkey = o_custkey AND o_comment NOT LIKE '%unusual%packages%'
GROUP BY c_custkey
)
GROUP BY c_count
ORDER BY custdist DESC, c_count DESC
;

-- result
"custdist","c_count"
"10002","0"
"1315","10"
"1305","9"
"1247","8"
"1183","11"
"1130","12"
"974","13"
"941","14"
"925","7"
"921","19"
"902","18"
"901","20"
"901","16"
"884","15"
"874","17"
"795","21"
"774","22"
"634","6"
"629","23"
"525","24"
"424","25"
"396","5"
"336","26"
"249","27"
"214","4"
"173","28"
"133","29"
"90","3"
"75","30"
"45","31"
"33","32"
"24","2"
"22","33"
"9","34"
"5","35"
"5","1"
"2","38"
"1","40"
"1","37"
"1","36"