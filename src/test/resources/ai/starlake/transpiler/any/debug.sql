-- provided
SELECT current_timezone() as tz;

-- expected
SELECT strftime( current_timestamp, '%Z') as tz;

-- result
"tz"
"Asia/Bangkok"