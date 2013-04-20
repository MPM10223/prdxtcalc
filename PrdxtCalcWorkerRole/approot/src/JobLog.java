import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

import algo.util.dao.ILog;


public class JobLog implements ILog {

	protected SQLDatabase db;
	String logTable;
	int serverID;
	protected int jobID;
	
	protected Stack<StepSequenceProgress> steps;
	protected double progress;
	
	public JobLog(SQLDatabase db, String logTable, int serverID, int jobID) {
		this.db = db;
		this.logTable = logTable;
		this.serverID = serverID;
		this.jobID = jobID;
	}

	@Override
	public void logMessage(String message) {
		System.out.println(message);
	}
	
	@Override
	public void logJobStarted() {
		logJobStatusChange(jobID, 0, 1);
	}

	@Override
	public void logJobCompleted() {
		logJobStatusChange(jobID, 1, 2);
	}
	
	@Override
	public void logJobFailed(Exception e) {
		logJobStatusChange(jobID, 1, 3);
	}
	
	protected void logJobStatusChange(int jobID, int oldStatus, int newStatus) {
		String sql = String.format("SELECT jobID FROM [%s] WHERE jobID = %d AND calcServerID = %d AND jobStatus = %d", this.logTable, jobID, this.serverID, oldStatus);
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		if(results.size() == 0) throw new RuntimeException("Invalid jobID / status combination - no matching job/state found");
		
		String dateStampColumn = null;
		if(oldStatus == 0 && newStatus == 1) dateStampColumn = "startTime";
		if(oldStatus == 1 && newStatus == 2) dateStampColumn = "endTime";
		if(oldStatus == 1 && newStatus == 3) dateStampColumn = "endTime";
		
		String dateStampSQL = "";
		if(dateStampColumn != null) {
			dateStampSQL = String.format(", [%s] = getDate()", dateStampColumn);
		}
		
		String progressSQL = "";
		if(newStatus == 2) progressSQL = ", progress = 1"; 

		sql = String.format("UPDATE [%s] SET jobStatus = %d %s %s WHERE jobID = %d AND calcServerID = %d AND jobStatus = %d", this.logTable, newStatus, dateStampSQL, progressSQL, jobID, this.serverID, oldStatus);
		db.executeQuery(sql);
	}
	
	@Override
	public void initJobProgress() {
		this.steps = new Stack<StepSequenceProgress>();
		this.progress = 0;
	}
	
	@Override
	public void logStepSequenceStarted(int numSteps) {
		//push into stack
		this.steps.push(new StepSequenceProgress(numSteps));
	}

	@Override
	public void logStepCompleted() {
		//peek from stack
		StepSequenceProgress ssp = this.steps.peek();
		ssp.advanceStep();
		this.setProgress(getProgressOf(this.steps));
	}
	
	@Override
	public void logStepSequenceCompleted() {
		this.steps.pop();
		// note: this will technically move progress backwards, so we do not log to the DB
		// instead we wait for the client to advance the higher-level step counter
		
		// special case: if we finished the root step list, we're done
		if(this.steps.isEmpty()) {
			this.setProgress(1.0);
		}
	}
	
	protected static double getProgressOf(Stack<StepSequenceProgress> steps) {
		if(steps.isEmpty()) return 0; // base case
		else {
			// pop from the bottom of the stack
			StepSequenceProgress ssp = steps.get(0);
			Stack<StepSequenceProgress> rest = (Stack<StepSequenceProgress>)steps.clone();
			rest.removeElementAt(0);
			
			return ((double)ssp.getStepsCompleted() / (double)ssp.getNumSteps())
				+ ((1 / (double)ssp.getNumSteps()) * getProgressOf(rest));
		}
	}
	
	protected void setProgress(double progress) {
		if(progress < 0) throw new IllegalArgumentException("Progress cannot be negative");
		if(progress > 1) throw new IllegalArgumentException("Progress cannot be greater than 1");
		this.progress = progress;
		
		String sql = String.format("UPDATE [%s] SET progress = %f WHERE jobID = %d", this.logTable, this.progress, this.jobID);
		db.executeQuery(sql);
	}
}
