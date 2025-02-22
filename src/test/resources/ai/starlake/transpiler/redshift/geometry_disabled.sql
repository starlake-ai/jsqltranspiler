-- provided
SELECT ST_Area(ST_GeomFromText('MULTIPOLYGON(((0 0,10 0,0 10,0 0)),((10 0,20 0,20 10,10 0)))')) as area;

-- result
"area"
"100.0"


-- provided
SELECT ST_Distance(ST_GeomFromText('POLYGON((0 2,1 1,0 -1,0 2))'), ST_GeomFromText('POLYGON((-1 -3,-2 -1,0 -3,-1 -3))')) d;

-- expected
SELECT ST_DISTANCE(ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POLYGON((0 2,1 1,0-1,0 2))')),ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('POLYGON((-1-3,-2-1,0-3,-1-3))')))D;

-- result
"d"
"1.414213562"


-- provided
SELECT ST_Length(ST_GeomFromText('MULTILINESTRING((0 0,10 0,0 10),(10 0,20 0,20 10))')) l;

-- expected
SELECT ST_LENGTH(ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('MULTILINESTRING((0 0,10 0,0 10),(10 0,20 0,20 10))')))L;

-- result
"l"
"44.142135624"


-- provided
SELECT ST_Perimeter(ST_GeomFromText('MULTIPOLYGON(((0 0,10 0,0 10,0 0)),((10 0,20 0,20 10,10 0)))')) p;

-- expected
SELECT ST_PERIMETER(ST_FLIPCOORDINATES(ST_GEOMFROMTEXT('MULTIPOLYGON(((0 0,10 0,0 10,0 0)),((10 0,20 0,20 10,10 0)))')))P;

-- result
"p"
"68.284271247"