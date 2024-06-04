.. meta::
   :description: Java Software Library for rewriting Big RDBMS Queries into Duck DB compatible queries.
   :keywords: java sql query transpiler DuckDB H2 BigQuery Snowflake Redshift Databricks

###########################
Java SQL Transpiler Library
###########################

.. toctree::
   :maxdepth: 2
   :hidden:

   install
   usage
   Java API <javadoc>
   changelog

A pure Java stand-alone SQL Transpiler for translating various large RDBMS SQL Dialects into a few smaller RDBMS Dialects for Unit Testing. Based on JSQLParser.

Supports `SELECT` queries as well as `INSERT`, `UPDATE`, `DELETE` and `MERGE` statements.

Internal Functions will be rewritten based on the actual meaning and purpose of the function (since DuckDB `Any()` function does not necessarily behave like the RDBMS specific `Any()`). Respecting different function arguments count, order and type.

Rewrite of Window- and Aggregate-Functions.

Latest stable release: |JSQLTRANSPILER_STABLE_VERSION_LINK|

Development version: |JSQLTRANSPILER_SNAPSHOT_VERSION_LINK|

.. code-block:: SQL
    :caption: Google BigQuery specific Statement

    -- Google BigQuery
    SELECT
        DATE(2016, 12, 25) AS date_ymd,
        DATE(DATETIME '2016-12-25 23:59:59') AS date_dt,
        DATE(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz;

    -- Rewritten DuckDB compliant statement
    SELECT
      MAKE_DATE(2016, 12, 25) AS date_ymd,
      CAST(DATETIME '2016-12-25 23:59:59' AS DATE) AS date_dt,
      CAST(TIMESTAMP '2016-12-25 05:30:00+07' AS DATE) AS date_tstz;

    -- Tally
    1

    -- Result
    "date_ymd","date_dt","date_tstz"
    "2016-12-15","2016-12-15","2016-12-15"


******************************
SQL Dialects
******************************

**JSQLTranspiler** will understand the following Big RDBMS dialects:

    * Google BigQuery
    * Databricks
    * Snowflake
    * Amazon Redshift

It will rewrite into to the following small RDBMS dialects:

    * DuckDB
    * planned: H2
    * planned: HyperSQL
    * planned: Apache Derby

*******************************
Features
*******************************

    * Comprehensive support for Query statements:
        - ``SELECT ...``
        - RDBMS specific Functions, Predicates and Operators
        - Date and Number formatting parameters
        - `ARRAY` access based on different indices (DuckDB starts with 1)
    * `INSERT` statements
    * `DELETE` statements
    * `UPDATE` statements
    * `MERGE` statements

    * Nested Expressions (e.g. Sub-Selects)
    * ``WITH`` clauses
    * PostgreSQL implicit ``CAST ::``
    * SQL Parameters (e.g. ``?`` or ``:parameter``)
    * Internal Function rewrite based on the actual meaning and purpose of the function (since DuckDB `Any()` is not always RDBMS specific `Any()`)





