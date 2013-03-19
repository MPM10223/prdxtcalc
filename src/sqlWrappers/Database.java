package sqlWrappers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Database {
	
	protected Connection c;
	
	protected abstract String getConnectionString();
	protected abstract String getDriverClassName();
	
	protected void establishConnection() {
		
		try {
			Class.forName(this.getDriverClassName());	
		} catch(java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException ("+this.getDriverClassName()+"): ");
			System.err.println(e.getMessage());
		}
		
		try {
			//System.out.println(System.getProperty("java.version"));
			String connString = this.getConnectionString();
			c = DriverManager.getConnection(connString);
			//System.out.println("Success: " + connString);
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}
	}
	
	protected void closeConnection() {
		try {
			c.close();
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}
	}

}
