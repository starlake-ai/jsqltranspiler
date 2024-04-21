-- provided
select sellerid, qty, percentile_cont(0.5)
within group (order by qty)
over() as median from winsales
order by 2,1;

-- expected
select sellerid, qty, quantile_cont(qty, 0.5)
OVER() as median from (select * from winsales order by qty) AS winsales
order by 2,1;

"sellerid","qty","median"
"1","10","20.00"
"1","10","20.00"
"3","10","20.00"
"4","10","20.00"
"3","15","20.00"
"2","20","20.00"
"2","20","20.00"
"3","20","20.00"
"1","30","20.00"
"3","30","20.00"
"4","40","20.00"

