CREATE OR REPLACE FUNCTION ifplus(a, b, c) AS CASE a WHEN b THEN b ELSE a + c END;

CREATE OR REPLACE FUNCTION ST_MaxDistance(a, b) AS TABLE
(
    SELECT max(ST_DISTANCE_SPHERE(ST_FlipCoordinates(g1.UNNEST.geom), ST_FlipCoordinates(g2.UNNEST.geom))) AS max_distance
    FROM
        UNNEST(st_dump(st_points(a))) AS g1
      , UNNEST(st_dump(st_points(b))) AS g2
);

CREATE OR REPLACE FUNCTION St_DWithin(g1, g2, distance) AS
COALESCE(st_distance_sphere(
               st_startpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(g1,g2))),
               st_endpoint(ST_FLIPCOORDINATES(ST_SHORTESTLINE(g1,g2)))) <= distance, FALSE);