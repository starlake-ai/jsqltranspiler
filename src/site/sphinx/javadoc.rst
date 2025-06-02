
#######################################################################
API 1.1-SNAPSHOT
#######################################################################

Base Package: ai.starlake.jsqltranspiler


..  _ai.starlake.transpiler:
***********************************************************************
Base
***********************************************************************

..  _ai.starlake.transpiler.JSQLTranspiler.Dialect

=======================================================================
Dialect
=======================================================================

[GOOGLE_BIG_QUERY, DATABRICKS, SNOWFLAKE, AMAZON_REDSHIFT, ANY, DUCK_DB]

| The enum Dialect.


..  _ai.starlake.transpiler.CatalogNotFoundException:

=======================================================================
CatalogNotFoundException
=======================================================================

*extends:* :ref:`RuntimeException<java.lang.RuntimeException>` 

| **CatalogNotFoundException** (catalogName, cause)
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`Throwable<java.lang.Throwable>` cause


| **CatalogNotFoundException** (catalogName)
|          :ref:`String<java.lang.String>` catalogName


| **getCatalogName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`




..  _ai.starlake.transpiler.ColumnNotFoundException:

=======================================================================
ColumnNotFoundException
=======================================================================

*extends:* :ref:`RuntimeException<java.lang.RuntimeException>` 

| **ColumnNotFoundException** (columnName, tableNames, cause)
|          :ref:`String<java.lang.String>` columnName
|          :ref:`String><java.util.Collection<java.lang.String>>` tableNames
|          :ref:`Throwable<java.lang.Throwable>` cause


| **ColumnNotFoundException** (columnName, tableNames)
|          :ref:`String<java.lang.String>` columnName
|          :ref:`String><java.util.Collection<java.lang.String>>` tableNames


| **getColumnName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **getTableNames** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`




..  _ai.starlake.transpiler.JSQLColumResolver:

=======================================================================
JSQLColumResolver
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`JdbcResultSetMetaData><net.sf.jsqlparser.statement.select.SelectVisitor<ai.starlake.transpiler.schema.JdbcResultSetMetaData>>`, :ref:`JdbcResultSetMetaData><net.sf.jsqlparser.statement.select.FromItemVisitor<ai.starlake.transpiler.schema.JdbcResultSetMetaData>>` *provides:* :ref:`JSQLResolver<ai.starlake.transpiler.JSQLResolver>` 

| A class for resolving the actual columns returned by a SELECT statement. Depends on virtual or physical Database Metadata holding the schema and table information.

| **JSQLColumResolver** (metaData)
| Instantiates a new JSQLColumnResolver for the provided Database Metadata
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData


| **JSQLColumResolver** (currentCatalogName, currentSchemaName, metaDataDefinition)
| Instantiates a new JSQLColumnResolver for the provided simplified Metadata, presented as an Array of Tables and Column Names only.
|          :ref:`String<java.lang.String>` currentCatalogName
|          :ref:`String<java.lang.String>` currentSchemaName
|          :ref:`String[][]<java.lang.String[][]>` metaDataDefinition


| **JSQLColumResolver** (metaDataDefinition)
| Instantiates a new JSQLColumnResolver for the provided simplified Metadata with an empty CURRENT_SCHEMA and CURRENT_CATALOG
|          :ref:`String[][]<java.lang.String[][]>` metaDataDefinition


| *@SuppressWarnings*
| **getResultSetMetaData** (select) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`Select<net.sf.jsqlparser.statement.select.Select>` select
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@SuppressWarnings*
| **getResultSetMetaData** (sqlStr, metaData) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| **getResultSetMetaData** (sqlStr, metaDataDefinition, currentCatalogName, currentSchemaName) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`String[][]<java.lang.String[][]>` metaDataDefinition
|          :ref:`String<java.lang.String>` currentCatalogName
|          :ref:`String<java.lang.String>` currentSchemaName
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| **getResultSetMetaData** (sqlStr, metaDataDefinition) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`String[][]<java.lang.String[][]>` metaDataDefinition
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| **getResultSetMetaData** (sqlStr) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
| Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
|          :ref:`String<java.lang.String>` sqlStr
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| **getResolvedStatementText** (sqlStr) → :ref:`String<java.lang.String>`
| Gets the rewritten statement text with any AllColumns "*" or AllTableColumns "t.*" expression resolved into the actual columns
|          :ref:`String<java.lang.String>` sqlStr
|          returns :ref:`String<java.lang.String>`



| **getLineage** (treeBuilderClass, sqlStr, connection) → T
|          :ref:`TreeBuilder<T>><java.lang.Class<? extends ai.starlake.transpiler.schema.treebuilder.TreeBuilder<T>>>` treeBuilderClass
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`Connection<java.sql.Connection>` connection
|          returns T



| **getLineage** (treeBuilderClass, sqlStr, metaDataDefinition, currentCatalogName, currentSchemaName) → T
|          :ref:`TreeBuilder<T>><java.lang.Class<? extends ai.starlake.transpiler.schema.treebuilder.TreeBuilder<T>>>` treeBuilderClass
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`String[][]<java.lang.String[][]>` metaDataDefinition
|          :ref:`String<java.lang.String>` currentCatalogName
|          :ref:`String<java.lang.String>` currentSchemaName
|          returns T



| **getLineage** (treeBuilderClass, select) → T
|          :ref:`TreeBuilder<T>><java.lang.Class<? extends ai.starlake.transpiler.schema.treebuilder.TreeBuilder<T>>>` treeBuilderClass
|          :ref:`Select<net.sf.jsqlparser.statement.select.Select>` select
|          returns T



| **getLineage** (treeBuilderClass, sqlStr) → T
|          :ref:`TreeBuilder<T>><java.lang.Class<? extends ai.starlake.transpiler.schema.treebuilder.TreeBuilder<T>>>` treeBuilderClass
|          :ref:`String<java.lang.String>` sqlStr
|          returns T



| **getQualifiedTableName** (catalogName, schemaName, tableName) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          returns :ref:`String<java.lang.String>`



| **getQualifiedColumnName** (catalogName, schemaName, tableName, columName) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columName
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **visit** (table, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`Table<net.sf.jsqlparser.schema.Table>` table
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (tableName)
|          :ref:`Table<net.sf.jsqlparser.schema.Table>` tableName


| **visit** (parenthesedSelect, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`ParenthesedSelect<net.sf.jsqlparser.statement.select.ParenthesedSelect>` parenthesedSelect
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (parenthesedSelect, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`ParenthesedSelect<net.sf.jsqlparser.statement.select.ParenthesedSelect>` parenthesedSelect
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (parenthesedSelect)
|          :ref:`ParenthesedSelect<net.sf.jsqlparser.statement.select.ParenthesedSelect>` parenthesedSelect


| *@SuppressWarnings*
| **visit** (select, metaData) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` select
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (select, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` select
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (plainSelect)
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect


| *@Override*
| **visit** (fromQuery, s) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`FromQuery<net.sf.jsqlparser.statement.piped.FromQuery>` fromQuery
|          S s
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| **visit** (select) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`Select<net.sf.jsqlparser.statement.select.Select>` select
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (setOperationList, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`SetOperationList<net.sf.jsqlparser.statement.select.SetOperationList>` setOperationList
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (setOpList)
|          :ref:`SetOperationList<net.sf.jsqlparser.statement.select.SetOperationList>` setOpList


| *@Override*
| **visit** (withItem, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`WithItem<?><net.sf.jsqlparser.statement.select.WithItem<?>>` withItem
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (withItem)
|          :ref:`WithItem<?><net.sf.jsqlparser.statement.select.WithItem<?>>` withItem


| *@Override*
| **visit** (values, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`Values<net.sf.jsqlparser.statement.select.Values>` values
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (values)
|          :ref:`Values<net.sf.jsqlparser.statement.select.Values>` values


| *@Override*
| **visit** (lateralSubSelect, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`LateralSubSelect<net.sf.jsqlparser.statement.select.LateralSubSelect>` lateralSubSelect
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (lateralSubSelect)
|          :ref:`LateralSubSelect<net.sf.jsqlparser.statement.select.LateralSubSelect>` lateralSubSelect


| *@Override*
| **visit** (tableFunction, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`TableFunction<net.sf.jsqlparser.statement.select.TableFunction>` tableFunction
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (tableFunction)
|          :ref:`TableFunction<net.sf.jsqlparser.statement.select.TableFunction>` tableFunction


| *@Override*
| **visit** (parenthesedFromItem, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`ParenthesedFromItem<net.sf.jsqlparser.statement.select.ParenthesedFromItem>` parenthesedFromItem
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (parenthesedFromItem)
|          :ref:`ParenthesedFromItem<net.sf.jsqlparser.statement.select.ParenthesedFromItem>` parenthesedFromItem


| *@Override*
| **visit** (tableStatement, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`TableStatement<net.sf.jsqlparser.statement.select.TableStatement>` tableStatement
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (tableStatement)
|          :ref:`TableStatement<net.sf.jsqlparser.statement.select.TableStatement>` tableStatement


| **getErrorMode** () → :ref:`ErrorMode<ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode>`
| Gets the error mode.
|          returns :ref:`ErrorMode<ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode>`



| **setErrorMode** (errorMode) → :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>`
| Sets the error mode.
|          :ref:`ErrorMode<ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode>` errorMode
|          returns :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>`



| **addUnresolved** (unquotedQualifiedName)
| Add the name of an unresolvable column or table to the list.
|          :ref:`String<java.lang.String>` unquotedQualifiedName


| **getUnresolvedObjects** () → :ref:`String><java.util.Set<java.lang.String>>`
| Gets unresolved column or table names, not existing in the schema
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **isCommentFlag** () → boolean
|          returns boolean



| **setCommentFlag** (commentFlag)
|          boolean commentFlag



..  _ai.starlake.transpiler.JSQLDeleteTranspiler:

=======================================================================
JSQLDeleteTranspiler
=======================================================================

*extends:* :ref:`DeleteDeParser<net.sf.jsqlparser.util.deparser.DeleteDeParser>` 


                |          :ref:`ExpressionVisitor<net.sf.jsqlparser.expression.ExpressionVisitor>` expressionVisitor

                |          :ref:`StringBuilder<java.lang.StringBuilder>` builder

            
..  _ai.starlake.transpiler.JSQLExpressionColumnResolver:

=======================================================================
JSQLExpressionColumnResolver
=======================================================================

*extends:* :ref:`JdbcColumn>><net.sf.jsqlparser.expression.ExpressionVisitorAdapter<java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>>` *implements:* :ref:`JdbcColumn>><net.sf.jsqlparser.statement.select.SelectVisitor<java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>>` 

| **JSQLExpressionColumnResolver** (columResolver)
|          :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>` columResolver



                |          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData

                |          :ref:`Column<net.sf.jsqlparser.schema.Column>` column

                |          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          S context

                |          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`


                
            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          S context

                |          :ref:`Expression><java.util.Collection<net.sf.jsqlparser.expression.Expression>>` subExpressions

                |          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`


                
            | *@Override*
| **visit** (function, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`Function<net.sf.jsqlparser.expression.Function>` function
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (function, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`TranscodingFunction<net.sf.jsqlparser.expression.TranscodingFunction>` function
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (function, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`JsonAggregateFunction<net.sf.jsqlparser.expression.JsonAggregateFunction>` function
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (function, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`JsonFunction<net.sf.jsqlparser.expression.JsonFunction>` function
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (function, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`AnalyticExpression<net.sf.jsqlparser.expression.AnalyticExpression>` function
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@SuppressWarnings*,| *@Override*
| **visit** (allTableColumns, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`AllTableColumns<net.sf.jsqlparser.statement.select.AllTableColumns>` allTableColumns
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@SuppressWarnings*,| *@Override*
| **visit** (allColumns, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`AllColumns<net.sf.jsqlparser.statement.select.AllColumns>` allColumns
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (column, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`Column<net.sf.jsqlparser.schema.Column>` column
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (select, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`ParenthesedSelect<net.sf.jsqlparser.statement.select.ParenthesedSelect>` select
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (select, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`Select<net.sf.jsqlparser.statement.select.Select>` select
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (plainSelect, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (setOperationList, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`SetOperationList<net.sf.jsqlparser.statement.select.SetOperationList>` setOperationList
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (withItem, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`WithItem<?><net.sf.jsqlparser.statement.select.WithItem<?>>` withItem
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (values, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`Values<net.sf.jsqlparser.statement.select.Values>` values
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (lateralSubSelect, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`LateralSubSelect<net.sf.jsqlparser.statement.select.LateralSubSelect>` lateralSubSelect
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| *@Override*
| **visit** (tableStatement, context) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`TableStatement<net.sf.jsqlparser.statement.select.TableStatement>` tableStatement
|          S context
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **clearFunctions** ()


| **getFunctions** () → :ref:`Expression><java.util.List<net.sf.jsqlparser.expression.Expression>>`
|          returns :ref:`Expression><java.util.List<net.sf.jsqlparser.expression.Expression>>`




..  _ai.starlake.transpiler.JSQLExpressionTranspiler:

=======================================================================
JSQLExpressionTranspiler
=======================================================================

*extends:* :ref:`ExpressionDeParser<net.sf.jsqlparser.util.deparser.ExpressionDeParser>` *provides:* :ref:`BigQueryExpressionTranspiler<ai.starlake.transpiler.bigquery.BigQueryExpressionTranspiler>`, :ref:`RedshiftExpressionTranspiler<ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler>` 

| The type Expression transpiler.

| **JSQLExpressionTranspiler** (deParser, builder)
|          :ref:`SelectDeParser<net.sf.jsqlparser.util.deparser.SelectDeParser>` deParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder


| **isDatePart** (expression, dialect) → boolean
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns boolean




                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            
                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression

                |          returns boolean


            | **isDateTimePart** (expression, dialect) → boolean
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns boolean



| **toDateTimePart** (expression, dialect) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **hasTimeZoneInfo** (timestampStr) → boolean
|          :ref:`String<java.lang.String>` timestampStr
|          returns boolean



| **hasTimeZoneInfo** (timestamp) → boolean
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` timestamp
|          returns boolean



| **rewriteDateLiteral** (p, dateTimeType) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` p
|          :ref:`DateTime<net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime>` dateTimeType
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`




                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          int index

                |          returns boolean


            | *@SuppressWarnings*,| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Function<net.sf.jsqlparser.expression.Function>` function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (allColumns, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`AllColumns<net.sf.jsqlparser.statement.select.AllColumns>` allColumns
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@SuppressWarnings*,| *@Override*
| **visit** (function, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`AnalyticExpression<net.sf.jsqlparser.expression.AnalyticExpression>` function
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns void


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns void


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          :ref:`DateTime<net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime>` dateTimeType

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`StringValue<net.sf.jsqlparser.expression.StringValue>` formatStringValue

                |          returns :ref:`StringValue<net.sf.jsqlparser.expression.StringValue>`


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns void


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          :ref:`DateTime<net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime>` dateTimeType

                |          returns void


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          :ref:`DateTime<net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime>` dateTimeType

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns void


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          :ref:`DateTime<net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime>` dateTimeType

                |          returns void


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns void


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`Function<net.sf.jsqlparser.expression.Function>` function

                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            
                |          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` parameters

                |          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`


            | *@Override*
| **visit** (extractExpression, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`ExtractExpression<net.sf.jsqlparser.expression.ExtractExpression>` extractExpression
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (stringValue, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`StringValue<net.sf.jsqlparser.expression.StringValue>` stringValue
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (hexValue, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`HexValue<net.sf.jsqlparser.expression.HexValue>` hexValue
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **convertUnicode** (input) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` input
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **visit** (castExpression, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`CastExpression<net.sf.jsqlparser.expression.CastExpression>` castExpression
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (structType, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`StructType<net.sf.jsqlparser.expression.StructType>` structType
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (jsonFunction, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`JsonFunction<net.sf.jsqlparser.expression.JsonFunction>` jsonFunction
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **rewriteType** (colDataType) → :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`
|          :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>` colDataType
|          returns :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`



| **warning** (s)
|          :ref:`String<java.lang.String>` s


| **convertByteStringToUnicode** (byteString) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` byteString
|          returns :ref:`String<java.lang.String>`




                |          :ref:`Date<java.util.Date>` date

                |          :ref:`String<java.lang.String>` pattern

                |          :ref:`String<java.lang.String>` tzID

                |          returns :ref:`String<java.lang.String>`


            | **castDateTime** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`String<java.lang.String>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castDateTime** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| *@SuppressWarnings*
| **castDateTime** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`DateTimeLiteralExpression<net.sf.jsqlparser.expression.DateTimeLiteralExpression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| *@SuppressWarnings*
| **castDateTime** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`CastExpression<net.sf.jsqlparser.expression.CastExpression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| *@SuppressWarnings*
| **castDateTime** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`StringValue<net.sf.jsqlparser.expression.StringValue>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`String<java.lang.String>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (e1, e2, dialect) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` e1
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` e2
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`StringValue<net.sf.jsqlparser.expression.StringValue>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`CastExpression<net.sf.jsqlparser.expression.CastExpression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`IntervalExpression<net.sf.jsqlparser.expression.IntervalExpression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| *@Override*
| **visit** (expression, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`TimeKeyExpression<net.sf.jsqlparser.expression.TimeKeyExpression>` expression
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (likeExpression, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`LikeExpression<net.sf.jsqlparser.expression.operators.relational.LikeExpression>` likeExpression
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`TranscodingFunction<net.sf.jsqlparser.expression.TranscodingFunction>` function
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **isEmpty** (collection) → boolean
|          :ref:`Collection<?><java.util.Collection<?>>` collection
|          returns boolean



| **hasParameters** (function) → boolean
|          :ref:`Function<net.sf.jsqlparser.expression.Function>` function
|          returns boolean



| *@Override*
| **visit** (column, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Column<net.sf.jsqlparser.schema.Column>` column
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (expressionList, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`ExpressionList<?><net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>>` expressionList
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (e, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`JsonExpression<net.sf.jsqlparser.expression.JsonExpression>` e
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (arrayConstructor, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`ArrayConstructor<net.sf.jsqlparser.expression.ArrayConstructor>` arrayConstructor
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




..  _ai.starlake.transpiler.JSQLFromQueryTranspiler:

=======================================================================
JSQLFromQueryTranspiler
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`PlainSelect><net.sf.jsqlparser.statement.piped.FromQueryVisitor<net.sf.jsqlparser.statement.select.PlainSelect,net.sf.jsqlparser.statement.select.PlainSelect>>`, :ref:`PlainSelect><net.sf.jsqlparser.statement.piped.PipeOperatorVisitor<net.sf.jsqlparser.statement.select.PlainSelect,net.sf.jsqlparser.statement.select.PlainSelect>>` 

| **JSQLFromQueryTranspiler** ()


| *@Override*
| **visit** (fromQuery, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`FromQuery<net.sf.jsqlparser.statement.piped.FromQuery>` fromQuery
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (aggregatePipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`AggregatePipeOperator<net.sf.jsqlparser.statement.piped.AggregatePipeOperator>` aggregatePipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (asPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`AsPipeOperator<net.sf.jsqlparser.statement.piped.AsPipeOperator>` asPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (callPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`CallPipeOperator<net.sf.jsqlparser.statement.piped.CallPipeOperator>` callPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (dropPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`DropPipeOperator<net.sf.jsqlparser.statement.piped.DropPipeOperator>` dropPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (extendPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`ExtendPipeOperator<net.sf.jsqlparser.statement.piped.ExtendPipeOperator>` extendPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (joinPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`JoinPipeOperator<net.sf.jsqlparser.statement.piped.JoinPipeOperator>` joinPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (limitPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`LimitPipeOperator<net.sf.jsqlparser.statement.piped.LimitPipeOperator>` limitPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (orderByPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`OrderByPipeOperator<net.sf.jsqlparser.statement.piped.OrderByPipeOperator>` orderByPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (pivotPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`PivotPipeOperator<net.sf.jsqlparser.statement.piped.PivotPipeOperator>` pivotPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (renamePipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`RenamePipeOperator<net.sf.jsqlparser.statement.piped.RenamePipeOperator>` renamePipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (selectPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`SelectPipeOperator<net.sf.jsqlparser.statement.piped.SelectPipeOperator>` selectPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (setPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`SetPipeOperator<net.sf.jsqlparser.statement.piped.SetPipeOperator>` setPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`




                |          :ref:`AllColumns<net.sf.jsqlparser.statement.select.AllColumns>` allColumns

                |          :ref:`SetPipeOperator<net.sf.jsqlparser.statement.piped.SetPipeOperator>` setPipeOperator

                |          returns void


            
                |          :ref:`AllColumns<net.sf.jsqlparser.statement.select.AllColumns>` allColumns

                |          :ref:`DropPipeOperator<net.sf.jsqlparser.statement.piped.DropPipeOperator>` setPipeOperator

                |          returns void


            | *@Override*
| **visit** (tableSamplePipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`TableSamplePipeOperator<net.sf.jsqlparser.statement.piped.TableSamplePipeOperator>` tableSamplePipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (setOperationPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`SetOperationPipeOperator<net.sf.jsqlparser.statement.piped.SetOperationPipeOperator>` setOperationPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (unPivotPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`UnPivotPipeOperator<net.sf.jsqlparser.statement.piped.UnPivotPipeOperator>` unPivotPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (wherePipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`WherePipeOperator<net.sf.jsqlparser.statement.piped.WherePipeOperator>` wherePipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`



| *@Override*
| **visit** (windowPipeOperator, plainSelect) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`WindowPipeOperator<net.sf.jsqlparser.statement.piped.WindowPipeOperator>` windowPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`




..  _ai.starlake.transpiler.JSQLInsertTranspiler:

=======================================================================
JSQLInsertTranspiler
=======================================================================

*extends:* :ref:`InsertDeParser<net.sf.jsqlparser.util.deparser.InsertDeParser>` 


                |          :ref:`ExpressionVisitor<net.sf.jsqlparser.expression.ExpressionVisitor>` expressionVisitor

                |          :ref:`SelectVisitor<net.sf.jsqlparser.statement.select.SelectVisitor>` selectVisitor

                |          :ref:`StringBuilder<java.lang.StringBuilder>` builder

            
..  _ai.starlake.transpiler.JSQLMergeTranspiler:

=======================================================================
JSQLMergeTranspiler
=======================================================================

*extends:* :ref:`MergeDeParser<net.sf.jsqlparser.util.deparser.MergeDeParser>` 

| **JSQLMergeTranspiler** (expressionDeParser, selectDeParser, buffer)
|          :ref:`ExpressionDeParser<net.sf.jsqlparser.util.deparser.ExpressionDeParser>` expressionDeParser
|          :ref:`SelectDeParser<net.sf.jsqlparser.util.deparser.SelectDeParser>` selectDeParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer



..  _ai.starlake.transpiler.JSQLResolver:

=======================================================================
JSQLResolver
=======================================================================

*extends:* :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>` 

| **JSQLResolver** (metaData)
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData


| **JSQLResolver** (currentCatalogName, currentSchemaName, metaDataDefinition)
|          :ref:`String<java.lang.String>` currentCatalogName
|          :ref:`String<java.lang.String>` currentSchemaName
|          :ref:`String[][]<java.lang.String[][]>` metaDataDefinition


| **JSQLResolver** (metaDataDefinition)
|          :ref:`String[][]<java.lang.String[][]>` metaDataDefinition


| *@Override*
| **visit** (withItem, context) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`WithItem<?><net.sf.jsqlparser.statement.select.WithItem<?>>` withItem
|          S context
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| *@Override*
| **visit** (select, metaData) → :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` select
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData
|          returns :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>`



| **getWhereColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getFlattendedWhereColumns** () → :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`



| **setWhereColumns** (whereColumns) → :ref:`JSQLResolver<ai.starlake.transpiler.JSQLResolver>`
|          :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>` whereColumns
|          returns :ref:`JSQLResolver<ai.starlake.transpiler.JSQLResolver>`



| **getWithColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getSelectColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getDeleteColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getUpdateColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getInsertColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getGroupByColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getHavingColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getJoinedOnColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getFlattenedJoinedOnColumns** () → :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getOrderByColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getFunctions** () → :ref:`Expression><java.util.List<net.sf.jsqlparser.expression.Expression>>`
|          returns :ref:`Expression><java.util.List<net.sf.jsqlparser.expression.Expression>>`



| **getFlatFunctionNames** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **flatten** (columns) → :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>` columns
|          returns :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`



| **resolve** (st) → :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`
| Resolves all the columns used at any clause of a SELECT, INSERT, UPDATE or DELETE statement for an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA.
|          :ref:`Statement<net.sf.jsqlparser.statement.Statement>` st
|          returns :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`



| **resolve** (sqlStr) → :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`
| Resolves all the columns used at any clause of a SELECT statement for an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA.
|          :ref:`String<java.lang.String>` sqlStr
|          returns :ref:`JdbcColumn><java.util.Set<ai.starlake.transpiler.schema.JdbcColumn>>`




..  _ai.starlake.transpiler.JSQLSelectTranspiler:

=======================================================================
JSQLSelectTranspiler
=======================================================================

*extends:* :ref:`SelectDeParser<net.sf.jsqlparser.util.deparser.SelectDeParser>` *provides:* :ref:`BigQuerySelectTranspiler<ai.starlake.transpiler.bigquery.BigQuerySelectTranspiler>`, :ref:`DatabricksSelectTranspiler<ai.starlake.transpiler.databricks.DatabricksSelectTranspiler>`, :ref:`RedshiftSelectTranspiler<ai.starlake.transpiler.redshift.RedshiftSelectTranspiler>`, :ref:`SnowflakeSelectTranspiler<ai.starlake.transpiler.snowflake.SnowflakeSelectTranspiler>` 


                Instantiates a new transpiler.
                |          :ref:`JSQLExpressionTranspiler<ai.starlake.transpiler.JSQLExpressionTranspiler>` expressionTranspiler

                |          :ref:`StringBuilder<java.lang.StringBuilder>` resultBuilder

            | **JSQLSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`ExpressionDeParser><java.lang.Class<? extends net.sf.jsqlparser.util.deparser.ExpressionDeParser>>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder


| **getResultBuilder** () → :ref:`StringBuilder<java.lang.StringBuilder>`
| Gets result builder.
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (top)
|          :ref:`Top<net.sf.jsqlparser.statement.select.Top>` top


| *@Override*
| **visit** (tableFunction, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`TableFunction<net.sf.jsqlparser.statement.select.TableFunction>` tableFunction
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (plainSelect, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` plainSelect
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@SuppressWarnings*
| **visit** (select, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`ParenthesedSelect<net.sf.jsqlparser.statement.select.ParenthesedSelect>` select
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (table, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Table<net.sf.jsqlparser.schema.Table>` table
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (selectItem, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`SelectItem<?><net.sf.jsqlparser.statement.select.SelectItem<?>>` selectItem
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (fromQuery, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`FromQuery<net.sf.jsqlparser.statement.piped.FromQuery>` fromQuery
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (selectPipeOperator, select) → :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`
|          :ref:`SelectPipeOperator<net.sf.jsqlparser.statement.piped.SelectPipeOperator>` selectPipeOperator
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` select
|          returns :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>`




..  _ai.starlake.transpiler.JSQLTranspiler:

=======================================================================
JSQLTranspiler
=======================================================================

*extends:* :ref:`StatementDeParser<net.sf.jsqlparser.util.deparser.StatementDeParser>` *provides:* :ref:`BigQueryTranspiler<ai.starlake.transpiler.bigquery.BigQueryTranspiler>`, :ref:`DatabricksTranspiler<ai.starlake.transpiler.databricks.DatabricksTranspiler>`, :ref:`RedshiftTranspiler<ai.starlake.transpiler.redshift.RedshiftTranspiler>`, :ref:`SnowflakeTranspiler<ai.starlake.transpiler.snowflake.SnowflakeTranspiler>` 

| The type JSQLTranspiler.


                |          :ref:`JSQLSelectTranspiler><java.lang.Class<? extends ai.starlake.transpiler.JSQLSelectTranspiler>>` selectTranspilerClass

                |          :ref:`JSQLExpressionTranspiler><java.lang.Class<? extends ai.starlake.transpiler.JSQLExpressionTranspiler>>` expressionTranspilerClass

                
                
                
                
            | **JSQLTranspiler** (parameters)
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters


| **JSQLTranspiler** ()


| *@SuppressWarnings*
| **transpileQuery** (qryStr, dialect, parameters, executorService, consumer) → :ref:`String<java.lang.String>`
| Transpile a query string in the defined dialect into DuckDB compatible SQL.
|          :ref:`String<java.lang.String>` qryStr
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          :ref:`ExecutorService<java.util.concurrent.ExecutorService>` executorService
|          :ref:`CCJSqlParser><java.util.function.Consumer<net.sf.jsqlparser.parser.CCJSqlParser>>` consumer
|          returns :ref:`String<java.lang.String>`



| **transpileQuery** (qryStr, dialect, parameters) → :ref:`String<java.lang.String>`
| Transpile a query string in the defined dialect into DuckDB compatible SQL.
|          :ref:`String<java.lang.String>` qryStr
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          returns :ref:`String<java.lang.String>`



| **transpileQuery** (qryStr, dialect) → :ref:`String<java.lang.String>`
| Transpile a query string in the defined dialect into DuckDB compatible SQL.
|          :ref:`String<java.lang.String>` qryStr
|          :ref:`Dialect<ai.starlake.transpiler.JSQLTranspiler.Dialect>` dialect
|          returns :ref:`String<java.lang.String>`



| *@SuppressWarnings*
| **transpile** (sqlStr, parameters, outputFile, executorService, consumer)
| Transpile a query string from a file or STDIN and write the transformed query string into a file or STDOUT. Using the provided Executor Service for observing the parser.
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          :ref:`File<java.io.File>` outputFile
|          :ref:`ExecutorService<java.util.concurrent.ExecutorService>` executorService
|          :ref:`CCJSqlParser><java.util.function.Consumer<net.sf.jsqlparser.parser.CCJSqlParser>>` consumer


| **transpile** (sqlStr, parameters, outputFile) → boolean
| Transpile a query string from a file or STDIN and write the transformed query string into a file or STDOUT.
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          :ref:`File<java.io.File>` outputFile
|          returns boolean



| **transpile** (sqlStr, outputFile) → boolean
| Transpile a query string from a file or STDIN and write the transformed query string into a file or STDOUT.
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`File<java.io.File>` outputFile
|          returns boolean



| **readResource** (url) → :ref:`String<java.lang.String>`
| Read the text content from a resource file.
|          :ref:`URL<java.net.URL>` url
|          returns :ref:`String<java.lang.String>`



| **readResource** (clazz, suffix) → :ref:`String<java.lang.String>`
| Read the text content from a resource file relative to a particular class' suffix
|          :ref:`Class<?><java.lang.Class<?>>` clazz
|          :ref:`String<java.lang.String>` suffix
|          returns :ref:`String<java.lang.String>`



| **getMacros** (executorService, consumer) → :ref:`String><java.util.Collection<java.lang.String>>`
| Get the Macro `CREATE FUNCTION` statements as a list of text, using the provided ExecutorService to monitor the parser
|          :ref:`ExecutorService<java.util.concurrent.ExecutorService>` executorService
|          :ref:`CCJSqlParser><java.util.function.Consumer<net.sf.jsqlparser.parser.CCJSqlParser>>` consumer
|          returns :ref:`String><java.util.Collection<java.lang.String>>`



| **getMacros** () → :ref:`String><java.util.Collection<java.lang.String>>`
| Get the Macro `CREATE FUNCTION` statements as a list of text
|          returns :ref:`String><java.util.Collection<java.lang.String>>`



| **getMacroArray** () → :ref:`String[]<java.lang.String[]>`
| Get the Macro `CREATE FUNCTION` statements as an Array of text
|          returns :ref:`String[]<java.lang.String[]>`



| **createMacros** (conn)
| Create the Macros in a given JDBC connection
|          :ref:`Connection<java.sql.Connection>` conn


| **transpile** (statement, parameters) → :ref:`String<java.lang.String>`
| Rewrite a given SQL Statement into a text representation.
|          :ref:`Statement<net.sf.jsqlparser.statement.Statement>` statement
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          returns :ref:`String<java.lang.String>`



| **transpileBigQuery** (statement, parameters) → :ref:`String<java.lang.String>`
| Rewrite a given BigQuery SQL Statement into a text representation.
|          :ref:`Statement<net.sf.jsqlparser.statement.Statement>` statement
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          returns :ref:`String<java.lang.String>`



| **transpileDatabricks** (statement, parameters) → :ref:`String<java.lang.String>`
| Rewrite a given DataBricks SQL Statement into a text representation.
|          :ref:`Statement<net.sf.jsqlparser.statement.Statement>` statement
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          returns :ref:`String<java.lang.String>`



| **transpileSnowflake** (statement, parameters) → :ref:`String<java.lang.String>`
| Rewrite a given Snowflake SQL Statement into a text representation.
|          :ref:`Statement<net.sf.jsqlparser.statement.Statement>` statement
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          returns :ref:`String<java.lang.String>`



| **transpileAmazonRedshift** (statement, parameters) → :ref:`String<java.lang.String>`
| Rewrite a given Redshift SQL Statement into a text representation.
|          :ref:`Statement<net.sf.jsqlparser.statement.Statement>` statement
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters
|          returns :ref:`String<java.lang.String>`



| **unpipe** (sqlStr, executorService, consumer) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`ExecutorService<java.util.concurrent.ExecutorService>` executorService
|          :ref:`CCJSqlParser><java.util.function.Consumer<net.sf.jsqlparser.parser.CCJSqlParser>>` consumer
|          returns :ref:`String<java.lang.String>`



| **unpipe** (sqlStr, consumer) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`CCJSqlParser><java.util.function.Consumer<net.sf.jsqlparser.parser.CCJSqlParser>>` consumer
|          returns :ref:`String<java.lang.String>`



| **unpipe** (sqlStr) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sqlStr
|          returns :ref:`String<java.lang.String>`



| **visit** (select, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Select<net.sf.jsqlparser.statement.select.Select>` select
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (insert, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Insert<net.sf.jsqlparser.statement.insert.Insert>` insert
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (update, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Update<net.sf.jsqlparser.statement.update.Update>` update
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (delete, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Delete<net.sf.jsqlparser.statement.delete.Delete>` delete
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **visit** (merge, context) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Merge<net.sf.jsqlparser.statement.merge.Merge>` merge
|          S context
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




..  _ai.starlake.transpiler.JSQLUpdateTranspiler:

=======================================================================
JSQLUpdateTranspiler
=======================================================================

*extends:* :ref:`UpdateDeParser<net.sf.jsqlparser.util.deparser.UpdateDeParser>` 


                |          :ref:`ExpressionVisitor<net.sf.jsqlparser.expression.ExpressionVisitor>` expressionVisitor

                |          :ref:`StringBuilder<java.lang.StringBuilder>` builder

            
..  _ai.starlake.transpiler.SchemaNotFoundException:

=======================================================================
SchemaNotFoundException
=======================================================================

*extends:* :ref:`RuntimeException<java.lang.RuntimeException>` 

| **SchemaNotFoundException** (SchemaName, cause)
|          :ref:`String<java.lang.String>` SchemaName
|          :ref:`Throwable<java.lang.Throwable>` cause


| **SchemaNotFoundException** (SchemaName)
|          :ref:`String<java.lang.String>` SchemaName


| **getSchemaName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`




..  _ai.starlake.transpiler.TableNotDeclaredException:

=======================================================================
TableNotDeclaredException
=======================================================================

*extends:* :ref:`RuntimeException<java.lang.RuntimeException>` 

| **TableNotDeclaredException** (tableName, tableNames, cause)
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String><java.util.Collection<java.lang.String>>` tableNames
|          :ref:`Throwable<java.lang.Throwable>` cause


| **TableNotDeclaredException** (tableName, tableNames)
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String><java.util.Collection<java.lang.String>>` tableNames


| **getTableName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`




..  _ai.starlake.transpiler.TableNotFoundException:

=======================================================================
TableNotFoundException
=======================================================================

*extends:* :ref:`RuntimeException<java.lang.RuntimeException>` 

| **TableNotFoundException** (tableName, schemaNames, cause)
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String><java.util.Collection<java.lang.String>>` schemaNames
|          :ref:`Throwable<java.lang.Throwable>` cause


| **TableNotFoundException** (tableName, schemaNames)
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String><java.util.Collection<java.lang.String>>` schemaNames


| **getTableName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`




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
|          :ref:`SelectDeParser<net.sf.jsqlparser.util.deparser.SelectDeParser>` selectDeParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer



..  _ai.starlake.transpiler.bigquery.BigQuerySelectTranspiler:

=======================================================================
BigQuerySelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **BigQuerySelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`ExpressionDeParser><java.lang.Class<? extends net.sf.jsqlparser.util.deparser.ExpressionDeParser>>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder


| *@Override*
| **visit** (select, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`PlainSelect<net.sf.jsqlparser.statement.select.PlainSelect>` select
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




..  _ai.starlake.transpiler.bigquery.BigQueryTranspiler:

=======================================================================
BigQueryTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **BigQueryTranspiler** (parameters)
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters



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
|          :ref:`SelectDeParser<net.sf.jsqlparser.util.deparser.SelectDeParser>` selectDeParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| **toDateTimePart** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (e1, e2) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` e1
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` e2
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| *@Override*,| *@SuppressWarnings*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Function<net.sf.jsqlparser.expression.Function>` function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`AnalyticExpression<net.sf.jsqlparser.expression.AnalyticExpression>` function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (column, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Column<net.sf.jsqlparser.schema.Column>` column
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **rewriteType** (colDataType) → :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`
|          :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>` colDataType
|          returns :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`




..  _ai.starlake.transpiler.databricks.DatabricksSelectTranspiler:

=======================================================================
DatabricksSelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **DatabricksSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`ExpressionDeParser><java.lang.Class<? extends net.sf.jsqlparser.util.deparser.ExpressionDeParser>>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder



..  _ai.starlake.transpiler.databricks.DatabricksTranspiler:

=======================================================================
DatabricksTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **DatabricksTranspiler** (parameters)
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters



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
|          :ref:`SelectDeParser<net.sf.jsqlparser.util.deparser.SelectDeParser>` deParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| *@Override*,| *@SuppressWarnings*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Function<net.sf.jsqlparser.expression.Function>` function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`AnalyticExpression<net.sf.jsqlparser.expression.AnalyticExpression>` function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expr1

                |          :ref:`String<java.lang.String>` type1

                |          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expr2

                |          :ref:`String<java.lang.String>` type2

                |          returns :ref:`CaseExpression<net.sf.jsqlparser.expression.CaseExpression>`


            | **visit** (column, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Column<net.sf.jsqlparser.schema.Column>` column
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **toFormat** (s) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` s
|          returns :ref:`String<java.lang.String>`



| **rewriteType** (colDataType) → :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`
|          :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>` colDataType
|          returns :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`




..  _ai.starlake.transpiler.redshift.RedshiftSelectTranspiler:

=======================================================================
RedshiftSelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **RedshiftSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`ExpressionDeParser><java.lang.Class<? extends net.sf.jsqlparser.util.deparser.ExpressionDeParser>>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder



..  _ai.starlake.transpiler.redshift.RedshiftTranspiler:

=======================================================================
RedshiftTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **RedshiftTranspiler** (parameters)
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters



..  _ai.starlake.transpiler.schema:
***********************************************************************
ma
***********************************************************************

..  _ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode

=======================================================================
ErrorMode
=======================================================================

[STRICT, LENIENT, IGNORE]


..  _ai.starlake.transpiler.schema.JdbcUtils.DatabaseSpecific

=======================================================================
DatabaseSpecific
=======================================================================

[ORACLE, POSTGRESQL, MSSQL, MYSQL, SNOWFLAKE, DUCKCB, OTHER]

| Used for detecting RDBMS type and DB specific handling


..  _ai.starlake.transpiler.schema.CaseInsensitiveConcurrentSet:

=======================================================================
CaseInsensitiveConcurrentSet
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **newSet** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **add** (s) → boolean
|          :ref:`String<java.lang.String>` s
|          returns boolean



| **contains** (s) → boolean
|          :ref:`String<java.lang.String>` s
|          returns boolean



| **remove** (s) → boolean
|          :ref:`String<java.lang.String>` s
|          returns boolean




..  _ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap&lt;V&gt;:

=======================================================================
CaseInsensitiveLinkedHashMap
=======================================================================

*extends:* :ref:`String,V><java.util.LinkedHashMap<java.lang.String,V>>` 

| A Case insensitive linked hash map preserving the original spelling of the keys. It can be used for looking up a database's schemas, tables, columns, indices and constraints.

| **CaseInsensitiveLinkedHashMap** ()


| **unquote** (quotedIdentifier) → :ref:`String<java.lang.String>`
| Removes leading and trailing quotes from a SQL quoted identifier
|          :ref:`String<java.lang.String>` quotedIdentifier
|          returns :ref:`String<java.lang.String>`



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
| **entrySet** () → :ref:`String,V>><java.util.Set<java.util.Map.Entry<java.lang.String,V>>>`
|          returns :ref:`String,V>><java.util.Set<java.util.Map.Entry<java.lang.String,V>>>`



| *@Override*
| **keySet** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`




..  _ai.starlake.transpiler.schema.JdbcCatalog:

=======================================================================
JdbcCatalog
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`JdbcCatalog><java.lang.Comparable<ai.starlake.transpiler.schema.JdbcCatalog>>` 

| **JdbcCatalog** (tableCatalog, catalogSeparator)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` catalogSeparator


| **JdbcCatalog** ()


| **getCatalogs** (metaData) → :ref:`JdbcCatalog><java.util.Collection<ai.starlake.transpiler.schema.JdbcCatalog>>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`JdbcCatalog><java.util.Collection<ai.starlake.transpiler.schema.JdbcCatalog>>`



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
|          :ref:`JdbcSchema><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcSchema,? extends ai.starlake.transpiler.schema.JdbcSchema>>` remappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **putAll** (m)
|          :ref:`JdbcSchema><java.util.Map<? extends java.lang.String,? extends ai.starlake.transpiler.schema.JdbcSchema>>` m


| **values** () → :ref:`JdbcSchema><java.util.Collection<ai.starlake.transpiler.schema.JdbcSchema>>`
|          returns :ref:`JdbcSchema><java.util.Collection<ai.starlake.transpiler.schema.JdbcSchema>>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` oldValue
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`JdbcSchema><java.util.function.BiConsumer<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcSchema>>` action


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
|          :ref:`JdbcSchema><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcSchema,? extends ai.starlake.transpiler.schema.JdbcSchema>>` remappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **replaceAll** (function)
|          :ref:`JdbcSchema><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcSchema,? extends ai.starlake.transpiler.schema.JdbcSchema>>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema><java.util.function.Function<? super java.lang.String,? extends ai.starlake.transpiler.schema.JdbcSchema>>` mappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **putIfAbsent** (value) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` value
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` value
|          :ref:`JdbcSchema><java.util.function.BiFunction<? super ai.starlake.transpiler.schema.JdbcSchema,? super ai.starlake.transpiler.schema.JdbcSchema,? extends ai.starlake.transpiler.schema.JdbcSchema>>` remappingFunction
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **containsKey** (key) → boolean
|          :ref:`String<java.lang.String>` key
|          returns boolean



| **remove** (key) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **clear** ()


| **entrySet** () → :ref:`JdbcSchema>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcSchema>>>`
|          returns :ref:`JdbcSchema>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcSchema>>>`



| **keySet** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **getTableCatalog** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **setTableCatalog** (tableCatalog)
|          :ref:`String<java.lang.String>` tableCatalog


| **getCatalogSeparator** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **setCatalogSeparator** (catalogSeparator)
|          :ref:`String<java.lang.String>` catalogSeparator


| **getSchemas** () → :ref:`JdbcSchema><java.util.List<ai.starlake.transpiler.schema.JdbcSchema>>`
|          returns :ref:`JdbcSchema><java.util.List<ai.starlake.transpiler.schema.JdbcSchema>>`



| **setSchemas** (schemas)
|          :ref:`JdbcSchema><java.util.List<ai.starlake.transpiler.schema.JdbcSchema>>` schemas



..  _ai.starlake.transpiler.schema.JdbcColumn:

=======================================================================
JdbcColumn
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`JdbcColumn><java.lang.Comparable<ai.starlake.transpiler.schema.JdbcColumn>>` 

| **JdbcColumn** (tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, numericPrecisionRadix, nullable, remarks, columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog, scopeSchema, scopeTable, scopeColumn, sourceDataType, isAutomaticIncrement, isGeneratedColumn, expression)
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
|          :ref:`String<java.lang.String>` scopeColumn
|          :ref:`Short<java.lang.Short>` sourceDataType
|          :ref:`String<java.lang.String>` isAutomaticIncrement
|          :ref:`String<java.lang.String>` isGeneratedColumn
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression


| **JdbcColumn** (tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, nullable, remarks, expression)
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
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression


| **JdbcColumn** (columnName, dataType, typeName, columnSize, decimalDigits, nullable, remarks, expression)
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Integer<java.lang.Integer>` dataType
|          :ref:`String<java.lang.String>` typeName
|          :ref:`Integer<java.lang.Integer>` columnSize
|          :ref:`Integer<java.lang.Integer>` decimalDigits
|          :ref:`Integer<java.lang.Integer>` nullable
|          :ref:`String<java.lang.String>` remarks
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression


| **JdbcColumn** (tableCatalog, tableSchema, tableName, columnName, expression)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression


| **JdbcColumn** (columnName, expression)
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression


| **JdbcColumn** (columnName)
|          :ref:`String<java.lang.String>` columnName


| **JdbcColumn** (tableName, columnName, expression)
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression


| **JdbcColumn** (tableName, columnName)
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName


| *@Override*,| *@SuppressWarnings*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*,| *@SuppressWarnings*
| **compareTo** (o) → int
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` o
|          returns int



| *@SuppressWarnings*,| *@Override*
| **toString** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*,| *@SuppressWarnings*
| **hashCode** () → int
|          returns int



| **getParent** () → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **getChildren** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **add** (children) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>` children
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **add** (children) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`JdbcColumn[]<ai.starlake.transpiler.schema.JdbcColumn[]>` children
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **getExpression** () → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **setExpression** (expression) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`




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

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`JdbcIndexColumn><java.lang.Comparable<ai.starlake.transpiler.schema.JdbcIndexColumn>>` 

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




..  _ai.starlake.transpiler.schema.JdbcJSONSerializer:

=======================================================================
JdbcJSONSerializer
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **JdbcJSONSerializer** ()


| **toJson** (metadata, out, indent)
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metadata
|          :ref:`Writer<java.io.Writer>` out
|          int indent


| **toJson** (metadata, out)
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metadata
|          :ref:`Writer<java.io.Writer>` out


| **toJson** (metadata) → :ref:`JSONObject<org.json.JSONObject>`
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metadata
|          returns :ref:`JSONObject<org.json.JSONObject>`



| **fromJson** (in) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`Reader<java.io.Reader>` in
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`




                |          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` catalog

                |          returns :ref:`JSONObject<org.json.JSONObject>`


            
                |          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` schema

                |          returns :ref:`JSONObject<org.json.JSONObject>`


            
                |          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` table

                |          returns :ref:`JSONObject<org.json.JSONObject>`


            
                |          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` column

                |          returns :ref:`JSONObject<org.json.JSONObject>`


            
                |          :ref:`JSONObject<org.json.JSONObject>` json

                |          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`


            
                |          :ref:`JSONObject<org.json.JSONObject>` json

                |          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`


            
                |          :ref:`JSONObject<org.json.JSONObject>` json

                |          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`


            
                |          :ref:`JSONObject<org.json.JSONObject>` json

                |          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`


            
..  _ai.starlake.transpiler.schema.JdbcMetaData:

=======================================================================
JdbcMetaData
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` 

| The type Jdbc metadata.

| **JdbcMetaData** (schemaDefinition)
| Instantiates a new virtual JDBC MetaData object with an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA and creates tables from the provided definition.
|          :ref:`String[][]<java.lang.String[][]>` schemaDefinition


| **JdbcMetaData** (catalogName, schemaName, schemaDefinition)
| Instantiates a new virtual JDBC MetaData object for the given CURRENT_CATALOG and CURRENT_SCHEMA and creates tables from the provided definition.
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String[][]<java.lang.String[][]>` schemaDefinition


| **JdbcMetaData** (catalogName, schemaName)
| Instantiates a new virtual JDBC MetaData object with a given CURRENT_CATALOG and CURRENT_SCHEMA.
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName


| **JdbcMetaData** ()
| Instantiates a new virtual JDBC MetaData object with an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA.


| **JdbcMetaData** (con)
| Derives JDBC MetaData object from a physical database connection.
|          :ref:`Connection<java.sql.Connection>` con


| **getTypeName** (sqlType) → :ref:`String<java.lang.String>`
|          int sqlType
|          returns :ref:`String<java.lang.String>`



| **put** (jdbcCatalog) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` jdbcCatalog
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **getCatalogMap** () → :ref:`JdbcCatalog><java.util.Map<java.lang.String,ai.starlake.transpiler.schema.JdbcCatalog>>`
|          returns :ref:`JdbcCatalog><java.util.Map<java.lang.String,ai.starlake.transpiler.schema.JdbcCatalog>>`



| **put** (jdbcSchema) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>` jdbcSchema
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **put** (jdbcTable) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` jdbcTable
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **put** (rsMetaData, name, errorMessage) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>` rsMetaData
|          :ref:`String<java.lang.String>` name
|          :ref:`String<java.lang.String>` errorMessage
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| *@SuppressWarnings*
| **getTableColumns** (catalogName, schemaName, tableName, columnName) → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



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
|          :ref:`JdbcCatalog><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcCatalog,? extends ai.starlake.transpiler.schema.JdbcCatalog>>` remappingFunction
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **putAll** (m)
|          :ref:`JdbcCatalog><java.util.Map<? extends java.lang.String,? extends ai.starlake.transpiler.schema.JdbcCatalog>>` m


| **values** () → :ref:`JdbcCatalog><java.util.Collection<ai.starlake.transpiler.schema.JdbcCatalog>>`
|          returns :ref:`JdbcCatalog><java.util.Collection<ai.starlake.transpiler.schema.JdbcCatalog>>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` oldValue
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`JdbcCatalog><java.util.function.BiConsumer<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcCatalog>>` action


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
|          :ref:`JdbcCatalog><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcCatalog,? extends ai.starlake.transpiler.schema.JdbcCatalog>>` remappingFunction
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **replaceAll** (function)
|          :ref:`JdbcCatalog><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcCatalog,? extends ai.starlake.transpiler.schema.JdbcCatalog>>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog><java.util.function.Function<? super java.lang.String,? extends ai.starlake.transpiler.schema.JdbcCatalog>>` mappingFunction
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **putIfAbsent** (value) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          returns :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcCatalog<ai.starlake.transpiler.schema.JdbcCatalog>` value
|          :ref:`JdbcCatalog><java.util.function.BiFunction<? super ai.starlake.transpiler.schema.JdbcCatalog,? super ai.starlake.transpiler.schema.JdbcCatalog,? extends ai.starlake.transpiler.schema.JdbcCatalog>>` remappingFunction
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


| **entrySet** () → :ref:`JdbcCatalog>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcCatalog>>>`
|          returns :ref:`JdbcCatalog>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcCatalog>>>`



| **keySet** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **addSchema** (schemaName) → :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`
|          :ref:`String<java.lang.String>` schemaName
|          returns :ref:`JdbcSchema<ai.starlake.transpiler.schema.JdbcSchema>`



| **addTable** (catalogName, schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (catalogName, schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` catalogName
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn[]<ai.starlake.transpiler.schema.JdbcColumn[]>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (schemaName, tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn[]<ai.starlake.transpiler.schema.JdbcColumn[]>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (tableName, columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn[]<ai.starlake.transpiler.schema.JdbcColumn[]>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addTable** (tableName, columnNames) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String[]<java.lang.String[]>` columnNames
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addColumns** (tableName, columns) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>` columns
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **addColumns** (tableName, columns) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` tableName
|          :ref:`JdbcColumn[]<ai.starlake.transpiler.schema.JdbcColumn[]>` columns
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
|          :ref:`String[]<java.lang.String[]>` types
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
|          int[] types
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
|          :ref:`Class<T><java.lang.Class<T>>` iface
|          returns T



| *@Override*
| **isWrapperFor** (iface) → boolean
|          :ref:`Class<?><java.lang.Class<?>>` iface
|          returns boolean



| **getFromTables** () → :ref:`Table><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Table>>`
|          returns :ref:`Table><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Table>>`



| **addFromTables** (fromTables) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`Table><java.util.Collection<net.sf.jsqlparser.schema.Table>>` fromTables
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addFromTables** (fromTables) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`Table[]<net.sf.jsqlparser.schema.Table[]>` fromTables
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **getLeftUsingJoinedColumns** () → :ref:`Column><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Column>>`
|          returns :ref:`Column><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Column>>`



| **addLeftUsingJoinColumns** (columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`Column><java.util.Collection<net.sf.jsqlparser.schema.Column>>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **getRightUsingJoinedColumns** () → :ref:`Column><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Column>>`
|          returns :ref:`Column><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Column>>`



| **addRightUsingJoinColumns** (columns) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`Column><java.util.Collection<net.sf.jsqlparser.schema.Column>>` columns
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **getNaturalJoinedTables** () → :ref:`Table><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Table>>`
|          returns :ref:`Table><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Table>>`



| **addNaturalJoinedTable** (t) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`Table<net.sf.jsqlparser.schema.Table>` t
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **copyOf** (metaData, fromTables) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData
|          :ref:`Table><ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap<net.sf.jsqlparser.schema.Table>>` fromTables
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **copyOf** (metaData) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>` metaData
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`




                |          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` table

                |          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`


            | **getCurrentCatalogName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **getCurrentSchemaName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **setCatalogSeparator** (catalogSeparator) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
|          :ref:`String<java.lang.String>` catalogSeparator
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **addUnresolved** (unquotedQualifiedName)
| Add the name of an unresolvable column or table to the list.
|          :ref:`String<java.lang.String>` unquotedQualifiedName


| **getUnresolvedObjects** () → :ref:`String><java.util.Set<java.lang.String>>`
| Gets unresolved column or table names, not existing in the schema
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **getErrorMode** () → :ref:`ErrorMode<ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode>`
| Gets the error mode.
|          returns :ref:`ErrorMode<ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode>`



| **setErrorMode** (errorMode) → :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`
| Sets the error mode.
|          :ref:`ErrorMode<ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode>` errorMode
|          returns :ref:`JdbcMetaData<ai.starlake.transpiler.schema.JdbcMetaData>`



| **getDatabaseType** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **setDatabaseType** (databaseType)
|          :ref:`String<java.lang.String>` databaseType


| **getCatalogsList** () → :ref:`JdbcCatalog><java.util.List<ai.starlake.transpiler.schema.JdbcCatalog>>`
|          returns :ref:`JdbcCatalog><java.util.List<ai.starlake.transpiler.schema.JdbcCatalog>>`



| **setCatalogsList** (catalogs)
|          :ref:`JdbcCatalog><java.util.List<ai.starlake.transpiler.schema.JdbcCatalog>>` catalogs


| **setCurrentCatalogName** (currentCatalogName)
|          :ref:`String<java.lang.String>` currentCatalogName


| **setCurrentSchemaName** (currentSchemaName)
|          :ref:`String<java.lang.String>` currentSchemaName



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


| **getColumns** () → :ref:`JdbcColumn><java.util.ArrayList<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.ArrayList<ai.starlake.transpiler.schema.JdbcColumn>>`



| **getLabels** () → :ref:`String><java.util.ArrayList<java.lang.String>>`
|          returns :ref:`String><java.util.ArrayList<java.lang.String>>`



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



| **getScopeTable** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| **getScopeSchema** (column) → :ref:`String<java.lang.String>`
|          int column
|          returns :ref:`String<java.lang.String>`



| **getScopeCatalog** (column) → :ref:`String<java.lang.String>`
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
|          :ref:`Class<T><java.lang.Class<T>>` iface
|          returns T



| *@Override*
| **isWrapperFor** (iface) → boolean
|          :ref:`Class<?><java.lang.Class<?>>` iface
|          returns boolean




..  _ai.starlake.transpiler.schema.JdbcSchema:

=======================================================================
JdbcSchema
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`JdbcSchema><java.lang.Comparable<ai.starlake.transpiler.schema.JdbcSchema>>` 

| **JdbcSchema** (tableSchema, tableCatalog)
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableCatalog


| **JdbcSchema** ()


| **getSchemas** (metaData) → :ref:`JdbcSchema><java.util.Collection<ai.starlake.transpiler.schema.JdbcSchema>>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`JdbcSchema><java.util.Collection<ai.starlake.transpiler.schema.JdbcSchema>>`



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
|          :ref:`JdbcTable><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcTable,? extends ai.starlake.transpiler.schema.JdbcTable>>` remappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **putAll** (m)
|          :ref:`JdbcTable><java.util.Map<? extends java.lang.String,? extends ai.starlake.transpiler.schema.JdbcTable>>` m


| **values** () → :ref:`JdbcTable><java.util.Collection<ai.starlake.transpiler.schema.JdbcTable>>`
|          returns :ref:`JdbcTable><java.util.Collection<ai.starlake.transpiler.schema.JdbcTable>>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` oldValue
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`JdbcTable><java.util.function.BiConsumer<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcTable>>` action


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
|          :ref:`JdbcTable><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcTable,? extends ai.starlake.transpiler.schema.JdbcTable>>` remappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **replaceAll** (function)
|          :ref:`JdbcTable><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcTable,? extends ai.starlake.transpiler.schema.JdbcTable>>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable><java.util.function.Function<? super java.lang.String,? extends ai.starlake.transpiler.schema.JdbcTable>>` mappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **putIfAbsent** (value) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>` value
|          :ref:`JdbcTable><java.util.function.BiFunction<? super ai.starlake.transpiler.schema.JdbcTable,? super ai.starlake.transpiler.schema.JdbcTable,? extends ai.starlake.transpiler.schema.JdbcTable>>` remappingFunction
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **containsKey** (key) → boolean
|          :ref:`String<java.lang.String>` key
|          returns boolean



| **remove** (key) → :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcTable<ai.starlake.transpiler.schema.JdbcTable>`



| **clear** ()


| **entrySet** () → :ref:`JdbcTable>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcTable>>>`
|          returns :ref:`JdbcTable>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcTable>>>`



| **keySet** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **getTables** () → :ref:`JdbcTable><java.util.List<ai.starlake.transpiler.schema.JdbcTable>>`
|          returns :ref:`JdbcTable><java.util.List<ai.starlake.transpiler.schema.JdbcTable>>`



| **setTables** (tables)
|          :ref:`JdbcTable><java.util.List<ai.starlake.transpiler.schema.JdbcTable>>` tables


| **getSchemaName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **setSchemaName** (schemaName)
|          :ref:`String<java.lang.String>` schemaName



..  _ai.starlake.transpiler.schema.JdbcTable:

=======================================================================
JdbcTable
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`JdbcTable><java.lang.Comparable<ai.starlake.transpiler.schema.JdbcTable>>` 

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


| **JdbcTable** ()


| **getTables** (metaData, currentCatalog, currentSchema) → :ref:`JdbcTable><java.util.Collection<ai.starlake.transpiler.schema.JdbcTable>>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          :ref:`String<java.lang.String>` currentCatalog
|          :ref:`String<java.lang.String>` currentSchema
|          returns :ref:`JdbcTable><java.util.Collection<ai.starlake.transpiler.schema.JdbcTable>>`



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



| **add** (tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, numericPrecisionRadix, nullable, remarks, columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog, scopeSchema, scopeTable, scopeColumn, sourceDataType, isAutomaticIncrement, isGeneratedColumn) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
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
|          :ref:`String<java.lang.String>` scopeColumn
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
|          :ref:`JdbcColumn><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcColumn,? extends ai.starlake.transpiler.schema.JdbcColumn>>` remappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **putAll** (m)
|          :ref:`JdbcColumn><java.util.Map<? extends java.lang.String,? extends ai.starlake.transpiler.schema.JdbcColumn>>` m


| **values** () → :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.Collection<ai.starlake.transpiler.schema.JdbcColumn>>`



| **replace** (key, oldValue, newValue) → boolean
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` oldValue
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` newValue
|          returns boolean



| **forEach** (action)
|          :ref:`JdbcColumn><java.util.function.BiConsumer<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcColumn>>` action


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
|          :ref:`JdbcColumn><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcColumn,? extends ai.starlake.transpiler.schema.JdbcColumn>>` remappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **replaceAll** (function)
|          :ref:`JdbcColumn><java.util.function.BiFunction<? super java.lang.String,? super ai.starlake.transpiler.schema.JdbcColumn,? extends ai.starlake.transpiler.schema.JdbcColumn>>` function


| **computeIfAbsent** (key, mappingFunction) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn><java.util.function.Function<? super java.lang.String,? extends ai.starlake.transpiler.schema.JdbcColumn>>` mappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **putIfAbsent** (key, value) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **merge** (key, value, remappingFunction) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` value
|          :ref:`JdbcColumn><java.util.function.BiFunction<? super ai.starlake.transpiler.schema.JdbcColumn,? super ai.starlake.transpiler.schema.JdbcColumn,? extends ai.starlake.transpiler.schema.JdbcColumn>>` remappingFunction
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **remove** (key) → :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`
|          :ref:`String<java.lang.String>` key
|          returns :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>`



| **clear** ()


| **entrySet** () → :ref:`JdbcColumn>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcColumn>>>`
|          returns :ref:`JdbcColumn>><java.util.Set<java.util.Map.Entry<java.lang.String,ai.starlake.transpiler.schema.JdbcColumn>>>`



| **keySet** () → :ref:`String><java.util.Set<java.lang.String>>`
|          returns :ref:`String><java.util.Set<java.lang.String>>`



| **getColumns** () → :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`
|          returns :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>`



| **setColumns** (columns)
|          :ref:`JdbcColumn><java.util.List<ai.starlake.transpiler.schema.JdbcColumn>>` columns


| **getTableName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **setTableName** (tableName)
|          :ref:`String<java.lang.String>` tableName


| **getTableType** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **setTableType** (tableType)
|          :ref:`String<java.lang.String>` tableType



..  _ai.starlake.transpiler.schema.JdbcUtils:

=======================================================================
JdbcUtils
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **JdbcUtils** ()


| **findColumnSafe** (rs, columnName) → int
| Safe variant of java.sql.ResultSet.findColumn() Does not throw SQLException if columnName does not exist in result set.
|          :ref:`ResultSet<java.sql.ResultSet>` rs
|          :ref:`String<java.lang.String>` columnName
|          returns int




                Retrieves column's value from ResultSet safely (does not throw SQLException if column (name)
 not present in ResultSet.
                
                
                
                |          :ref:`ResultSet<java.sql.ResultSet>` rs

                |          :ref:`String<java.lang.String>` columnName

                |          returns :ref:`String<java.lang.String>`


            
                Retrieves column's value from ResultSet safely (does not throw SQLException if column (name)
 not present in ResultSet.
                
                
                
                
                |          :ref:`ResultSet<java.sql.ResultSet>` rs

                |          :ref:`String<java.lang.String>` columnName

                |          :ref:`String<java.lang.String>` defaultValue

                |          returns :ref:`String<java.lang.String>`


            
                |          :ref:`ResultSet<java.sql.ResultSet>` rs

                |          int columnIdx

                |          :ref:`String<java.lang.String>` defaultValue

                |          returns :ref:`String<java.lang.String>`


            
                |          :ref:`ResultSet<java.sql.ResultSet>` rs

                |          :ref:`String<java.lang.String>` columnName

                |          returns :ref:`Integer<java.lang.Integer>`


            
                |          :ref:`ResultSet<java.sql.ResultSet>` rs

                |          :ref:`String<java.lang.String>` columnName

                |          returns :ref:`Short<java.lang.Short>`


            
                |          :ref:`ResultSet<java.sql.ResultSet>` rs

                |          :ref:`String<java.lang.String>` columnName

                |          returns :ref:`Boolean<java.lang.Boolean>`


            
..  _ai.starlake.transpiler.schema.SampleSchemaProvider:

=======================================================================
SampleSchemaProvider
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`SchemaProvider<ai.starlake.transpiler.schema.SchemaProvider>` 

| **SampleSchemaProvider** ()


| **getTables** () → :ref:`String>><java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>>`
|          returns :ref:`String>><java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>>`



| **getTable** (schemaName, tableName) → :ref:`String><java.util.Map<java.lang.String,java.lang.String>>`
|          :ref:`String<java.lang.String>` schemaName
|          :ref:`String<java.lang.String>` tableName
|          returns :ref:`String><java.util.Map<java.lang.String,java.lang.String>>`



| **getTables** (tableName) → :ref:`String>><java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>>`
|          :ref:`String<java.lang.String>` tableName
|          returns :ref:`String>><java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>>`




..  _ai.starlake.transpiler.schema.treebuilder:
***********************************************************************
ma.treebuilder
***********************************************************************

..  _ai.starlake.transpiler.schema.treebuilder.JsonTreeBuilder:

=======================================================================
JsonTreeBuilder
=======================================================================

*extends:* :ref:`String><ai.starlake.transpiler.schema.treebuilder.TreeBuilder<java.lang.String>>` 

| **JsonTreeBuilder** (resultSetMetaData)
|          :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>` resultSetMetaData



                |          int indent

                |          returns void


            
                |          :ref:`String<java.lang.String>` input

                |          int indentLevel

                |          returns :ref:`StringBuilder<java.lang.StringBuilder>`


            
                |          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` column

                |          :ref:`String<java.lang.String>` alias

                |          int indent

                |          returns void


                
                    
                        
                        PMD.CyclomaticComplexity
                    
                
            | *@Override*
| **getConvertedTree** (resolver) → :ref:`String<java.lang.String>`
|          :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>` resolver
|          returns :ref:`String<java.lang.String>`




..  _ai.starlake.transpiler.schema.treebuilder.JsonTreeBuilderMinimized:

=======================================================================
JsonTreeBuilderMinimized
=======================================================================

*extends:* :ref:`String><ai.starlake.transpiler.schema.treebuilder.TreeBuilder<java.lang.String>>` 

| Concise/minimized version of output generated by JsonTreeBuilder. Useful when the output needs to be transported somewhere or parsed back into POJO.

| **JsonTreeBuilderMinimized** (resultSetMetaData)
|          :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>` resultSetMetaData



                |          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` column

                |          :ref:`String<java.lang.String>` alias

                |          returns void


                
                    
                        
                        PMD.CyclomaticComplexity
                    
                
            | *@Override*
| **getConvertedTree** (resolver) → :ref:`String<java.lang.String>`
|          :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>` resolver
|          returns :ref:`String<java.lang.String>`




..  _ai.starlake.transpiler.schema.treebuilder.TreeBuilder&lt;T&gt;:

=======================================================================
TreeBuilder
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **TreeBuilder** (resultSetMetaData)
|          :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>` resultSetMetaData


| **getConvertedTree** (resolver) → T
|          :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>` resolver
|          returns T




..  _ai.starlake.transpiler.schema.treebuilder.XmlTreeBuilder:

=======================================================================
XmlTreeBuilder
=======================================================================

*extends:* :ref:`String><ai.starlake.transpiler.schema.treebuilder.TreeBuilder<java.lang.String>>` 

| **XmlTreeBuilder** (resultSetMetaData)
|          :ref:`JdbcResultSetMetaData<ai.starlake.transpiler.schema.JdbcResultSetMetaData>` resultSetMetaData



                |          int indent

                |          returns void


            
                |          :ref:`String<java.lang.String>` input

                |          int indentLevel

                |          returns :ref:`StringBuilder<java.lang.StringBuilder>`


            
                |          :ref:`JdbcColumn<ai.starlake.transpiler.schema.JdbcColumn>` column

                |          :ref:`String<java.lang.String>` alias

                |          int indent

                |          returns void


                
                    
                        
                        PMD.CyclomaticComplexity
                    
                
            | *@Override*
| **getConvertedTree** (resolver) → :ref:`String<java.lang.String>`
|          :ref:`JSQLColumResolver<ai.starlake.transpiler.JSQLColumResolver>` resolver
|          returns :ref:`String<java.lang.String>`




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
|          :ref:`SelectDeParser<net.sf.jsqlparser.util.deparser.SelectDeParser>` deParser
|          :ref:`StringBuilder<java.lang.StringBuilder>` buffer


| **toDateTimePart** (expression) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` expression
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| **castInterval** (e1, e2) → :ref:`Expression<net.sf.jsqlparser.expression.Expression>`
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` e1
|          :ref:`Expression<net.sf.jsqlparser.expression.Expression>` e2
|          returns :ref:`Expression<net.sf.jsqlparser.expression.Expression>`



| *@Override*,| *@SuppressWarnings*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Function<net.sf.jsqlparser.expression.Function>` function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (function, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`AnalyticExpression<net.sf.jsqlparser.expression.AnalyticExpression>` function
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (column, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Column<net.sf.jsqlparser.schema.Column>` column
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (hexValue, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`HexValue<net.sf.jsqlparser.expression.HexValue>` hexValue
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*
| **visit** (likeExpression, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`LikeExpression<net.sf.jsqlparser.expression.operators.relational.LikeExpression>` likeExpression
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **rewriteType** (colDataType) → :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`
|          :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>` colDataType
|          returns :ref:`ColDataType<net.sf.jsqlparser.statement.create.table.ColDataType>`




..  _ai.starlake.transpiler.snowflake.SnowflakeSelectTranspiler:

=======================================================================
SnowflakeSelectTranspiler
=======================================================================

*extends:* :ref:`JSQLSelectTranspiler<ai.starlake.transpiler.JSQLSelectTranspiler>` 

| **SnowflakeSelectTranspiler** (expressionDeparserClass, builder)
|          :ref:`ExpressionDeParser><java.lang.Class<? extends net.sf.jsqlparser.util.deparser.ExpressionDeParser>>` expressionDeparserClass
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder


| *@Override*
| **visit** (values, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`Values<net.sf.jsqlparser.statement.select.Values>` values
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| *@Override*,| *@SuppressWarnings*
| **visit** (tableFunction, params) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`TableFunction<net.sf.jsqlparser.statement.select.TableFunction>` tableFunction
|          S params
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`




..  _ai.starlake.transpiler.snowflake.SnowflakeTranspiler:

=======================================================================
SnowflakeTranspiler
=======================================================================

*extends:* :ref:`JSQLTranspiler<ai.starlake.transpiler.JSQLTranspiler>` 

| **SnowflakeTranspiler** (parameters)
|          :ref:`Object><java.util.Map<java.lang.String,java.lang.Object>>` parameters


