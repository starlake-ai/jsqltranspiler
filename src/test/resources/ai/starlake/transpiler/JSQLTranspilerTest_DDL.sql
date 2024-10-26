CREATE TABLE users (
    userid               INTEGER         NOT NULL PRIMARY KEY
    , username           CHAR (8)
    , firstname          VARCHAR (30)
    , lastname           VARCHAR (30)
    , city               VARCHAR (30)
    , state              CHAR (2)
    , email              VARCHAR (100)
    , phone              CHAR (14)
    , likesports         BOOLEAN
    , liketheatre        BOOLEAN
    , likeconcerts       BOOLEAN
    , likejazz           BOOLEAN
    , likeclassical      BOOLEAN
    , likeopera          BOOLEAN
    , likerock           BOOLEAN
    , likevegas          BOOLEAN
    , likebroadway       BOOLEAN
    , likemusicals       BOOLEAN
)
;

CREATE TABLE venue (
    venueid          SMALLINT        NOT NULL PRIMARY KEY
    , venuename      VARCHAR (100)
    , venuecity      VARCHAR (30)
    , venuestate     CHAR (2)
    , venueseats     INTEGER
)
;

CREATE TABLE category (
    catid            SMALLINT        NOT NULL PRIMARY KEY
    , catgroup       VARCHAR (10)
    , catname        VARCHAR (10)
    , catdesc        VARCHAR (50)
)
;

CREATE TABLE date (
    dateid       SMALLINT        NOT NULL PRIMARY KEY
    , caldate    DATE            NOT NULL
    , day        CHARACTER (3)   NOT NULL
    , week       SMALLINT        NOT NULL
    , month      CHARACTER (5)   NOT NULL
    , qtr        CHARACTER (5)   NOT NULL
    , year       SMALLINT        NOT NULL
    , holiday    BOOLEAN         DEFAULT ('N')
)
;

CREATE TABLE event (
    eventid          INTEGER         NOT NULL PRIMARY KEY
    , venueid        SMALLINT        NOT NULL
    , catid          SMALLINT        NOT NULL
    , dateid         SMALLINT        NOT NULL
    , eventname      VARCHAR (200)
    , starttime      TIMESTAMP
)
;

CREATE TABLE listing (
    listid               INTEGER         NOT NULL PRIMARY KEY
    , sellerid           INTEGER         NOT NULL
    , eventid            INTEGER         NOT NULL
    , dateid             SMALLINT        NOT NULL
    , numtickets         SMALLINT        NOT NULL
    , priceperticket     DECIMAL (8,2)
    , totalprice         DECIMAL (8,2)
    , listtime           TIMESTAMP
)
;

CREATE TABLE sales (
    salesid          INTEGER         NOT NULL PRIMARY KEY
    , listid         INTEGER         NOT NULL
    , sellerid       INTEGER         NOT NULL
    , buyerid        INTEGER         NOT NULL
    , eventid        INTEGER         NOT NULL
    , dateid         SMALLINT        NOT NULL
    , qtysold        SMALLINT        NOT NULL
    , pricepaid      DECIMAL (8,2)
    , commission     DECIMAL (8,2)
    , saletime       TIMESTAMP
)
;

-- Additional window function tests from https://docs.aws.amazon.com/redshift/latest/dg/c_Window_functions.html#r_Window_function_example
CREATE TABLE winsales(
  salesid int,
  dateid date,
  sellerid int,
  buyerid char(10),
  qty int,
  qty_shipped int)
;

INSERT INTO winsales VALUES
  (30001, '2003-8-2', 3, 'b', 10, 10),
  (10001, '2003-12-24', 1, 'c', 10, 10),
  (10005, '2003-12-24', 1, 'a', 30, null),
  (40001, '2004-1-9', 4, 'a', 40, null),
  (10006, '2004-1-18', 1, 'c', 10, null),
  (20001, '2004-2-12', 2, 'b', 20, 20),
  (40005, '2004-2-12', 4, 'a', 10, 10),
  (20002, '2004-2-16', 2, 'c', 20, 20),
  (30003, '2004-4-18', 3, 'b', 15, null),
  (30004, '2004-4-18', 3, 'b', 20, null),
  (30007, '2004-9-7', 3, 'c', 30, null)
;

CREATE OR REPLACE TABLE menu_items(
  menu_id INT NOT NULL,
  menu_category VARCHAR(20),
  menu_item_name VARCHAR(50),
  menu_cogs_usd NUMERIC(7,2),
  menu_price_usd NUMERIC(7,2)
  );

INSERT INTO menu_items VALUES(1,'Beverage','Bottled Soda',0.500,3.00);
INSERT INTO menu_items VALUES(2,'Beverage','Bottled Water',0.500,2.00);
INSERT INTO menu_items VALUES(3,'Main','Breakfast Crepe',5.00,12.00);
INSERT INTO menu_items VALUES(4,'Main','Buffalo Mac & Cheese',6.00,10.00);
INSERT INTO menu_items VALUES(5,'Main','Chicago Dog',4.00,9.00);
INSERT INTO menu_items VALUES(6,'Main','Chicken Burrito',3.2500,12.500);
INSERT INTO menu_items VALUES(7,'Main','Chicken Pot Pie Crepe',6.00,15.00);
INSERT INTO menu_items VALUES(8,'Main','Combination Curry',9.00,15.00);
INSERT INTO menu_items VALUES(9,'Main','Combo Fried Rice',5.00,11.00);
INSERT INTO menu_items VALUES(10,'Main','Combo Lo Mein',6.00,13.00);
INSERT INTO menu_items VALUES(11,'Main','Coney Dog',5.00,10.00);
INSERT INTO menu_items VALUES(12,'Main','Creamy Chicken Ramen',8.00,17.2500);
INSERT INTO menu_items VALUES(13,'Snack','Crepe Suzette',4.00,9.00);
INSERT INTO menu_items VALUES(14,'Main','Fish Burrito',3.7500,12.500);
INSERT INTO menu_items VALUES(15,'Snack','Fried Pickles',1.2500,6.00);
INSERT INTO menu_items VALUES(16,'Snack','Greek Salad',4.00,11.00);
INSERT INTO menu_items VALUES(17,'Main','Gyro Plate',8.00,12.00);
INSERT INTO menu_items VALUES(18,'Main','Hot Ham & Cheese',7.00,11.00);
INSERT INTO menu_items VALUES(19,'Dessert','Ice Cream Sandwich',1.00,4.00);
INSERT INTO menu_items VALUES(20,'Beverage','Iced Tea',0.7500,3.00);
INSERT INTO menu_items VALUES(21,'Main','Italian',6.00,11.00);
INSERT INTO menu_items VALUES(22,'Main','Lean Beef Tibs',6.00,13.00);
INSERT INTO menu_items VALUES(23,'Main','Lean Burrito Bowl',3.500,12.500);
INSERT INTO menu_items VALUES(24,'Main','Lean Chicken Tibs',5.00,11.00);
INSERT INTO menu_items VALUES(25,'Main','Lean Chicken Tikka Masala',10.00,17.00);
INSERT INTO menu_items VALUES(26,'Beverage','Lemonade',0.6500,3.500);
INSERT INTO menu_items VALUES(27,'Main','Lobster Mac & Cheese',10.00,15.00);
INSERT INTO menu_items VALUES(28,'Dessert','Mango Sticky Rice',1.2500,5.00);
INSERT INTO menu_items VALUES(29,'Main','Miss Piggie',2.600,6.00);
INSERT INTO menu_items VALUES(30,'Main','Mothers Favorite',4.500,12.00);
INSERT INTO menu_items VALUES(31,'Main','New York Dog',4.00,8.00);
INSERT INTO menu_items VALUES(32,'Main','Pastrami',8.00,11.00);
INSERT INTO menu_items VALUES(33,'Dessert','Popsicle',0.500,3.00);
INSERT INTO menu_items VALUES(34,'Main','Pulled Pork Sandwich',7.00,12.00);
INSERT INTO menu_items VALUES(35,'Main','Rack of Pork Ribs',11.2500,21.00);
INSERT INTO menu_items VALUES(36,'Snack','Seitan Buffalo Wings',4.00,7.00);
INSERT INTO menu_items VALUES(37,'Main','Spicy Miso Vegetable Ramen',7.00,17.2500);
INSERT INTO menu_items VALUES(38,'Snack','Spring Mix Salad',2.2500,6.00);
INSERT INTO menu_items VALUES(39,'Main','Standard Mac & Cheese',3.00,8.00);
INSERT INTO menu_items VALUES(40,'Dessert','Sugar Cone',2.500,6.00);
INSERT INTO menu_items VALUES(41,'Main','Tandoori Mixed Grill',11.00,18.00);
INSERT INTO menu_items VALUES(42,'Main','The Classic',4.00,12.00);
INSERT INTO menu_items VALUES(43,'Main','The King Combo',12.00,20.00);
INSERT INTO menu_items VALUES(44,'Main','The Kitchen Sink',6.00,14.00);
INSERT INTO menu_items VALUES(45,'Main','The Original',1.500,5.00);
INSERT INTO menu_items VALUES(46,'Main','The Ranch',2.400,6.00);
INSERT INTO menu_items VALUES(47,'Main','The Salad of All Salads',6.00,12.00);
INSERT INTO menu_items VALUES(48,'Main','Three Meat Plate',10.00,17.00);
INSERT INTO menu_items VALUES(49,'Main','Three Taco Combo Plate',7.00,11.00);
INSERT INTO menu_items VALUES(50,'Main','Tonkotsu Ramen',7.00,17.2500);
INSERT INTO menu_items VALUES(51,'Main','Two Meat Plate',9.00,14.00);
INSERT INTO menu_items VALUES(52,'Dessert','Two Scoop Bowl',3.00,7.00);
INSERT INTO menu_items VALUES(53,'Main','Two Taco Combo Plate',6.00,9.00);
INSERT INTO menu_items VALUES(54,'Main','Veggie Burger',5.00,9.00);
INSERT INTO menu_items VALUES(55,'Main','Veggie Combo',4.00,9.00);
INSERT INTO menu_items VALUES(56,'Main','Veggie Taco Bowl',6.00,10.00);
INSERT INTO menu_items VALUES(57,'Dessert','Waffle Cone',2.500,6.00);
INSERT INTO menu_items VALUES(58,'Main','Wonton Soup',2.00,6.00);
INSERT INTO menu_items VALUES(59,'Main','Mini Pizza',null,null);
INSERT INTO menu_items VALUES(60,'Main','Large Pizza',null,null);

CREATE TABLE store_sales (
    branch_ID    INTEGER,
    city        VARCHAR,
    gross_sales NUMERIC(9, 2),
    gross_costs NUMERIC(9, 2),
    net_profit  NUMERIC(9, 2)
    );

INSERT INTO store_sales (branch_ID, city, gross_sales, gross_costs)
    VALUES
    (1, 'Vancouver', 110000, 100000),
    (2, 'Vancouver', 140000, 125000),
    (3, 'Montreal', 150000, 140000),
    (4, 'Montreal', 155000, 146000);

UPDATE store_sales SET net_profit = gross_sales - gross_costs;

CREATE OR REPLACE TABLE example_cumulative (p INT, o INT, i INT);

INSERT INTO example_cumulative VALUES
    (  0, 1, 10), (0, 2, 20), (0, 3, 30),
    (100, 1, 10),(100, 2, 30),(100, 2, 5),(100, 3, 11),(100, 3, 120),
    (200, 1, 10000),(200, 1, 200),(200, 1, 808080),(200, 2, 33333),(200, 3, null), (200, 3, 4),
    (300, 1, null), (300, 1, null);


CREATE TABLE example_sliding
  (p INT, o INT, i INT, r INT, s VARCHAR(100));

INSERT INTO example_sliding VALUES
  (100,1,1,70,'seventy'),(100,2,2,30, 'thirty'),(100,3,3,40,'forty'),(100,4,NULL,90,'ninety'),
  (100,5,5,50,'fifty'),(100,6,6,30,'thirty'),
  (200,7,7,40,'forty'),(200,8,NULL,NULL,'n_u_l_l'),(200,9,NULL,NULL,'n_u_l_l'),(200,10,10,20,'twenty'),
  (200,11,NULL,90,'ninety'),
  (300,12,12,30,'thirty'),
  (400,13,NULL,20,'twenty');

CREATE TABLE sales_table (salesperson_name VARCHAR, sales_in_dollars INTEGER);
INSERT INTO sales_table (salesperson_name, sales_in_dollars) VALUES
    ('Smith', 600),
    ('Jones', 1000),
    ('Torkelson', 700),
    ('Dolenz', 800);

CREATE TABLE accommodations (
  id INTEGER PRIMARY KEY,
  shape GEOMETRY,
  name VARCHAR(100),
  host_name VARCHAR(100),
  neighbourhood_group VARCHAR(100),
  neighbourhood VARCHAR(100),
  room_type VARCHAR(100),
  price SMALLINT,
  minimum_nights SMALLINT,
  number_of_reviews SMALLINT,
  last_review DATE,
  reviews_per_month NUMERIC(8,2),
  calculated_host_listings_count SMALLINT,
  availability_365 SMALLINT
);

CREATE TABLE zipcode (
  ogc_field INTEGER PRIMARY KEY NOT NULL,
  wkb_geometry GEOMETRY,
  gml_id VARCHAR(256),
  spatial_name VARCHAR(256),
  spatial_alias VARCHAR(256),
  spatial_type VARCHAR(256)
 );