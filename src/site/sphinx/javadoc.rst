
.. raw:: html

    <div id="floating-toc">
        <div class="search-container">
            <input type="button" id="toc-hide-show-btn"></input>
            <input type="text" id="toc-search" placeholder="Search" />
        </div>
        <ul id="toc-list"></ul>
    </div>



#######################################################################
JSQLTranspiler 0.1-SNAPSHOT API
#######################################################################

Base Package: com.manticore.jsqlformatter


..  _com.manticore.transpiler:
***********************************************************************
Base
***********************************************************************

..  _com.manticore.transpiler.JSQLTranspiler.Dialect

=======================================================================
JSQLTranspiler.Dialect
=======================================================================

[GOOGLE_BIG_QUERY, DATABRICKS, SNOWFLAKE, AMAZON_REDSHIFT, ANY, DUCK_DB]

| The enum Dialect.


..  _com.manticore.transpiler.ExpressionTranspiler:

=======================================================================
ExpressionTranspiler
=======================================================================

*extends:* ExpressionDeParser 

| The type Expression transpiler.

| **ExpressionTranspiler** (selectVisitor, buffer)
|          SelectVisitor selectVisitor
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| **isDatePart** (expression, dialect) → boolean
|          Expression expression
|          :ref:`Dialect<com.manticore.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns boolean



| **isDateTimePart** (expression, dialect) → boolean
|          Expression expression
|          :ref:`Dialect<com.manticore.transpiler.JSQLTranspiler.Dialect>` dialect
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


| **visit** (extractExpression)
|          ExtractExpression extractExpression


| **visit** (stringValue)
|          StringValue stringValue


| **convertUnicode** (input) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` input
|          returns :ref:`String<java.lang.String>`



| **visit** (castExpression)
|          CastExpression castExpression


| **rewriteType** (colDataType) → ColDataType
|          ColDataType colDataType
|          returns ColDataType



| **warning** (s)
|          :ref:`String<java.lang.String>` s



..  _com.manticore.transpiler.JSQLTranspiler:

=======================================================================
JSQLTranspiler
=======================================================================

*extends:* SelectDeParser 

| The type Jsql transpiler.

| **JSQLTranspiler** ()
| Instantiates a new Jsql transpiler.


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


| **transpileQuery** (qryStr, dialect) → :ref:`String<java.lang.String>`
| Transpile a query string in the defined dialect into DuckDB compatible SQL.
|          :ref:`String<java.lang.String>` qryStr  | qryStr the original query string
|          :ref:`Dialect<com.manticore.transpiler.JSQLTranspiler.Dialect>` dialect  | dialect the dialect of the query string
|          returns :ref:`String<java.lang.String>`  | the transformed query string



| **transpile** (sqlStr, outputFile)
| Transpile a query string from a file or STDIN and write the transformed query string into a file or STDOUT.
|          :ref:`String<java.lang.String>` sqlStr  | sqlStr the original query string
|          :ref:`File<java.io.File>` outputFile  | outputFile the output file, writing to STDOUT when not defined


| **transpile** (select) → :ref:`String<java.lang.String>`
| Transpile string.
|          PlainSelect select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileGoogleBigQuery** (select) → :ref:`String<java.lang.String>`
| Transpile google big query string.
|          PlainSelect select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileDatabricksQuery** (select) → :ref:`String<java.lang.String>`
| Transpile databricks query string.
|          PlainSelect select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileSnowflakeQuery** (select) → :ref:`String<java.lang.String>`
| Transpile snowflake query string.
|          PlainSelect select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **transpileAmazonRedshiftQuery** (select) → :ref:`String<java.lang.String>`
| Transpile amazon redshift query string.
|          PlainSelect select  | select the select
|          returns :ref:`String<java.lang.String>`  | the string



| **getExpressionTranspiler** () → :ref:`ExpressionTranspiler<com.manticore.transpiler.ExpressionTranspiler>`
| Gets expression transpiler.
|          returns :ref:`ExpressionTranspiler<com.manticore.transpiler.ExpressionTranspiler>`  | the expression transpiler



| **getResultBuilder** () → :ref:`StringBuilder<java.lang.StringBuilder>`
| Gets result builder.
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`  | the result builder



| **visit** (top)
|          Top top


| **visit** (tableFunction)
|          TableFunction tableFunction


