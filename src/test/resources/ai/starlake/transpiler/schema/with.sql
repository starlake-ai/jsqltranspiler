-- provided
with a as (
select *
from sales)
select * from a;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","salesid","salesid","a",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"2","listid","listid","a",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"3","sellerid","sellerid","a",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"4","buyerid","buyerid","a",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"5","eventid","eventid","a",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"6","dateid","dateid","a",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"7","qtysold","qtysold","a",,"JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"8","pricepaid","pricepaid","a",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"9","commission","commission","a",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"10","saletime","saletime","a",,"JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"