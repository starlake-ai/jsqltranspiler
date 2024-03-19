# JSQLTranspiler [Website](https://manticore-projects/JSQLTranspiler)

A pure Java stand-alone SQL Transpiler for translating various large RDBMS SQL Dialects into a few smaller RDBMS Dialects for Unit Testing. Based on JSQLParser.

Focus is on Queries (only) and work is on progress based on the [Feature Matrix](src/main/resources/doc/JSQLTranspiler.ods).

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
import com.manticore.transpiler.JSQLTranspiler;

String providedSQL="SELECT Nvl(null, 1) a";
String expectedSQL="SELECT Coalesce(null, 1) a";

String result = JSQLTranspiler.transpile(providedSQL, Dialect.AMAZON_REDSHIFT);
assertEquals(expectedSQL, result);
```

### Java Command Line Interface
```text
usage: java -jar JSQLTranspiler.jar [-d <arg> | --any | --bigquery |
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

**JSQLTranspiler** is licensed under [**GNU Affero General Public License**](https://www.gnu.org/licenses/agpl-3.0.html).
