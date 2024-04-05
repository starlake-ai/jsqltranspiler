
.. raw:: html

    <div id="floating-toc">
        <div class="search-container">
            <input type="button" id="toc-hide-show-btn"></input>
            <input type="text" id="toc-search" placeholder="Search" />
        </div>
        <ul id="toc-list"></ul>
    </div>



#######################################################################
JSQLTranspiler 0.2-SNAPSHOT API
#######################################################################

Base Package: ai.starlake.jsqltranspiler


..  _ai.starlake.transpiler:
***********************************************************************
Base
***********************************************************************

..  _ai.starlake.transpiler.JSQLTranspiler.Dialect

=======================================================================
JSQLTranspiler.Dialect
=======================================================================

[GOOGLE_BIG_QUERY, DATABRICKS, SNOWFLAKE, AMAZON_REDSHIFT, ANY, DUCK_DB]

| The enum Dialect.


..  _ai.starlake.transpiler.JSQLExpressionTranspiler:

=======================================================================
JSQLExpressionTranspiler
=======================================================================

*extends:* ExpressionDeParser *provides:* :ref:`BigQueryExpressionTranspiler<ai.starlake.transpiler.bigquery.BigQueryExpressionTranspiler>`, :ref:`DatabricksExpressionTranspiler<ai.starlake.transpiler.databricks.DatabricksExpressionTranspiler>`, :ref:`RedshiftExpressionTranspiler<ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler>`, :ref:`SnowflakeExpressionTranspiler<ai.starlake.transpiler.snowflake.SnowflakeExpressionTranspiler>` 

| The type Expression transpiler.

| **JSQLExpressionTranspiler** (transpiler, buffer)
|          :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` transpiler
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| **isDatePart** (expression, dialect) → boolean
|          Expression expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns boolean



| **isDateTimePart** (expression, dialect) → boolean
|          Expression expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns boolean



| **hasTimeZoneInfo** (timestampStr) → boolean
|          :ref:`String<java.lang.String>` timestampStr
|          returns boolean



| **hasTimeZoneInfo** (timestamp) → boolean
|          Expression timestamp
|          returns boolean



| **rewriteDateLiteral** (p, dateTimeType) → Expression
|          Expression p
|          :ref:`DateTime<DateTimeLiteralExpression.DateTime>` dateTimeType
|          returns Expression



| *@SuppressWarnings*
| **visit** (function)
|          Function function


| **visit** (function)
|          AnalyticExpression function


| **visit** (extractExpression)
|          ExtractExpression extractExpression


| **visit** (stringValue)
|          StringValue stringValue


| **visit** (hexValue)
|          HexValue hexValue


| **convertUnicode** (input) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` input
|          returns :ref:`String<java.lang.String>`



| **visit** (castExpression)
|          CastExpression castExpression


| **visit** (structType)
|          StructType structType


| **rewriteType** (colDataType) → ColDataType
|          ColDataType colDataType
|          returns ColDataType



| **warning** (s)
|          :ref:`String<java.lang.String>` s



..  _ai.starlake.transpiler.JSQLTranspiler:

=======================================================================
JSQLTranspiler
=======================================================================

*extends:* SelectDeParser *provides:* :ref:`BigQueryTranspiler<ai.starlake.transpiler.bigquery.BigQueryTranspiler>`, :ref:`DatabricksTranspiler<ai.starlake.transpiler.databricks.DatabricksTranspiler>`, :ref:`RedshiftTranspiler<ai.starlake.transpiler.redshift.RedshiftTranspiler>`, :ref:`SnowflakeTranspiler<ai.starlake.transpiler.snowflake.SnowflakeTranspiler>` 

| The type Jsql transpiler.


                Instantiates a new transpiler.
                |          :ref:`Class<java.lang.Class>` expressionTranspilerClass

            | **JSQLTranspiler** ()


| **getAbsoluteFile** (filename) → :ref:`File<java.io.File>`
| Resolves the absolute File from a relative filename, considering $HOME variable and "~"
|          :ref:`String<java.lang.String>` filename  | filename the relative filename
|          returns :ref:`File<java.io.File>`  | the resolved absolute file



| **getAbsoluteFileName** (filename) → :ref:`String<java.lang.String>`
| Resolves the absolute File Name from a relative filename, considering $HOME variable and "~"
|          :ref:`String<java.lang.String>` filename  | filename the relative filename
|          returns :ref:`String<java.lang.String>`  | the resolved absolute file name



| *@SuppressWarnings*
| **main** (args)
| The entry point of application.
|          :ref:`String<java.lang.String>` args  | args the input arguments


| *@SuppressWarnings*
| **transpileQuery** (qryStr, dialect) → :ref:`String<java.lang.String>`
| Transpile a query string in the defined dialect into DuckDB compatible SQL.
|          :ref:`String<java.lang.String>` qryStr  | qryStr the original query string
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect  | dialect the dialect of the query string
|          returns :ref:`String<java.lang.String>`  | the transformed query string



| **transpile** (sqlStr, outputFile)
| Transpile a query string from a file or STDIN and write the transformed query string into a file or STDOUT.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the original query string
|          :ref:`File<java.io.File>` outputFile  | outputFile the output file, writing to STDOUT when not defined


| **transpile** (select) → :ref:`String<java.lang.String>`
| Transpile string.
|          Select select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileGoogleBigQuery** (select) → :ref:`String<java.lang.String>`
| Transpile google big query string.
|          Select select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileDatabricksQuery** (select) → :ref:`String<java.lang.String>`
| Transpile databricks query string.
|          Select select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileSnowflakeQuery** (select) → :ref:`String<java.lang.String>`
| Transpile snowflake query string.
|          Select select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileAmazonRedshiftQuery** (select) → :ref:`String<java.lang.String>`
| Transpile amazon redshift query string.
|          Select select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **getResultBuilder** () → :ref:`StringBuilder<java.lang.StringBuilder>`
| Gets result builder.
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`  | the result builder



| **visit** (top)
|          Top top


| **visit** (tableFunction)
|          TableFunction tableFunction



..  _ai.starlake.transpiler.bigquery:
***********************************************************************
uery
***********************************************************************

..  _ai.starlake.transpiler.bigquery.BigQueryExpressionTranspiler:

=======================================================================
BigQueryExpressionTranspiler
=======================================================================

*extends:* :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` 

| **BigQueryExpressionTranspiler** (transpiler, buffer)
|          :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` transpiler
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer



..  _ai.starlake.transpiler.bigquery.BigQueryTranspiler:

=======================================================================
BigQueryTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **BigQueryTranspiler** ()



..  _ai.starlake.transpiler.databricks:
***********************************************************************
bricks
***********************************************************************

..  _ai.starlake.transpiler.databricks.DatabricksExpressionTranspiler:

=======================================================================
DatabricksExpressionTranspiler
=======================================================================

*extends:* :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` 

| **DatabricksExpressionTranspiler** (transpiler, buffer)
|          :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` transpiler
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer



..  _ai.starlake.transpiler.databricks.DatabricksTranspiler:

=======================================================================
DatabricksTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **DatabricksTranspiler** ()



..  _ai.starlake.transpiler.redshift:
***********************************************************************
hift
***********************************************************************

..  _ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler:

=======================================================================
RedshiftExpressionTranspiler
=======================================================================

*extends:* :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` 

| **RedshiftExpressionTranspiler** (transpiler, buffer)
|          :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` transpiler
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| *@SuppressWarnings*
| **visit** (function)
|          Function function



..  _ai.starlake.transpiler.redshift.RedshiftTranspiler:

=======================================================================
RedshiftTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **RedshiftTranspiler** ()



..  _ai.starlake.transpiler.snowflake:
***********************************************************************
flake
***********************************************************************

..  _ai.starlake.transpiler.snowflake.SnowflakeExpressionTranspiler:

=======================================================================
SnowflakeExpressionTranspiler
=======================================================================

*extends:* :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` 

| **SnowflakeExpressionTranspiler** (transpiler, buffer)
|          :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` transpiler
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer



..  _ai.starlake.transpiler.snowflake.SnowflakeTranspiler:

=======================================================================
SnowflakeTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **SnowflakeTranspiler** ()


