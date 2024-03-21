-- provided
SELECT FORMAT_DATE('%b-%d-%Y', DATE '2008-12-25') AS formatted
;

-- expected
SELECT strftime(DATE '2008-12-25', '%b-%d-%Y') AS formatted
;

-- count
1

-- result
"formatted"
"Dec-25-2008"

-- provided
SELECT
  FORMAT_DATETIME('%c', DATETIME '2008-12-25 15:30:00')
  AS formatted;

-- expected
SELECT
  strftime(DATETIME '2008-12-25 15:30:00', '%a %b %-d %-H:%M:%S %Y')
  AS formatted;

-- result
"formatted"
"Thu Dec 25 15:30:00 2008"


-- provided
SELECT
  FORMAT_DATETIME('%b-%d-%Y', DATETIME '2008-12-25 15:30:00')
  AS formatted;

-- expected
SELECT
  strftime(DATETIME '2008-12-25 15:30:00', '%b-%d-%Y')
  AS formatted;

-- result
"formatted"
"Dec-25-2008"

-- provided
SELECT
  FORMAT_DATETIME('%b %Y', DATETIME '2008-12-25 15:30:00')
  AS formatted;

-- expected
SELECT
  strftime(DATETIME '2008-12-25 15:30:00', '%b %Y')
  AS formatted;

-- result
"formatted"
"Dec 2008"

-- provided
SELECT FORMAT_TIME('%R', TIME '15:30:00') as formatted_time;

-- expected
SELECT
  strftime(CURRENT_DATE() + TIME '15:30:00', '%H:%M')
  AS formatted_time;

-- result
"formatted_time"
"15:30"