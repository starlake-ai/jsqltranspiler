
.. raw:: html

    <div id="floating-toc">
        <div class="search-container">
            <input type="button" id="toc-hide-show-btn"></input>
            <input type="text" id="toc-search" placeholder="Search" />
        </div>
        <ul id="toc-list"></ul>
    </div>



#######################################################################
JSQLTranspiler 0.5-SNAPSHOT API
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


..  _ai.starlake.transpiler.JSQLDeleteTranspiler:

=======================================================================
JSQLDeleteTranspiler
=======================================================================

*extends:* DeleteDeParser 


..  _ai.starlake.transpiler.JSQLExpressionTranspiler:

=======================================================================
JSQLExpressionTranspiler
=======================================================================

*extends:* ExpressionDeParser *provides:* :ref:`BigQueryExpressionTranspiler<ai.starlake.transpiler.bigquery.BigQueryExpressionTranspiler>`, :ref:`RedshiftExpressionTranspiler<ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler>` 

| The type Expression transpiler.

| **JSQLExpressionTranspiler** (deParser, buffer)
|          SelectDeParser deParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| **isDatePart** (expression, dialect) → boolean
|          Expression expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns boolean




                |          Expression expression

                |          returns boolean


            | **isDateTimePart** (expression, dialect) → boolean
|          Expression expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns boolean



| **toDateTimePart** (expression, dialect) → Expression
|          Expression expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns Expression



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


| *@SuppressWarnings*
| **visit** (function)
|          AnalyticExpression function



                |          Function function

                |          <any> parameters

                |          :ref:`DateTime<DateTimeLiteralExpression.DateTime>` dateTimeType

                |          returns void


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


| **convertByteStringToUnicode** (byteString) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` byteString
|          returns :ref:`String<java.lang.String>`



| **castDateTime** (expression) → Expression
|          :ref:`String<java.lang.String>` expression
|          returns Expression



| **castDateTime** (expression) → Expression
|          Expression expression
|          returns Expression



| *@SuppressWarnings*
| **castDateTime** (expression) → Expression
|          DateTimeLiteralExpression expression
|          returns Expression



| *@SuppressWarnings*
| **castDateTime** (expression) → Expression
|          CastExpression expression
|          returns Expression



| *@SuppressWarnings*
| **castDateTime** (expression) → Expression
|          StringValue expression
|          returns Expression



| **castInterval** (expression) → Expression
|          :ref:`String<java.lang.String>` expression
|          returns Expression



| **castInterval** (e1, e2, dialect) → Expression
|          Expression e1
|          Expression e2
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns Expression



| **castInterval** (expression) → Expression
|          Expression expression
|          returns Expression



| **castInterval** (expression) → Expression
|          StringValue expression
|          returns Expression



| **castInterval** (expression) → Expression
|          CastExpression expression
|          returns Expression



| **castInterval** (expression) → Expression
|          IntervalExpression expression
|          returns Expression



| **visit** (expression)
|          TimeKeyExpression expression


| **visit** (likeExpression)
|          LikeExpression likeExpression


| **visit** (function)
|          TranscodingFunction function


| **isEmpty** (collection) → boolean
|          :ref:`Collection<java.util.Collection>` collection
|          returns boolean



| **hasParameters** (function) → boolean
|          Function function
|          returns boolean



| **visit** (column)
|          Column column


| **visit** (expressionList)
|          ExpressionList expressionList



..  _ai.starlake.transpiler.JSQLInsertTranspiler:

=======================================================================
JSQLInsertTranspiler
=======================================================================

*extends:* InsertDeParser 


..  _ai.starlake.transpiler.JSQLMergeTranspiler:

=======================================================================
JSQLMergeTranspiler
=======================================================================

*extends:* MergeDeParser 

| **JSQLMergeTranspiler** (expressionDeParser, selectDeParser, buffer)
|          ExpressionDeParser expressionDeParser
|          SelectDeParser selectDeParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer



..  _ai.starlake.transpiler.JSQLSelectTranspiler:

=======================================================================
JSQLSelectTranspiler
=======================================================================

*extends:* SelectDeParser *provides:* :ref:`BigQuerySelectTranspiler<ai.starlake.transpiler.bigquery.BigQuerySelectTranspiler>`, :ref:`DatabricksSelectTranspiler<ai.starlake.transpiler.databricks.DatabricksSelectTranspiler>`, :ref:`RedshiftSelectTranspiler<ai.starlake.transpiler.redshift.RedshiftSelectTranspiler>`, :ref:`SnowflakeSelectTranspiler<ai.starlake.transpiler.snowflake.SnowflakeSelectTranspiler>` 


                Instantiates a new transpiler.
                |          :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` expressionTranspiler

                |          :ref:`StringBuilder<java.lang.StringBuilder>` resultBuilder

            | **JSQLSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`Class<java.lang.Class>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder


| **getResultBuilder** () → :ref:`StringBuilder<java.lang.StringBuilder>`
| Gets result builder.
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`  | the result builder



| **visit** (top)
|          Top top


| **visit** (tableFunction)
|          TableFunction tableFunction


| **visit** (plainSelect)
|          PlainSelect plainSelect


| **visit** (table)
|          Table table


| **visit** (selectItem)
|          SelectItem selectItem



..  _ai.starlake.transpiler.JSQLTranspiler:

=======================================================================
JSQLTranspiler
=======================================================================

*extends:* StatementDeParser *provides:* :ref:`BigQueryTranspiler<ai.starlake.transpiler.bigquery.BigQueryTranspiler>`, :ref:`DatabricksTranspiler<ai.starlake.transpiler.databricks.DatabricksTranspiler>`, :ref:`RedshiftTranspiler<ai.starlake.transpiler.redshift.RedshiftTranspiler>`, :ref:`SnowflakeTranspiler<ai.starlake.transpiler.snowflake.SnowflakeTranspiler>` 

| The type JSQLTranspiler.


                |          :ref:`Class<java.lang.Class>` selectTranspilerClass

                |          :ref:`Class<java.lang.Class>` expressionTranspilerClass

                
                
                
                
            | **JSQLTranspiler** ()


| *@SuppressWarnings*
| **transpileQuery** (qryStr, dialect, executorService, consumer) → :ref:`String<java.lang.String>`
| Transpile a query string in the defined dialect into DuckDB compatible SQL.
|          :ref:`String<java.lang.String>` qryStr  | qryStr the original query string
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect  | dialect the dialect of the query string
|          :ref:`ExecutorService<java.util.concurrent.ExecutorService>` executorService  | executorService the ExecutorService to use for running and observing JSQLParser
|          :ref:`Consumer<java.util.function.Consumer>` consumer  | consumer the parser configuration to use for the parsing
|          returns :ref:`String<java.lang.String>`  | the transformed query string



| **transpileQuery** (qryStr, dialect) → :ref:`String<java.lang.String>`
| Transpile a query string in the defined dialect into DuckDB compatible SQL.
|          :ref:`String<java.lang.String>` qryStr  | qryStr the original query string
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect  | dialect the dialect of the query string
|          returns :ref:`String<java.lang.String>`  | the transformed query string



| *@SuppressWarnings*
| **transpile** (sqlStr, outputFile, executorService, consumer)
| Transpile a query string from a file or STDIN and write the transformed query string into a file or STDOUT. Using the provided Executor Service for observing the parser.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the original query string
|          :ref:`File<java.io.File>` outputFile  | outputFile the output file, writing to STDOUT when not defined
|          :ref:`ExecutorService<java.util.concurrent.ExecutorService>` executorService  | executorService the ExecutorService to use for running and observing JSQLParser
|          :ref:`Consumer<java.util.function.Consumer>` consumer  | consumer the parser configuration to use for the parsing


| **transpile** (sqlStr, outputFile) → boolean
| Transpile a query string from a file or STDIN and write the transformed query string into a file or STDOUT.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the original query string
|          :ref:`File<java.io.File>` outputFile  | outputFile the output file, writing to STDOUT when not defined
|          returns boolean



| **readResource** (url) → :ref:`String<java.lang.String>`
| Read the text content from a resource file.
|          :ref:`URL<java.net.URL>` url  | url the URL of the resource file
|          returns :ref:`String<java.lang.String>`  | the text content



| **readResource** (clazz, suffix) → :ref:`String<java.lang.String>`
| Read the text content from a resource file relative to a particular class' suffix
|          :ref:`Class<java.lang.Class>` clazz  | clazz the Class which defines the classpath URL of the resource file
|          :ref:`String<java.lang.String>` suffix  | suffix the Class Name suffix used for naming the resource file
|          returns :ref:`String<java.lang.String>`  | the text content



| **getMacros** (executorService, consumer) → :ref:`Collection<java.util.Collection>`
| Get the Macro `CREATE FUNCTION` statements as a list of text, using the provided ExecutorService to monitor the parser
|          :ref:`ExecutorService<java.util.concurrent.ExecutorService>` executorService  | executorService the ExecutorService to use for running and observing JSQLParser
|          :ref:`Consumer<java.util.function.Consumer>` consumer  | consumer the parser configuration to use for the parsing
|          returns :ref:`Collection<java.util.Collection>`  | the list of statement texts



| **getMacros** () → :ref:`Collection<java.util.Collection>`
| Get the Macro `CREATE FUNCTION` statements as a list of text
|          returns :ref:`Collection<java.util.Collection>`  | the list of statement texts



| **getMacroArray** () → :ref:`String<java.lang.String>`
| Get the Macro `CREATE FUNCTION` statements as an Array of text
|          returns :ref:`String<java.lang.String>`  | the array of statement texts



| **createMacros** (conn)
| Create the Macros in a given JDBC connection
|          :ref:`Connection<java.sql.Connection>` conn


| **transpile** (statement) → :ref:`String<java.lang.String>`
| Rewrite a given SQL Statement into a text representation.
|          Statement statement  | statement the statement
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileBigQuery** (statement) → :ref:`String<java.lang.String>`
| Rewrite a given BigQuery SQL Statement into a text representation.
|          Statement statement  | statement the statement
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileDatabricks** (statement) → :ref:`String<java.lang.String>`
| Rewrite a given DataBricks SQL Statement into a text representation.
|          Statement statement  | statement the statement
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileSnowflake** (statement) → :ref:`String<java.lang.String>`
| Rewrite a given Snowflake SQL Statement into a text representation.
|          Statement statement  | statement the statement
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileAmazonRedshift** (statement) → :ref:`String<java.lang.String>`
| Rewrite a given Redshift SQL Statement into a text representation.
|          Statement statement  | statement the statement
|          returns :ref:`String<java.lang.String>`  | the string



| **visit** (select)
|          Select select


| **visit** (insert)
|          Insert insert


| **visit** (update)
|          Update update


| **visit** (delete)
|          Delete delete


| **visit** (merge)
|          Merge merge



..  _ai.starlake.transpiler.JSQLUpdateTranspiler:

=======================================================================
JSQLUpdateTranspiler
=======================================================================

*extends:* UpdateDeParser 


..  _ai.starlake.transpiler.bigquery:
***********************************************************************
uery
***********************************************************************

..  _ai.starlake.transpiler.bigquery.BigQueryExpressionTranspiler:

=======================================================================
BigQueryExpressionTranspiler
=======================================================================

*extends:* :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` 

| **BigQueryExpressionTranspiler** (selectDeParser, buffer)
|          SelectDeParser selectDeParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer



..  _ai.starlake.transpiler.bigquery.BigQuerySelectTranspiler:

=======================================================================
BigQuerySelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **BigQuerySelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`Class<java.lang.Class>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder



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

*extends:* :ref:`RedshiftExpressionTranspiler<ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler>` 

| **DatabricksExpressionTranspiler** (selectDeParser, buffer)
|          SelectDeParser selectDeParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| **toDateTimePart** (expression) → Expression
|          Expression expression
|          returns Expression



| **castInterval** (e1, e2) → Expression
|          Expression e1
|          Expression e2
|          returns Expression



| *@SuppressWarnings*
| **visit** (function)
|          Function function


| **visit** (function)
|          AnalyticExpression function


| **visit** (column)
|          Column column


| **rewriteType** (colDataType) → ColDataType
|          ColDataType colDataType
|          returns ColDataType




..  _ai.starlake.transpiler.databricks.DatabricksSelectTranspiler:

=======================================================================
DatabricksSelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **DatabricksSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`Class<java.lang.Class>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder



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

*extends:* :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` *provides:* :ref:`DatabricksExpressionTranspiler<ai.starlake.transpiler.databricks.DatabricksExpressionTranspiler>`, :ref:`SnowflakeExpressionTranspiler<ai.starlake.transpiler.snowflake.SnowflakeExpressionTranspiler>` 

| **RedshiftExpressionTranspiler** (deParser, buffer)
|          SelectDeParser deParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| *@SuppressWarnings*
| **visit** (function)
|          Function function


| **visit** (function)
|          AnalyticExpression function


| **visit** (column)
|          Column column


| **toFormat** (s) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` s
|          returns :ref:`String<java.lang.String>`



| **rewriteType** (colDataType) → ColDataType
|          ColDataType colDataType
|          returns ColDataType




..  _ai.starlake.transpiler.redshift.RedshiftSelectTranspiler:

=======================================================================
RedshiftSelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **RedshiftSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`Class<java.lang.Class>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder



..  _ai.starlake.transpiler.redshift.RedshiftTranspiler:

=======================================================================
RedshiftTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **RedshiftTranspiler** ()



..  _ai.starlake.transpiler.schemas:
***********************************************************************
mas
***********************************************************************

..  _ai.starlake.transpiler.schemas.SampleSchemaProvider:

=======================================================================
SampleSchemaProvider
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`SchemaProvider<ai.starlake.transpiler.schemas.SchemaProvider>` 

| **SampleSchemaProvider** ()


| **getTables** () → :ref:`Map<java.util.Map>`
|          returns :ref:`Map<java.util.Map>`



| **getTable** (schemaName, tableName) → :ref:`Map<java.util.Map>`
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          returns :ref:`Map<java.util.Map>`



| **getTables** (tableName) → :ref:`Map<java.util.Map>`
|          :ref:`String<java.lang.String>` tableName
|          returns :ref:`Map<java.util.Map>`




..  _ai.starlake.transpiler.snowflake:
***********************************************************************
flake
***********************************************************************

..  _ai.starlake.transpiler.snowflake.SnowflakeExpressionTranspiler:

=======================================================================
SnowflakeExpressionTranspiler
=======================================================================

*extends:* :ref:`RedshiftExpressionTranspiler<ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler>` 

| **SnowflakeExpressionTranspiler** (deParser, buffer)
|          SelectDeParser deParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| **toDateTimePart** (expression) → Expression
|          Expression expression
|          returns Expression



| **castInterval** (e1, e2) → Expression
|          Expression e1
|          Expression e2
|          returns Expression



| *@SuppressWarnings*
| **visit** (function)
|          Function function


| **visit** (function)
|          AnalyticExpression function


| **visit** (column)
|          Column column


| **visit** (hexValue)
|          HexValue hexValue


| **visit** (likeExpression)
|          LikeExpression likeExpression


| **rewriteType** (colDataType) → ColDataType
|          ColDataType colDataType
|          returns ColDataType




..  _ai.starlake.transpiler.snowflake.SnowflakeSelectTranspiler:

=======================================================================
SnowflakeSelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **SnowflakeSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`Class<java.lang.Class>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder


| **visit** (values)
|          Values values


| *@SuppressWarnings*
| **visit** (tableFunction)
|          TableFunction tableFunction



..  _ai.starlake.transpiler.snowflake.SnowflakeTranspiler:

=======================================================================
SnowflakeTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **SnowflakeTranspiler** ()


