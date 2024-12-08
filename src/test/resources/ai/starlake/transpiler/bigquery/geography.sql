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
,ST_DISTANCE(
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

-- result
"max_distance"
1228126.109277834


-- provided
SELECT st_perimeter(st_geogfromtext('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as perimeter

-- expected
SELECT st_perimeter_spheroid(st_geomfromtext('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as perimeter

-- result
"perimeter"
443770.91724830196