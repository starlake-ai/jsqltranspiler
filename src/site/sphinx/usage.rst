.. meta::
   :description: Java Software Library for rewriting Big RDBMS Queries into Duck DB compatible queries.
   :keywords: java sql query transpiler DuckDB H2 BigQuery Snowflake Redshift DataBricks

*****************
How to use it
*****************

.. tab:: Command Line

  .. code:: shell

    java -jar JSQLTranspiler.jar [-d <arg> | --any | --bigquery |
       --databricks | --snowflake | --redshift]      [-D <arg> | --duckdb]
       [-i <arg>] [-o <arg>] [-h]


.. tab:: Web API Call

  .. code:: shell

    curl -X 'POST'                                                                   \
      'https://secure-api.starlake.ai/api/v1/transpiler/transpile?dialect=SNOWFLAKE' \
      -H 'accept: text/plain'                                                        \
      -H 'Content-Type: text/plain'                                                  \
      -d 'SELECT Nvl(null, 1) a'


.. tab:: Java Library Call

  .. code:: java

    import ai.starlake.transpiler.JSQLTranspiler;
    String providedSQL="SELECT Nvl(null, 1) a";
    String expectedSQL="SELECT Coalesce(null, 1) a";
    String result = JSQLTranspiler.transpile(providedSQL, Dialect.AMAZON_REDSHIFT);
    assertEquals(expectedSQL, result);


..........................
Command Line Options (CLI)
..........................

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
                             Read from STDIN when no input file
                             provided.
 -o,--outputFile <arg>       The out SQL file for the formatted
                             statements.
                             Create new SQL file when folder provided.
                             Append when existing file provided.
                             Write to STDOUT when no output file
                             provided.
 -h,--help                   Print the help synopsis.
 
.. note::

  You can provide the SQL Statements as an argument to the program, e.g.
   
  .. code:: Bash
        
    java -jar jsqltranspiler.jar \
        "SELECT Nvl( NULL, 1 ) a;
        SELECT TOP 10 qtysold, sellerid
        FROM sales
        ORDER BY qtysold DESC, sellerid;"


***********************
TimeKey substitution
***********************

The transpiler can substitute time key expressions such as `CURRENT_DATE`, `CURRENT_TIME` or `CURRENT_TIMESTAMP` with System's properties like

.. code:: java

    System.setProperty("CURRENT_TIMESTAMP", "2024-06-09 16:24:23.123");
    String expected = "SELECT TIMESTAMP WITHOUT TIME ZONE '2024-06-09T16:24:23.123'";
    String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_TIMESTAMP", JSQLTranspiler.Dialect.ANY);

    Assertions.assertThat(actual).isEqualTo(expected);
