package sqlWrappers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SQLDatabase extends Database {
	
	protected String server;
	protected String database;
	protected String port;
	protected String userName;
	protected String password;
	
	protected static final String DriverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"; 

	public SQLDatabase(String server, String port, String database, String userName, String password) {
		super();
		this.server = server;
		this.port = port;
		this.database = database;
		this.userName = userName;
		this.password = password;
	}
	
	public String getConnectionString() {
		//http://msdn.microsoft.com/en-us/library/ms378428.aspx
		return String.format("jdbc:sqlserver://%s:%s;DatabaseName=%s;user=%s;password=%s", server, port, database, userName, password);
		//return String.format("jdbc:sqlserver://%s;DatabaseName=%s;integratedSecurity=true;", server, database);
	}
	
	public void executeQuery(String sql) {
		this.getQueryRows(sql, false);
	}
	
	public String getQueryResult(String sql) {
		Map<String,String> row = getQueryRow(sql);
		if(row.size() == 0) throw new RuntimeException("Specified query did not return any columns: " + sql);
		if(row.size() > 1) throw new RuntimeException("Specified query returned more than one column: " + sql);
		return row.values().iterator().next();
	}
	
	public Map<String,String> getQueryRow(String sql) {
		Vector<Map<String,String>> rows = getQueryRows(sql);
		if(rows.size() == 0) throw new RuntimeException("Specified query did not return any rows: " + sql);
		if(rows.size() > 1) throw new RuntimeException("Specified query returned more than one row: " + sql);
		return rows.firstElement();
	}
	
	public Vector<Map<String,String>> getQueryRows(String sql) {
		return getQueryRows(sql, true);
	}
	
	public Vector<Vector<Map<String,String>>> getQueryBatchResults(String sqlBatch) {
		
		System.out.println(sqlBatch);
		
		Vector<Vector<Map<String,String>>> results = null;
		
		try {
			establishConnection();
			
			Statement s = c.createStatement();
			s.execute(sqlBatch);
			
			results = new Vector<Vector<Map<String,String>>>();
			
			do {
				Vector<Map<String,String>> result = null;
				
				ResultSet rs = s.getResultSet();
				
				if(rs != null) {
					ResultSetMetaData d = rs.getMetaData();
					
					result = new Vector<Map<String,String>>(rs.getFetchSize());
					
					while(rs.next()) {
						HashMap<String,String> row = new HashMap<String,String>(d.getColumnCount()); 
						for(int i = 0; i < d.getColumnCount(); i++) {
							String columnName = d.getColumnName(i + 1);
							row.put(columnName, rs.getString(columnName));
						}
						result.add(row);
					}
					
					rs.close();
				}
				
				results.add(result);
				
			} while(s.getMoreResults(Statement.CLOSE_CURRENT_RESULT));
			
			s.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			closeConnection();
		}
		
		return results;
	}
	
	protected Vector<Map<String,String>> getQueryRows(String sql, boolean expectResults) {

		System.out.println(sql);
		
		Vector<Map<String,String>> results = null;
		
		try {
			establishConnection();
			
			Statement s = c.createStatement();
			
			if(expectResults) {
				ResultSet rs = s.executeQuery(sql);
			
				ResultSetMetaData d = rs.getMetaData();
				
				results = new Vector<Map<String,String>>(rs.getFetchSize());
				
				while(rs.next()) {
					HashMap<String,String> row = new HashMap<String,String>(d.getColumnCount()); 
					for(int i = 0; i < d.getColumnCount(); i++) {
						String columnName = d.getColumnName(i + 1);
						row.put(columnName, rs.getString(columnName));
					}
					results.add(row);
				}
				
				rs.close();
			} else {
				s.execute(sql);
			}
			s.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			closeConnection();
		}
		
		return results;
	}
	
	public void dropTableIfExists(String tableName) {
		dropObjectIfExists(tableName, "TABLE");
	}
	
	public void dropViewIfExists(String viewName) {
		dropObjectIfExists(viewName, "VIEW");
	}
	
	protected void dropObjectIfExists(String objectName, String objectType) {
		String sql = String.format("IF object_id('%s') IS NOT NULL BEGIN DROP %s [%s] END", objectType, objectType, objectName);
		this.executeQuery(sql);
	}
	

	public void renameObject(String oldName, String newName) {
		String sql = String.format("sp_rename '%s', '%s'", oldName, newName);
		this.executeQuery(sql);
	}

	@Override
	protected String getDriverClassName() {
		return DriverClassName;
	}
	
	public String getServer() {
		return server;
	}
	
	public String getPort() {
		return port;
	}

	public String getDatabase() {
		return database;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

}
