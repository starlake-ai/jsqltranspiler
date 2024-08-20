-- provided
with a as (
select *
from sales)
select * from a;

-- result
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","salesid","salesid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"2","listid","listid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"3","sellerid","sellerid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"4","buyerid","buyerid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"5","eventid","eventid","a","main","JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"6","dateid","dateid","a","main","JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"7","qtysold","qtysold","a","main","JSQLTranspilerTest","SMALLINT","SMALLINT","0","16","0"
"8","pricepaid","pricepaid","a","main","JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"9","commission","commission","a","main","JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"10","saletime","saletime","a","main","JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"