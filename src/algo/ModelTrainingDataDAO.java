package algo;

import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public class ModelTrainingDataDAO {

	protected SQLDatabase db;
	
	private static String server = "tcp:hwvhpv4cb1.database.windows.net,1433";
	private static String database = "prdxt";
	private static String userName = "prdxt_calc@hwvhpv4cb1";
	private static String password = "s1mul4t3";
	
	protected String dataTable;
	protected String dvColumn;
	protected String predicate;
	
	public ModelTrainingDataDAO(String dataTable, String dvColumn, String predicate) {
		this.db = new SQLDatabase(server, database, userName, password);
		this.dataTable = dataTable;
		this.dvColumn = dvColumn;
		this.predicate = predicate;
	}

	public int getDatasetObservationCount() {
		String sql = String.format("SELECT COUNT(*) as numRows FROM [$s] WHERE %s", dataTable, this.getSQLPredicate());
		return Integer.parseInt(this.db.getQueryResult(sql));
	}

	public SQLDatabase getDb() {
		return db;
	}

	public String getDataTable() {
		return dataTable;
	}

	public void setDataTable(String dataTable) {
		this.dataTable = dataTable;
	}

	public String getDvColumn() {
		return dvColumn;
	}

	public void setDvColumn(String dvColumn) {
		this.dvColumn = dvColumn;
	}

	public String[] getIvColumns() {
		String sql = String.format("SELECT TOP 1 * FROM [$s] WHERE %s", dataTable, this.getSQLPredicate());
		Map<String,String> row = this.db.getQueryRow(sql);
		if(row.size() < 2) throw new RuntimeException("Data source table must have at least 2 columns");
		
		String[] ivColumns = new String[row.size() - 1];
		int i = 0;
		for(String c : row.keySet()) {
			if(!c.equals(this.dvColumn)) ivColumns[i] = c;
			i++;
		}
		
		return ivColumns;
	}

	public boolean getDVIsBinary() {
		String sql = String.format("SELECT [%s], COUNT(*) as numRows FROM [%s] WHERE %s GROUP BY [%s] ORDER BY [%s] ASC", this.dvColumn, this.dataTable, this.getSQLPredicate(), this.dvColumn, this.dvColumn);
		Vector<Map<String,String>> r = this.db.getQueryRows(sql);
		if(r.size() == 2) {
			if(r.firstElement().get(this.dvColumn).equals("0") && r.lastElement().get(this.dvColumn).equals("1"))
				return true;
		}
		return false;
	}
	
	protected String getSQLPredicate() {
		return "1=1" + (predicate == null ? "" : " AND " + predicate);
	}

	
}
