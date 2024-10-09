package ai.starlake.transpiler.schema;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtils {


	public static abstract class SchemaHandler {
		
		abstract List<String[]> getCatalogSchemas(Connection con);
		
	}
	
	/**
	 * Used for detecting RDBMS type and DB specific handling 
	 */
	public enum DatabaseSpecific {
		  ORACLE("ORACLE",new String[] {"SYNONYM","TABLE","VIEW"}, new String[] {"SYS"}, "SELECT SYS_CONTEXT('USERENV', 'DB_NAME') AS database_name , SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS current_schema FROM dual"),
		  POSTGRESQL("POSTGRESQL",new String[] {"TABLE","VIEW","FOREIGN TABLE","MATERIALIZED VIEW","PARTITIONED TABLE","SYSTEM TABLE","TEMPORARY TABLE","TEMPORARY VIEW"}, null,"SELECT current_database(), current_schema()"),
		  MSSQL("MICROSOFT SQL SERVER",new String[] {"SYSTEM TABLE", "TABLE", "VIEW"},null,"SELECT DB_NAME(), SCHEMA_NAME()"),
		  MYSQL("MYSQL",new String[] {"TABLE","VIEW"},null,"SELECT DATABASE(), DATABASE()"),
		  OTHER("OTHER",new String[] {"TABLE","VIEW"},null,"SELECT current_database(), current_schema()");
		  
		  String identString;
		  String currentSchemaQuery;
		  String[] tableTypes;
		  String[] excludedSchemas;
		  
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

	static String getStringSafe(ResultSet rs,String columnName) {
		  try {
			  return rs.getString(columnName);
		  }catch(SQLException e) {
			  return null;
		  }
	  }
	
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
