-- provided
SELECT LENGTH(CAST('français' AS VARBYTE)) as bytes, LENGTH('français') as chars;

-- expected
SELECT  CASE Typeof( Encode( 'français' ) )
            WHEN 'VARCHAR'
                THEN Length(  Try_Cast( Encode( 'français' ) AS VARCHAR ) )
            WHEN 'BLOB'
                THEN Octet_Length(  Try_Cast( Encode( 'français' ) AS BLOB ) )
            END AS bytes
        , CASE Typeof( 'français' )
            WHEN 'VARCHAR'
                THEN Length(  Try_Cast( 'français' AS VARCHAR ) )
            WHEN 'BLOB'
                THEN Octet_Length(  Try_Cast( 'français' AS BLOB ) )
            END as chars
;

-- result
"bytes","chars"
"9","8"