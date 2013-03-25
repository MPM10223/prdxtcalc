package dao;

public class ProblemDefinition {
	
	protected String table;
	protected String dvColumn;
	protected String idColumn;
	protected String[] ivColumns;
	protected int[] ivFeatureIDs;
	
	public ProblemDefinition(String table, String dvColumn, String idColumn, String[] ivColumns, int[] ivFeatureIDs) {
		super();
		this.table = table;
		this.dvColumn = dvColumn;
		this.idColumn = idColumn;
		this.ivColumns = ivColumns;
		this.ivFeatureIDs = ivFeatureIDs;
	}
	
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
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
	public String[] getIvColumns() {
		return ivColumns;
	}
	public void setIvColumns(String[] ivColumns) {
		this.ivColumns = ivColumns;
	}
	public void setIvFeatureIDs(int[] ivFeatureIDs) {
		this.ivFeatureIDs = ivFeatureIDs;
	}
	public int[] getIvFeatureIDs() {
		return ivFeatureIDs;
	}

}
