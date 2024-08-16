.. meta::
   :description: Java Software Library for rewriting Big RDBMS Queries into Duck DB compatible queries, Column resolution and tracing the Lineage.
   :keywords: java sql query transpiler resolve lineage DuckDB H2 BigQuery Snowflake Redshift Databricks

###########################
Java SQL Transpiler Library
###########################

.. toctree::
   :maxdepth: 2
   :hidden:

   usage
   resolve
   Java API <javadoc>
   changelog



.. image:: https://img.shields.io/nexus/s/ai.starlake.jsqltranspiler/jsqltranspiler?server=https%3A%2F%2Fs01.oss.sonatype.org
    :alt: Sonatype Nexus (Snapshots)
    :target: https://s01.oss.sonatype.org/#nexus-search;quick~ai.starlake.jsqltranspiler/jsqltranspiler

.. image:: https://javadoc.io/badge2/ai.starlake.jsqltranspiler/jsqltranspiler/javadoc.svg
    :alt: javadoc
    :target: https://javadoc.io/doc/ai.starlake.jsqltranspiler/jsqltranspiler

.. image:: https://github.com/starlake-ai/jsqltranspiler/actions/workflows/snapshot.yml/badge.svg
    :alt: Gradle CI/QA
    :target: https://github.com/starlake-ai/jsqltranspiler/actions/workflows/snapshot.yml

.. image:: https://app.codacy.com/project/badge/Grade/80374649d914462ebd6e5b160a1ebdbb
    :alt: Quality
    :target: https://app.codacy.com/gh/starlake-ai/jsqltranspiler/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade

.. image:: https://coveralls.io/repos/github/starlake-ai/jsqltranspiler/badge.svg
    :alt: Coverage
    :target: https://coveralls.io/github/starlake-ai/jsqltranspiler

.. image:: https://img.shields.io/badge/License-Apache-blue
    :alt: license
    :target: https://www.apache.org/licenses/LICENSE-2.0

.. image:: https://img.shields.io/github/issues/starlake-ai/jsqltranspiler
    :alt: Issues
    :target: https://github.com/starlake-ai/jsqltranspiler/issues

.. image:: https://img.shields.io/badge/PRs-welcome-brightgreen.svg
    :alt: PRs Welcome
    :target: https://egghead.io/courses/how-to-contribute-to-an-open-source-project-on-github?af=5236ad


**Transpile**, **Resolve**, **Lineage** -- A Database independent, stand-alone SQL Transpiler, Column Resolver and Lineage Tracer written in pure Java, translating various large RDBMS SQL Dialects into a few smaller RDBMS Dialects for Unit Testing.

JSQLTranspiler allows you to **develop and test your Big Data SQL at no cost on a local DuckDB instance** before deploying and running it in the cloud.

Internal Functions will be rewritten based on the actual meaning and purpose of the function, respecting different function arguments' count, order and type. Rewrite of Window- and Aggregate-Functions as well.

Download
===================

.. tab:: A -- Binaries

    .. list-table:: Java 11 Binaries
        :widths: 35 50 15
        :header-rows: 1

        * - Flavor
          - File
          - Size
        * - Java Library Stable
          - |JSQLTRANSPILER_STABLE_VERSION_LINK|
          - (80 kb)
        * - Java Library Snapshot
          - |JSQLTRANSPILER_SNAPSHOT_VERSION_LINK|
          - (80 kb)
        * - CLI Fat JAR Stable
          - |JSQLTRANSPILER_CLI_STABLE_VERSION_LINK|
          - (1.3 MB)
        * - CLI Fat JAR Snapshot
          - |JSQLTRANSPILER_CLI_SNAPSHOT_VERSION_LINK|
          - (1.3 MB)


.. tab:: B -- Source Code

    Like us on the `JSQLTranspiler Git Repository <https://github.com/starlake-ai/jsqltranspiler>`_

    .. code-block:: bash
        :substitutions:
        :caption: Cloning the repository

        git clone https://github.com/starlake-ai/JSQLTranspiler.git
        cd JSQLTranspiler
        ./gradlew build


.. tab:: C -- Maven Release

    .. code-block:: xml
        :substitutions:

        <dependency>
            <groupId>ai.starlake.jsqltranspiler</groupId>
            <artifactId>jsqltranspiler</artifactId>
            <version>|JSQLTRANSPILER_VERSION|</version>
        </dependency>


.. tab:: D -- Maven Snapshot

    .. code-block:: xml
        :substitutions:

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
            <groupId>ai.starlake.jsqltranspiler</groupId>
            <artifactId>jsqltranspiler</artifactId>
            <version>|JSQLTRANSPILER_SNAPSHOT_VERSION|</version>
        </dependency>


.. tab:: E -- Gradle Release

    .. code-block:: groovy
        :substitutions:

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'ai.starlake.jsqltranspiler:jsqltranspiler:|JSQLTRANSPILER_VERSION|'
        }


.. tab:: F -- Gradle Snapshot

    .. code-block:: groovy
        :substitutions:

        repositories {
            maven {
                url = uri('https://s01.oss.sonatype.org/content/repositories/snapshots/')
            }
        }

        dependencies {
            implementation 'ai.starlake.jsqltranspiler:jsqltranspiler:|JSQLTRANSPILER_SNAPSHOT_VERSION|'
        }



Examples
===================

.. tab:: 1 -- Transpiling

    .. code-block:: sql
        :caption: Rewrite Google BigQuery to DuckDB and execute
        :substitutions:

        -- Google BigQuery
        SELECT
            DATE(2016, 12, 25) AS date_ymd,
            DATE(DATETIME '2016-12-25 23:59:59') AS date_dt,
            DATE(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz;

        -- Rewritten DuckDB compliant statement
        SELECT
            MAKE_DATE(2016, 12, 25) AS date_ymd,
                CAST(DATETIME '2016-12-25 23:59:59' AS DATE) AS date_dt,
                CAST(TIMESTAMP '2016-12-25 05:30:00+07' AT TIME ZONE 'America/Los_Angeles' AS DATE) AS date_tstz;

        -- Same Tally
        1

        -- Same Result
        "date_ymd","date_dt","date_tstz"
        "2016-12-15","2016-12-15","2016-12-15"



.. tab:: 2 -- Resolving Columns

    .. code-block:: sql
        :caption: Resolve the Star Operator of a deeply nested Query with disguised physical columns
        :substitutions:

        /* Schema:
        Table a: Columns col1, col2, col3, colAA, colBA
        Table b: Columns col1, col2, col3, colBA, colBB
        */

        -- provided SELECT with STAR Operators
        SELECT *
        FROM (  (   SELECT *
                    FROM b ) c
                    INNER JOIN a
                        ON c.col1 = a.col1 ) d
        ;

        -- Resolved Columns via JSQLColumnResolver.rewrite(...)
        -- Without needing an actual database connection
        SELECT  d.col1
                , d.col2
                , d.col3
                , d.colBA
                , d.colBB
                , d.col1_1
                , d.col2_1
                , d.col3_1
                , d.colAA
                , d.colAB
        FROM (  (   SELECT  b.col1
                            , b.col2
                            , b.col3
                            , b.colBA
                            , b.colBB
                    FROM b ) c
                    INNER JOIN a
                        ON c.col1 = a.col1 ) d
        ;



.. tab:: 3 -- Tracing Lineage

    .. code-block:: sql
        :caption: Trace the actual physical columns
        :substitutions:

        /* Schema:
        Table a: Columns col1, col2, col3, colAA, colBA
        Table b: Columns col1, col2, col3, colBA, colBB
        */

        -- provided SELECT with STAR Operator
        -- Without needing an actual database connection
        SELECT  Sum( colBA + colBB ) AS total
                , ( SELECT col1 AS test
                    FROM b ) col2
                , CURRENT_TIMESTAMP() AS col3
        FROM a
            INNER JOIN (    SELECT *
                            FROM b ) c
                ON a.col1 = c.col1
        ;

    .. code-block:: xml
        :caption: Trace of the physical tables and columns
        :substitutions:

        <?xml version="1.0" encoding="UTF-8"?>
        <ColumnSet>
            <Column alias='total' name='Sum'>
                <ColumnSet>
                    <Column name='Addition'>
                        <ColumnSet>

                            <!-- scope points on the actual physical column b.colBA -->
                            <Column name='colBA' table='c' scope='b.colBA' dataType='java.sql.Types.OTHER' typeName='Other' columnSize='0' decimalDigits='0' nullable=''/>

                            <!-- scope points on the actual physical column b.colBB -->
                            <Column name='colBB' table='c' scope='b.colBB' dataType='java.sql.Types.OTHER' typeName='Other' columnSize='0' decimalDigits='0' nullable=''/>

                        </ColumnSet>
                    </Column>
                </ColumnSet>
            </Column>
            <Column alias='col2' name='col1'>
                <ColumnSet>
                    <Column alias='test' name='col1' table='b' dataType='java.sql.Types.OTHER' typeName='Other' columnSize='0' decimalDigits='0' nullable=''/>
                </ColumnSet>
            </Column>
            <Column alias='col3' name='CURRENT_TIMESTAMP'/>
        </ColumnSet>


.. tab:: 4 -- Find actual tables

    .. code-block:: java
        :caption: Retrieve list of actual physical tables
        :substitutions:

        String sqlStr = "SELECT * FROM (SELECT * FROM A) AS A \n" +
                "JOIN B ON A.a = B.a \n" +
                "JOIN C ON A.a = C.a;";
        Set<String> tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("A", "B", "C");

        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("B", "C");



SQL Dialects
===================

**JSQLTranspiler** currently understands the following Big RDBMS dialects:

    * Google BigQuery
    * Databricks
    * Snowflake
    * Amazon Redshift

and rewrites into to the following small RDBMS dialects:

    * DuckDB
    * planned: H2
    * planned: Postgres


Features
===================

    * Comprehensive support for Query and DML statements (`INSERT`, `DELETE`, `UPDATE`, `MERGE`)
    * RDBMS specific Functions, Predicates and Operators
    * RDBMS specific Date and Number formatting parameters
    * Extensive ``ARRAY``, ``ROW`` and ``STRUCT`` support
    * Deeply Nested Expressions such as correlated Sub-Selects, CTE's and ``WITH`` clauses
    * Explicit and Implicit Cast expressions, e. g. ``DATE '2023-12-31'``, ``'2023-12-31'::Date`` and ``Cast('2023-12-31' AS Date)``
    * SQL Named and Ordinal Parameters: ``?``, ``?1`` or ``:parameter``
    * Lateral Table and Sub-Select Functions, e. g. ``UNNEST()``, ``TABLE()``
    * Window and Aggregate Functions
    * Columns Resolution for ``EXCEPT`` and ``REPLACE`` filters as well as for ``USING`` joins (left or right)






