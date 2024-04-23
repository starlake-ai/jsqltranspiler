-- provided
select * from values (1,2), (3,4);

-- expected
select *
from (select unnest( [{column1:1, column2:2}, {column1:3, column2:4}], recursive=>true));

-- result
"column1","column2"
"1","2"
"3","4"

