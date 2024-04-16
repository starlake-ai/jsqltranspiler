-- provided
select to_char(timestamp '2009-12-31 23:15:59', 'MONTH-DY-DD-YYYY HH12:MIPM') AS chars;

-- expected
SELECT Strftime( TIMESTAMP '2009-12-31 23:15:59', '%B -%a-%d-%Y %I:%M%p' ) AS chars
;

-- result
"CHARS"
"December -Thu-31-2009 11:15PM"


-- provided
select to_char(timestamp '2022-05-16 23:15:59', 'ID') AS chars;

-- expected
SELECT STRFTIME(TIMESTAMP '2022-05-16 23:15:59','%u') AS chars;

-- result
"CHARS"
"1"


-- provided
select to_char(starttime, 'HH12:MI:SS')  AS chars
from event where eventid between 1 and 5
order by eventid;

-- expected
SELECT Strftime( starttime, '%I:%M:%S' ) AS chars
FROM event
WHERE eventid BETWEEN 1
                     AND 5
ORDER BY eventid
;

-- result
"CHARS"
"02:30:00"
"08:00:00"
"02:30:00"
"02:30:00"
"07:00:00"


-- provided
select to_char(125.8, '999.99') AS chars;

-- expected
SELECT PRINTF(125.8,'%g') AS chars;

-- result
"chars"
"125.8"


-- provided
select to_char(125.8, '999D99') AS chars;

-- expected
SELECT PRINTF(125.8,'%g') AS chars;

-- result
"chars"
"125.8"


-- provided
select to_char(125.8, '0999D99') AS chars;

-- expected
SELECT PRINTF(125.8,'%g') AS chars;

-- result
"chars"
"125.8"

