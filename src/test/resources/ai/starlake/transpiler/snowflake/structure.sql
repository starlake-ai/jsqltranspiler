-- provided
select * from values (1,2), (3,4);

-- expected
select *
from (select unnest( [{column1:1, column2:2}, {column1:3, column2:4}], recursive=>true));

-- result
"column1","column2"
"1","2"
"3","4"


-- provided
SELECT OBJECT_CONSTRUCT('a', 1, 'b', 'BBBB', 'c', NULL) o;

-- expected
SELECT { 'a' : 1, 'b' : 'BBBB', 'c' : NULL} o;

-- result
"o"
"{'a': 1, 'b': BBBB, 'c': NULL}"


