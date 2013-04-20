import java.util.Stack;

import junit.framework.Assert;

import org.junit.Test;

import sqlWrappers.SQLDatabase;


public class JobLogTest {
	
	@Test
	public void test00_getProgressOf() {
		Stack<StepSequenceProgress> steps = new Stack<StepSequenceProgress>();
		steps.push(new StepSequenceProgress(5, 2));
		steps.push(new StepSequenceProgress(3, 1));
		steps.push(new StepSequenceProgress(5, 4));
		Assert.assertEquals(0.4 + 0.2*(1/(double)3 + 1/(double)(3)*(0.8)), JobLog.getProgressOf(steps));
	}
	
	@Test
	public void test01_NoNesting() {
		JobLog log = new JobLog(new MockSQLDatabase(), "", 1, 1);
		log.initJobProgress();
		log.logStepSequenceStarted(1);
		log.logStepCompleted();
		log.logStepSequenceCompleted();
		Assert.assertEquals(1.0, log.progress);
	}
	
	@Test
	public void test02_NoNesting_EarlyCompletion() {
		JobLog log = new JobLog(new MockSQLDatabase(), "", 1, 1);
		log.initJobProgress();
		log.logStepSequenceStarted(2);
		log.logStepCompleted();
		Assert.assertEquals(0.5, log.progress);
		log.logStepSequenceCompleted();
		Assert.assertEquals(1.0, log.progress);
	}
	
	@Test
	public void test03_OneNesting() {
		JobLog log = new JobLog(new MockSQLDatabase(), "", 1, 1);
		log.initJobProgress();
		log.logStepSequenceStarted(2);
		log.logStepSequenceStarted(2);
		log.logStepCompleted();
		Assert.assertEquals(0.25, log.progress);
		log.logStepSequenceCompleted();
		log.logStepCompleted();
		Assert.assertEquals(0.5, log.progress);
		log.logStepCompleted();
		Assert.assertEquals(1.0, log.progress);
	}
	
	@Test
	public void test04_Loop() {
		JobLog log = new JobLog(new MockSQLDatabase(), "", 1, 1);
		int[] loop = new int[] { 1, 2, 3, 4, 5};
		
		log.initJobProgress();
		
		log.logStepSequenceStarted(loop.length);
		for(int i = 0; i < loop.length; i++) {
			log.logStepCompleted();
			Assert.assertEquals((double)loop[i] / (double)loop.length, log.progress);
		}
		Assert.assertEquals(1.0, log.progress);
		log.logStepSequenceCompleted();
		Assert.assertEquals(1.0, log.progress);
	}
	
	@Test
	public void test05_NestedLoop() {
		JobLog log = new JobLog(new MockSQLDatabase(), "", 1, 1);
		int[] loop1 = new int[] { 1, 2, 3, 4, 5};
		
		log.initJobProgress();
		
		log.logStepSequenceStarted(loop1.length);
		for(int i = 0; i < loop1.length; i++) {
			
			int[] loop2 = new int[] { 1, 2, 3 };
			log.logStepSequenceStarted(loop2.length);
			for(int j = 0; j < loop2.length; j++) {
				log.logStepCompleted();
				double expected = (double)i/(double)loop1.length
						+ (1/(double)loop1.length)
						* (double)loop2[j]/(double)loop2.length;
				Assert.assertTrue(Math.abs(expected - log.progress) < 0.00001);
			}
			log.logStepSequenceCompleted();
			
			log.logStepCompleted();
			
			Assert.assertEquals((double)loop1[i] / (double)loop1.length, log.progress);
		}
		Assert.assertEquals(1.0, log.progress);
		log.logStepSequenceCompleted();
		Assert.assertEquals(1.0, log.progress);
	}
	
	private class MockSQLDatabase extends SQLDatabase {

		public MockSQLDatabase() {
			super();
		}

		@Override
		public void executeQuery(String sql) {
			//NoOp
		}
	}

}
