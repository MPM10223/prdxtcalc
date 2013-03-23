package algo.util.dao;

import java.util.Arrays;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public class SQLInsertBuffer {
	
	protected final static int MAX_BUFFER_SIZE = 255;
	
	protected SQLDatabase db;
	protected String tableName;
	protected String[] columns;
	
	protected Vector<String[]> buffer;
	
	public SQLInsertBuffer(SQLDatabase db, String tableName, String[] columns) {
		this.db = db;
		this.tableName = tableName;
		this.columns = columns;
	}
	
	public void startBufferedInsert(int numRows) {
		this.buffer = new Vector<String[]>(numRows);
	}
	
	public int finishBufferedInsert() {
		return this.flush();
	}
	
	public int insertRow(String[] values) {
		if(values.length != columns.length) throw new RuntimeException();
		this.buffer.add(values);
		if(this.buffer.size() >= MAX_BUFFER_SIZE) {
			return this.flush();
		}
		return 0;
	}
	
	public int flush() {
		String columnList = Arrays.toString(this.columns);
		columnList = columnList.substring(1, columnList.length() - 1);
		StringBuilder b = new StringBuilder(String.format("INSERT INTO [%s] (%s) ", this.tableName, columnList));
		
		boolean first = true;
		for(String[] values : this.buffer) {
			if(first) first = false;
			else b.append(" UNION ALL ");
			String valueList = Arrays.toString(values);
			valueList = valueList.substring(1, valueList.length() - 1);
			b.append(String.format("SELECT %s", valueList));
		}
		
		int numRows = buffer.size();
		
		db.executeQuery(b.toString());
		
		return numRows;
	}
	
	public void clear() {
		this.buffer = null;
	}

}
