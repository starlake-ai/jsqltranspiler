-- provided
select st_area(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area;

-- expected
SELECT ST_Area_Spheroid(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) AS area;

-- result
"area"
"12308778361.469452"


-- provided
select ST_DISTANCE(ST_GEOGFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOGFROMTEXT('POINT(11.4594367 48.1549958)'), true) / 1000 as km

-- expected
SELECT IF(TRUE
,ST_DISTANCE_SPHEROID(
    ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POINT(2.3058359 48.858904)'))
    ,ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POINT(11.4594367 48.1549958)')))
,ST_DISTANCE_SPHERE(
    ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POINT(2.3058359 48.858904)'))
    ,ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POINT(11.4594367 48.1549958)')))
)/1000 AS KM;

-- results
"km"
"680.463998149"

-- provided
select ST_DISTANCE(ST_GEOGFROMTEXT('POINT(2.3058359 48.858904)'),ST_GEOGFROMTEXT('POINT(11.4594367 48.1549958)')) / 1000 as km

-- expected
SELECT ST_DISTANCE_SPHERE(
    ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POINT(2.3058359 48.858904)'))
    ,ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POINT(11.4594367 48.1549958)'))
)/1000 AS KM;

-- results
"km"
"678.451551489"


-- provided
SELECT st_length(st_geogfromtext("LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)")) as geo;

-- expected
SELECT /* APPROXIMATION: ST_LENGTH SPHERE */ st_length_spheroid(st_flipcoordinates(st_geomfromtext('LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)'))) as geo;

-- results
"geo"
"443770.917248302"


-- provided
SELECT st_maxdistance(st_geogfromtext('LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)'), st_geogfromtext('LINESTRING(10 0, 10 1, 11 1, 11 0, 10 0)')) max_distance;

-- expected
SELECT(SELECT MAX_DISTANCE FROM ST_MAXDISTANCE(ST_GEOMFROMTEXT('LINESTRING(0 0,0 1,1 1,1 0,0 0)'),ST_GEOMFROMTEXT('LINESTRING(10 0,10 1,11 1,11 0,10 0)')))AS MAX_DISTANCE;

-- result
"max_distance"
"1228126.109277834"


-- provided
SELECT st_perimeter(st_geogfromtext('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as perimeter;

-- expected
SELECT ST_PERIMETER_SPHEROID(ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POLYGON((0 0,0 1,1 1,1 0,0 0))')))AS PERIMETER;

-- result
"perimeter"
"443770.917248302"


-- provided
select st_asbinary(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as wkb;

-- expected
SELECT ST_ASWKB(ST_GEOMFROMTEXT('POLYGON((0 0,0 1,1 1,1 0,0 0))')::GEOMETRY) AS WKB;

-- result
"WKB"
"POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))"


-- provided
select ST_CLOSESTPOINT(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), ST_GEOGFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))')) as closest_point

-- expected
select ST_STARTPOINT(ST_ShortestLine(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'))) as closest_point

-- result
"closest_point"
"POINT (1 1)"


-- provided
select 'in' AS label, st_dwithin(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOGFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'), 157226) AS within_distance
UNION ALL
select 'out' AS label, st_dwithin(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOGFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'), 157225) AS within_distance
;

-- expected
select 'in' AS label, st_dwithin(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'), 157226) AS within_distance
UNION ALL
select 'out' AS label, st_dwithin(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'),ST_GEOMFROMTEXT('POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))'), 157225) AS within_distance
;

-- results
"label","within_distance"
"in","true"
"out","false"