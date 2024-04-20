-- provided
SELECT  Any_Value( dateid ) AS dateid
        , eventname
FROM event
WHERE eventname = 'Eagles'
GROUP BY eventname
;

-- result
"dateid","eventname"
"2063","Eagles"


-- provided
select top 10 date.caldate,
count(totalprice), sum(totalprice),
approximate percentile_disc(0.5)
within group (order by totalprice) AS percentile
from listing
join date on listing.dateid = date.dateid
group by date.caldate
order by 3 desc;

-- expected
select date.caldate,
count(totalprice), sum(totalprice),
quantile_disc(0.5 order by totalprice) AS percentile
from listing
join date on listing.dateid = date.dateid
group by date.caldate
order by 3 desc
limit 10;

-- result
"caldate","count(totalprice)","sum(totalprice)","percentile"
"2008-01-07","658","2081400.00","2028.00"
"2008-01-02","614","2064840.00","2232.00"
"2008-07-22","593","1994256.00","2240.00"
"2008-01-26","595","1993188.00","2280.00"
"2008-02-24","655","1975345.00","2096.00"
"2008-02-04","616","1972491.00","2052.00"
"2008-02-14","628","1971759.00","2205.00"
"2008-09-01","600","1944976.00","2124.00"
"2008-07-29","597","1944488.00","2112.00"
"2008-07-23","592","1943265.00","1998.00"



-- provided
select avg(pricepaid) as avg_price, month
from sales, date
where sales.dateid = date.dateid
group by month
order by avg_price desc;

-- result
"avg_price","month"
"659.3426367976497","MAR"
"655.0662156888253","APR"
"645.8242672299973","JAN"
"643.102732511308","MAY"
"642.722631913541","JUN"
"642.3760896637609","SEP"
"640.7244489576489","OCT"
"640.5731690041082","DEC"
"635.3405044059556","JUL"
"635.242600237912","FEB"
"634.2405862141504","NOV"
"632.7825848849945","AUG"


-- provided
select approximate count(distinct pricepaid) AS count from sales;

-- expected
select approx_count_distinct(distinct pricepaid) AS count from sales;

-- result
"count"
"4607"


-- provided
select  count(distinct pricepaid) AS count from sales;

-- result
"count"
"4528"


-- provided
SELECT LISTAGG(sellerid, ', ')
WITHIN GROUP (ORDER BY sellerid) AS list
FROM sales
WHERE eventid = 4337;

-- expected
SELECT LISTAGG(sellerid, ', ' ORDER BY sellerid)  AS list
FROM sales
WHERE eventid = 4337;

-- result
"list"
"380, 380, 1178, 1178, 1178, 2731, 8117, 12905, 32043, 32043, 32043, 32432, 32432, 38669, 38750, 41498, 45676, 46324, 47188, 47188, 48294"


-- provided
SELECT LISTAGG(DISTINCT sellerid, ', ')
WITHIN GROUP (ORDER BY sellerid) AS list
FROM sales
WHERE eventid = 4337;

-- expected
SELECT LISTAGG(DISTINCT sellerid, ', ' ORDER BY sellerid) AS list
FROM sales
WHERE eventid = 4337;

-- result
"list"
"380, 1178, 2731, 8117, 12905, 32043, 32432, 38669, 38750, 41498, 45676, 46324, 47188, 48294"


-- provided
SELECT LISTAGG(sellerid, ', ')
WITHIN GROUP (ORDER BY dateid, sellerid) AS list
FROM sales
WHERE eventid = 4337;

-- expected
SELECT LISTAGG(sellerid, ', ' ORDER BY dateid, sellerid) AS list
FROM sales
WHERE eventid = 4337;

-- result
"list"
"41498, 47188, 1178, 47188, 1178, 1178, 380, 45676, 46324, 32043, 32043, 48294, 32432, 12905, 8117, 38750, 2731, 32043, 32432, 380, 38669"


-- provided
SELECT LISTAGG(
    (SELECT caldate FROM date WHERE date.dateid=sales.dateid), ' | '
)
WITHIN GROUP (ORDER BY sellerid DESC, salesid ASC) AS list
FROM sales
WHERE buyerid = 660;

-- expected
SELECT LISTAGG(
    (SELECT caldate FROM date WHERE date.dateid=sales.dateid), ' | ' ORDER BY sellerid DESC, salesid ASC
) AS list
FROM sales
WHERE buyerid = 660;

-- result
"list"
"2008-07-16 | 2008-07-09 | 2008-01-01 | 2008-10-26"


-- provided
SELECT buyerid,
LISTAGG(salesid,', ')
WITHIN GROUP (ORDER BY salesid) AS sales_id
FROM sales
WHERE buyerid BETWEEN 660 AND 662
GROUP BY buyerid
ORDER BY buyerid;

-- expected
SELECT buyerid,
LISTAGG(salesid,', ' ORDER BY salesid) AS sales_id
FROM sales
WHERE buyerid BETWEEN 660 AND 662
GROUP BY buyerid
ORDER BY buyerid;

-- result
"buyerid","sales_id"
"660","32872, 33095, 33514, 34548"
"661","19951, 20517, 21695, 21931"
"662","3318, 3823, 4215, 51980, 53202, 55908, 57832, 171603"


-- provided
select max(pricepaid/qtysold) as max_ticket_price
from sales;

-- result
"max_ticket_price"
"2500.0"


-- provided
SELECT TOP 10 DISTINCT sellerid, qtysold,
PERCENTILE_CONT(0.5) WITHIN GROUP(ORDER BY qtysold)  AS percentile,
MEDIAN(qtysold)  AS median
FROM sales
GROUP BY sellerid, qtysold
order by 1, 2;

-- expected
SELECT DISTINCT sellerid, qtysold,
QUANTILE_CONT(0.5 ORDER BY qtysold) AS percentile,
MEDIAN(qtysold) AS median
FROM sales
GROUP BY sellerid, qtysold
order by 1, 2
LIMIT 10;

-- result
"sellerid","qtysold","percentile","median"
"1","1","1.0","1.0"
"1","2","2.0","2.0"
"2","1","1.0","1.0"
"2","2","2.0","2.0"
"2","3","3.0","3.0"
"2","4","4.0","4.0"
"3","1","1.0","1.0"
"3","2","2.0","2.0"
"3","4","4.0","4.0"
"4","1","1.0","1.0"


-- provided
select min(pricepaid/qtysold) as min_ticket_price
from sales;

-- result
"min_ticket_price"
"20.0"


-- provided
select avg(venueseats),
cast(stddev_samp(venueseats) as dec(14,2)) stddevsamp,
cast(stddev_pop(venueseats) as dec(14,2)) stddevpop
from venue;

-- expected
select avg(venueseats),
cast(stddev_samp(venueseats) as decimal(14,2)) stddevsamp,
cast(stddev_pop(venueseats) as decimal(14,2)) stddevpop
from venue;

-- result
"avg(venueseats)","stddevsamp","stddevpop"
"17503.96256684492","27847.76","27773.20"


-- provided
select sum(qtysold) AS sum from sales, date
where sales.dateid = date.dateid and date.month = 'MAY';

-- result
"sum"
"32291"


-- provided
select avg(numtickets) AS avg,
cast(var_samp(numtickets) as dec(10,4)) varsamp,
cast(var_pop(numtickets) as dec(10,4)) varpop
from listing;

-- expected
select avg(numtickets) AS avg,
cast(var_samp(numtickets) as decimal(10,4)) varsamp,
cast(var_pop(numtickets) as decimal(10,4)) varpop
from listing;

-- result
"avg","varsamp","varpop"
"10.159316768573017","53.6291","53.6288"

