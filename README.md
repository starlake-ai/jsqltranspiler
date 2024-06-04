# JSQLTranspiler [Website](https://starlake-ai.com/JSQLTranspiler)

A pure Java stand-alone SQL Transpiler for translating various large RDBMS SQL Dialects into a few smaller RDBMS Dialects for Unit Testing. Based on JSQLParser.

Supports `SELECT` queries as well as `INSERT`, `UPDATE`, `DELETE` and `MERGE` statements.

Internal Functions will be rewritten based on the actual meaning and purpose of the function (since DuckDB `Any()` function does not necessarily behave like the RDBMS specific `Any()`). Respecting different function arguments count, order and type.

Rewrite of Window- and Aggregate-Functions.

## Dialects

**Input**: Google BigQuery, Databricks, Snowflake, Amazon Redshift

**Output**: DuckDB

## Example

Google BigQuery specific SQL

```sql
-- BigQuery specific DATE() function
SELECT
  DATE(2016, 12, 25) AS date_ymd,
  DATE(DATETIME '2016-12-25 23:59:59') AS date_dt,
  DATE(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz;

/* Output
"date_ymd","date_dt","date_tstz"
"2016-12-15","2016-12-15","2016-12-15"
*/
```

will become DuckDB compatible SQL

```sql
-- DuckDB compliant rewrite producing the same result
SELECT
  MAKE_DATE(2016, 12, 25) AS date_ymd,
  CAST(DATETIME '2016-12-25 23:59:59' AS DATE) AS date_dt,
  CAST(TIMESTAMP '2016-12-25 05:30:00+07' AS DATE) AS date_tstz;

/* Output
"date_ymd","date_dt","date_tstz"
"2016-12-15","2016-12-15","2016-12-15"
*/
```


## How to use

### Java Library

```java
import ai.starlake.transpiler.JSQLTranspiler;

String providedSQL="SELECT Nvl(null, 1) a";
String expectedSQL="SELECT Coalesce(null, 1) a";

String result = JSQLTranspiler.transpile(providedSQL, Dialect.AMAZON_REDSHIFT);
assertEquals(expectedSQL, result);
```

### Web API
```shell
curl -X 'POST'                                                                   \
  'https://secure-api.starlake.ai/api/v1/transpiler/transpile?dialect=SNOWFLAKE' \
  -H 'accept: text/plain'                                                        \
  -H 'Content-Type: text/plain'                                                  \
  -d 'SELECT Nvl(null, 1) a'
```

### Java Command Line Interface
```text
usage: java -jar JSQLTranspilerCLI.jar [-d <arg> | --any | --bigquery |
       --databricks | --snowflake | --redshift]      [-D <arg> | --duckdb]
       [-i <arg>] [-o <arg>] [-h]

 -d,--input-dialect <arg>    The SQL dialect to parse.
                             [ANY*, GOOGLE_BIG_QUERY, DATABRICKS,
                             SNOWFLAKE, AMAZON_REDSHIFT]
    --any                    Interpret the SQL as Generic Dialect
                             [DEFAULT].
    --bigquery               Interpret the SQL as Google BigQuery Dialect.
    --databricks             Interpret the SQL as DataBricks Dialect.
    --snowflake              Interpret the SQL as Snowflake Dialect.
    --redshift               Interpret the SQL as Amazon Snowflake
                             Dialect.
 -D,--output-dialect <arg>   The SQL dialect to write.
                             [DUCKDB*]
    --duckdb                 Write the SQL in the Duck DB Dialect
                             [DEFAULT].
 -i,--inputFile <arg>        The input SQL file or folder.
                             - Read from STDIN when no input file
                             provided.
 -o,--outputFile <arg>       The out SQL file for the formatted
                             statements.
                             - Create new SQL file when folder provided.
                             - Append when existing file provided.
                             - Write to STDOUT when no output file
                             provided.
 -h,--help                   Print the help synopsis.
```

## License

**JSQLTranspiler** is licensed under [**Apache License, Version 2.0**](https://www.apache.org/licenses/LICENSE-2.0).
