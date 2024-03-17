
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


..  _com.manticore.transpiler.ExpressionTranspiler:

=======================================================================
ExpressionTranspiler
=======================================================================

*extends:* ExpressionDeParser 

| **ExpressionTranspiler** ()


| **visit** (function)
|          Function function



..  _com.manticore.transpiler.JSQLTranspiler:

=======================================================================
JSQLTranspiler
=======================================================================

*extends:* SelectDeParser 

| **JSQLTranspiler** ()


| **getAbsoluteFile** (filename) → :ref:`File<java.io.File>`
|          :ref:`String<java.lang.String>` filename
|          returns :ref:`File<java.io.File>`



| **getAbsoluteFileName** (filename) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` filename
|          returns :ref:`String<java.lang.String>`



| *@SuppressWarnings*
| **main** (args)
|          :ref:`String<java.lang.String>` args


| **transpileQuery** (qryStr, dialect) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` qryStr
|          :ref:`Dialect<com.manticore.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns :ref:`String<java.lang.String>`



| **transpile** (sqlStr, inputDialect, outputDialect, outputFile)
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`Dialect<com.manticore.transpiler.JSQLTranspiler.Dialect>` inputDialect
|          :ref:`Dialect<com.manticore.transpiler.JSQLTranspiler.Dialect>` outputDialect
|          :ref:`File<java.io.File>` outputFile


| **transpile** (select) → :ref:`String<java.lang.String>`
|          PlainSelect select
|          returns :ref:`String<java.lang.String>`



| **transpileGoogleBigQuery** (select) → :ref:`String<java.lang.String>`
|          PlainSelect select
|          returns :ref:`String<java.lang.String>`



| **transpileDatabricksQuery** (select) → :ref:`String<java.lang.String>`
|          PlainSelect select
|          returns :ref:`String<java.lang.String>`



| **transpileSnowflakeQuery** (select) → :ref:`String<java.lang.String>`
|          PlainSelect select
|          returns :ref:`String<java.lang.String>`



| **transpileAmazonRedshiftQuery** (select) → :ref:`String<java.lang.String>`
|          PlainSelect select
|          returns :ref:`String<java.lang.String>`



| **getExpressionTranspiler** () → :ref:`ExpressionTranspiler<com.manticore.transpiler.ExpressionTranspiler>`
|          returns :ref:`ExpressionTranspiler<com.manticore.transpiler.ExpressionTranspiler>`



| **getResultBuilder** () → :ref:`StringBuilder<java.lang.StringBuilder>`
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (top)
|          Top top


