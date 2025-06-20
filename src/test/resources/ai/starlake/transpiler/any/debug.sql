-- prolog
create or replace table aggr(k int, v decimal(10,2));
insert into aggr (k, v) values
    (0,  0),
    (0, 10),
    (0, 20),
    (0, 30),
    (0, 40),
    (1, 10),
    (1, 20),
    (2, 10),
    (2, 20),
    (2, 25),
    (2, 30),
    (3, 60),
    (4, NULL);

-- provided
select k, percentile_disc(0.25) within group (order by v) as perc
  from aggr
  group by k
  order by k;

-- expected
SELECT K,QUANTILE_DISC(0.25 ORDER BY V)AS PERC FROM AGGR GROUP BY K ORDER BY K;