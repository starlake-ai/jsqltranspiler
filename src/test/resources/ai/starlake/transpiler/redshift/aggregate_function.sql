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
"2008-01-07","658","2081400.0","2028.0"
"2008-01-02","614","2064840.0","2232.0"
"2008-07-22","593","1994256.0","2240.0"
"2008-01-26","595","1993188.0","2280.0"
"2008-02-24","655","1975345.0","2096.0"
"2008-02-04","616","1972491.0","2052.0"
"2008-02-14","628","1971759.0","2205.0"
"2008-09-01","600","1944976.0","2124.0"
"2008-07-29","597","1944488.0","2112.0"
"2008-07-23","592","1943265.0","1998.0"



-- provided
select avg(pricepaid) as avg_price, month
from sales, date
where sales.dateid = date.dateid
group by month
order by avg_price desc;

-- result
"avg_price","month"
"659.342636798","MAR"
"655.066215689","APR"
"645.82426723","JAN"
"643.102732511","MAY"
"642.722631914","JUN"
"642.376089664","SEP"
"640.724448958","OCT"
"640.573169004","DEC"
"635.340504406","JUL"
"635.242600238","FEB"
"634.240586214","NOV"
"632.782584885","AUG"


-- provided
select approximate count(distinct pricepaid) AS count from sales;

-- expected
select approx_count_distinct(distinct pricepaid) AS count from sales;

-- result
"count"
"4655"


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
"17503.962566845","27847.76","27773.2"


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
"10.159316769","53.6291","53.6288"


-- provided
select salesid, dateid, sellerid, qty,
avg(qty) over
(order by dateid, salesid rows unbounded preceding) as avg
from winsales
order by 2,1;

-- result
"salesid","dateid","sellerid","qty","avg"
"30001","2003-08-02","3","10","10.0"
"10001","2003-12-24","1","10","10.0"
"10005","2003-12-24","1","30","16.666666667"
"40001","2004-01-09","4","40","22.5"
"10006","2004-01-18","1","10","20.0"
"20001","2004-02-12","2","20","20.0"
"40005","2004-02-12","4","10","18.571428571"
"20002","2004-02-16","2","20","18.75"
"30003","2004-04-18","3","15","18.333333333"
"30004","2004-04-18","3","20","18.5"
"30007","2004-09-07","3","30","19.545454545"


-- provided
select salesid, qty,
count(*) over (order by salesid rows unbounded preceding) as count
from winsales
order by salesid;

-- result
"salesid","qty","count"
"10001","10","1"
"10005","30","2"
"10006","10","3"
"20001","20","4"
"20002","20","5"
"30001","10","6"
"30003","15","7"
"30004","20","8"
"30007","30","9"
"40001","40","10"
"40005","10","11"


-- provide
select salesid, qty, qty_shipped,
count(qty_shipped)
over (order by salesid rows unbounded preceding) as count
from winsales
order by salesid;

-- result
"salesid","qty","qty_shipped","count"
"10001","10","10","1"
"10005","30","","1"
"10006","10","","1"
"20001","20","20","2"
"20002","20","20","3"
"30001","10","10","4"
"30003","15","","4"
"30004","20","","4"
"30007","30","","4"
"40001","40","","4"
"40005","10","10","5"


-- provided
select sellerid, qty, cume_dist()
over (partition by sellerid order by qty) as cumdist
from winsales
ORDER BY 1, 2;

-- result
"sellerid","qty","cumdist"
"1","10","0.666666667"
"1","10","0.666666667"
"1","30","1.0"
"2","20","1.0"
"2","20","1.0"
"3","10","0.25"
"3","15","0.5"
"3","20","0.75"
"3","30","1.0"
"4","10","0.5"
"4","40","1.0"


-- provided
SELECT salesid, qty,
DENSE_RANK() OVER(ORDER BY qty DESC) AS d_rnk,
RANK() OVER(ORDER BY qty DESC) AS rnk
FROM winsales
ORDER BY 2,1;

-- result
"salesid","qty","d_rnk","rnk"
"10001","10","5","8"
"10006","10","5","8"
"30001","10","5","8"
"40005","10","5","8"
"30003","15","4","7"
"20001","20","3","4"
"20002","20","3","4"
"30004","20","3","4"
"10005","30","2","2"
"30007","30","2","2"
"40001","40","1","1"


-- provided
select venuestate, venueseats, venuename,
first_value(venuename)
over(partition by venuestate
order by venueseats desc
rows between unbounded preceding and unbounded following) AS first_value
from (select * from venue where venueseats >0)
order by venuestate;

-- expected
select venuestate, venueseats, venuename,
first(venuename)
over(partition by venuestate
order by venueseats desc
rows between unbounded preceding and unbounded following) AS first_value
from (select * from venue where venueseats >0)
order by venuestate;

-- result
"venuestate","venueseats","venuename","first_value"
"CA","70561","Qualcomm Stadium","Qualcomm Stadium"
"CA","69843","Monster Park","Qualcomm Stadium"
"CA","63026","McAfee Coliseum","Qualcomm Stadium"
"CA","56000","Dodger Stadium","Qualcomm Stadium"
"CA","45050","Angel Stadium of Anaheim","Qualcomm Stadium"
"CA","42445","PETCO Park","Qualcomm Stadium"
"CA","41503","AT&T Park","Qualcomm Stadium"
"CA","22000","Shoreline Amphitheatre","Qualcomm Stadium"
"CO","76125","INVESCO Field","INVESCO Field"
"CO","50445","Coors Field","INVESCO Field"
"DC","41888","Nationals Park","Nationals Park"
"FL","74916","Dolphin Stadium","Dolphin Stadium"
"FL","73800","Jacksonville Municipal Stadium","Dolphin Stadium"
"FL","65647","Raymond James Stadium","Dolphin Stadium"
"FL","36048","Tropicana Field","Dolphin Stadium"
"GA","71149","Georgia Dome","Georgia Dome"
"GA","50091","Turner Field","Georgia Dome"
"IL","63000","Soldier Field","Soldier Field"
"IL","41118","Wrigley Field","Soldier Field"
"IL","40615","U.S. Cellular Field","Soldier Field"
"IN","63000","Lucas Oil Stadium","Lucas Oil Stadium"
"LA","72000","Louisiana Superdome","Louisiana Superdome"
"MA","68756","Gillette Stadium","Gillette Stadium"
"MA","39928","Fenway Park","Gillette Stadium"
"MD","91704","FedExField","FedExField"
"MD","70107","M&T Bank Stadium","FedExField"
"MD","48876","Oriole Park at Camden Yards","FedExField"
"MI","65000","Ford Field","Ford Field"
"MI","41782","Comerica Park","Ford Field"
"MN","64035","Hubert H. Humphrey Metrodome","Hubert H. Humphrey Metrodome"
"MO","79451","Arrowhead Stadium","Arrowhead Stadium"
"MO","66965","Edward Jones Dome","Arrowhead Stadium"
"MO","49660","Busch Stadium","Arrowhead Stadium"
"MO","40793","Kauffman Stadium","Arrowhead Stadium"
"NC","73298","Bank of America Stadium","Bank of America Stadium"
"NJ","80242","New York Giants Stadium","New York Giants Stadium"
"NY","73967","Ralph Wilson Stadium","Ralph Wilson Stadium"
"NY","52325","Yankee Stadium","Ralph Wilson Stadium"
"NY","20000","Madison Square Garden","Ralph Wilson Stadium"
"OH","73200","Cleveland Browns Stadium","Cleveland Browns Stadium"
"OH","65535","Paul Brown Stadium","Cleveland Browns Stadium"
"OH","43345","Progressive Field","Cleveland Browns Stadium"
"OH","42059","Great American Ball Park","Cleveland Browns Stadium"
"ON","50516","Rogers Centre","Rogers Centre"
"PA","68532","Lincoln Financial Field","Lincoln Financial Field"
"PA","65050","Heinz Field","Lincoln Financial Field"
"PA","43647","Citizens Bank Park","Lincoln Financial Field"
"PA","38496","PNC Park","Lincoln Financial Field"
"TN","68804","LP Field","LP Field"
"TX","72000","Reliant Stadium","Reliant Stadium"
"TX","65595","Texas Stadium","Reliant Stadium"
"TX","49115","Rangers BallPark in Arlington","Reliant Stadium"
"TX","40950","Minute Maid Park","Reliant Stadium"
"WA","67000","Qwest Field","Qwest Field"
"WA","47116","Safeco Field","Qwest Field"
"WI","72922","Lambeau Field","Lambeau Field"
"WI","42200","Miller Park","Lambeau Field"


-- provided
select venuestate, venueseats, venuename,
first_value(venuename) ignore nulls
over(partition by venuestate
order by venueseats desc
rows between unbounded preceding and unbounded following) AS first
from (select * from venue where venuestate='CA')
order by venuestate;


-- expected
select venuestate, venueseats, venuename,
first(venuename ignore nulls)
over(partition by venuestate
order by venueseats desc
rows between unbounded preceding and unbounded following) AS first
from (select * from venue where venuestate='CA')
order by venuestate;


-- result
"venuestate","venueseats","venuename","first"
"CA","70561","Qualcomm Stadium","Qualcomm Stadium"
"CA","69843","Monster Park","Qualcomm Stadium"
"CA","63026","McAfee Coliseum","Qualcomm Stadium"
"CA","56000","Dodger Stadium","Qualcomm Stadium"
"CA","45050","Angel Stadium of Anaheim","Qualcomm Stadium"
"CA","42445","PETCO Park","Qualcomm Stadium"
"CA","41503","AT&T Park","Qualcomm Stadium"
"CA","22000","Shoreline Amphitheatre","Qualcomm Stadium"
"CA","0","The Home Depot Center","Qualcomm Stadium"
"CA","0","HP Pavilion at San Jose","Qualcomm Stadium"
"CA","0","Honda Center","Qualcomm Stadium"
"CA","0","ARCO Arena","Qualcomm Stadium"
"CA","0","Staples Center","Qualcomm Stadium"
"CA","0","Oracle Arena","Qualcomm Stadium"
"CA","0","Fox Theatre","Qualcomm Stadium"
"CA","0","Mountain Winery","Qualcomm Stadium"
"CA","0","Villa Montalvo","Qualcomm Stadium"
"CA","0","Buck Shaw Stadium","Qualcomm Stadium"
"CA","0","Greek Theatre","Qualcomm Stadium"
"CA","0","San Jose Repertory Theatre","Qualcomm Stadium"
"CA","0","Curran Theatre","Qualcomm Stadium"
"CA","0","Geffen Playhouse","Qualcomm Stadium"
"CA","0","Pasadena Playhouse","Qualcomm Stadium"
"CA","0","Royce Hall","Qualcomm Stadium"
"CA","0","War Memorial Opera House","Qualcomm Stadium"
"CA","0","San Francisco Opera","Qualcomm Stadium"
"CA","0","Los Angeles Opera","Qualcomm Stadium"


-- provided
select buyerid, saletime, qtysold,
lag(qtysold,1) over (order by buyerid, saletime) as prev_qtysold
from sales where buyerid = 3 order by buyerid, saletime;

-- result
"buyerid","saletime","qtysold","prev_qtysold"
"3","2008-01-16 01:06:09.0","1",""
"3","2008-01-28 02:10:01.0","1","1"
"3","2008-03-12 10:39:53.0","1","1"
"3","2008-03-13 02:56:07.0","1","1"
"3","2008-03-29 08:21:39.0","2","1"
"3","2008-04-27 02:39:01.0","1","2"
"3","2008-08-16 07:04:37.0","2","1"
"3","2008-08-22 11:45:26.0","2","2"
"3","2008-09-12 09:11:25.0","1","2"
"3","2008-10-01 06:22:37.0","1","1"
"3","2008-10-20 01:55:51.0","2","1"
"3","2008-10-28 01:30:40.0","1","2"


-- provided
select venuestate, venueseats, venuename,
last_value(venuename)
over(partition by venuestate
order by venueseats desc
rows between unbounded preceding and unbounded following) AS last
from (select * from venue where venueseats >0)
order by venuestate;

-- expected
select venuestate, venueseats, venuename,
last(venuename)
over(partition by venuestate
order by venueseats desc
rows between unbounded preceding and unbounded following) AS last
from (select * from venue where venueseats >0)
order by venuestate;

-- result
"venuestate","venueseats","venuename","last"
"CA","70561","Qualcomm Stadium","Shoreline Amphitheatre"
"CA","69843","Monster Park","Shoreline Amphitheatre"
"CA","63026","McAfee Coliseum","Shoreline Amphitheatre"
"CA","56000","Dodger Stadium","Shoreline Amphitheatre"
"CA","45050","Angel Stadium of Anaheim","Shoreline Amphitheatre"
"CA","42445","PETCO Park","Shoreline Amphitheatre"
"CA","41503","AT&T Park","Shoreline Amphitheatre"
"CA","22000","Shoreline Amphitheatre","Shoreline Amphitheatre"
"CO","76125","INVESCO Field","Coors Field"
"CO","50445","Coors Field","Coors Field"
"DC","41888","Nationals Park","Nationals Park"
"FL","74916","Dolphin Stadium","Tropicana Field"
"FL","73800","Jacksonville Municipal Stadium","Tropicana Field"
"FL","65647","Raymond James Stadium","Tropicana Field"
"FL","36048","Tropicana Field","Tropicana Field"
"GA","71149","Georgia Dome","Turner Field"
"GA","50091","Turner Field","Turner Field"
"IL","63000","Soldier Field","U.S. Cellular Field"
"IL","41118","Wrigley Field","U.S. Cellular Field"
"IL","40615","U.S. Cellular Field","U.S. Cellular Field"
"IN","63000","Lucas Oil Stadium","Lucas Oil Stadium"
"LA","72000","Louisiana Superdome","Louisiana Superdome"
"MA","68756","Gillette Stadium","Fenway Park"
"MA","39928","Fenway Park","Fenway Park"
"MD","91704","FedExField","Oriole Park at Camden Yards"
"MD","70107","M&T Bank Stadium","Oriole Park at Camden Yards"
"MD","48876","Oriole Park at Camden Yards","Oriole Park at Camden Yards"
"MI","65000","Ford Field","Comerica Park"
"MI","41782","Comerica Park","Comerica Park"
"MN","64035","Hubert H. Humphrey Metrodome","Hubert H. Humphrey Metrodome"
"MO","79451","Arrowhead Stadium","Kauffman Stadium"
"MO","66965","Edward Jones Dome","Kauffman Stadium"
"MO","49660","Busch Stadium","Kauffman Stadium"
"MO","40793","Kauffman Stadium","Kauffman Stadium"
"NC","73298","Bank of America Stadium","Bank of America Stadium"
"NJ","80242","New York Giants Stadium","New York Giants Stadium"
"NY","73967","Ralph Wilson Stadium","Madison Square Garden"
"NY","52325","Yankee Stadium","Madison Square Garden"
"NY","20000","Madison Square Garden","Madison Square Garden"
"OH","73200","Cleveland Browns Stadium","Great American Ball Park"
"OH","65535","Paul Brown Stadium","Great American Ball Park"
"OH","43345","Progressive Field","Great American Ball Park"
"OH","42059","Great American Ball Park","Great American Ball Park"
"ON","50516","Rogers Centre","Rogers Centre"
"PA","68532","Lincoln Financial Field","PNC Park"
"PA","65050","Heinz Field","PNC Park"
"PA","43647","Citizens Bank Park","PNC Park"
"PA","38496","PNC Park","PNC Park"
"TN","68804","LP Field","LP Field"
"TX","72000","Reliant Stadium","Minute Maid Park"
"TX","65595","Texas Stadium","Minute Maid Park"
"TX","49115","Rangers BallPark in Arlington","Minute Maid Park"
"TX","40950","Minute Maid Park","Minute Maid Park"
"WA","67000","Qwest Field","Safeco Field"
"WA","47116","Safeco Field","Safeco Field"
"WI","72922","Lambeau Field","Miller Park"
"WI","42200","Miller Park","Miller Park"


-- provided
SELECT eventid, commission, saletime, LEAD(commission, 1) over ( ORDER BY saletime ) AS next_comm
FROM sales
WHERE saletime BETWEEN '2008-01-09 00:00:00' AND '2008-01-10 12:59:59'
LIMIT 10;

-- result
"eventid","commission","saletime","next_comm"
"1664","13.2","2008-01-09 01:00:21.0","69.6"
"184","69.6","2008-01-09 01:00:36.0","116.1"
"6870","116.1","2008-01-09 01:02:37.0","11.1"
"3718","11.1","2008-01-09 01:05:19.0","205.5"
"6772","205.5","2008-01-09 01:14:04.0","38.4"
"3074","38.4","2008-01-09 01:26:50.0","209.4"
"5254","209.4","2008-01-09 01:29:16.0","26.4"
"3724","26.4","2008-01-09 01:40:09.0","57.6"
"5303","57.6","2008-01-09 01:40:21.0","51.6"
"3678","51.6","2008-01-09 01:42:54.0","43.8"


-- provided
select listagg(sellerid)
within group (order by sellerid)
over() AS list from winsales;

-- expected
select listagg(sellerid)
over() AS list
from (select * from winsales order by sellerid) AS winsales;

-- result
"list"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"

-- provided
select listagg(sellerid order by sellerid)
over() AS list from winsales;

-- expected
select listagg(sellerid)
over() AS list
from (select * from winsales order by sellerid) AS winsales;

-- result
"list"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"
"1,1,1,2,2,3,3,3,3,4,4"


-- provided
select buyerid,
listagg(salesid,',')
within group (order by salesid)
over (partition by buyerid) as sales_id
from winsales
order by buyerid;


-- expected
select buyerid,
listagg(salesid,',')
over (partition by buyerid) as sales_id
from (select * from winsales order by salesid) AS winsales
order by buyerid;


-- result
"buyerid","sales_id"
"a","10005,40001,40005"
"a","10005,40001,40005"
"a","10005,40001,40005"
"b","20001,30001,30003,30004"
"b","20001,30001,30003,30004"
"b","20001,30001,30003,30004"
"b","20001,30001,30003,30004"
"c","10001,10006,20002,30007"
"c","10001,10006,20002,30007"
"c","10001,10006,20002,30007"
"c","10001,10006,20002,30007"


-- provided
select salesid, qty,
max(qty) over (order by salesid rows unbounded preceding) as max
from winsales
order by salesid;

-- result
"salesid","qty","max"
"10001","10","10"
"10005","30","30"
"10006","10","30"
"20001","20","30"
"20002","20","30"
"30001","10","30"
"30003","15","30"
"30004","20","30"
"30007","30","30"
"40001","40","40"
"40005","10","40"



-- provided
select salesid, qty,
max(qty) over (order by salesid rows between 2 preceding and 1 preceding) as max
from winsales
order by salesid;

-- result
"salesid","qty","max"
"10001","10",""
"10005","30","10"
"10006","10","30"
"20001","20","30"
"20002","20","20"
"30001","10","20"
"30003","15","20"
"30004","20","15"
"30007","30","20"
"40001","40","30"
"40005","10","40"


-- provided
select sellerid, qty, median(qty)
over (partition by sellerid) AS median
from winsales
order by sellerid;

-- result
"sellerid","qty","median"
"1","10","10.0"
"1","30","10.0"
"1","10","10.0"
"2","20","20.0"
"2","20","20.0"
"3","10","17.5"
"3","15","17.5"
"3","20","17.5"
"3","30","17.5"
"4","40","25.0"
"4","10","25.0"


-- provided
select venuestate, venuename, venueseats,
nth_value(venueseats, 3)
ignore nulls
over(partition by venuestate order by venueseats desc
rows between unbounded preceding and unbounded following)
as third_most_seats
from (select * from venue where venueseats > 0 and
venuestate in('CA', 'FL', 'NY'))
order by venuestate;

-- expected
select venuestate, venuename, venueseats,
nth_value(venueseats, 3 ignore nulls)
over(partition by venuestate order by venueseats desc
rows between unbounded preceding and unbounded following)
as third_most_seats
from (select * from venue where venueseats > 0 and
venuestate in('CA', 'FL', 'NY'))
order by venuestate;

-- result
"venuestate","venuename","venueseats","third_most_seats"
"CA","Qualcomm Stadium","70561","63026"
"CA","Monster Park","69843","63026"
"CA","McAfee Coliseum","63026","63026"
"CA","Dodger Stadium","56000","63026"
"CA","Angel Stadium of Anaheim","45050","63026"
"CA","PETCO Park","42445","63026"
"CA","AT&T Park","41503","63026"
"CA","Shoreline Amphitheatre","22000","63026"
"FL","Dolphin Stadium","74916","65647"
"FL","Jacksonville Municipal Stadium","73800","65647"
"FL","Raymond James Stadium","65647","65647"
"FL","Tropicana Field","36048","65647"
"NY","Ralph Wilson Stadium","73967","20000"
"NY","Yankee Stadium","52325","20000"
"NY","Madison Square Garden","20000","20000"


-- provided
select eventname, caldate, pricepaid, ntile(4)
over(order by pricepaid desc) ntile from sales, event, date
where sales.eventid=event.eventid and event.dateid=date.dateid and eventname='Hamlet'
and caldate='2008-08-26'
order by 4,1,2,3;

-- result
"eventname","caldate","pricepaid","ntile"
"Hamlet","2008-08-26","472.0","1"
"Hamlet","2008-08-26","530.0","1"
"Hamlet","2008-08-26","589.0","1"
"Hamlet","2008-08-26","1065.0","1"
"Hamlet","2008-08-26","1883.0","1"
"Hamlet","2008-08-26","296.0","2"
"Hamlet","2008-08-26","334.0","2"
"Hamlet","2008-08-26","355.0","2"
"Hamlet","2008-08-26","460.0","2"
"Hamlet","2008-08-26","106.0","3"
"Hamlet","2008-08-26","212.0","3"
"Hamlet","2008-08-26","216.0","3"
"Hamlet","2008-08-26","230.0","3"
"Hamlet","2008-08-26","25.0","4"
"Hamlet","2008-08-26","53.0","4"
"Hamlet","2008-08-26","94.0","4"
"Hamlet","2008-08-26","100.0","4"


-- provided
select sellerid, qty, percent_rank()
over (partition by sellerid order by qty) prcnt_rank
from winsales
order by 1;

-- result
"sellerid","qty","prcnt_rank"
"1","10","0.0"
"1","10","0.0"
"1","30","1.0"
"2","20","0.0"
"2","20","0.0"
"3","10","0.0"
"3","15","0.333333333"
"3","20","0.666666667"
"3","30","1.0"
"4","10","0.0"
"4","40","1.0"


-- provided
select sellerid, qty, percentile_cont(0.5)
within group (order by qty)
over() as median from winsales
order by 2,1;

-- expected
select sellerid, qty, quantile_cont(qty, 0.5)
OVER() as median from (select * from winsales order by qty) AS winsales
order by 2,1;

-- result
"sellerid","qty","median"
"1","10","20.0"
"1","10","20.0"
"3","10","20.0"
"4","10","20.0"
"3","15","20.0"
"2","20","20.0"
"2","20","20.0"
"3","20","20.0"
"1","30","20.0"
"3","30","20.0"
"4","40","20.0"

-- provided
SELECT salesid, sellerid, qty,
ROW_NUMBER() OVER(
   ORDER BY qty ASC) AS row
FROM winsales
ORDER BY 4,1;

-- result
"salesid","sellerid","qty","row"
"30001","3","10","1"
"10001","1","10","2"
"10006","1","10","3"
"40005","4","10","4"
"30003","3","15","5"
"20001","2","20","6"
"20002","2","20","7"
"30004","3","20","8"
"10005","1","30","9"
"30007","3","30","10"
"40001","4","40","11"

-- provided
SELECT salesid, sellerid, qty,
ROW_NUMBER() OVER(
  PARTITION BY sellerid
  ORDER BY qty ASC) AS row_by_seller
FROM winsales
ORDER BY 2,4;

-- result
"salesid","sellerid","qty","row_by_seller"
"10001","1","10","1"
"10006","1","10","2"
"10005","1","30","3"
"20001","2","20","1"
"20002","2","20","2"
"30001","3","10","1"
"30003","3","15","2"
"30004","3","20","3"
"30007","3","30","4"
"40005","4","10","1"
"40001","4","40","2"


-- provided
select salesid, dateid, pricepaid,
round(stddev_pop(pricepaid) over
(order by dateid, salesid rows unbounded preceding)) as stddevpop,
round(var_pop(pricepaid) over
(order by dateid, salesid rows unbounded preceding)) as varpop
from sales
order by 2,1
limit 10;

-- result
"salesid","dateid","pricepaid","stddevpop","varpop"
"33095","1827","234.0","0.0","0.0"
"65082","1827","472.0","119.0","14161.0"
"88268","1827","836.0","248.0","61283.0"
"97197","1827","708.0","230.0","53019.0"
"110328","1827","347.0","223.0","49845.0"
"110917","1827","337.0","215.0","46159.0"
"150314","1827","688.0","211.0","44414.0"
"157751","1827","1730.0","447.0","199679.0"
"165890","1827","4192.0","1185.0","1403323.0"
"1134","1828","218.0","1152.0","1326865.0"


-- provided
select salesid, dateid, sellerid, qty,
sum(qty) over (partition by sellerid
order by dateid, salesid rows unbounded preceding) as sum
from winsales
order by 2,1;

-- result
"salesid","dateid","sellerid","qty","sum"
"30001","2003-08-02","3","10","10"
"10001","2003-12-24","1","10","10"
"10005","2003-12-24","1","30","40"
"40001","2004-01-09","4","40","40"
"10006","2004-01-18","1","10","50"
"20001","2004-02-12","2","20","20"
"40005","2004-02-12","4","10","50"
"20002","2004-02-16","2","20","40"
"30003","2004-04-18","3","15","25"
"30004","2004-04-18","3","20","45"
"30007","2004-09-07","3","30","75"


-- provided
select salesid, dateid, pricepaid,
round(stddev_samp(pricepaid) over
(order by dateid, salesid rows unbounded preceding)) as stddevsamp,
round(var_samp(pricepaid) over
(order by dateid, salesid rows unbounded preceding)) as varsamp
from sales
order by 2,1
limit 10;

-- result
"salesid","dateid","pricepaid","stddevsamp","varsamp"
"33095","1827","234.0","",""
"65082","1827","472.0","168.0","28322.0"
"88268","1827","836.0","303.0","91924.0"
"97197","1827","708.0","266.0","70692.0"
"110328","1827","347.0","250.0","62307.0"
"110917","1827","337.0","235.0","55390.0"
"150314","1827","688.0","228.0","51816.0"
"157751","1827","1730.0","478.0","228205.0"
"165890","1827","4192.0","1256.0","1578738.0"
"1134","1828","218.0","1214.0","1474294.0"


-- provided
select salesid, sellerid, qty,
sum(1) over (partition by sellerid
order by sellerid, salesid rows unbounded preceding) as rownum
from winsales
order by 2,1;

-- result
"salesid","sellerid","qty","rownum"
"10001","1","10","1"
"10005","1","30","2"
"10006","1","10","3"
"20001","2","20","1"
"20002","2","20","2"
"30001","3","10","1"
"30003","3","15","2"
"30004","3","20","3"
"30007","3","30","4"
"40001","4","40","1"
"40005","4","10","2"

