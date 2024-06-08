-- provided
select salesid
    , listid
    , sellerid
    , buyerid
    , eventid
    , dateid
    , qtysold
    , pricepaid
    , commission
    , saletime
from sales;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","salesid","salesid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"2","listid","listid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"3","sellerid","sellerid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"4","buyerid","buyerid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"5","eventid","eventid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"6","dateid","dateid","sales",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"7","qtysold","qtysold","sales",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"8","pricepaid","pricepaid","sales",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"9","commission","commission","sales",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"10","saletime","saletime","sales",,"JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"


-- provided
SELECT * FROM sales;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","salesid","salesid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"2","listid","listid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"3","sellerid","sellerid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"4","buyerid","buyerid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"5","eventid","eventid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"6","dateid","dateid","sales",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"7","qtysold","qtysold","sales",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"8","pricepaid","pricepaid","sales",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"9","commission","commission","sales",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"10","saletime","saletime","sales",,"JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"


-- provided
select *
from sales
inner join listing on sales.listid=listing.listid;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","salesid","salesid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"2","listid","listid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"3","sellerid","sellerid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"4","buyerid","buyerid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"5","eventid","eventid","sales",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"6","dateid","dateid","sales",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"7","qtysold","qtysold","sales",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"8","pricepaid","pricepaid","sales",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"9","commission","commission","sales",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"10","saletime","saletime","sales",,"JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"
"11","listid","listid","listing",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"12","sellerid","sellerid","listing",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"13","eventid","eventid","listing",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"14","dateid","dateid","listing",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"15","numtickets","numtickets","listing",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"16","priceperticket","priceperticket","listing",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"17","totalprice","totalprice","listing",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"18","listtime","listtime","listing",,"JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"


-- provided
select a.*
from sales
inner join listing a on sales.listid=a.listid;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","listid","listid","listing",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"2","sellerid","sellerid","listing",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"3","eventid","eventid","listing",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"4","dateid","dateid","listing",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"5","numtickets","numtickets","listing",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"6","priceperticket","priceperticket","listing",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"7","totalprice","totalprice","listing",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"8","listtime","listtime","listing",,"JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"