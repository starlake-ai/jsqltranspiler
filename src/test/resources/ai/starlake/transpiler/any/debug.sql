-- provided
SELECT LEN(CAST('français' AS VARBYTE)) as bytes, LEN('français');

-- expected
SELECT case typeof(encode('français'))
            when 'BLOB' then octet_length( try_cast(encode('français') AS BLOB))
            when 'VARCHAR' then length(try_cast(encode('français') AS VARCHAR))
            end as bytes
       , case typeof('français')
            when 'BLOB' then octet_length(try_cast('français' AS BLOB))
            when 'VARCHAR' then length(try_cast('français' AS VARCHAR))
            end as chars
;

-- result
"bytes","chars"
"9","8"