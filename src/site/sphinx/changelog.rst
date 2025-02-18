
************************
Changelog
************************


Latest Changes since |JSQLTRANSPILER_VERSION|
=============================================================


  * **feat: additional Spatial functions `ST_DWITHIN`, `ST_CLOSESTPOINT`, `ST_BUFFER`**
    
    manticore-projects, 2024-12-09
  * **test: update failing tests**
    
    manticore-projects, 2024-12-08
  * **feat: implement `ST_MaxDistance`**
    
    manticore-projects, 2024-12-08
  * **fix: rework `ST_Area` and `ST_Dinstance` to support `GEO_MODE` `GEOMETRY` vs. `GEOGRAPHY`**
    
    manticore-projects, 2024-12-08
  * **feat: extend the test framework to support `GEO_MODE` `GEOMETRY` vs. `GEOGRAPHY`**
    
    manticore-projects, 2024-12-07
  * **feat: switch `GEO_MODE` to `GEOMETRY` or `GEOGRAPHY`**
    
    manticore-projects, 2024-12-07
  * **fix: Safe divide shall return NULL on division by Zero**
    
    manticore-projects, 2024-12-07
  * **fix: current date with time zone shall return a `Date`**
    
    manticore-projects, 2024-12-07
  * **fix: bigquery select as value**
    
    manticore-projects, 2024-12-07
  * **fix: disable BigQuery Timeseries functions**
    
    manticore-projects, 2024-12-07
  * **build: document JDK 11 requirement**
    
    Andreas Reichel, 2024-12-04
  * **fix: Boun's RedShift Json examples**
    
    Andreas Reichel, 2024-12-01
  * **feat: Update Test framework to reflect the latest improvements**
    
    Andreas Reichel, 2024-12-01
  * **fix: Boun's exceptions on BogQuery JSon**
    
    Andreas Reichel, 2024-12-01
  * **test: additional BigQuery JSon tests**
    
    Andreas Reichel, 2024-11-25
  * **feat: complete DataBricks JSon support**
    
    Andreas Reichel, 2024-11-19
  * **test: move Boun's samples into separate file, WIP**
    
    Andreas Reichel, 2024-11-08
  * **build: bump DuckDB 1.1.3**
    
    Andreas Reichel, 2024-11-08
  * **feat: enhance geography coverage in bigquery (#46)**
    
    manticore-projects, 2024-11-08
  * **feat: enhance geography coverage in bigquery**
    
    tiboun, 2024-11-06
  * **fix: improve the `LENIENT` mode and return scope table, when column points to a table alias**
    
    Andreas Reichel, 2024-11-06
  * **test: Disable 2 RedShift Test which only fail because of DuckDBs unpredictable output order**
    
    Andreas Reichel, 2024-11-06
  * **feat: Snowflake JSON and Geo-Spatial functions**
    
    Andreas Reichel, 2024-11-04
  * **feat: Amazon RedShift JSON**
    
    Andreas Reichel, 2024-11-03
  * **feat: Amazon RedShift Geo-Spatial**
    
    Andreas Reichel, 2024-10-28
  * **feat: Amazon RedShift Geo-Spatial**
    
    Andreas Reichel, 2024-10-27
  * **feat: Amazon RedShift Geo-Spatial**
    
    Andreas Reichel, 2024-10-27
  * **test: Amazon Geo-Spatial sample database**
    
    Andreas Reichel, 2024-10-26
  * **test: limit the DuckDB memory to 250MB**
    
    Andreas Reichel, 2024-10-23
  * **style: re-format source**
    
    Andreas Reichel, 2024-10-23
  * **doc: update license information (e-mail)**
    
    Andreas Reichel, 2024-10-23
  * **feat: Good BigQuery Geo-Spat functions, document all unsupported functions**
    
    Andreas Reichel, 2024-10-23
  * **feat: BigQuery Geo Spatial functions**
    
    Andreas Reichel, 2024-10-21
  * **feat: Support DuckDB 1.1.2**
    
    Andreas Reichel, 2024-10-20
  * **fix: preserve lineage within query blocks**
    
    Andreas Reichel, 2024-10-19
  * **changes merged**
    
    David Pavlis, 2024-10-18
  * **added DatabaseSpecific for DuckDB to allow tests passing.**
    
    David Pavlis, 2024-10-18
  * **style: update PMD and license header**
    
    Andreas Reichel, 2024-10-18
  * **style: update PMD**
    
    Andreas Reichel, 2024-10-18
  * **Revert "Improvements to various DBs compatibility, serialization of JdbcMetaData to/from JSON, small changes to improve lineage" (#40)**
    
    manticore-projects, 2024-10-18
  * **Revert "Improvements to various DBs compatibility, serialization of JdbcMetaData to/from JSON, small changes to improve lineage"**
    
    manticore-projects, 2024-10-18
  * **style: update license information**
    
    Andreas Reichel, 2024-10-18
  * **Improvements to various DBs compatibility, serialization of JdbcMetaData to/from JSON, small changes to improve lineage (#39)**
    
    manticore-projects, 2024-10-18
  * **build: update Gradle plugin**
    
    Andreas Reichel, 2024-10-18
  * **re-implementation of JSON serialization of JdbcMetaData via org.json.**
    
    David Pavlis, 2024-10-17
  * **polished comments**
    
    David Pavlis, 2024-10-14
  * **added scopeColumn attribute to JdbcColumn to track "scope" of column -**
    
    David Pavlis, 2024-10-14
  * **added Snowflake DB specific support. Renamed JsonTreeBuilderConcise to**
    
    David Pavlis, 2024-10-14
  * **code polished - added comments, removed unused code**
    
    David Pavlis, 2024-10-09
  * **initial batch of changes to support various DBs and (de)serialization**
    
    David Pavlis, 2024-10-09
  * **Update README.md**
    
    dpavlis, 2024-10-09
  * **fix: translate `SAFE_CAST` into `TRY_CAST`**
    
    Andreas Reichel, 2024-10-03
  * **chore: Git Changelog dependency Java 11 Version**
    
    Andreas Reichel, 2024-10-03
  * **feat: adopt latest JSQLParser Snapshot**
    
    manticore-projects, 2024-09-10
  * **feat: BigQuery Json support, complete**
    
    manticore-projects, 2024-09-10
  * **feat: BigQuery Json support (WIP)**
    
    manticore-projects, 2024-09-10
  * **feat: BigQuery Json support (WIP)**
    
    manticore-projects, 2024-09-10
  * **test: enforce array sorting**
    
    manticore-projects, 2024-09-04
  * **test: enforce array sorting**
    
    manticore-projects, 2024-09-04
  * **test: enforce array sorting**
    
    manticore-projects, 2024-09-04
  * **feat: finalise the Error Mode**
    
    manticore-projects, 2024-09-03
  * **feat: don't fail on unresolvable columns or tables [WIP]**
    
    manticore-projects, 2024-09-02
  * **feat: don't fail on unresolvable columns or tables [WIP]**
    
    manticore-projects, 2024-09-02
  * **Update verify.yml**
    
    manticore-projects, 2024-09-02
  * **Update verify.yml**
    
    manticore-projects, 2024-09-02
  * **style: fix the 4 failing tests**
    
    manticore-projects, 2024-09-02
  * **style: fix Q/A exceptions**
    
    manticore-projects, 2024-09-02
  * **test: remove dependency on SED command**
    
    manticore-projects, 2024-09-02
  * **build: back to JSQLParser Snapshot artifacts**
    
    manticore-projects, 2024-08-24
  * **fix: support CTE referencing to previously defined CTEs**
    
    manticore-projects, 2024-08-20
  * **feat: allow parsing BigQuery single pair quotes, e. g. "catalog.schema.tablename"**
    
    manticore-projects, 2024-08-20
  * **build: add `workflow_dispatch` trigger**
    
    manticore-projects, 2024-08-20
  * **build: add `workflow_dispatch` trigger**
    
    manticore-projects, 2024-08-20
  * **build: add `workflow_dispatch` trigger**
    
    manticore-projects, 2024-08-20
  * **test: fix the precision of BigDecimals in the CSV test output**
    
    manticore-projects, 2024-08-20
  * **build: use pre-compiled JSQLParser (temporarily)**
    
    manticore-projects, 2024-08-19
  * **doc: update change log**
    
    manticore-projects, 2024-08-19
  * **doc: update change log**
    
    manticore-projects, 2024-08-19
  * **fix: better handling of quoted identifiers**
    
    manticore-projects, 2024-08-19
  * **chore: add Q/A tasks for verifying PRs**
    
    manticore-projects, 2024-08-19
  * **add missing license header (#27)**
    
    manticore-projects, 2024-08-16
  * **add missing license header**
    
    Stefan Bischof, 2024-08-16
  * **add OSGi Manifest using bnd (#24)**
    
    manticore-projects, 2024-08-16
  * **add OSGi Manifest using bnd**
    
    Stefan Bischof, 2024-08-16
  * **remove javax.swing.Treenode (#22)**
    
    manticore-projects, 2024-08-16
  * **remove javax.swing.Treenode**
    
    Stefan Bischof, 2024-08-16
  * **fix: syntax errors**
    
    manticore-projects, 2024-08-16
  * **fix: Maven coordinates**
    
    manticore-projects, 2024-08-16
  * **fix maven coordinates (#21)**
    
    Hayssam Saleh, 2024-08-15
  * **fix maven coordinates**
    
    Stefan Bischof, 2024-08-15
  * **feat: JSQLColumnResolver supports quoted identifiers**
    
    manticore-projects, 2024-07-26
  * **fix: aliased expressions in sub-query**
    
    Andreas Reichel, 2024-07-23
  * **doc: Sphinx website**
    
    Andreas Reichel, 2024-07-15
  * **doc: fine tuning**
    
    Andreas Reichel, 2024-07-15
  * **Create dependabot.yml**
    
    manticore-projects, 2024-07-15
  * **test: temporally disable 4 tests failing on GitHub (only)**
    
    Andreas Reichel, 2024-07-15
  * **build: update gradle wrapper**
    
    Andreas Reichel, 2024-07-15
  * **doc: README badges incl. coverage**
    
    Andreas Reichel, 2024-07-15
  * **feat: support NATURAL Joins**
    
    Andreas Reichel, 2024-07-14
  * **feat: support USING Joins**
    
    Andreas Reichel, 2024-07-14
  * **fix: remove local libs and improve tests**
    
    Andreas Reichel, 2024-07-08
  * **feat: improve JSon and XML lineage**
    
    Andreas Reichel, 2024-07-08
  * **feat: Columns defined as `SELECT` Expression**
    
    Andreas Reichel, 2024-07-07
  * **feat: access the Lineage via TreeBuilder interface**
    
    Andreas Reichel, 2024-07-07
  * **doc: documentation of the Column Lineage resolver for expressions**
    
    Andreas Reichel, 2024-07-06
  * **feat: Column Lineage resolver for expressions**
    
    Andreas Reichel, 2024-07-06
  * **doc: fix typo**
    
    Andreas Reichel, 2024-06-27
  * **feat: provide TimeKeyParameters as parameters for each call**
    
    Andreas Reichel, 2024-06-27
  * **build: reduce coverage temporarily**
    
    Andreas Reichel, 2024-06-25
  * **build: reduce coverage temporarily**
    
    Andreas Reichel, 2024-06-25

Version 0.6
=============================================================


  * **feat: JSQLColumnResolver with deeply nested `SelectVisitor` and `FromItemVisitor`**
    
    Andreas Reichel, 2024-06-25
  * **feat: Resolve columns for `WITH ... ` clauses**
    
    Andreas Reichel, 2024-06-19
  * **fix: BigQuery default sort order**
    
    Andreas Reichel, 2024-06-13
  * **style: fix Q/A exceptions**
    
    Andreas Reichel, 2024-06-13
  * **fix: BigQuery `SELECT AS STRUCT ...` and `SELECT AS VALUE ...`**
    
    Andreas Reichel, 2024-06-13
  * **API URL update**
    
    Hayssam Saleh, 2024-06-12
  * **Update readme & licence**
    
    Hayssam Saleh, 2024-06-11
  * **docs: Move feature matrix to Google Sheets**
    
    Andreas Reichel, 2024-06-10
  * **fix: BigQuery `GENERATE_DATE_ARRAY` with only 2 parameters**
    
    Andreas Reichel, 2024-06-10
  * **feat: support `EXCEPT` and `REPLACE` clauses**
    
    Andreas Reichel, 2024-06-10
  * **feat: add syntax sugar**
    
    Andreas Reichel, 2024-06-10
  * **feat: further Schema Provider and Test simplifications**
    
    Andreas Reichel, 2024-06-10
  * **feat: STAR column resolver, wip**
    
    Andreas Reichel, 2024-06-09
  * **feat: STAR column resolver, wip**
    
    Andreas Reichel, 2024-06-08
  * **feat: STAR column resolver, wip**
    
    Andreas Reichel, 2024-06-08
  * **feat: STAR column resolver, wip**
    
    Andreas Reichel, 2024-06-07

Version 0.5
=============================================================


  * **Update README.md**
    
    manticore-projects, 2024-06-10
  * **feat: Transpile `EXCEPT` and `REPLACE` clauses**
    
    Andreas Reichel, 2024-06-10
  * **feat: Time Key substitutions**
    
    Andreas Reichel, 2024-06-09
  * **feat: Time Key substitutions**
    
    Andreas Reichel, 2024-06-09
  * **style: apply license headers**
    
    Andreas Reichel, 2024-06-07
  * **build: small gradle fixes**
    
    Andreas Reichel, 2024-06-06
  * **Update project root name**
    
    Hayssam Saleh, 2024-06-04
  * **test publication by updating secrets**
    
    Hayssam Saleh, 2024-06-04
  * **Sonatype credentials passed through gradle.properties**
    
    Hayssam Saleh, 2024-06-04
  * **Do not sign snapshots**
    
    Hayssam Saleh, 2024-06-04
  * **build: fix gradle upload task**
    
    Andreas Reichel, 2024-06-04

Version 0.4
=============================================================


  * **feat: support Insert, Update, Delete and Merge statements**
    
    Andreas Reichel, 2024-06-04
  * **feat: support Insert, Update, Delete and Merge statements**
    
    Andreas Reichel, 2024-06-04
  * **feat: INSERT, UPDATE, DELETE, MERGE transpilers**
    
    Andreas Reichel, 2024-06-03
  * **build: update Gradle**
    
    Andreas Reichel, 2024-06-03
  * **feat: Databricks Aggregate functions**
    
    Andreas Reichel, 2024-05-30
  * **improve mock**
    
    Hayssam Saleh, 2024-05-29
  * **Proposed interface & mock implementation for tests case**
    
    Hayssam Saleh, 2024-05-29
  * **feat: Databricks Aggregate functions**
    
    Andreas Reichel, 2024-05-29
  * **feat: Databricks Aggregate functions**
    
    Andreas Reichel, 2024-05-29
  * **feat: Databricks Aggregate functions**
    
    Andreas Reichel, 2024-05-27
  * **chore: update the GitHub Actions**
    
    Andreas Reichel, 2024-05-27
  * **chore: update the GitHub Actions**
    
    Andreas Reichel, 2024-05-27

Version 0.2
=============================================================


  * **chore: update the GitHub Actions**
    
    Andreas Reichel, 2024-05-27
  * **test: enforce time zone `Asia/Bangkok` for CI**
    
    Andreas Reichel, 2024-05-27
  * **style: house-keeping and tidying**
    
    Andreas Reichel, 2024-05-27
  * **feat: Quote DuckDB keywords in Table, Column and Alias**
    
    Andreas Reichel, 2024-05-25
  * **Add snapshot Github Action**
    
    Hayssam Saleh, 2024-05-20
  * **build: fix dependencies after split-off CLI**
    
    Andreas Reichel, 2024-05-18
  * **feat: provide methods accepting prepared `ExecutorService` and `Consumer`**
    
    Andreas Reichel, 2024-05-17
  * **feat: provide methods accepting prepared `ExecutorService` and `Consumer`**
    
    Andreas Reichel, 2024-05-17
  * **style: check-style exception**
    
    Andreas Reichel, 2024-05-17
  * **feat: Databricks Date functions**
    
    Andreas Reichel, 2024-05-13
  * **build: bring back JaCoCo**
    
    Andreas Reichel, 2024-05-11
  * **feat: get the Macros as text collection or array**
    
    Andreas Reichel, 2024-05-11
  * **chore: split-off the CLI and minimize dependencies to `JSQLParser` only**
    
    Andreas Reichel, 2024-05-06
  * **chore: split-off the CLI and minimize dependencies to `JSQLParser` only**
    
    Andreas Reichel, 2024-05-06
  * **fix: complete DataBricks text functions**
    
    Andreas Reichel, 2024-05-06
  * **fix: DataBricks text functions**
    
    Andreas Reichel, 2024-05-05
  * **fix: DataBricks text functions**
    
    Andreas Reichel, 2024-05-04
  * **feat: Snowflake math functions, complete**
    
    Andreas Reichel, 2024-05-04
  * **feat: Add missing Redshift conversion functions**
    
    Andreas Reichel, 2024-05-04
  * **feat: Snowflake conversion functions**
    
    Andreas Reichel, 2024-05-04
  * **feat: Snowflake array functions**
    
    Andreas Reichel, 2024-05-02
  * **feat: Snowflake aggregate function**
    
    Andreas Reichel, 2024-05-01
  * **feat: Snowflake TEXT functions complete**
    
    Andreas Reichel, 2024-04-26
  * **feature: remove `Parenthesis` in favor of `ParenthesedExpressionList`**
    
    Andreas Reichel, 2024-04-25
  * **feature: Snowflake regular expressions**
    
    Andreas Reichel, 2024-04-25
  * **feature: complete Snowflake Date/Time functions**
    
    Andreas Reichel, 2024-04-24
  * **style: apply license headers**
    
    Andreas Reichel, 2024-04-24
  * **feat: rework UnitTest and support Prologues and Epilogues as per test**
    
    Andreas Reichel, 2024-04-24
  * **feat: Snowflake DateTime function and Structs with virtual columns**
    
    Andreas Reichel, 2024-04-23
  * **feat: Snowflake DateTime functions**
    
    Andreas Reichel, 2024-04-23
  * **feat: fascilitate BigQuery and Snowflake and add SQLGlot Tests for all**
    
    Andreas Reichel, 2024-04-23
  * **feat: RedShift Window Functions complete**
    
    Andreas Reichel, 2024-04-21
  * **feat: RedShift Window functions**
    
    Andreas Reichel, 2024-04-20
  * **feat: RedShift Aggregate functions**
    
    Andreas Reichel, 2024-04-20
  * **feat: Redshift MATH functions**
    
    Andreas Reichel, 2024-04-19
  * **feat: Redshift ARRAY functions**
    
    Andreas Reichel, 2024-04-19
  * **Fix artifact group name**
    
    Hayssam Saleh, 2024-04-16
  * **build: rewrite `CURRENT_TIMESTAMP()` into `CURRENT_TIMESTAMP`**
    
    Andreas Reichel, 2024-04-16
  * **build: remove unneeded plugins and task dependencies**
    
    Andreas Reichel, 2024-04-16
  * **feat: Redshift DateTime functions completed**
    
    Andreas Reichel, 2024-04-15
  * **feat: Redshift DateTime functions**
    
    Andreas Reichel, 2024-04-14
  * **style: Q/A**
    
    Andreas Reichel, 2024-04-14
  * **feat: auto-cast ISO_8601 DateTime Literals**
    
    Andreas Reichel, 2024-04-14
  * **feat: Redshift DateTime functions, wip**
    
    Andreas Reichel, 2024-04-13
  * **feat: complete Redshift TEXT functions**
    
    Andreas Reichel, 2024-04-13
  * **feat: Redshift String functions**
    
    Andreas Reichel, 2024-04-12
  * **style: formatting**
    
    Andreas Reichel, 2024-04-12
  * **fix: ByteString handling**
    
    Andreas Reichel, 2024-04-12
  * **fix: Stack-overflow when RedShift Expression Transpiler calling SUPER**
    
    Andreas Reichel, 2024-04-10
  * **feat: redshift string functions**
    
    Andreas Reichel, 2024-04-09
  * **feat: Adopt Implicit Cast and better Type information**
    
    Andreas Reichel, 2024-04-08
  * **style: Separate the Dialects into distinguished packages**
    
    Andreas Reichel, 2024-04-05
  * **This commit to fix the final package names and keep Andreas Reichel as the only developer of this initial version.**
    
    Hayssam Saleh, 2024-04-04

Version 0.1
=============================================================


  * **feat: Complete the Aggregate functions**
    
    Andreas Reichel, 2024-04-04
  * **feat: Array functions**
    
    Andreas Reichel, 2024-04-03
  * **feat: more Aggregate functions**
    
    Andreas Reichel, 2024-04-02
  * **feat: more Aggregate functions**
    
    Andreas Reichel, 2024-04-02
  * **feat: Aggregate Functions, wip**
    
    Andreas Reichel, 2024-04-02
  * **feat: complete the BigQuery Math functions**
    
    Andreas Reichel, 2024-04-01
  * **feat: add MATH functions**
    
    Andreas Reichel, 2024-03-31
  * **feat: completed the TEXT functions**
    
    Andreas Reichel, 2024-03-31
  * **feat: more String functions incl. Lambda based transpilation**
    
    Andreas Reichel, 2024-03-30
  * **feat: support BigQuery Structs, DuckDB structs and translation**
    
    Andreas Reichel, 2024-03-28
  * **feature: support many more TEXT functions**
    
    Andreas Reichel, 2024-03-25
  * **feat: support more BigQuery Date/Time functions**
    
    Andreas Reichel, 2024-03-21
  * **feat: support more BigQuery Date/Time functions**
    
    Andreas Reichel, 2024-03-21
  * **build: Snapshot dependency**
    
    Andreas Reichel, 2024-03-21
  * **feat: implement a Python SQLGlot based test for comparision**
    
    Andreas Reichel, 2024-03-21
  * **feat: support more BigQuery Date/Time functions**
    
    Andreas Reichel, 2024-03-21
  * **style: fix QA exceptions**
    
    Andreas Reichel, 2024-03-19
  * **doc: fix the link to th Website**
    
    Andreas Reichel, 2024-03-19
  * **feat: many more DateTime functions**
    
    Andreas Reichel, 2024-03-19
  * **doc: update/fix the documentation**
    
    Andreas Reichel, 2024-03-19
  * **style: improve the function rewrite**
    
    Andreas Reichel, 2024-03-19
  * **test: fix the test template**
    
    Andreas Reichel, 2024-03-19
  * **doc: Google BigQuery date parts and date formats**
    
    Andreas Reichel, 2024-03-18
  * **feat: many more Google BigData date functions**
    
    Andreas Reichel, 2024-03-18
  * **feat: date parts**
    
    Andreas Reichel, 2024-03-18
  * **feat: `DATE_DIFF()` function**
    
    Andreas Reichel, 2024-03-17
  * **doc: update feature matrix**
    
    Andreas Reichel, 2024-03-17
  * **test: refactor the test parametrization**
    
    Andreas Reichel, 2024-03-17
  * **build: Ueber JAR and Publish**
    
    Andreas Reichel, 2024-03-17
  * **test: improve the test framework**
    
    Andreas Reichel, 2024-03-17
  * **doc: add basic SPHINX website**
    
    Andreas Reichel, 2024-03-17
  * **doc: add a simple README**
    
    Andreas Reichel, 2024-03-16
  * **feat: CLI**
    
    Andreas Reichel, 2024-03-16
  * **feat: functions**
    
    Andreas Reichel, 2024-03-15
  * **feat: functions**
    
    Andreas Reichel, 2024-03-15
  * **build: fix the GitHub Action**
    
    Andreas Reichel, 2024-03-15
  * **test: Abstract parametrised Unit Tests**
    
    Andreas Reichel, 2024-03-15
  * **feat: `TOP ...` rewrite**
    
    Andreas Reichel, 2024-03-14
  * **build: Gradle plugins for Q/A and publishing**
    
    Andreas Reichel, 2024-03-14
  * **progress the functional mapping**
    
    Andreas Reichel, 2024-03-14
  * **chore: set up the project**
    
    Andreas Reichel, 2024-03-13
  * **Initial commit**
    
    manticore-projects, 2024-03-13

