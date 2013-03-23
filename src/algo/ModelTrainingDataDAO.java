package algo;

import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public class ModelTrainingDataDAO {

	protected SQLDatabase db;
	
	private static String server = "hwvhpv4cb1.database.windows.net:1433";
	private static String database = "prdxt";
	private static String userName = "username@hwvhpv4cb1";
	private static String password = "pwd";
	
	protected String dataTable;
	protected String[] ivColumns;
	protected String dvColumn;
	protected String idColumn;
	protected String predicate;

	public ModelTrainingDataDAO(String dataTable, String[] ivColumns, String dvColumn, String idColumn, String predicate) {
		this.db = new SQLDatabase(server, database, userName, password);
		this.dataTable = dataTable;
		this.ivColumns = ivColumns;
		this.dvColumn = dvColumn;
		this.idColumn = idColumn;
		this.predicate = predicate;
	}

	public int getDatasetObservationCount() {
		String sql = String.format("SELECT COUNT(*) as numRows FROM [%s] WHERE %s", dataTable, this.getSQLPredicate());
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

	public String getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
	
	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public String[] getIvColumns() {
		
		/*
		String sql = String.format("SELECT TOP 1 * FROM [%s] WHERE %s", dataTable, this.getSQLPredicate());
		Map<String,String> row = this.db.getQueryRow(sql);
		if(row.size() < 2) throw new RuntimeException("Data source table must have at least 2 columns");
		
		String[] ivColumns = new String[row.size() - 2];
		int i = 0;
		for(String c : row.keySet()) {
			if(!c.equalsIgnoreCase(this.dvColumn) && !c.equalsIgnoreCase(this.idColumn) && !c.equalsIgnoreCase("foldID")) {
				ivColumns[i] = "[" + c + "]";
				i++;
			}
		}
		*/
		
		return ivColumns;
	}
	
	public Vector<Map<String,String>> getColumnInfo() {
		String sql = String.format("SELECT c.name as [column], t.name as [type], c.is_nullable, CASE c.name WHEN '%s' THEN 1 ELSE 0 END as isIDColumn, CASE c.name WHEN '%s' THEN 1 ELSE 0 END as isDVColumn FROM sys.columns c JOIN sys.types t ON c.system_type_id = t.system_type_id WHERE c.object_id=object_id('%s') ORDER BY c.column_id", this.idColumn, this.dvColumn, this.dataTable);
		return this.db.getQueryRows(sql);
	}

	public boolean getDVIsBinary() {
		String sql = String.format("SELECT [%s], COUNT(*) as numRows FROM [%s] WHERE [%s] IS NOT NULL AND %s GROUP BY [%s] ORDER BY [%s] ASC", this.dvColumn, this.dataTable, this.dvColumn, this.getSQLPredicate(), this.dvColumn, this.dvColumn);
		Vector<Map<String,String>> r = this.db.getQueryRows(sql);
		if(r.size() == 2) {
			if(r.firstElement().get(this.dvColumn).equals("0.0") && r.lastElement().get(this.dvColumn).equals("1.0"))
				return true;
		}
		return false;
	}
	
	protected String getSQLPredicate() {
		return "1=1" + (predicate == null ? "" : " AND " + predicate);
	}

	
}
