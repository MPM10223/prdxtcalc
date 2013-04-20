package algo.util.dao;

public interface ILog {
	public void initJobProgress();
	public void logStepSequenceStarted(int numSteps);
	public void logStepCompleted();
	public void logStepSequenceCompleted();
	
	public void logJobStarted();
	public void logJobCompleted();
	public void logJobFailed(Exception e);
	public void logMessage(String message);
}
