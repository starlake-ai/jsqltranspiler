
.. raw:: html

    <div id="floating-toc">
        <div class="search-container">
            <input type="button" id="toc-hide-show-btn"></input>
            <input type="text" id="toc-search" placeholder="Search" />
        </div>
        <ul id="toc-list"></ul>
    </div>



#######################################################################
JSQLTranspiler 0.7-SNAPSHOT API
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


..  _ai.starlake.transpiler.JSQLColumResolver:

=======================================================================
JSQLColumResolver
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| A class for resolving the actual columns returned by a SELECT statement. Depends on virtual or physical Database Metadata holding the schema and table information.

| **JSQLColumResolver** (currentCatalogName, currentSchemaName, metaData)
|          :ref:`String<java.lang.String>` currentCatalogName
|          :ref:`String<java.lang.String>` currentSchemaName
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData


| **JSQLColumResolver** (metaData)
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData


| **JSQLColumResolver** (currentCatalogName, currentSchemaName, metaDataDefinition)
|          :ref:`String<java.lang.String>` currentCatalogName
|          :ref:`String<java.lang.String>` currentSchemaName
|          :ref:`String<java.lang.String>` metaDataDefinition


| **JSQLColumResolver** (metaDataDefinition)
|          :ref:`String<java.lang.String>` metaDataDefinition


| *@SuppressWarnings*
| **getResultSetMetaData** (sqlStr, metaData, currentCatalogName, currentSchemaName) → :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the `SELECT` statement text
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData  | metaData the Database Meta Data
|          :ref:`String<java.lang.String>` currentCatalogName  | currentCatalogName the CURRENT_CATALOG name (which is the default catalog for accessing the schemas)
|          :ref:`String<java.lang.String>` currentSchemaName  | currentSchemaName the CURRENT_SCHEMA name (which is the default schema for accessing the tables)
|          returns :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`  | the ResultSetMetaData representing the actual columns returned by the `SELECT` statement



| **getResultSetMetaData** (sqlStr, metaDataDefinition, currentCatalogName, currentSchemaName) → :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the `SELECT` statement text
|          :ref:`String<java.lang.String>` metaDataDefinition  | metaDataDefinition the meta data definition as an array of Tables with Columns e.g. { TABLE_NAME, COLUMN1, COLUMN2 ... COLUMN10 }
|          :ref:`String<java.lang.String>` currentCatalogName  | currentCatalogName the CURRENT_CATALOG name (which is the default catalog for accessing the schemas)
|          :ref:`String<java.lang.String>` currentSchemaName  | currentSchemaName the CURRENT_SCHEMA name (which is the default schema for accessing the tables)
|          returns :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`  | the ResultSetMetaData representing the actual columns returned by the `SELECT` statement



| **getResultSetMetaData** (sqlStr, metaDataDefinition) → :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and and empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the `SELECT` statement text
|          :ref:`String<java.lang.String>` metaDataDefinition  | metaDataDefinition the meta data definition as an array of Tables with Columns e.g. { TABLE_NAME, COLUMN1, COLUMN2 ... COLUMN10 }
|          returns :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`  | the ResultSetMetaData representing the actual columns returned by the `SELECT` statement



| **getResultSetMetaData** (sqlStr) → :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the `SELECT` statement text
|          returns :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>`  | the ResultSetMetaData representing the actual columns returned by the `SELECT` statement



| *@Override*
| **visit** (table, s) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          Table table
|          S s
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| **visit** (parenthesedSelect, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          ParenthesedSelect parenthesedSelect
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (parenthesedSelect, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          ParenthesedSelect parenthesedSelect
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@SuppressWarnings*
| **visit** (select, metaData) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          PlainSelect select
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (select, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          PlainSelect select
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (setOperationList, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          SetOperationList setOperationList
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (withItem, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          WithItem withItem
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (values, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          Values values
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (lateralSubSelect, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          LateralSubSelect lateralSubSelect
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (tableFunction, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          TableFunction tableFunction
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (parenthesedFromItem, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          ParenthesedFromItem parenthesedFromItem
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (tableStatement, parameters) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          TableStatement tableStatement
|          S parameters
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`




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



| *@SuppressWarnings*,| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Function function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (allColumns, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          AllColumns allColumns
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@SuppressWarnings*,| *@Override*
| **visit** (function, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          AnalyticExpression function
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




                |          Function function

                |          <any> parameters

                |          :ref:`DateTime<DateTimeLiteralExpression.DateTime>` dateTimeType

                |          returns void


            | *@Override*
| **visit** (extractExpression, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          ExtractExpression extractExpression
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (stringValue, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          StringValue stringValue
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (hexValue, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          HexValue hexValue
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **convertUnicode** (input) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` input
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **visit** (castExpression, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          CastExpression castExpression
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (structType, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          StructType structType
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



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



| *@Override*
| **visit** (expression, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          TimeKeyExpression expression
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (likeExpression, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          LikeExpression likeExpression
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          TranscodingFunction function
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **isEmpty** (collection) → boolean
|          :ref:`Collection<java.util.Collection>` collection
|          returns boolean



| **hasParameters** (function) → boolean
|          Function function
|          returns boolean



| *@Override*
| **visit** (column, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Column column
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (expressionList, parameters) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          <any> expressionList
|          S parameters
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




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



| *@Override*
| **visit** (top)
|          Top top


| *@Override*
| **visit** (tableFunction, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          TableFunction tableFunction
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (plainSelect, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          PlainSelect plainSelect
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (table, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Table table
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (selectItem, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          <any> selectItem
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




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



| **visit** (select) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Select select
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (insert) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Insert insert
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (update) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Update update
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (delete) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Delete delete
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (merge) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Merge merge
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




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


| *@Override*
| **visit** (select, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          PlainSelect select
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




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



| *@Override*,| *@SuppressWarnings*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Function function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          AnalyticExpression function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (column, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Column column
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



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


| *@Override*,| *@SuppressWarnings*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Function function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          AnalyticExpression function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (column, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Column column
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



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



..  _ai.starlake.transpiler.schema:
***********************************************************************
ma
***********************************************************************

..  _ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap:

=======================================================================
CaseInsensitiveLinkedHashMap
=======================================================================

*extends:* :ref:`LinkedHashMap<java.util.LinkedHashMap>` 

| A Case insensitive linked hash map preserving the original spelling of the keys. It can be used for looking up a database's schemas, tables, columns, indices and constraints.

| **CaseInsensitiveLinkedHashMap** ()


| *@Override*
| **put** (key, value) → V
|          :ref:`String<java.lang.String>` key
|          V value
|          returns V



| *@Override*
| **get** (key) → V
|          :ref:`Object<java.lang.Object>` key
|          returns V



| *@Override*
| **containsKey** (key) → boolean
|          :ref:`Object<java.lang.Object>` key
|          returns boolean



| *@Override*
| **remove** (key) → V
|          :ref:`Object<java.lang.Object>` key
|          returns V



| *@Override*
| **clear** ()


| *@Override*
| **entrySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`



| *@Override*
| **keySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`




..  _ai.starlake.transpiler.schema.JdbcCatalog:

=======================================================================
JdbcCatalog
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **JdbcCatalog** (tableCatalog, catalogSeparator)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` catalogSeparator


| **getCatalogs** (metaData) → :ref:`Collection<java.util.Collection>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`Collection<java.util.Collection>`



| **put** (jdbcSchema) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` jdbcSchema
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **get** (tableSchema) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` tableSchema
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| *@Override*
| **compareTo** (o) → int
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` o
|          returns int



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int



| **put** (key, value) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` value
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **containsValue** (value) → boolean
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` value
|          returns boolean



| **size** () → int
|          returns int



| **replace** (key, value) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` value
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **isEmpty** () → boolean
|          returns boolean



| **compute** (key, remappingFunction) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **putAll** (m)
|          :ref:`Map<java.util.Map>` m


| **values** () → :ref:`Collection<java.util.Collection>`
|          returns :ref:`Collection<java.util.Collection>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` oldValue
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`BiConsumer<java.util.function.BiConsumer>` action


| **getOrDefault** (key, defaultValue) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` defaultValue
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **remove** (key, value) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`Object<java.lang.Object>` value
|          returns boolean



| **computeIfPresent** (key, remappingFunction) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **replaceAll** (function)
|          :ref:`BiFunction<java.util.function.BiFunction>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`Function<java.util.function.Function>` mappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **putIfAbsent** (value) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` value
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` value
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **containsKey** (key) → boolean
|          :ref:`String<java.lang.String>` key
|          returns boolean



| **remove** (key) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **clear** ()


| **entrySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`



| **keySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`




..  _ai.starlake.transpiler.schema.JdbcColumn:

=======================================================================
JdbcColumn
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **JdbcColumn** (tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, numericPrecisionRadix, nullable, remarks, columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog, scopeSchema, scopeTable, sourceDataType, isAutomaticIncrement, isGeneratedColumn)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Integer<java.lang.Integer>` dataType
|          :ref:`String<java.lang.String>` typeName
|          :ref:`Integer<java.lang.Integer>` columnSize
|          :ref:`Integer<java.lang.Integer>` decimalDigits
|          :ref:`Integer<java.lang.Integer>` numericPrecisionRadix
|          :ref:`Integer<java.lang.Integer>` nullable
|          :ref:`String<java.lang.String>` remarks
|          :ref:`String<java.lang.String>` columnDefinition
|          :ref:`Integer<java.lang.Integer>` characterOctetLength
|          :ref:`Integer<java.lang.Integer>` ordinalPosition
|          :ref:`String<java.lang.String>` isNullable
|          :ref:`String<java.lang.String>` scopeCatalog
|          :ref:`String<java.lang.String>` scopeSchema
|          :ref:`String<java.lang.String>` scopeTable
|          :ref:`Short<java.lang.Short>` sourceDataType
|          :ref:`String<java.lang.String>` isAutomaticIncrement
|          :ref:`String<java.lang.String>` isGeneratedColumn


| **JdbcColumn** (tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, nullable, remarks)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Integer<java.lang.Integer>` dataType
|          :ref:`String<java.lang.String>` typeName
|          :ref:`Integer<java.lang.Integer>` columnSize
|          :ref:`Integer<java.lang.Integer>` decimalDigits
|          :ref:`Integer<java.lang.Integer>` nullable
|          :ref:`String<java.lang.String>` remarks


| **JdbcColumn** (columnName, dataType, typeName, columnSize, decimalDigits, nullable, remarks)
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Integer<java.lang.Integer>` dataType
|          :ref:`String<java.lang.String>` typeName
|          :ref:`Integer<java.lang.Integer>` columnSize
|          :ref:`Integer<java.lang.Integer>` decimalDigits
|          :ref:`Integer<java.lang.Integer>` nullable
|          :ref:`String<java.lang.String>` remarks


| **JdbcColumn** (tableCatalog, tableSchema, tableName, columnName)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName


| **JdbcColumn** (columnName)
|          :ref:`String<java.lang.String>` columnName


| *@Override*,| *@SuppressWarnings*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*,| *@SuppressWarnings*
| **compareTo** (o) → int
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` o
|          returns int



| *@Override*
| **toString** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*,| *@SuppressWarnings*
| **hashCode** () → int
|          returns int




..  _ai.starlake.transpiler.schema.JdbcIndex:

=======================================================================
JdbcIndex
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **JdbcIndex** (tableCatalog, tableSchema, tableName, nonUnique, indexQualifier, indexName, type)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`Boolean<java.lang.Boolean>` nonUnique
|          :ref:`String<java.lang.String>` indexQualifier
|          :ref:`String<java.lang.String>` indexName
|          :ref:`Short<java.lang.Short>` type


| **put** (ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition) → :ref:`JdbcIndexColumn<ai.starlake.transpiler.schema.JdbcIndexColumn>`
|          :ref:`Short<java.lang.Short>` ordinalPosition
|          :ref:`String<java.lang.String>` columnName
|          :ref:`String<java.lang.String>` ascOrDesc
|          :ref:`Long<java.lang.Long>` cardinality
|          :ref:`Long<java.lang.Long>` pages
|          :ref:`String<java.lang.String>` filterCondition
|          returns :ref:`JdbcIndexColumn<ai.starlake.transpiler.schema.JdbcIndexColumn>`



| *@Override*,| *@SuppressWarnings*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*,| *@SuppressWarnings*
| **hashCode** () → int
|          returns int




..  _ai.starlake.transpiler.schema.JdbcIndexColumn:

=======================================================================
JdbcIndexColumn
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **JdbcIndexColumn** (ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition)
|          :ref:`Short<java.lang.Short>` ordinalPosition
|          :ref:`String<java.lang.String>` columnName
|          :ref:`String<java.lang.String>` ascOrDesc
|          :ref:`Long<java.lang.Long>` cardinality
|          :ref:`Long<java.lang.Long>` pages
|          :ref:`String<java.lang.String>` filterCondition


| *@Override*
| **compareTo** (o) → int
|          :ref:`JdbcIndexColumn<ai.starlake.transpiler.schema.JdbcIndexColumn>` o
|          returns int



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _ai.starlake.transpiler.schema.JdbcMetaData:

=======================================================================
JdbcMetaData
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` 

| **JdbcMetaData** (schemaDefinition)
| Instantiates a new virtual JDBC MetaData object with an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA and creates tables from the provided definition.
|          :ref:`String<java.lang.String>` schemaDefinition  | schemaDefinition the schema definition of tables and columns


| **JdbcMetaData** (catalogName, schemaName, schemaDefinition)
| Instantiates a new virtual JDBC MetaData object for the given CURRENT_CATALOG and CURRENT_SCHEMA and creates tables from the provided definition.
|          :ref:`String<java.lang.String>` catalogName  | catalogName the CURRENT_CATALOG
|          :ref:`String<java.lang.String>` schemaName  | schemaName the CURRENT_SCHEMA
|          :ref:`String<java.lang.String>` schemaDefinition  | schemaDefinition the schema definition of tables and columns


| **JdbcMetaData** (catalogName, schemaName)
| Instantiates a new virtual JDBC MetaData object with a given CURRENT_CATALOG and CURRENT_SCHEMA.
|          :ref:`String<java.lang.String>` catalogName  | catalogName the CURRENT_CATALOG to set
|          :ref:`String<java.lang.String>` schemaName  | schemaName the CURRENT_SCHEMA to set


| **JdbcMetaData** ()
| Instantiates a new virtual JDBC MetaData object with an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA.


| **JdbcMetaData** (con)
| Derives JDBC MetaData object from a physical database connection.
|          :ref:`Connection<java.sql.Connection>` con  | con the physical database connection


| **getTypeName** (sqlType) → :ref:`String<java.lang.String>`
|          int sqlType
|          returns :ref:`String<java.lang.String>`



| **put** (jdbcCatalog) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` jdbcCatalog
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **getCatalogMap** () → :ref:`Map<java.util.Map>`
|          returns :ref:`Map<java.util.Map>`



| **put** (jdbcSchema) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` jdbcSchema
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **put** (jdbcTable) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` jdbcTable
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| *@SuppressWarnings*
| **getTableColumns** (catalogName, schemaName, tableName, columnName) → :ref:`List<java.util.List>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          returns :ref:`List<java.util.List>`



| *@SuppressWarnings*
| **getColumn** (catalogName, schemaName, tableName, columnName) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **put** (key, value) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **containsValue** (value) → boolean
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          returns boolean



| **size** () → int
|          returns int



| **replace** (key, value) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **isEmpty** () → boolean
|          returns boolean



| **compute** (key, remappingFunction) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **putAll** (m)
|          :ref:`Map<java.util.Map>` m


| **values** () → :ref:`Collection<java.util.Collection>`
|          returns :ref:`Collection<java.util.Collection>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` oldValue
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`BiConsumer<java.util.function.BiConsumer>` action


| **getOrDefault** (key, defaultValue) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` defaultValue
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **remove** (key, value) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          returns boolean



| **computeIfPresent** (key, remappingFunction) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **replaceAll** (function)
|          :ref:`BiFunction<java.util.function.BiFunction>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`Function<java.util.function.Function>` mappingFunction
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **putIfAbsent** (value) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **get** (key) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **containsKey** (key) → boolean
|          :ref:`String<java.lang.String>` key
|          returns boolean



| **remove** (key) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **clear** ()


| **entrySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`



| **keySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`



| **addSchema** (schemaName) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` schemaName
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **addTable** (catalogName, schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`Collection<java.util.Collection>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (catalogName, schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`Collection<java.util.Collection>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`Collection<java.util.Collection>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (tableName, columnNames) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnNames
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addColumns** (tableName, columns) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`Collection<java.util.Collection>` columns
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **addColumns** (tableName, columns) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` columns
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| *@Override*
| **allProceduresAreCallable** () → boolean
|          returns boolean



| *@Override*
| **allTablesAreSelectable** () → boolean
|          returns boolean



| *@Override*
| **getURL** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getUserName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **isReadOnly** () → boolean
|          returns boolean



| *@Override*
| **nullsAreSortedHigh** () → boolean
|          returns boolean



| *@Override*
| **nullsAreSortedLow** () → boolean
|          returns boolean



| *@Override*
| **nullsAreSortedAtStart** () → boolean
|          returns boolean



| *@Override*
| **nullsAreSortedAtEnd** () → boolean
|          returns boolean



| *@Override*
| **getDatabaseProductName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getDatabaseProductVersion** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getDriverName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getDriverVersion** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getDriverMajorVersion** () → int
|          returns int



| *@Override*
| **getDriverMinorVersion** () → int
|          returns int



| *@Override*
| **usesLocalFiles** () → boolean
|          returns boolean



| *@Override*
| **usesLocalFilePerTable** () → boolean
|          returns boolean



| *@Override*
| **supportsMixedCaseIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **storesUpperCaseIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **storesLowerCaseIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **storesMixedCaseIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **supportsMixedCaseQuotedIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **storesUpperCaseQuotedIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **storesLowerCaseQuotedIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **storesMixedCaseQuotedIdentifiers** () → boolean
|          returns boolean



| *@Override*
| **getIdentifierQuoteString** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getSQLKeywords** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getNumericFunctions** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getStringFunctions** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getSystemFunctions** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getTimeDateFunctions** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getSearchStringEscape** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getExtraNameCharacters** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **supportsAlterTableWithAddColumn** () → boolean
|          returns boolean



| *@Override*
| **supportsAlterTableWithDropColumn** () → boolean
|          returns boolean



| *@Override*
| **supportsColumnAliasing** () → boolean
|          returns boolean



| *@Override*
| **nullPlusNonNullIsNull** () → boolean
|          returns boolean



| *@Override*
| **supportsConvert** () → boolean
|          returns boolean



| *@Override*
| **supportsConvert** (fromType, toType) → boolean
|          int fromType
|          int toType
|          returns boolean



| *@Override*
| **supportsTableCorrelationNames** () → boolean
|          returns boolean



| *@Override*
| **supportsDifferentTableCorrelationNames** () → boolean
|          returns boolean



| *@Override*
| **supportsExpressionsInOrderBy** () → boolean
|          returns boolean



| *@Override*
| **supportsOrderByUnrelated** () → boolean
|          returns boolean



| *@Override*
| **supportsGroupBy** () → boolean
|          returns boolean



| *@Override*
| **supportsGroupByUnrelated** () → boolean
|          returns boolean



| *@Override*
| **supportsGroupByBeyondSelect** () → boolean
|          returns boolean



| *@Override*
| **supportsLikeEscapeClause** () → boolean
|          returns boolean



| *@Override*
| **supportsMultipleResultSets** () → boolean
|          returns boolean



| *@Override*
| **supportsMultipleTransactions** () → boolean
|          returns boolean



| *@Override*
| **supportsNonNullableColumns** () → boolean
|          returns boolean



| *@Override*
| **supportsMinimumSQLGrammar** () → boolean
|          returns boolean



| *@Override*
| **supportsCoreSQLGrammar** () → boolean
|          returns boolean



| *@Override*
| **supportsExtendedSQLGrammar** () → boolean
|          returns boolean



| *@Override*
| **supportsANSI92EntryLevelSQL** () → boolean
|          returns boolean



| *@Override*
| **supportsANSI92IntermediateSQL** () → boolean
|          returns boolean



| *@Override*
| **supportsANSI92FullSQL** () → boolean
|          returns boolean



| *@Override*
| **supportsIntegrityEnhancementFacility** () → boolean
|          returns boolean



| *@Override*
| **supportsOuterJoins** () → boolean
|          returns boolean



| *@Override*
| **supportsFullOuterJoins** () → boolean
|          returns boolean



| *@Override*
| **supportsLimitedOuterJoins** () → boolean
|          returns boolean



| *@Override*
| **getSchemaTerm** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getProcedureTerm** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getCatalogTerm** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **isCatalogAtStart** () → boolean
|          returns boolean



| *@Override*
| **getCatalogSeparator** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **supportsSchemasInDataManipulation** () → boolean
|          returns boolean



| *@Override*
| **supportsSchemasInProcedureCalls** () → boolean
|          returns boolean



| *@Override*
| **supportsSchemasInTableDefinitions** () → boolean
|          returns boolean



| *@Override*
| **supportsSchemasInIndexDefinitions** () → boolean
|          returns boolean



| *@Override*
| **supportsSchemasInPrivilegeDefinitions** () → boolean
|          returns boolean



| *@Override*
| **supportsCatalogsInDataManipulation** () → boolean
|          returns boolean



| *@Override*
| **supportsCatalogsInProcedureCalls** () → boolean
|          returns boolean



| *@Override*
| **supportsCatalogsInTableDefinitions** () → boolean
|          returns boolean



| *@Override*
| **supportsCatalogsInIndexDefinitions** () → boolean
|          returns boolean



| *@Override*
| **supportsCatalogsInPrivilegeDefinitions** () → boolean
|          returns boolean



| *@Override*
| **supportsPositionedDelete** () → boolean
|          returns boolean



| *@Override*
| **supportsPositionedUpdate** () → boolean
|          returns boolean



| *@Override*
| **supportsSelectForUpdate** () → boolean
|          returns boolean



| *@Override*
| **supportsStoredProcedures** () → boolean
|          returns boolean



| *@Override*
| **supportsSubqueriesInComparisons** () → boolean
|          returns boolean



| *@Override*
| **supportsSubqueriesInExists** () → boolean
|          returns boolean



| *@Override*
| **supportsSubqueriesInIns** () → boolean
|          returns boolean



| *@Override*
| **supportsSubqueriesInQuantifieds** () → boolean
|          returns boolean



| *@Override*
| **supportsCorrelatedSubqueries** () → boolean
|          returns boolean



| *@Override*
| **supportsUnion** () → boolean
|          returns boolean



| *@Override*
| **supportsUnionAll** () → boolean
|          returns boolean



| *@Override*
| **supportsOpenCursorsAcrossCommit** () → boolean
|          returns boolean



| *@Override*
| **supportsOpenCursorsAcrossRollback** () → boolean
|          returns boolean



| *@Override*
| **supportsOpenStatementsAcrossCommit** () → boolean
|          returns boolean



| *@Override*
| **supportsOpenStatementsAcrossRollback** () → boolean
|          returns boolean



| *@Override*
| **getMaxBinaryLiteralLength** () → int
|          returns int



| *@Override*
| **getMaxCharLiteralLength** () → int
|          returns int



| *@Override*
| **getMaxColumnNameLength** () → int
|          returns int



| *@Override*
| **getMaxColumnsInGroupBy** () → int
|          returns int



| *@Override*
| **getMaxColumnsInIndex** () → int
|          returns int



| *@Override*
| **getMaxColumnsInOrderBy** () → int
|          returns int



| *@Override*
| **getMaxColumnsInSelect** () → int
|          returns int



| *@Override*
| **getMaxColumnsInTable** () → int
|          returns int



| *@Override*
| **getMaxConnections** () → int
|          returns int



| *@Override*
| **getMaxCursorNameLength** () → int
|          returns int



| *@Override*
| **getMaxIndexLength** () → int
|          returns int



| *@Override*
| **getMaxSchemaNameLength** () → int
|          returns int



| *@Override*
| **getMaxProcedureNameLength** () → int
|          returns int



| *@Override*
| **getMaxCatalogNameLength** () → int
|          returns int



| *@Override*
| **getMaxRowSize** () → int
|          returns int



| *@Override*
| **doesMaxRowSizeIncludeBlobs** () → boolean
|          returns boolean



| *@Override*
| **getMaxStatementLength** () → int
|          returns int



| *@Override*
| **getMaxStatements** () → int
|          returns int



| *@Override*
| **getMaxTableNameLength** () → int
|          returns int



| *@Override*
| **getMaxTablesInSelect** () → int
|          returns int



| *@Override*
| **getMaxUserNameLength** () → int
|          returns int



| *@Override*
| **getDefaultTransactionIsolation** () → int
|          returns int



| *@Override*
| **supportsTransactions** () → boolean
|          returns boolean



| *@Override*
| **supportsTransactionIsolationLevel** (level) → boolean
|          int level
|          returns boolean



| *@Override*
| **supportsDataDefinitionAndDataManipulationTransactions** () → boolean
|          returns boolean



| *@Override*
| **supportsDataManipulationTransactionsOnly** () → boolean
|          returns boolean



| *@Override*
| **dataDefinitionCausesTransactionCommit** () → boolean
|          returns boolean



| *@Override*
| **dataDefinitionIgnoredInTransactions** () → boolean
|          returns boolean



| *@Override*
| **getProcedures** (catalog, schemaPattern, procedureNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` procedureNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getProcedureColumns** (catalog, schemaPattern, procedureNamePattern, columnNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` procedureNamePattern
|          :ref:`String<java.lang.String>` columnNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getTables** (catalog, schemaPattern, tableNamePattern, types) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` tableNamePattern
|          :ref:`String<java.lang.String>` types
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getSchemas** () → :ref:`ResultSet<java.sql.ResultSet>`
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getCatalogs** () → :ref:`ResultSet<java.sql.ResultSet>`
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getTableTypes** () → :ref:`ResultSet<java.sql.ResultSet>`
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getColumns** (catalog, schemaPattern, tableNamePattern, columnNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` tableNamePattern
|          :ref:`String<java.lang.String>` columnNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getColumnPrivileges** (catalog, schema, table, columnNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schema
|          :ref:`String<java.lang.String>` table
|          :ref:`String<java.lang.String>` columnNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getTablePrivileges** (catalog, schemaPattern, tableNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` tableNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getBestRowIdentifier** (catalog, schema, table, scope, nullable) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schema
|          :ref:`String<java.lang.String>` table
|          int scope
|          boolean nullable
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getVersionColumns** (catalog, schema, table) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schema
|          :ref:`String<java.lang.String>` table
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getPrimaryKeys** (catalog, schema, table) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schema
|          :ref:`String<java.lang.String>` table
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getImportedKeys** (catalog, schema, table) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schema
|          :ref:`String<java.lang.String>` table
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getExportedKeys** (catalog, schema, table) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schema
|          :ref:`String<java.lang.String>` table
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getCrossReference** (parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` parentCatalog
|          :ref:`String<java.lang.String>` parentSchema
|          :ref:`String<java.lang.String>` parentTable
|          :ref:`String<java.lang.String>` foreignCatalog
|          :ref:`String<java.lang.String>` foreignSchema
|          :ref:`String<java.lang.String>` foreignTable
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getTypeInfo** () → :ref:`ResultSet<java.sql.ResultSet>`
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getIndexInfo** (catalog, schema, table, unique, approximate) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schema
|          :ref:`String<java.lang.String>` table
|          boolean unique
|          boolean approximate
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **supportsResultSetType** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **supportsResultSetConcurrency** (type, concurrency) → boolean
|          int type
|          int concurrency
|          returns boolean



| *@Override*
| **ownUpdatesAreVisible** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **ownDeletesAreVisible** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **ownInsertsAreVisible** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **othersUpdatesAreVisible** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **othersDeletesAreVisible** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **othersInsertsAreVisible** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **updatesAreDetected** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **deletesAreDetected** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **insertsAreDetected** (type) → boolean
|          int type
|          returns boolean



| *@Override*
| **supportsBatchUpdates** () → boolean
|          returns boolean



| *@Override*
| **getUDTs** (catalog, schemaPattern, typeNamePattern, types) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` typeNamePattern
|          int types
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getConnection** () → :ref:`Connection<java.sql.Connection>`
|          returns :ref:`Connection<java.sql.Connection>`



| *@Override*
| **supportsSavepoints** () → boolean
|          returns boolean



| *@Override*
| **supportsNamedParameters** () → boolean
|          returns boolean



| *@Override*
| **supportsMultipleOpenResults** () → boolean
|          returns boolean



| *@Override*
| **supportsGetGeneratedKeys** () → boolean
|          returns boolean



| *@Override*
| **getSuperTypes** (catalog, schemaPattern, typeNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` typeNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getSuperTables** (catalog, schemaPattern, tableNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` tableNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getAttributes** (catalog, schemaPattern, typeNamePattern, attributeNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` typeNamePattern
|          :ref:`String<java.lang.String>` attributeNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **supportsResultSetHoldability** (holdability) → boolean
|          int holdability
|          returns boolean



| *@Override*
| **getResultSetHoldability** () → int
|          returns int



| *@Override*
| **getDatabaseMajorVersion** () → int
|          returns int



| *@Override*
| **getDatabaseMinorVersion** () → int
|          returns int



| *@Override*
| **getJDBCMajorVersion** () → int
|          returns int



| *@Override*
| **getJDBCMinorVersion** () → int
|          returns int



| *@Override*
| **getSQLStateType** () → int
|          returns int



| *@Override*
| **locatorsUpdateCopy** () → boolean
|          returns boolean



| *@Override*
| **supportsStatementPooling** () → boolean
|          returns boolean



| *@Override*
| **getRowIdLifetime** () → :ref:`RowIdLifetime<java.sql.RowIdLifetime>`
|          returns :ref:`RowIdLifetime<java.sql.RowIdLifetime>`



| *@Override*
| **getSchemas** (catalog, schemaPattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **supportsStoredFunctionsUsingCallSyntax** () → boolean
|          returns boolean



| *@Override*
| **autoCommitFailureClosesAllResultSets** () → boolean
|          returns boolean



| *@Override*
| **getClientInfoProperties** () → :ref:`ResultSet<java.sql.ResultSet>`
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getFunctions** (catalog, schemaPattern, functionNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` functionNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getFunctionColumns** (catalog, schemaPattern, functionNamePattern, columnNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` functionNamePattern
|          :ref:`String<java.lang.String>` columnNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **getPseudoColumns** (catalog, schemaPattern, tableNamePattern, columnNamePattern) → :ref:`ResultSet<java.sql.ResultSet>`
|          :ref:`String<java.lang.String>` catalog
|          :ref:`String<java.lang.String>` schemaPattern
|          :ref:`String<java.lang.String>` tableNamePattern
|          :ref:`String<java.lang.String>` columnNamePattern
|          returns :ref:`ResultSet<java.sql.ResultSet>`



| *@Override*
| **generatedKeyAlwaysReturned** () → boolean
|          returns boolean



| *@Override*
| **unwrap** (iface) → T
|          :ref:`Class<java.lang.Class>` iface
|          returns T



| *@Override*
| **isWrapperFor** (iface) → boolean
|          :ref:`Class<java.lang.Class>` iface
|          returns boolean



| **copyOf** (metaData) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`




..  _ai.starlake.transpiler.schema.JdbcPrimaryKey:

=======================================================================
JdbcPrimaryKey
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **JdbcPrimaryKey** (tableCatalog, tableSchema, tableName, primaryKeyName)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` primaryKeyName


| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _ai.starlake.transpiler.schema.JdbcReference:

=======================================================================
JdbcReference
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **JdbcReference** (pkTableCatalog, pkTableSchema, pkTableName, fkTableCatalog, fkTableSchema, fkTableName, updateRule, deleteRule, fkName, pkName, deferrability)
|          :ref:`String<java.lang.String>` pkTableCatalog
|          :ref:`String<java.lang.String>` pkTableSchema
|          :ref:`String<java.lang.String>` pkTableName
|          :ref:`String<java.lang.String>` fkTableCatalog
|          :ref:`String<java.lang.String>` fkTableSchema
|          :ref:`String<java.lang.String>` fkTableName
|          :ref:`Short<java.lang.Short>` updateRule
|          :ref:`Short<java.lang.Short>` deleteRule
|          :ref:`String<java.lang.String>` fkName
|          :ref:`String<java.lang.String>` pkName
|          :ref:`Short<java.lang.Short>` deferrability


| *@Override*,| *@SuppressWarnings*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*,| *@SuppressWarnings*
| **hashCode** () → int
|          returns int




..  _ai.starlake.transpiler.schema.JdbcResultSetMetaData:

=======================================================================
JdbcResultSetMetaData
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`ResultSetMetaData<java.sql.ResultSetMetaData>` 

| **JdbcResultSetMetaData** ()


| **add** (jdbcColumn, label)
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` jdbcColumn
|          :ref:`String<java.lang.String>` label


| **clear** ()


| **add** (resultSetMetaData)
|          :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>` resultSetMetaData


| *@Override*
| **getColumnCount** () → int
|          returns int



| *@Override*
| **isAutoIncrement** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **isCaseSensitive** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **isSearchable** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **isCurrency** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **isNullable** (column) → int
|          int column
|          returns int



| *@Override*
| **isSigned** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **getColumnDisplaySize** (column) → int
|          int column
|          returns int



| *@Override*
| **getColumnLabel** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getColumnName** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getSchemaName** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getPrecision** (column) → int
|          int column
|          returns int



| *@Override*
| **getScale** (column) → int
|          int column
|          returns int



| *@Override*
| **getTableName** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getCatalogName** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **getColumnType** (column) → int
|          int column
|          returns int



| *@Override*
| **getColumnTypeName** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **isReadOnly** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **isWritable** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **isDefinitelyWritable** (column) → boolean
|          int column
|          returns boolean



| *@Override*
| **getColumnClassName** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **unwrap** (iface) → T
|          :ref:`Class<java.lang.Class>` iface
|          returns T



| *@Override*
| **isWrapperFor** (iface) → boolean
|          :ref:`Class<java.lang.Class>` iface
|          returns boolean




..  _ai.starlake.transpiler.schema.JdbcSchema:

=======================================================================
JdbcSchema
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **JdbcSchema** (tableSchema, tableCatalog)
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableCatalog


| **getSchemas** (metaData) → :ref:`Collection<java.util.Collection>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`Collection<java.util.Collection>`



| **put** (jdbcTable) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` jdbcTable
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **get** (tableName) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` tableName
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| *@Override*
| **compareTo** (o) → int
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` o
|          returns int



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int



| **put** (key, value) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **containsValue** (value) → boolean
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          returns boolean



| **size** () → int
|          returns int



| **replace** (key, value) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **isEmpty** () → boolean
|          returns boolean



| **compute** (key, remappingFunction) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **putAll** (m)
|          :ref:`Map<java.util.Map>` m


| **values** () → :ref:`Collection<java.util.Collection>`
|          returns :ref:`Collection<java.util.Collection>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` oldValue
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`BiConsumer<java.util.function.BiConsumer>` action


| **getOrDefault** (key, defaultValue) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` defaultValue
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **remove** (key, value) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          returns boolean



| **computeIfPresent** (key, remappingFunction) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **replaceAll** (function)
|          :ref:`BiFunction<java.util.function.BiFunction>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`Function<java.util.function.Function>` mappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **putIfAbsent** (value) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **containsKey** (key) → boolean
|          :ref:`String<java.lang.String>` key
|          returns boolean



| **remove** (key) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **clear** ()


| **entrySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`



| **keySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`




..  _ai.starlake.transpiler.schema.JdbcTable:

=======================================================================
JdbcTable
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **JdbcTable** (tableCatalog, tableSchema, tableName, tableType, remarks, typeCatalog, typeSchema, typeName, selfReferenceColName, referenceGeneration)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` tableType
|          :ref:`String<java.lang.String>` remarks
|          :ref:`String<java.lang.String>` typeCatalog
|          :ref:`String<java.lang.String>` typeSchema
|          :ref:`String<java.lang.String>` typeName
|          :ref:`String<java.lang.String>` selfReferenceColName
|          :ref:`String<java.lang.String>` referenceGeneration


| **JdbcTable** (tableCatalog, tableSchema, tableName)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName


| **JdbcTable** (catalog, schema, tableName)
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` catalog
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` schema
|          :ref:`String<java.lang.String>` tableName


| **getTables** (metaData) → :ref:`Collection<java.util.Collection>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`Collection<java.util.Collection>`



| **getColumns** (metaData)
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData


| **getIndices** (metaData, approximate)
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          boolean approximate


| **getPrimaryKey** (metaData)
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData


| *@Override*
| **compareTo** (o) → int
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` o
|          returns int



| **add** (jdbcColumn) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` jdbcColumn
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **add** (tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, numericPrecisionRadix, nullable, remarks, columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog, scopeSchema, scopeTable, sourceDataType, isAutomaticIncrement, isGeneratedColumn) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Integer<java.lang.Integer>` dataType
|          :ref:`String<java.lang.String>` typeName
|          :ref:`Integer<java.lang.Integer>` columnSize
|          :ref:`Integer<java.lang.Integer>` decimalDigits
|          :ref:`Integer<java.lang.Integer>` numericPrecisionRadix
|          :ref:`Integer<java.lang.Integer>` nullable
|          :ref:`String<java.lang.String>` remarks
|          :ref:`String<java.lang.String>` columnDefinition
|          :ref:`Integer<java.lang.Integer>` characterOctetLength
|          :ref:`Integer<java.lang.Integer>` ordinalPosition
|          :ref:`String<java.lang.String>` isNullable
|          :ref:`String<java.lang.String>` scopeCatalog
|          :ref:`String<java.lang.String>` scopeSchema
|          :ref:`String<java.lang.String>` scopeTable
|          :ref:`Short<java.lang.Short>` sourceDataType
|          :ref:`String<java.lang.String>` isAutomaticIncrement
|          :ref:`String<java.lang.String>` isGeneratedColumn
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **containsKey** (columnName) → boolean
|          :ref:`String<java.lang.String>` columnName
|          returns boolean



| **contains** (jdbcColumn) → boolean
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` jdbcColumn
|          returns boolean



| **put** (jdbcIndex) → :ref:`JdbcIndex<ai.starlake.transpiler.schema.JdbcIndex>`
|          :ref:`JdbcIndex<ai.starlake.transpiler.schema.JdbcIndex>` jdbcIndex
|          returns :ref:`JdbcIndex<ai.starlake.transpiler.schema.JdbcIndex>`



| **containsIndexKey** (indexName) → boolean
|          :ref:`String<java.lang.String>` indexName
|          returns boolean



| **get** (indexName) → :ref:`JdbcIndex<ai.starlake.transpiler.schema.JdbcIndex>`
|          :ref:`String<java.lang.String>` indexName
|          returns :ref:`JdbcIndex<ai.starlake.transpiler.schema.JdbcIndex>`



| *@Override*,| *@SuppressWarnings*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*,| *@SuppressWarnings*
| **hashCode** () → int
|          returns int



| **put** (key, value) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **containsValue** (value) → boolean
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          returns boolean



| **size** () → int
|          returns int



| **replace** (key, value) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **isEmpty** () → boolean
|          returns boolean



| **compute** (key, remappingFunction) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **putAll** (m)
|          :ref:`Map<java.util.Map>` m


| **values** () → :ref:`Collection<java.util.Collection>`
|          returns :ref:`Collection<java.util.Collection>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` oldValue
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`BiConsumer<java.util.function.BiConsumer>` action


| **getOrDefault** (key, defaultValue) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` defaultValue
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **remove** (key, value) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          returns boolean



| **computeIfPresent** (key, remappingFunction) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **replaceAll** (function)
|          :ref:`BiFunction<java.util.function.BiFunction>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`Function<java.util.function.Function>` mappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **putIfAbsent** (key, value) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          :ref:`BiFunction<java.util.function.BiFunction>` remappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **remove** (key) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **clear** ()


| **entrySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`



| **keySet** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`




..  _ai.starlake.transpiler.schema.SampleSchemaProvider:

=======================================================================
SampleSchemaProvider
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`SchemaProvider<ai.starlake.transpiler.schema.SchemaProvider>` 

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



| *@Override*,| *@SuppressWarnings*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Function function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          AnalyticExpression function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (column, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Column column
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (hexValue, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          HexValue hexValue
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (likeExpression, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          LikeExpression likeExpression
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



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


| *@Override*
| **visit** (values, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          Values values
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*,| *@SuppressWarnings*
| **visit** (tableFunction, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          TableFunction tableFunction
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




..  _ai.starlake.transpiler.snowflake.SnowflakeTranspiler:

=======================================================================
SnowflakeTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **SnowflakeTranspiler** ()


