-- provided
SELECT
  DATE_DIFF('2017-12-30', '2014-12-30', YEAR) AS year_diff,
  DATE_DIFF('2017-12-30', '2014-12-30', ISOYEAR) AS isoyear_diff;

-- expected
SELECT
  DATE_DIFF('YEAR', DATE '2014-12-30', DATE '2017-12-30' ) AS year_diff,
  DATE_DIFF('ISOYEAR', DATE '2014-12-30', DATE '2017-12-30') AS isoyear_diff;

-- result
"year_diff","isoyear_diff"
"3","2"

