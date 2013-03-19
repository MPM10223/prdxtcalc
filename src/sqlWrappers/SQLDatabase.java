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
	protected String userName;
	protected String password;
	
	protected static final String DriverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"; 

	public SQLDatabase(String server, String database, String userName, String password) {
		super();
		this.server = server;
		this.database = database;
		this.userName = userName;
		this.password = password;
	}
	
	public String getConnectionString() {
		//http://msdn.microsoft.com/en-us/library/ms378428.aspx
		return String.format("jdbc:sqlserver://%s;DatabaseName=%s;user=%s;password=%s", server, database, userName, password);
		//return String.format("jdbc:sqlserver://%s;DatabaseName=%s;integratedSecurity=true;", server, database);
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
		Vector<Map<String,String>> results = null;
		
		try {
			establishConnection();
			
			Statement s = c.createStatement();
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
			s.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			closeConnection();
		}
		
		return results;
	}

	@Override
	protected String getDriverClassName() {
		return DriverClassName;
	}

}
