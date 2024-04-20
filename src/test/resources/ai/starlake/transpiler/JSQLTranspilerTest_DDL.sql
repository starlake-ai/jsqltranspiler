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