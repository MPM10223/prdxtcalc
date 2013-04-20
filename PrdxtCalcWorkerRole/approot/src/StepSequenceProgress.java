
public class StepSequenceProgress {
	
	protected int numSteps;		// 1-indexed
	protected int stepsCompleted;	// 0-indexed
	
	public StepSequenceProgress(int numSteps) {
		this(numSteps, 0);
	}
	
	public StepSequenceProgress(int numSteps, int stepsCompleted) {
		if(numSteps < 0) throw new IllegalArgumentException("numSteps cannot be negative");
		if(stepsCompleted > numSteps) throw new IllegalArgumentException("steps completed cannot be greater than numSteps");
		
		this.numSteps = numSteps;
		this.stepsCompleted = stepsCompleted;
	}

	public int getNumSteps() {
		return numSteps;
	}

	public void setNumSteps(int numSteps) {
		this.numSteps = numSteps;
	}

	public int getStepsCompleted() {
		return stepsCompleted;
	}

	public void setStepsCompleted(int stepsCompleted) {
		this.stepsCompleted = stepsCompleted;
	}

	public void advanceStep() {
		if(stepsCompleted > numSteps) throw new IllegalStateException("stepsCompleted is greater than numSteps"); 
		if(stepsCompleted == numSteps) throw new IndexOutOfBoundsException("step cannot be advanced any further");
		this.stepsCompleted++;
	}
}
