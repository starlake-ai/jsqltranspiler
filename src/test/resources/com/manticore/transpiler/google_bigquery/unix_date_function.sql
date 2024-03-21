-- provided
SELECT UNIX_DATE(DATE '2008-12-25') AS days_from_epoch
;

-- expected
SELECT DATE_DIFF('DAY', DATE '1970-01-01', DATE '2008-12-25') AS days_from_epoch
;

-- count
1

-- result
"days_from_epoch"
"14238"


-- provided
SELECT DATE_FROM_UNIX_DATE(14238) AS date_from_epoch
;

-- expected
SELECT DATE_ADD(DATE '1970-01-01', INTERVAL '14238 DAY') AS date_from_epoch
;

-- count
1

-- result
"date_from_epoch"
"2008-12-25 00:00:00"

-- provided
SELECT TIMESTAMP_MICROS(1230219000000000) AS timestamp_value;

-- expected
SELECT MAKE_TIMESTAMP(1230219000000000) AS timestamp_value;

-- result
"timestamp_value"
"2008-12-25 15:30:00"


-- provided
SELECT TIMESTAMP_MILLIS(1230219000000) AS timestamp_value;

-- expected
SELECT EPOCH_MS(1230219000000) AS timestamp_value;

-- result
"timestamp_value"
"2008-12-25 15:30:00"


-- provided
SELECT TIMESTAMP_SECONDS(1230219000) AS timestamp_value;

-- expected
SELECT EPOCH_MS(Cast(1230219000 AS INT64) * 1000) AS timestamp_value;

-- result
"timestamp_value"
"2008-12-25 15:30:00"




