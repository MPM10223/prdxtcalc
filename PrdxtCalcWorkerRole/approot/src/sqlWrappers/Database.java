package sqlWrappers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Database {
	
	protected Connection c;
	
	protected abstract String getConnectionString();
	protected abstract String getDriverClassName();
	
	public static String getAzureODBCConnectionString(String server, String port, String db, String uid, String pwd, boolean encrypt) {
		//Driver={SQL Server Native Client 10.0};Server=tcp:hwvhpv4cb1.database.windows.net,1433;Database=prdxt;Uid=xxxxx@hwvhpv4cb1;Pwd=xxxxx;Encrypt=yes;
		return String.format("Driver={%s};Server=tcp:%s,%s;Database=%s;Uid=%s;Pwd=%s;Encrypt=%s;"
				, System.getProperty("os.name").equals("Windows XP") ? "SQL Server Native Client 10.0" : "SQL Server Native Client 11.0"
				, server
				, port
				, db
				, uid
				, pwd
				, encrypt ? "yes" : "no"
			);
	}
	
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
