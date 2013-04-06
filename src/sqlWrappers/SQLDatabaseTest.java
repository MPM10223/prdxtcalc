package sqlWrappers;


import java.util.Map;
import java.util.Vector;

import org.junit.Test;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SQLDatabaseTest extends TestCase {

	protected SQLDatabase db;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = new SQLDatabase(
				"server.database.windows.net"
				, "1433"
				, "db"
				, "login@server"
				, "pwd"
			);
	}

	@Test
	public void test01_GetQueryResult() {
		String sql = "SELECT 1 as result";
		Assert.assertEquals("1", db.getQueryResult(sql));
	}
	
	@Test
	public void test02_GetQueryRow() {
		String sql = "SELECT 1 as result1, 'foo' as result2";
		Map<String,String> row = db.getQueryRow(sql);
		Assert.assertEquals("1", row.get("result1"));
		Assert.assertEquals("foo", row.get("result2"));
	}
	
	@Test
	public void test03_GetQueryRows() {
		String sql = "SELECT 1 as result1, 'foo' as result2  UNION ALL  SELECT 2, 'bar'";
		Vector<Map<String,String>> rows = db.getQueryRows(sql);
		
		Assert.assertEquals("1", rows.firstElement().get("result1"));
		Assert.assertEquals("foo", rows.firstElement().get("result2"));
		Assert.assertEquals("2", rows.lastElement().get("result1"));
		Assert.assertEquals("bar", rows.lastElement().get("result2"));
	}
	
	@Test
	public void test04_GetQueryBatchResults() {
		String sql = String.format("SELECT 1 as result1, 'foo' as result2%nSELECT 2 as newResult1, 'bar' as newResult2");
		Vector<Vector<Map<String,String>>> results = db.getQueryBatchResults(sql);
		
		Vector<Map<String,String>> result1 = results.firstElement();
		Vector<Map<String,String>> result2 = results.lastElement();
		
		Assert.assertEquals("1", result1.firstElement().get("result1"));
		Assert.assertEquals("foo", result1.firstElement().get("result2"));
		Assert.assertEquals("2", result2.firstElement().get("newResult1"));
		Assert.assertEquals("bar", result2.firstElement().get("newResult2"));
	}
	
	@Test
	public void test05_ExecuteQuery() {
		String sql = "IF(object_id('foobar') IS NOT NULL) BEGIN UPDATE foobar SET foo = 1 WHERE 0=1 END";
		db.executeQuery(sql);
	}
	
	@Test
	public void test06_ExecuteQuery_withResults() {
		String sql = "SELECT 1 as result";
		try {
			db.executeQuery(sql); // should not throw
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void test07_GetQueryBatchResults_1Batch() {
		String sql = "SELECT 1 as result1, 'foo' as result2";
		Vector<Vector<Map<String,String>>> results = db.getQueryBatchResults(sql);
		
		Vector<Map<String,String>> result1 = results.firstElement();
		
		Assert.assertEquals("1", result1.firstElement().get("result1"));
		Assert.assertEquals("foo", result1.firstElement().get("result2"));
	}
	
	@Test
	public void test08_GetQueryRow_NoResults() {
		String sql = "IF(object_id('foobar') IS NOT NULL) BEGIN UPDATE foobar SET foo = 1 WHERE 0=1 END";
		try {
			db.getQueryRow(sql); // should throw
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(true);
		}
	}

}
