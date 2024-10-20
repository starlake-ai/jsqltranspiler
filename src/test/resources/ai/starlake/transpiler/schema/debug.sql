-- provided
SELECT  numtickets
        , priceperticket
        , totalprice
        , listtime
FROM sales
    , listing
LIMIT 10
;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","numtickets","numtickets","listing","main","JSQLTranspilerTest","SMALLINT","SMALLINT","16","0","16"
"2","priceperticket","priceperticket","listing","main","JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","8","2","8"
"3","totalprice","totalprice","listing","main","JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","8","2","8"
"4","listtime","listtime","listing","main","JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"