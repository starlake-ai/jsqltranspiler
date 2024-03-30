-- provided
SELECT SAFE_CONVERT_BYTES_TO_STRING(b'\x61') as safe_convert
;

-- expected
SELECT DECODE(COALESCE(TRY_CAST('\x61' AS BLOB),ENCODE('\x61')))AS SAFE_CONVERT;

-- result
"safe_convert"
"a"

