package ai.starlake.transpiler.schema;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtils {


	/**
	 * Used for detecting RDBMS type and DB specific handling 
	 */
	public enum DatabaseSpecific {
		  ORACLE("ORACLE",new String[] {"SYNONYM","TABLE","VIEW"}, new String[] {"SYS"}, "SELECT SYS_CONTEXT('USERENV', 'DB_NAME') AS database_name , SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS current_schema FROM dual"),
		  POSTGRESQL("POSTGRESQL",new String[] {"TABLE","VIEW","FOREIGN TABLE","MATERIALIZED VIEW","PARTITIONED TABLE","SYSTEM TABLE","TEMPORARY TABLE","TEMPORARY VIEW"}, null,"SELECT current_database(), current_schema()"),
		  MSSQL("MICROSOFT SQL SERVER",new String[] {"SYSTEM TABLE", "TABLE", "VIEW"},null,"SELECT DB_NAME(), SCHEMA_NAME()"),
		  MYSQL("MYSQL",new String[] {"TABLE","VIEW"},null,"SELECT DATABASE(), DATABASE()"),
		  SNOWFLAKE("SNOWFLAKE",new String[] {"TABLE","VIEW"},null,"SELECT CURRENT_DATABASE(), CURRENT_SCHEMA()"),
		  OTHER("OTHER",new String[] {"TABLE","VIEW"},null,"SELECT current_database(), current_schema()");
		  
		  String identString;
		  String currentSchemaQuery;
		  String[] tableTypes;
		  String[] excludedSchemas;

		  /**
		   * DB specific "configuration" for extracting metadata through JDBC connection
		   * 
		   * @param identString an unique string which identifying this DB type - will be compared to {@link java.sql.DatabaseMetaData#getDatabaseProductName()} value to identify the DB specific variant
		   * @param tableTypes which table types are considered when processing DB's schema to extract metadata information used for parsing&analyzing SQL statement (usualy TABLE, VIEW)
		   * @param excludedSchemas which schemas should be excluded/ignored when processing particular DB's catalog&schemas
		   * @param schemaQuery query to execute against particular DB type to get information about current catalog/db & schema.
		   */
		DatabaseSpecific(String identString,String[] tableTypes, String[] excludedSchemas, String schemaQuery){
			  this.identString=identString;
			  this.tableTypes=tableTypes;
			  this.excludedSchemas=excludedSchemas;
			  this.currentSchemaQuery=schemaQuery;
		  }
		  
		  public static DatabaseSpecific getType(String productName) {
			  productName=productName.toUpperCase();
			  for(DatabaseSpecific type : DatabaseSpecific.values()) {
				  if(productName.contains(type.identString)) {
					  return type;
				  }
			  }
			  return OTHER;
		  }
		  
		  public String getCurrentSchemaQuery() {
			  return this.currentSchemaQuery;
		  }
		  
		  /**
		   * Filtering out certain schemas(usually system)
		   * 
		   * @param schema
		   * @return true if the passed-
		   */
		  public boolean processSchema(String schema) {
			  if (excludedSchemas!=null) {  
				  for(String itm:excludedSchemas) {
					  if (schema.equalsIgnoreCase(itm)) {
						  return false;
					  }
				  }
			  }
			  return true;
		  }

		  public String[] getTableTypes() {
			  return tableTypes;
		  }
	  }

	/**
	   * Safe variant of  {@link java.sql.ResultSet#findColumn()} <br/>
	   * Does not throw SQLException if columnName does not exist in result set.
	   * 
	   * @param rs
	   * @param columnName 
	   * @return index of the searched column in the results set or -1 if not found
	   */
	  public static int findColumnSafe(ResultSet rs, String columnName) {
		  try {
			  return rs.findColumn(columnName);
		  }catch(SQLException e) {
			  return -1;
		  }
	  }
	  
	/**
	 * Retrieves column's value from ResultSet safely (does not throw SQLException
	 * if column (name) not present in ResultSet.
	 * 
	 * @param rs
	 * @param columnName
	 * @return column's value or NULL if column not found
	 */
	static String getStringSafe(ResultSet rs,String columnName) {
		  try {
			  return rs.getString(columnName);
		  }catch(SQLException e) {
			  return null;
		  }
	  }
	
	/**
	 * Retrieves column's value from ResultSet safely (does not throw SQLException
	 * if column (name) not present in ResultSet.
	 * 
	 * @param rs
	 * @param columnName
	 * @param defaultValue
	 * @return column's value or passed-in defaultValue if column not found or has NULL value
	 */
	static String getStringSafe(ResultSet rs,String columnName, String defaultValue) {
		  try {
			  final String val=rs.getString(columnName);
			  return val!=null ? val : defaultValue;
		  }catch(SQLException e) {
			  return defaultValue;
		  }
	  }

	static Integer getIntSafe(ResultSet rs,String columnName) {
		  try {
			  return rs.getInt(columnName);
		  }catch(SQLException e) {
			  return null;
		  }
	  }

	static Short getShortSafe(ResultSet rs,String columnName) {
		  try {
			  return rs.getShort(columnName);
		  }catch(SQLException e) {
			  return null;
		  }
	  }

	static Boolean getBooleanSafe(ResultSet rs,String columnName) {
		  try {
			  return rs.getBoolean(columnName);
		  }catch(SQLException e) {
			  return null;
		  }
	  }
}
