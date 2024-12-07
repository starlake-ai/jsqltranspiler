-- provided
select st_area(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area;

-- expected
SELECT ST_Area_Spheroid(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) AS area;

-- result
"area"
"12308778361.469452"
