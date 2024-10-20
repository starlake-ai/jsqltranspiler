-- provided
with a as (
select *
from sales)
select * from a;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","salesid","salesid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","32","0","32"
"2","listid","listid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","32","0","32"
"3","sellerid","sellerid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","32","0","32"
"4","buyerid","buyerid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","32","0","32"
"5","eventid","eventid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","32","0","32"
"6","dateid","dateid","a","main","JSQLTranspilerTest","SMALLINT","SMALLINT","16","0","16"
"7","qtysold","qtysold","a","main","JSQLTranspilerTest","SMALLINT","SMALLINT","16","0","16"
"8","pricepaid","pricepaid","a","main","JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","8","2","8"
"9","commission","commission","a","main","JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","8","2","8"
"10","saletime","saletime","a","main","JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"