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

