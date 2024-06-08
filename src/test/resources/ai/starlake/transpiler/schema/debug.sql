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