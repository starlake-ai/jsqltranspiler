# [JSQLTranspiler](https://starlake.ai/starlake/index.html#sql-transpiler) - Transpile Dialect, Resolve Columns, Show Lineage

A pure Java stand-alone SQL Transpiler, Column- and Lineage Resolver for translating various large RDBMS SQL Dialects into a few smaller RDBMS Dialects for Unit Testing. Based on JSQLParser.

Supports `SELECT` queries as well as `INSERT`, `UPDATE`, `DELETE` and `MERGE` statements.

Internal Functions will be rewritten based on the actual meaning and purpose of the function (since DuckDB `Any()` function does not necessarily behave like the RDBMS specific `Any()`). Respecting different function arguments count, order and type.

Rewrite of Window- and Aggregate-Functions with full coverage of the RDBMS specific published samples.
The [matrix of supported features and functions](https://docs.google.com/spreadsheets/d/1jK6E1s2c0CWcw9rFeDvALdZ5wCshztdtlAHuNDaKQt4/edit?usp=sharing) is shared on Google Sheets.

## Dialects

**Input**: Google BigQuery, Databricks, Snowflake, Amazon Redshift

**Output**: DuckDB

## Transpile Example

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

## Column Lineage Example
For the simplified schema definition and the given query
```java
String[][] schemaDefinition = {
        // Table A with Columns col1, col2, col3, colAA, colAB
        {"a", "col1", "col2", "col3", "colAA", "colAB"},

        // Table B with Columns col1, col2, col3, colBA, colBB
        {"b", "col1", "col2", "col3", "colBA", "colBB"}
};

String sqlStr =
        "SELECT Case when Sum(colBA + colBB)=0 then c.col1 else a.col2 end AS total FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";

JdbcResultSetMetaData resultSetMetaData = new JSQLColumResolver(databaseMetaData).getResultSetMetaData(sqlStr);
```

the ResultSetMetaData return a list of JdbcColumns, each traversable using the `TreeNode` interface. The resulting Column Lineage can be illustrated as:
```
SELECT
 └─total AS CaseExpression: CASE WHEN Sum(colBA + colBB) = 0 THEN c.col1 ELSE a.col2 END
    ├─WhenClause: WHEN Sum(colBA + colBB) = 0 THEN c.col1
    │  ├─EqualsTo: Sum(colBA + colBB) = 0
    │  │  └─Function: Sum(colBA + colBB)
    │  │     └─Addition: colBA + colBB
    │  │        ├─c.colBA → b.colBA : Other
    │  │        └─c.colBB → b.colBB : Other
    │  └─c.col1 → b.col1 : Other
    └─a.col2 : Other
```


## Resolve ``*`` Star Operator Example

For the simplified schema definition and the given query with Star Operators
```java
String[][] schemaDefinition = {
    // Table A with Columns col1, col2, col3, colAA, colAB
    {"a", "col1", "col2", "col3", "colAA", "colAB"},

    // Table B with Columns col1, col2, col3, colBA, colBB
    {"b", "col1", "col2", "col3", "colBA", "colBB"}
};

String sqlStr = "SELECT * FROM ( (SELECT * FROM b) c inner join a on c.col1 = a.col1 ) d;";
String resolved =  new JSQLColumResolver(schemaDefinition).getResolvedStatementText(sqlStr);
```

the query will be resolved and (optionally rewritten into):
```sql
SELECT  d.col1                 /* Resolved Column*/
        , d.col2               /* Resolved Column*/
        , d.col3               /* Resolved Column*/
        , d.colBA              /* Resolved Column*/
        , d.colBB              /* Resolved Column*/
        , d.col1_1             /* Resolved Column*/
        , d.col2_1             /* Resolved Column*/
        , d.col3_1             /* Resolved Column*/
        , d.colAA              /* Resolved Column*/
        , d.colAB              /* Resolved Column*/
FROM (  (   SELECT  b.col1     /* Resolved Column*/
                    , b.col2   /* Resolved Column*/
                    , b.col3   /* Resolved Column*/
                    , b.colba  /* Resolved Column*/
                    , b.colbb  /* Resolved Column*/
            FROM b ) c
            INNER JOIN a
                ON c.col1 = a.col1 ) d
;
```

Alternatively, the information about returned columns can be fetched as JDBC `ResultsetMetaData` (without actually executing this query):

```java
import java.sql.DatabaseMetaData;

String sqlStr = "SELECT * FROM (  (  SELECT * FROM sales ) c INNER JOIN listing a ON c.listid = a.listid ) d;";
// the meta data of catalgogs, schemas, tables, columns, either virtually and physically
DatabaseMetaData databaseMetaData = ...;
ResultSetMetaData resultSetMetaData = new JSQLColumResolver(databaseMetaData).getResultSetMetaData(sqlStr);
System.out.println(resultSetMetaData.toString());

/*
"#","label","name","table","schema","catalog","type","type name","precision","scale","display size"
"1","salesid","salesid","d",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
"2","listid","listid","d",,"JSQLTranspilerTest","INTEGER","INTEGER","0","32","0"
... (shortened) ...
"17","totalprice","totalprice","d",,"JSQLTranspilerTest","DECIMAL","DECIMAL(8,2)","0","8","0"
"18","listtime","listtime","d",,"JSQLTranspilerTest","TIMESTAMP","TIMESTAMP","0","0","0"
 */

```

## How to use

### Java Library

Maven Artifact with Snapshot support:
```xml
<repositories>
    <repository>
        <id>jsqltranspiler-snapshots</id>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.starlake-ai.jsqltranspiler</groupId>
    <artifactId>jsqltranspiler</artifactId>
    <version>0.6-SNAPSHOT</version>
</dependency>
```

Calling the Java class:
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
  'https://starlake.ai/api/v1/transpiler/transpile?dialect=SNOWFLAKE'            \
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

### TimeKey substitution

The transpiler can substitute time key expressions such as `CURRENT_DATE` or `CURRENT_TIMESTAMP` with System's properties like

```java
System.setProperty("CURRENT_TIMESTAMP", "2024-06-09 16:24:23.123");
String expected = "SELECT TIMESTAMP WITHOUT TIME ZONE '2024-06-09T16:24:23.123'";
String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_TIMESTAMP", JSQLTranspiler.Dialect.ANY);

Assertions.assertThat(actual).isEqualTo(expected);
```

Alternatively parameters can be provided as `Map<String,Object>` (which would take precedence over any System's properties):

```java
String expected = "SELECT TIME WITHOUT TIME ZONE '17:24:23.123'";
String actual =
        JSQLTranspiler.transpileQuery(
                "SELECT CURRENT_TIME"
                , JSQLTranspiler.Dialect.ANY
                , Map.of("CURRENT_TIME", "17:24:23.123")
        );

Assertions.assertThat(actual).isEqualTo(expected);
```

### Unsupported features

Please refer to the [Feature Matrix](https://docs.google.com/spreadsheets/d/1jK6E1s2c0CWcw9rFeDvALdZ5wCshztdtlAHuNDaKQt4/edit?usp=sharing):

- DuckDB's Number and Currency formatting is very limited right now
- `Geography`, `JSon` and `XML` functions have not been implemented yet, but are planned
- `SELECT * REPLACE(...)` on DuckDB works very differently (replaces value instead of label)

## License

**JSQLTranspiler** is licensed under [**Apache License, Version 2.0**](https://www.apache.org/licenses/LICENSE-2.0).
