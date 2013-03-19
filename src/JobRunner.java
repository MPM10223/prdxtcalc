import sqlWrappers.SQLDatabase;


public abstract class JobRunner {
	
	/**
	 * @param server
	 * @param database
	 * @param jobqtable
	 * @param username
	 * @param password
	 */
	public static void main(String[] args) {
		
		/*
		if(args.length != 5)
			throw new RuntimeException();
		
		String server = args[0];
		String database = args[1];
		String jobqtable = args[2];
		String username = args[3];
		String password = args[4];
		*/
		
		String server = "tcp:hwvhpv4cb1.database.windows.net,1433";
		String database = "prdxt";
		String jobQtable = "jobQueue";
		String username = "prdxt_calc";
		String password = "s1mul4t3";
		
		SQLDatabase db = new SQLDatabase(server, database, username, password);
		
		String sql = String.format("SELECT TOP 1 * FROM {0} WHERE ", jobQtable);
	}

}
