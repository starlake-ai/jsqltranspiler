-- provided
WITH polygon AS (SELECT 'POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))' AS p)
SELECT
  ST_CONTAINS(ST_GEOGFROMTEXT(p), ST_GEOGPOINT(1, 1)) AS fromtext_default,
  ST_CONTAINS(ST_GEOGFROMTEXT(p, oriented => FALSE), ST_GEOGPOINT(1, 1)) AS non_oriented,
  ST_CONTAINS(ST_GEOGFROMTEXT(p, oriented => TRUE),  ST_GEOGPOINT(1, 1)) AS oriented
FROM polygon;

-- expected
WITH polygon AS (SELECT 'POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))' AS p)
SELECT
  ST_CONTAINS(ST_GEOMFROMTEXT(p), ST_POINT(1, 1)) AS fromtext_default,
  /*Warning: ORIENTED, PLANAR, MAKE_VALID parameters unsupported.*/ ST_CONTAINS(ST_GEOmFROMTEXT(p, oriented => FALSE), ST_POINT(1, 1)) AS non_oriented,
  /*Warning: ORIENTED, PLANAR, MAKE_VALID parameters unsupported.*/ ST_CONTAINS(ST_GEOmFROMTEXT(p, oriented => TRUE),  ST_POINT(1, 1)) AS oriented
FROM polygon;

-- result
"fromtext_default","non_oriented","oriented"
"true","true","true"


-- provided
select ST_GEOGFROMGEOJSON('{
    "type": "Point",
    "coordinates": [30.0, 10.0]}') as p;

-- expected
select ST_GeomFromGeoJSON('{
    "type": "Point",
    "coordinates": [30.0, 10.0]}') as p;

-- result
"p"
"POINT (30 10)"


--provided
WITH wkb_data AS (
  SELECT '010200000002000000feffffffffffef3f000000000000f03f01000000000008400000000000000040' geo
)
SELECT
  ST_GeogFromWkb(geo, planar=>TRUE) AS from_planar,
  ST_GeogFromWkb(geo, planar=>FALSE) AS from_geodesic
FROM wkb_data
;

-- expected
WITH wkb_data AS (
        SELECT '010200000002000000feffffffffffef3f000000000000f03f01000000000008400000000000000040' geo  )
SELECT  /*Warning: ORIENTED, PLANAR, MAKE_VALID parameters unsupported.*/ If( Regexp_Matches( geo, '^[0-9A-Fa-f]+$' ), St_Geomfromhexewkb( geo ), St_Geomfromwkb( geo::BLOB ) ) AS from_planar
        , /*Warning: ORIENTED, PLANAR, MAKE_VALID parameters unsupported.*/  If( Regexp_Matches( geo, '^[0-9A-Fa-f]+$' ), St_Geomfromhexewkb( geo ), St_Geomfromwkb( geo::BLOB ) ) AS from_geodesic
FROM wkb_data
;

-- result
"from_planar","from_geodesic"
"LINESTRING (1 1, 3 2)","LINESTRING (1 1, 3 2)"


--provided
SELECT
  -- num_seg_quarter_circle=2
  ST_NUMPOINTS(ST_BUFFER(ST_GEOGFROMTEXT('POINT(1 2)'), 50, 2)) AS eight_sides,
  -- num_seg_quarter_circle=8, since 8 is the default
  ST_NUMPOINTS(ST_BUFFER(ST_GEOGFROMTEXT('POINT(100 2)'), 50)) AS thirty_two_sides;

-- expected
SELECT  St_Numpoints( St_Buffer( St_Geomfromtext( 'POINT(1 2)' )::GEOMETRY, 50, 2 )::GEOMETRY ) AS eight_sides
        , St_Numpoints( St_Buffer( St_Geomfromtext( 'POINT(100 2)' )::GEOMETRY, 50 )::GEOMETRY ) AS thirty_two_sides
;

-- result
"eight_sides","thirty_two_sides"
"9","33"
