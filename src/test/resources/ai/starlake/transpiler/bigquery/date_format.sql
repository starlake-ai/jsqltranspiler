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

-- provided
SELECT FORMAT_TIMESTAMP('%c', TIMESTAMP '2050-12-25 15:30:55+00', 'UTC')
  AS formatted;

-- expected
SELECT
  strftime(TIMESTAMPTZ '2050-12-25 15:30:55+00' AT TIME ZONE 'UTC', '%a %b %-d %-H:%M:%S %Y')
  AS formatted;

-- result
"formatted"
"Sun Dec 25 15:30:55 2050"

-- provided
SELECT FORMAT_TIMESTAMP('%b-%d-%Y', TIMESTAMP '2050-12-25 15:30:55+00')
  AS formatted;

-- expected
SELECT
  strftime(TIMESTAMPTZ '2050-12-25 15:30:55+00', '%b-%d-%Y')
  AS formatted;

-- result
"formatted"
"Dec-25-2050"

-- provided
SELECT FORMAT_TIMESTAMP('%b %Y', TIMESTAMP '2050-12-25 15:30:55+00')
  AS formatted;

-- expected
SELECT
  strftime(TIMESTAMPTZ '2050-12-25 15:30:55+00', '%b %Y')
  AS formatted;

-- result
"formatted"
"Dec 2050"

-- provided
SELECT FORMAT_TIMESTAMP('%Y-%m-%dT%H:%M:%SZ', TIMESTAMP '2050-12-25 15:30:55', 'UTC')
  AS formatted;

-- expected
SELECT
  strftime(TIMESTAMP '2050-12-25 15:30:55' AT TIME ZONE 'UTC', '%Y-%m-%dT%H:%M:%SZ')
  AS formatted;

-- result
"formatted"
"2050-12-25T22:30:55Z"

-- provided
SELECT STRING(TIMESTAMP '2008-12-25 15:30:00+00', 'UTC') AS string;

-- expected
SELECT STRFTIME(TIMESTAMP WITH TIME ZONE '2008-12-25T15:30:00.000+0000' AT TIME ZONE 'UTC','%c%z')AS STRING;

-- result
"string"
"2008-12-25 15:30:00+00"

