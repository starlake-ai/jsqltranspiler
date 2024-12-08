-- provided
SELECT GeometryType(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))')) as type;

-- expected
SELECT ST_Geometrytype( St_Geomfromtext( 'POLYGON((0 2,1 1,0 -1,0 2))' ) ) as type
;

-- result
"type"
"POLYGON"


-- provided
SELECT ST_Area(ST_GeomFromText('MULTIPOLYGON(((0 0,10 0,0 10,0 0)),((10 0,20 0,20 10,10 0)))')) as area;

-- result
"area"
"100.0"


-- provided
SELECT ST_AsBinary(ST_GeomFromText('POLYGON((0 0,0 1,1 1,1 0,0 0))',4326)) b;

-- expected
SELECT ST_ASWKB(ST_GeomFromText('POLYGON((0 0,0 1,1 1,1 0,0 0))'))::BLOB b;

-- count
1

-- provided
SELECT ST_AsEWKT(ST_GeogFromText('SRID=4324;POLYGON((0 0,0 1,1 1,10 10,1 0,0 0))')) wkb;

-- expected
SELECT ST_ASTEXT(ST_GeomFromText('POLYGON((0 0,0 1,1 1,10 10,1 0,0 0))')) wkb;

-- result
"wkb"
"POLYGON ((0 0, 0 1, 1 1, 10 10, 1 0, 0 0))"


-- provided
SELECT ST_AsGeoJSON(ST_GeomFromText('LINESTRING(3.141592653589793 -6.283185307179586,2.718281828459045 -1.414213562373095)'), 6) j;

--expected
SELECT ST_AsGeoJSON(ST_GeomFromText('LINESTRING(3.141592653589793 -6.283185307179586,2.718281828459045 -1.414213562373095)')::GEOMETRY) j;

-- result
"j"
"{""type"":""LineString"",""coordinates"":[[3.141592653589793,-6.283185307179586],[2.718281828459045,-1.414213562373095]]}"


-- provided
SELECT ST_AsHexWKB(ST_GeomFromText('POLYGON((0 0,0 1,1 1,1 0,0 0))',4326)) h;

--expected
SELECT ST_AsHexWKB(ST_GeomFromText('POLYGON((0 0,0 1,1 1,1 0,0 0))')) h;

-- result
"h"
"01030000000100000005000000000000000000000000000000000000000000000000000000000000000000F03F000000000000F03F000000000000F03F000000000000F03F000000000000000000000000000000000000000000000000"


-- provided
SELECT ST_AsText(ST_GeomFromText('LINESTRING(3.141592653589793 -6.283185307179586,2.718281828459045 -1.414213562373095)', 4326), 6) t;

--expected
SELECT ST_AsText(ST_GeomFromText('LINESTRING(3.141592653589793 -6.283185307179586,2.718281828459045 -1.414213562373095)')::GEOMETRY) t;

-- result
"t"
"LINESTRING (3.141592653589793 -6.283185307179586, 2.718281828459045 -1.414213562373095)"


-- provided
SELECT ST_AsEWKT(ST_Boundary(ST_GeomFromText('POLYGON((0 0,10 0,10 10,0 10,0 0),(1 1,1 2,2 1,1 1))'))) as b;

--expected
SELECT ST_AsText(ST_Boundary(ST_GeomFromText('POLYGON((0 0,10 0,10 10,0 10,0 0),(1 1,1 2,2 1,1 1))'))) as b;

-- result
"b"
"MULTILINESTRING ((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 1 2, 2 1, 1 1))"


-- provided
SELECT ST_AsEwkt(ST_Buffer(ST_GeomFromText('POINT(3 4)'), 2)) b;

--expected
SELECT ST_AsText(ST_Buffer(ST_GeomFromText('POINT(3 4)'), 2)) b;

-- result
"b"
"POLYGON ((5 4, 4.961570560806461 3.609819355967744, 4.847759065022574 3.23463313526982, 4.662939224605091 2.888859533960796, 4.414213562373095 2.585786437626905, 4.111140466039204 2.337060775394909, 3.76536686473018 2.152240934977426, 3.390180644032257 2.038429439193539, 3 2, 2.609819355967744 2.038429439193539, 2.234633135269821 2.152240934977426, 1.888859533960796 2.337060775394909, 1.585786437626905 2.585786437626905, 1.337060775394909 2.888859533960796, 1.152240934977426 3.23463313526982, 1.038429439193539 3.609819355967743, 1 4, 1.038429439193539 4.390180644032257, 1.152240934977426 4.765366864730179, 1.337060775394909 5.111140466039204, 1.585786437626905 5.414213562373095, 1.888859533960796 5.662939224605091, 2.234633135269819 5.847759065022573, 2.609819355967743 5.961570560806461, 3 6, 3.390180644032257 5.961570560806461, 3.76536686473018 5.847759065022573, 4.111140466039204 5.662939224605091, 4.414213562373095 5.414213562373096, 4.662939224605091 5.111140466039204, 4.847759065022573 4.765366864730181, 4.961570560806461 4.390180644032258, 5 4))"


-- provided
SELECT ST_AsEWKT(ST_Centroid(ST_GeomFromText('LINESTRING(110 40, 2 3, -10 80, -7 9, -22 -33)', 4326))) b;

--expected
SELECT ST_AsText(ST_Centroid(ST_GeomFromText('LINESTRING(110 40, 2 3, -10 80, -7 9, -22 -33)'))) b;

-- result
"b"
"POINT (15.696510345521444 27.02067828819046)"


-- provided
SELECT ST_AsText(ST_Collect(ST_GeomFromText('LINESTRING(0 0,1 1)'), ST_GeomFromText('POLYGON((10 10,20 10,10 20,10 10))'))) b;

--expected
SELECT ST_AsText(ST_Collect(array_value(ST_GeomFromText('LINESTRING(0 0,1 1)'), ST_GeomFromText('POLYGON((10 10,20 10,10 20,10 10))')))::GEOMETRY) b;

-- result
"b"
"GEOMETRYCOLLECTION (LINESTRING (0 0, 1 1), POLYGON ((10 10, 20 10, 10 20, 10 10)))"


-- provided
WITH tmp(g1, g2)
AS (SELECT ST_GeomFromText('POLYGON((0 0,10 0,10 10,0 10,0 0))'), ST_GeomFromText('LINESTRING(5 5,10 5,10 6,5 5)')) SELECT ST_Contains(g1, g2) c1, ST_ContainsProperly(g1, g2) c2
FROM tmp;

-- result
"c1","c2"
"true","false"


-- provided
SELECT ST_AsEWKT(ST_ConvexHull(ST_GeomFromText('LINESTRING(0 0,1 0,0 1,1 1,0.5 0.5)'))) as o;

--expected
SELECT ST_AsText(ST_ConvexHull(ST_GeomFromText('LINESTRING(0 0,1 0,0 1,1 1,0.5 0.5)'))) as o;

-- result
"o"
"POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))"


-- provided
SELECT ST_CoveredBy(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))'), ST_GeomFromText('POLYGON((-1 3,2 1,0 -3,-1 3))')) o;

-- result
"o"
"true"


-- provided
SELECT ST_Covers(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))'), ST_GeomFromText('POLYGON((-1 3,2 1,0 -3,-1 3))')) o;

-- result
"o"
"false"


-- provided
SELECT ST_Crosses (ST_GeomFromText('polygon((0 0,10 0,10 10,0 10,0 0))'), ST_GeomFromText('multipoint(5 5,0 0,-1 -1)')) o;

-- result
"o"
"true"


-- provided
SELECT ST_Dimension(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)')) d;

-- result
"d"
"1"


-- provided
SELECT ST_Disjoint(ST_GeomFromText('POLYGON((0 0,10 0,10 10,0 10,0 0),(2 2,2 5,5 5,5 2,2 2))'), ST_Point(4, 4)) d;

-- result
"d"
"true"


-- provided
SELECT ST_Distance(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))'), ST_GeomFromText('POLYGON((-1 -3,-2 -1,0 -3,-1 -3))')) d;

-- expected
SELECT ST_DISTANCE(ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POLYGON((0 2,1 1,0-1,0 2))')),ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POLYGON((-1-3,-2-1,0-3,-1-3))')))D;

-- result
"d"
"1.414213562"



-- provided
WITH airports_raw(code,lon,lat) AS (
(SELECT 'MUC', 11.786111, 48.353889) UNION
(SELECT 'FRA', 8.570556, 50.033333) UNION
(SELECT 'TXL', 13.287778, 52.559722)),
airports1(code,location) AS (SELECT code, ST_Point(lon, lat) FROM airports_raw),
airports2(code,location) AS (SELECT * from airports1)
SELECT (airports1.code || ' <-> ' || airports2.code) AS airports,
round(ST_DistanceSphere(airports1.location, airports2.location) / 1000, 0) AS distance_in_km
FROM airports1, airports2 WHERE airports1.code < airports2.code ORDER BY 1;

-- expected
WITH airports_raw ( code
                    , lon
                    , lat )
    AS (    (   SELECT  'MUC'
                        , 11.786111
                        , 48.353889 )
            UNION (
                SELECT  'FRA'
                        , 8.570556
                        , 50.033333 )
            UNION (
                SELECT  'TXL'
                        , 13.287778
                        , 52.559722 ) )
    , airports1 ( code, location )
    AS (    SELECT  code
                    , St_Point( lon, lat )
            FROM airports_raw )
    , airports2 ( code, location )
    AS (    SELECT *
            FROM airports1 )
SELECT  ( airports1.code || ' <-> ' || airports2.code ) AS airports
        , Round( St_Distance_Sphere( airports1.location, airports2.location ) / 1000, 0 ) AS distance_in_km
FROM airports1
    , airports2
WHERE airports1.code < airports2.code
ORDER BY 1
;

-- result
"airports","distance_in_km"
"FRA <-> MUC","402.0"
"FRA <-> TXL","593.0"
"MUC <-> TXL","486.0"


-- provided
SELECT ST_DWithin(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))'), ST_GeomFromText('POLYGON((-1 3,2 1,0 -3,-1 3))'),5) w;

-- result
"w"
"true"


-- provided
SELECT ST_AsEWKT(ST_EndPoint(ST_GeomFromText('LINESTRING(0 0,10 0,10 10,5 5,0 5)',4326))) p;

-- expected
SELECT ST_AsText(ST_EndPoint(ST_GeomFromText('LINESTRING(0 0,10 0,10 10,5 5,0 5)'))) p;

-- result
"p"
"POINT (0 5)"


-- provided
SELECT ST_AsText(ST_Envelope(ST_GeomFromText('GEOMETRYCOLLECTION(POLYGON((0 0,10 0,0 10,0 0)),LINESTRING(20 10,20 0,10 0))'))) p;

-- expected
SELECT ST_ASTEXT(ST_ENVELOPE(ST_GEOMFROMTEXT('GEOMETRYCOLLECTION(POLYGON((0 0,10 0,0 10,0 0)),LINESTRING(20 10,20 0,10 0))'))::GEOMETRY) p;

-- result
"p"
"POLYGON ((0 0, 20 0, 20 10, 0 10, 0 0))"


-- provided
SELECT ST_Equals(ST_GeomFromText('LINESTRING(1 0,10 0)'), ST_GeomFromText('LINESTRING(1 0,5 0,10 0)')) p;

-- result
"p"
"true"


-- provided
SELECT ST_AsText(ST_ExteriorRing(ST_GeomFromText('POLYGON((7 9,8 7,11 6,15 8,16 6,17 7,17 10,18 12,17 14,15 15,11 15,10 13,9 12,7 9),(9 9,10 10,11 11,11 10,10 8,9 9),(12 14,15 14,13 11,12 14))'))) p;

-- expected
SELECT ST_ASTEXT(ST_EXTERIORRING(ST_GEOMFROMTEXT('POLYGON((7 9,8 7,11 6,15 8,16 6,17 7,17 10,18 12,17 14,15 15,11 15,10 13,9 12,7 9),(9 9,10 10,11 11,11 10,10 8,9 9),(12 14,15 14,13 11,12 14))'))::GEOMETRY) p;

-- result
"p"
"LINESTRING (7 9, 8 7, 11 6, 15 8, 16 6, 17 7, 17 10, 18 12, 17 14, 15 15, 11 15, 10 13, 9 12, 7 9)"


-- provided
SELECT ST_GeometryType(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)')) t;

-- result
"t"
"LINESTRING"


-- provided
SELECT ST_AsText(ST_GeomFromWKB('01030000000100000005000000000000000000000000000000000000000000000000000000000000000000F03F000000000000F03F000000000000F03F000000000000F03F000000000000000000000000000000000000000000000000')) g;

-- expected
SELECT ST_AsText(ST_GeomFromHexWKB('01030000000100000005000000000000000000000000000000000000000000000000000000000000000000F03F000000000000F03F000000000000F03F000000000000F03F000000000000000000000000000000000000000000000000')::GEOMETRY) g;

-- result
"g"
"POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))"


-- provided
SELECT ST_AsEWKT(ST_GeomFromGeoJSON('{"type":"Point","coordinates":[1,2]}')) g;

-- expected
SELECT ST_AsText(ST_GeomFromGeoJSON('{"type":"Point","coordinates":[1,2]}')) g;

-- result
"g"
"POINT (1 2)"


-- provided
SELECT ST_Intersects(ST_GeomFromText('POLYGON((0 0,10 0,10 10,0 10,0 0),(2 2,2 5,5 5,5 2,2 2))'), ST_GeomFromText('MULTIPOINT((4 4),(6 6))')) t;

-- result
"t"
"true"


-- provided
SELECT ST_AsEWKT(ST_Intersection(ST_GeomFromText('polygon((0 0,100 100,0 200,0 0))'), ST_GeomFromText('polygon((0 0,10 0,0 10,0 0))'))) i;

-- expected
SELECT ST_AsText(ST_Intersection(ST_GeomFromText('polygon((0 0,100 100,0 200,0 0))'), ST_GeomFromText('polygon((0 0,10 0,0 10,0 0))'))) i;

-- result
"i"
"POLYGON ((0 0, 0 10, 5 5, 0 0))"


-- provided
SELECT ST_IsEmpty(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))')) e;

-- result
"e"
"false"


-- provided
SELECT ST_IsRing(ST_GeomFromText('linestring(0 0, 1 1, 1 2, 0 0)')) e;

-- result
"e"
"true"

-- provided
SELECT ST_IsSimple(ST_GeomFromText('LINESTRING(0 0,10 0,5 5,5 -5)')) e;

-- result
"e"
"false"


-- provided
SELECT ST_IsValid(ST_GeomFromText('POLYGON((0 0,10 0,10 10,0 10,0 0),(5 0,10 5,5 10,0 5,5 0))')) b;

-- result
"b"
"false"


-- provided
SELECT ST_Length(ST_GeomFromText('MULTILINESTRING((0 0,10 0,0 10),(10 0,20 0,20 10))')) l;

-- expected
SELECT ST_LENGTH(ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('MULTILINESTRING((0 0,10 0,0 10),(10 0,20 0,20 10))')))L;

-- result
"l"
"44.142135624"


-- provided
SELECT ST_LengthSphere(ST_GeomFromText('LINESTRING(10 10,45 45)')) l;

-- expected
SELECT ST_Length_Spheroid(ST_GeomFromText('LINESTRING(10 10,45 45)')) l;

-- result
"l"
"5122094.403815614"


-- provided
SELECT ST_M(ST_GeomFromEWKT('POINT M (1 2 3)')) m;

-- expected
SELECT ST_M(ST_GeomFromText('POINT M (1 2 3)')) m;

-- result
"m"
"3.0"


-- provided
SELECT ST_AsEWKT(ST_MakeEnvelope(2,4,5,7)) e;

-- expected
SELECT ST_AsText(ST_MakeEnvelope(2,4,5,7)) e;

-- result
"e"
"POLYGON ((2 4, 2 7, 5 7, 5 4, 2 4))"



-- provided
SELECT ST_AsText(ST_MakePoint(1,3)) p;

-- expected
SELECT ST_AsText(ST_Point(1,3)::GEOMETRY) p;

-- result
"p"
"POINT (1 3)"


-- provided
SELECT ST_AsText(ST_MakePolygon(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)'))) p;

-- expected
SELECT ST_AsText(ST_MakePolygon(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)'))::GEOMETRY) p;

-- result
"p"
"POLYGON ((77.29 29.07, 77.42 29.26, 77.27 29.31, 77.29 29.07))"


-- provided
SELECT ST_AsEWKT(ST_Multi(ST_GeomFromText('MULTIPOINT((1 2),(3 4))', 4326))) m;

-- expected
SELECT ST_AsText(ST_Multi(ST_GeomFromText('MULTIPOINT((1 2),(3 4))'))) m;

-- result
"m"
"MULTIPOINT (1 2, 3 4)"


-- provided
SELECT ST_NDims(ST_GeomFromText('LINESTRING Z(0 0 3,1 1 3,2 2 3,0 0 3)')) d;

-- expected
SELECT ST_Dimension(ST_GeomFromText('LINESTRING Z(0 0 3,1 1 3,2 2 3,0 0 3)')) d;

-- result
"d"
"1"


-- provided
SELECT ST_NPoints(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)')) p;

-- result
"p"
"4"


-- provided
SELECT ST_NumGeometries(ST_GeomFromText('MULTILINESTRING((0 0,1 0,0 5),(3 4,13 26))')) n;

-- result
"n"
"2"


-- provided
SELECT ST_NumInteriorRings(ST_GeomFromText('POLYGON((0 0,100 0,100 100,0 100,0 0),(1 1,1 5,5 1,1 1),(7 7,7 8,8 7,7 7))')) n;

-- result
"n"
"2"


-- provided
SELECT ST_NumPoints(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)')) n;

-- expected
SELECT ST_NumPoints(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)')::GEOMETRY) n;

-- result
"n"
"4"


-- provided
SELECT ST_Perimeter(ST_GeomFromText('MULTIPOLYGON(((0 0,10 0,0 10,0 0)),((10 0,20 0,20 10,10 0)))')) p;

-- expected
SELECT ST_PERIMETER(ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('MULTIPOLYGON(((0 0,10 0,0 10,0 0)),((10 0,20 0,20 10,10 0)))')))P;

-- result
"p"
"68.284271247"


-- provided
SELECT ST_AsEWKT(ST_PointN(ST_GeomFromText('LINESTRING(0 0,10 0,10 10,5 5,0 5,0 0)',4326), 5)) p;

-- expected
SELECT ST_AsText(ST_PointN(ST_GeomFromText('LINESTRING(0 0,10 0,10 10,5 5,0 5,0 0)'), 5)) p;

-- result
"p"
"POINT (0 5)"


-- provided
SELECT ST_AsEWKT(ST_Points(ST_GeomFromText('MULTIPOLYGON(((0 0,1 0,0 1,0 0)))'))) p;

-- expected
SELECT ST_AsText(ST_Points(ST_GeomFromText('MULTIPOLYGON(((0 0,1 0,0 1,0 0)))'))) p;

-- result
"p"
"MULTIPOINT (0 0, 1 0, 0 1, 0 0)"


-- provided
SELECT ST_AsEWKT(ST_Polygon(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)'),4356)) p;

-- expected
SELECT ST_AsText(ST_MakePolygon(ST_GeomFromText('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)'))) p;

-- result
"p"
"POLYGON ((77.29 29.07, 77.42 29.26, 77.27 29.31, 77.29 29.07))"


-- provided
SELECT ST_AsEWKT(ST_Reverse(ST_GeomFromText('LINESTRING(1 0,2 0,3 0,4 0)', 4326))) r;

-- expected
SELECT ST_AsText(ST_Reverse(ST_GeomFromText('LINESTRING(1 0,2 0,3 0,4 0)'))) r;

-- result
"r"
"LINESTRING (4 0, 3 0, 2 0, 1 0)"


-- provided
SELECT ST_AsEWKT(ST_Simplify(ST_GeomFromText('LINESTRING(0 0,1 2,1 1,2 2,2 1)'), 1)) s;

-- expected
SELECT ST_AsText(ST_Simplify(ST_GeomFromText('LINESTRING(0 0,1 2,1 1,2 2,2 1)'), 1)) s;

-- result
"s"
"LINESTRING (0 0, 1 2, 2 1)"


-- provided
SELECT ST_Touches(ST_GeomFromText('POLYGON((0 0,10 0,0 10,0 0))'), ST_GeomFromText('LINESTRING(20 10,20 0,10 0)')) t;

-- result
"t"
"true"


-- provided
SELECT ST_AsEWKT(ST_Union(ST_GeomFromText('POLYGON((0 0,100 100,0 200,0 0))'), ST_GeomFromText('POLYGON((0 0,10 0,0 10,0 0))'))) u;

-- expected
SELECT ST_AsText(ST_Union(ST_GeomFromText('POLYGON((0 0,100 100,0 200,0 0))'), ST_GeomFromText('POLYGON((0 0,10 0,0 10,0 0))'))) u;

-- result
"u"
"POLYGON ((0 200, 100 100, 5 5, 10 0, 0 0, 0 10, 0 200))"


-- provided
SELECT ST_Within(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))'), ST_GeomFromText('POLYGON((-1 3,2 1,0 -3,-1 3))')) w;

-- result
"w"
"true"
