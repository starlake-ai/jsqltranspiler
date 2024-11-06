-- provided
SELECT commission, TRUNC(commission) AS trunc
FROM sales WHERE salesid=784;

-- result
"commission","trunc"
"111.15","111"

-- provided
select format('{:CC}', DATE '2023-12-31') century;

-- expected
"century"
"21"


-- provided
WITH tbl ( g )
    AS (    SELECT St_Geomfromtext( 'POINT(1 2)', 4326 )
            UNION ALL
            SELECT St_Geomfromtext( 'LINESTRING(0 0,10 0)', 4326 )
            UNION ALL
            SELECT St_Geomfromtext( 'MULTIPOINT(13 4,8 5,4 4)', 4326 )
            UNION ALL
            SELECT NULL::GEOMETRY
            UNION ALL
            SELECT St_Geomfromtext( 'POLYGON((0 0,10 0,0 10,0 0))', 4326 ) )
SELECT St_AsText( St_Collect( g )::GEOMETRY ) g
FROM tbl
;

--expected
WITH tbl ( g )
    AS (    SELECT St_Geomfromtext( 'POINT(1 2)' )
            UNION ALL
            SELECT St_Geomfromtext( 'LINESTRING(0 0,10 0)' )
            UNION ALL
            SELECT St_Geomfromtext( 'MULTIPOINT(13 4,8 5,4 4)' )
            UNION ALL
            SELECT NULL::GEOMETRY
            UNION ALL
            SELECT St_Geomfromtext( 'POLYGON((0 0,10 0,0 10,0 0))' ) )
SELECT St_AsText( St_Collect( list(g) )::GEOMETRY ) g
FROM tbl
;

-- result
"g"
"GEOMETRYCOLLECTION (MULTIPOINT (13 4, 8 5, 4 4), POINT (1 2), POLYGON ((0 0, 10 0, 0 10, 0 0)), LINESTRING (0 0, 10 0))"


-- provided
WITH tbl(id, g) AS (SELECT 1, ST_GeomFromText('POINT(1 2)', 4326) UNION ALL
SELECT 1, ST_GeomFromText('POINT(4 5)', 4326) UNION ALL
SELECT 2, ST_GeomFromText('LINESTRING(0 0,10 0)', 4326) UNION ALL
SELECT 2, ST_GeomFromText('LINESTRING(10 0,20 -5)', 4326) UNION ALL
SELECT 3, ST_GeomFromText('MULTIPOINT(13 4,8 5,4 4)', 4326) UNION ALL
SELECT 3, ST_GeomFromText('MULTILINESTRING((-1 -1,-2 -2),(-3 -3,-5 -5))', 4326) UNION ALL
SELECT 4, ST_GeomFromText('POLYGON((0 0,10 0,0 10,0 0))', 4326) UNION ALL
SELECT 4, ST_GeomFromText('POLYGON((20 20,20 30,30 20,20 20))', 4326) UNION ALL
SELECT 1, NULL::geometry UNION ALL SELECT 2, NULL::geometry UNION ALL
SELECT 5, NULL::geometry UNION ALL SELECT 5, NULL::geometry)
SELECT id, ST_AsEWKT(ST_Collect(g)) g FROM tbl GROUP BY id ORDER BY id;

-- expected
WITH tbl (id, g)
    AS (    SELECT  1
                    , St_Geomfromtext( 'POINT(1 2)')
            UNION ALL
            SELECT  1
                    , St_Geomfromtext( 'POINT(4 5)' )
            UNION ALL
            SELECT  2
                    , St_Geomfromtext( 'LINESTRING(0 0,10 0)' )
            UNION ALL
            SELECT  2
                    , St_Geomfromtext( 'LINESTRING(10 0,20 -5)' )
            UNION ALL
            SELECT  3
                    , St_Geomfromtext( 'MULTIPOINT(13 4,8 5,4 4)' )
            UNION ALL
            SELECT  3
                    , St_Geomfromtext( 'MULTILINESTRING((-1 -1,-2 -2),(-3 -3,-5 -5))' )
            UNION ALL
            SELECT  4
                    , St_Geomfromtext( 'POLYGON((0 0,10 0,0 10,0 0))' )
            UNION ALL
            SELECT  4
                    , St_Geomfromtext( 'POLYGON((20 20,20 30,30 20,20 20))' )
            UNION ALL
            SELECT  1
                    , NULL::GEOMETRY
            UNION ALL
            SELECT  2
                    , NULL::GEOMETRY
            UNION ALL
            SELECT  5
                    , NULL::GEOMETRY
            UNION ALL
            SELECT  5
                    , NULL::GEOMETRY )
SELECT  id
        , St_AsText( St_Collect( List(g) ) ) g
FROM tbl
GROUP BY id
ORDER BY id
;

-- result
"id","g"
"1","MULTIPOINT (1 2, 4 5)"
"2","MULTILINESTRING ((10 0, 20 -5), (0 0, 10 0))"
"3","GEOMETRYCOLLECTION (MULTILINESTRING ((-1 -1, -2 -2), (-3 -3, -5 -5)), MULTIPOINT (13 4, 8 5, 4 4))"
"4","MULTIPOLYGON (((20 20, 20 30, 30 20, 20 20)), ((0 0, 10 0, 0 10, 0 0)))"
"5","GEOMETRYCOLLECTION EMPTY"