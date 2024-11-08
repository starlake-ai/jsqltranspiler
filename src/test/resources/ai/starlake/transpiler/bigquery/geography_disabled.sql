-- provided
select st_area(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area

-- expected
SELECT /*APPROXIMATION: SPHERE */ ST_Area_Spheroid(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area

-- results
"area"
12308778361.469452

-- provided
select st_asbinary(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as wkb

-- expected
SELECT ST_aswkb(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as wkb

-- results
"wkb"
"POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))"

-- provided
select ST_BUFFER(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), 20) as buffer

-- expected
SELECT /*APPROXIMATION: ST_BUFFER IN METER */ ST_ASGEOJSON(ST_TRANSFORM(ST_BUFFER(ST_TRANSFORM(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), 'EPSG:4326', 'EPSG:6933'), 20), 'EPSG:6933', 'EPSG:4326'))  as buffer

-- count
1

-- provided
select ST_BUFFERWITHTOLERANCE(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), 20, tolerance_meters => 10) as buffer

-- expected
SELECT /*APPROXIMATION: ST_BUFFERWITHTOLERANCE AS ST_BUFFER IN METER */ ST_ASGEOJSON(ST_TRANSFORM(ST_BUFFER(ST_TRANSFORM(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), 'EPSG:4326', 'EPSG:6933'), 20), 'EPSG:6933', 'EPSG:4326'))  as buffer

-- count
    1

-- provided
select ST_CLOSESTPOINT(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), ST_GEOGFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))')) as closest_point

-- expected
select ST_STARTPOINT(ST_ShortestLine(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'))) as closest_point

-- results
"closest_point"
"POINT (1 1)"

-- provided
select ST_DISTANCE(ST_GEOGFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOGFROMTEXT('POINT(11.4594367 48.1549958)'), true) / 1000 as km

-- expected
select st_distance_spheroid(
               st_startpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOMFROMTEXT('POINT(11.4594367 48.1549958)')))),
               st_endpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOMFROMTEXT('POINT(11.4594367 48.1549958)'))))
       ) / 1000 AS km

-- results
"km"
680.463998149257

-- provided
select ST_DISTANCE(ST_GEOGFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOGFROMTEXT('POINT(11.4594367 48.1549958)')) / 1000 as km

-- expected
select st_distance_sphere(
               st_startpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOMFROMTEXT('POINT(11.4594367 48.1549958)')))),
               st_endpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOMFROMTEXT('POINT(11.4594367 48.1549958)'))))
       ) / 1000 AS km

-- results
"km"
678.4515514892884

-- provided
select 'in' AS label, st_dwithin(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOGFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'), 157226) AS within_distance
UNION ALL
select 'out' AS label, st_dwithin(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOGFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'), 157225) AS within_distance

-- expected
select 'in' AS label, COALESCE(st_distance_sphere(
                                       st_startpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))')))),
                                       st_endpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'))))) <= 157226, FALSE) AS within_distance
UNION ALL
select 'out' AS label, COALESCE(st_distance_sphere(
                                        st_startpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))')))),
                                        st_endpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'))))) <= 157225, FALSE) AS within_distance

-- results
"label","within_distance"
"in","true"
"out","false"

-- provided
SELECT ST_GEOGFROMWKB(FROM_HEX('010100000000000000000000400000000000001040')) AS geo

-- expected
SELECT
    CASE TYPEOF(FROM_HEX('010100000000000000000000400000000000001040'))
        WHEN 'VARCHAR' THEN ST_GeomFromHEXWKB(FROM_HEX('010100000000000000000000400000000000001040')::VARCHAR)
        ELSE ST_GeomFromWKB(FROM_HEX('010100000000000000000000400000000000001040')::BLOB)
    END AS geo

-- results
"geo"
"POINT (2 4)"

-- provided
SELECT ST_GEOGFROMWKB('010100000000000000000000400000000000001040') AS geo

-- expected
SELECT
    CASE TYPEOF('010100000000000000000000400000000000001040')
        WHEN 'VARCHAR' THEN ST_GeomFromHEXWKB('010100000000000000000000400000000000001040'::VARCHAR)
        ELSE ST_GeomFromWKB('010100000000000000000000400000000000001040'::BLOB)
    END AS geo

-- results
"geo"
"POINT (2 4)"

-- provided
SELECT "Point" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "Point", "coordinates": [30.0, 10.0]}')) as geo
UNION ALL
SELECT "LineString" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "LineString", "coordinates": [[30.0, 10.0],[10.0, 30.0],[40.0, 40.0]]}'))
UNION ALL
SELECT "Polygon1" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "Polygon", "coordinates": [[[30.0, 10.0],[40.0, 40.0],[20.0, 40.0],[10.0, 20.0],[30.0, 10.0]]]}'))
UNION ALL
SELECT "Polygon2" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "Polygon", "coordinates": [[[35.0, 10.0],[45.0, 45.0],[15.0, 40.0],[10.0, 20.0],[35.0, 10.0]],[[20.0, 30.0],[35.0, 35.0],[30.0, 20.0],[20.0, 30.0]]]}'))
UNION ALL
SELECT "MultiPoint" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "MultiPoint", "coordinates": [[10.0, 40.0],[40.0, 30.0],[20.0, 20.0],[30.0, 10.0]]}'))
UNION ALL
SELECT "MultiLineString" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "MultiLineString", "coordinates": [[[10.0, 10.0],[20.0, 20.0],[10.0, 40.0]],[[40.0, 40.0],[30.0, 30.0],[40.0, 20.0],[30.0, 10.0]]]}'))
UNION ALL
SELECT "MultiPolygon" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "MultiPolygon", "coordinates": [[[[40.0, 40.0],[20.0, 45.0],[45.0, 30.0],[40.0, 40.0]]], [[[20.0, 35.0],[10.0, 30.0],[10.0, 10.0],[30.0, 5.0],[45.0, 20.0],[20.0, 35.0]],[[30.0, 20.0],[20.0, 15.0],[20.0, 25.0],[30.0, 20.0]]]]}'))
UNION ALL
SELECT "GeometryCollection" as type, ST_ISCOLLECTION(ST_GEOGFROMGEOJSON('{"type": "GeometryCollection","geometries": [{"type": "Point","coordinates": [40.0, 10.0]},{"type": "LineString","coordinates": [[10.0, 10.0],[20.0, 20.0],[10.0, 40.0]]},{"type": "Polygon","coordinates": [[[40.0, 40.0],[20.0, 45.0],[45.0, 30.0],[40.0, 40.0]]]}]}'))
UNION ALL
SELECT "Empty GeometryCollection" as type, ST_ISCOLLECTION(ST_GEOGFROMTEXT('GEOMETRYCOLLECTION EMPTY'))

-- expected
SELECT 'Point' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "Point", "coordinates": [30.0, 10.0]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "Point", "coordinates": [30.0, 10.0]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'LineString' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "LineString", "coordinates": [[30.0, 10.0],[10.0, 30.0],[40.0, 40.0]]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "LineString", "coordinates": [[30.0, 10.0],[10.0, 30.0],[40.0, 40.0]]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'Polygon1' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "Polygon", "coordinates": [[[30.0, 10.0],[40.0, 40.0],[20.0, 40.0],[10.0, 20.0],[30.0, 10.0]]]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "Polygon", "coordinates": [[[30.0, 10.0],[40.0, 40.0],[20.0, 40.0],[10.0, 20.0],[30.0, 10.0]]]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'Polygon2' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "Polygon", "coordinates": [[[35.0, 10.0],[45.0, 45.0],[15.0, 40.0],[10.0, 20.0],[35.0, 10.0]],[[20.0, 30.0],[35.0, 35.0],[30.0, 20.0],[20.0, 30.0]]]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "Polygon", "coordinates": [[[35.0, 10.0],[45.0, 45.0],[15.0, 40.0],[10.0, 20.0],[35.0, 10.0]],[[20.0, 30.0],[35.0, 35.0],[30.0, 20.0],[20.0, 30.0]]]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'MultiPoint' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "MultiPoint", "coordinates": [[10.0, 40.0],[40.0, 30.0],[20.0, 20.0],[30.0, 10.0]]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "MultiPoint", "coordinates": [[10.0, 40.0],[40.0, 30.0],[20.0, 20.0],[30.0, 10.0]]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'MultiLineString' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "MultiLineString", "coordinates": [[[10.0, 10.0],[20.0, 20.0],[10.0, 40.0]],[[40.0, 40.0],[30.0, 30.0],[40.0, 20.0],[30.0, 10.0]]]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "MultiLineString", "coordinates": [[[10.0, 10.0],[20.0, 20.0],[10.0, 40.0]],[[40.0, 40.0],[30.0, 30.0],[40.0, 20.0],[30.0, 10.0]]]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'MultiPolygon' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "MultiPolygon", "coordinates": [[[[40.0, 40.0],[20.0, 45.0],[45.0, 30.0],[40.0, 40.0]]], [[[20.0, 35.0],[10.0, 30.0],[10.0, 10.0],[30.0, 5.0],[45.0, 20.0],[20.0, 35.0]],[[30.0, 20.0],[20.0, 15.0],[20.0, 25.0],[30.0, 20.0]]]]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "MultiPolygon", "coordinates": [[[[40.0, 40.0],[20.0, 45.0],[45.0, 30.0],[40.0, 40.0]]], [[[20.0, 35.0],[10.0, 30.0],[10.0, 10.0],[30.0, 5.0],[45.0, 20.0],[20.0, 35.0]],[[30.0, 20.0],[20.0, 15.0],[20.0, 25.0],[30.0, 20.0]]]]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'GeometryCollection' as type, NOT ST_ISEMPTY(ST_GEOMFROMGEOJSON('{"type": "GeometryCollection","geometries": [{"type": "Point","coordinates": [40.0, 10.0]},{"type": "LineString","coordinates": [[10.0, 10.0],[20.0, 20.0],[10.0, 40.0]]},{"type": "Polygon","coordinates": [[[40.0, 40.0],[20.0, 45.0],[45.0, 30.0],[40.0, 40.0]]]}]}')) AND ST_GEOMETRYTYPE(ST_GEOMFROMGEOJSON('{"type": "GeometryCollection","geometries": [{"type": "Point","coordinates": [40.0, 10.0]},{"type": "LineString","coordinates": [[10.0, 10.0],[20.0, 20.0],[10.0, 40.0]]},{"type": "Polygon","coordinates": [[[40.0, 40.0],[20.0, 45.0],[45.0, 30.0],[40.0, 40.0]]]}]}')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')
UNION ALL
SELECT 'Empty GeometryCollection' AS TYPE, NOT ST_ISEMPTY(ST_GEOMFROMTEXT('GEOMETRYCOLLECTION EMPTY')) AND ST_GEOMETRYTYPE(ST_GEOMFROMTEXT('GEOMETRYCOLLECTION EMPTY')) IN ('MULTIPOINT', 'GEOMETRYCOLLECTION', 'MULTILINESTRING', 'MULTIPOLYGON')

-- results
"type","geo"
Point,false
LineString,false
Polygon1,false
Polygon2,false
MultiPoint,true
MultiLineString,true
MultiPolygon,true
GeometryCollection,true
Empty GeometryCollection,false

-- provided
SELECT st_length(st_geogfromtext("LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)")) as geo

-- expected
SELECT /* APPROXIMATION: ST_LENGTH SPHERE */ st_length_spheroid(st_flipcoordinates(st_geomfromtext('LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)'))) as geo

-- results
"geo"
443770.91724830196

-- provided
SELECT st_maxdistance(st_geogfromtext('LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)'), st_geogfromtext('LINESTRING(10 0, 10 1, 11 1, 11 0, 10 0)'))

-- expected
SELECT max(ST_DISTANCE_SPHERE(ST_FlipCoordinates(g1.UNNEST.geom), ST_FlipCoordinates(g2.UNNEST.geom))) AS max_distance FROM UNNEST(st_dump(st_points(st_geomfromtext('LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)')))) AS g1, UNNEST(st_dump(st_points(st_geomfromtext('LINESTRING(10 0, 10 1, 11 1, 11 0, 10 0)')))) g2

-- results
"max_distance"
1228126.109277834

-- provided
SELECT st_perimeter(st_geogfromtext('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as perimeter

-- expected
SELECT st_perimeter_spheroid(st_geomfromtext('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as perimeter

-- results
"perimeter"
443770.91724830196

-- provided
WITH DATA AS (
	SELECT '010100000000000000000000400000000000001040' AS INPUT
	UNION ALL
	SELECT 'POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))'
	UNION ALL
	SELECT '{ "type": "Polygon", "coordinates": [ [ [2, 0], [2, 2], [1, 2], [0, 2], [0, 0], [2, 0] ] ] }'
)
SELECT
    ST_GEOGFROM(INPUT) AS geo
FROM DATA

-- expected
WITH DATA AS (
	SELECT '010100000000000000000000400000000000001040' AS INPUT
	UNION ALL
	SELECT 'POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))'
	UNION ALL
	SELECT '{ "type": "Polygon", "coordinates": [ [ [2, 0], [2, 2], [1, 2], [0, 2], [0, 0], [2, 0] ] ] }'
)
SELECT
    CASE typeof(INPUT)
        WHEN 'VARCHAR' THEN
            CASE
                WHEN trim(INPUT::VARCHAR) LIKE '{%' THEN ST_GeomFromGeoJSON(INPUT::VARCHAR)
                WHEN regexp_full_match(upper(INPUT::VARCHAR), '^[0-9a-fA-F]+$') THEN ST_GeomFromHEXWKB(INPUT::VARCHAR)
                ELSE ST_GEOMFROMTEXT(INPUT::VARCHAR)
                --WHEN upper(trim(INPUT::VARCHAR)[0:5]) IN ('POINT', 'LINES', 'POLYG', 'MULTI', 'GEOME', 'CIRCU', 'COMPO', 'CURVE', 'SURFA', 'POLYH', 'TIN', 'TRIAN', 'CIRCL', 'GEODE', 'ELLIP', 'NURBS', 'CLOTH', 'SPIRA', 'BREPS', 'AFFIN') THEN ST_GEOMFROMTEXT(INPUT::VARCHAR)
                END
        ELSE ST_GeomFromWKB(INPUT::BLOB)
        END AS geo
FROM DATA

-- results
"geo"
"POINT(2 4)"
"POLYGON ((0 0, 0 2, 2 2, 2 0, 0 0))"
"POLYGON ((2 0, 2 2, 1 2, 0 2, 0 0, 2 0))"