with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce;


with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce
|> WHERE
    item != 'bananas'
     AND category IN ('fruit', 'nut')
    |> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales
GROUP BY item
    |> ORDER BY item DESC;


with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM
  Produce AS p1
  JOIN Produce AS p2
    USING (item)
|> WHERE item = 'bananas'
|> SELECT p1.item, p2.sales;

FROM (SELECT 'apples' AS item, 2 AS sales)
|> SELECT item AS fruit_name;


(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales
)
|> EXTEND item IN ('carrots', 'oranges') AS is_orange;

(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales
)
|> EXTEND SUM(sales) OVER() AS total_sales;


(
    SELECT 1 AS x, 11 AS y
    UNION ALL
    SELECT 2 AS x, 22 AS y
)
|> SET x = x * x, y = 3;


FROM (SELECT 2 AS x, 3 AS y) AS t
|> SET x = x * x, y = 8
|> SELECT t.x AS original_x, x, y;

SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    |> DROP sales, category;


FROM (SELECT 1 AS x, 2 AS y) AS t
|> DROP x
|> SELECT t.x AS original_x, y;


SELECT 1 AS x, 2 AS y, 3 AS z
    |> AS t
|> RENAME y AS renamed_y
|> SELECT *, t.y AS t_y;


(
    SELECT "000123" AS id, "apples" AS item, 2 AS sales
    UNION ALL
    SELECT "000456" AS id, "bananas" AS item, 5 AS sales
) AS sales_table
|> AGGREGATE SUM(sales) AS total_sales GROUP BY id, item
-- The sales_table alias is now out of scope. We must introduce a new one.
|> AS t1
|> JOIN (SELECT 456 AS id, "yellow" AS color) AS t2
   ON CAST(t1.id AS INT64) = t2.id
|> SELECT t2.id, total_sales, color;


(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales
)
|> WHERE sales >= 3;


(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales
)
|> ORDER BY item
|> LIMIT 1;


(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales
)
|> ORDER BY item
|> LIMIT 1 OFFSET 2;


(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales
)
|> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales;


(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales
)
|> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales
   GROUP BY item;


with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce
|> AGGREGATE SUM(sales) AS total_sales
   GROUP AND ORDER BY category, item DESC;


with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce
|> AGGREGATE SUM(sales) AS total_sales
    GROUP BY category, item
|> ORDER BY category, item DESC;


with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce
|> AGGREGATE SUM(sales) AS total_sales ASC
    GROUP BY item, category DESC;


with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce
|> AGGREGATE SUM(sales) AS total_sales
    GROUP BY item, category
|> ORDER BY category DESC, total_sales;


(
    SELECT 1 AS x
    UNION ALL
    SELECT 3 AS x
    UNION ALL
    SELECT 2 AS x
)
|> ORDER BY x DESC;


SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3]) AS number
    |> UNION ALL (SELECT 1);

SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3]) AS number
    |> UNION DISTINCT (SELECT 1);

SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3]) AS number
    |> UNION DISTINCT
        (SELECT 1),
        (SELECT 2);


SELECT 1 AS one_digit, 10 AS two_digit
    |> UNION ALL BY NAME
        (SELECT 20 AS two_digit, 2 AS one_digit);


SELECT 1 AS one_digit, 10 AS two_digit
    |> UNION ALL
        (SELECT 20 AS two_digit, 2 AS one_digit);


SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3, 3, 4]) AS number
    |> INTERSECT DISTINCT
        (SELECT * FROM UNNEST(ARRAY<INT64>[2, 3, 3, 5]) AS number);


SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3, 3, 4]) AS number
    |> INTERSECT DISTINCT
        (SELECT * FROM UNNEST(ARRAY<INT64>[2, 3, 3, 5]) AS number),
        (SELECT * FROM UNNEST(ARRAY<INT64>[3, 3, 4, 5]) AS number);


WITH
    NumbersTable AS (
        SELECT 1 AS one_digit, 10 AS two_digit
        UNION ALL
        SELECT 2, 20
        UNION ALL
        SELECT 3, 30
    )
SELECT one_digit, two_digit FROM NumbersTable
    |> INTERSECT ALL BY NAME
        (SELECT 10 AS two_digit, 1 AS one_digit);


WITH
    NumbersTable AS (
        SELECT 1 AS one_digit, 10 AS two_digit
        UNION ALL
        SELECT 2, 20
        UNION ALL
        SELECT 3, 30
    )
SELECT one_digit, two_digit FROM NumbersTable
    |> INTERSECT ALL
        (SELECT 10 AS two_digit, 1 AS one_digit);


SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3, 3, 4]) AS number
    |> EXCEPT DISTINCT
        (SELECT * FROM UNNEST(ARRAY<INT64>[1, 2]) AS number);


SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3, 3, 4]) AS number
    |> EXCEPT DISTINCT
        (SELECT * FROM UNNEST(ARRAY<INT64>[1, 2]) AS number),
        (SELECT * FROM UNNEST(ARRAY<INT64>[1, 4]) AS number);


SELECT * FROM UNNEST(ARRAY<INT64>[1, 2, 3, 3, 4]) AS number
    |> EXCEPT DISTINCT
        (
            SELECT * FROM UNNEST(ARRAY<INT64>[1, 2]) AS number
                |> EXCEPT DISTINCT
                    (SELECT * FROM UNNEST(ARRAY<INT64>[1, 4]) AS number)
        );


WITH
    NumbersTable AS (
        SELECT 1 AS one_digit, 10 AS two_digit
        UNION ALL
        SELECT 2, 20
        UNION ALL
        SELECT 3, 30
    )
SELECT one_digit, two_digit FROM NumbersTable
    |> EXCEPT ALL BY NAME
        (SELECT 10 AS two_digit, 1 AS one_digit);

WITH
    NumbersTable AS (
        SELECT 1 AS one_digit, 10 AS two_digit
        UNION ALL
        SELECT 2, 20
        UNION ALL
        SELECT 3, 30
    )
SELECT one_digit, two_digit FROM NumbersTable
    |> EXCEPT ALL
        (SELECT 10 AS two_digit, 1 AS one_digit);

(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
)
|> AS produce_sales
|> LEFT JOIN
     (
       SELECT "apples" AS item, 123 AS id
     ) AS produce_data
   ON produce_sales.item = produce_data.item
|> SELECT produce_sales.item, sales, id;

(
    SELECT 'apples' AS item, 2 AS sales
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales
)
|> WINDOW SUM(sales) OVER() AS total_sales;

with produce as (
    SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'carrots' AS item, 8 AS sales, 'vegetable' AS category
    UNION ALL
    SELECT 'apples' AS item, 7 AS sales, 'fruit' AS category
    UNION ALL
    SELECT 'bananas' AS item, 5 AS sales, 'fruit' AS category
)
FROM Produce
|> TABLESAMPLE SYSTEM (1 PERCENT);

(
    SELECT "kale" AS product, 51 AS sales, "Q1" AS quarter
    UNION ALL
    SELECT "kale" AS product, 4 AS sales, "Q1" AS quarter
    UNION ALL
    SELECT "kale" AS product, 45 AS sales, "Q2" AS quarter
    UNION ALL
    SELECT "apple" AS product, 8 AS sales, "Q1" AS quarter
    UNION ALL
    SELECT "apple" AS product, 10 AS sales, "Q2" AS quarter
)
|> PIVOT(SUM(sales) FOR quarter IN ('Q1', 'Q2'));

(
    SELECT 'kale' as product, 55 AS Q1, 45 AS Q2
    UNION ALL
    SELECT 'apple', 8, 10
)
|> UNPIVOT(sales FOR quarter IN (Q1, Q2));


WITH client_info AS (
    WITH client AS (
        SELECT 1 AS client_id
        |> UNION ALL
            ( SELECT 2) ,
            ( SELECT 3)
    ),
    basket AS (
        SELECT 1 AS basket_id, 1 AS client_id
            |> UNION ALL
                ( SELECT 2, 2)
        ),
    basket_item AS (
        SELECT 1 AS item_id, 1 AS basket_id
        |> UNION ALL
            ( SELECT 2, 1),
            ( SELECT 3, 1),
            ( SELECT 4, 2)
        ),
    item AS (
        SELECT 1 AS item_id, 'milk' AS name
        |> UNION ALL
            (SELECT 2, "chocolate"),
            (SELECT 3, "donut"),
            (SELECT 4, "croissant")
        )
    FROM
        client c
            LEFT JOIN basket b USING (client_id)
            LEFT JOIN basket_item bi USING (basket_id)
            LEFT JOIN item i
    ON
        i.item_id = bi.item_id
        |> AGGREGATE COUNT(i.item_id) AS bought_item
           GROUP BY c.client_id, i.item_id, i.name
        |> AGGREGATE ARRAY_AGG((SELECT AS STRUCT item_id, name, bought_item)) AS items_info
           GROUP BY client_id
    )
FROM client_info
;