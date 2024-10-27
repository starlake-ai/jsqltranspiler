-- provided
SELECT ST_AsBinary(ST_GeomFromText('POLYGON((0 0,0 1,1 1,1 0,0 0))',4326)) b;

-- expected
SELECT ST_ASWKB(ST_GeomFromText('POLYGON((0 0,0 1,1 1,1 0,0 0))'))::BLOB b;

-- count
1