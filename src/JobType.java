
public enum JobType {
	BUILD_MODEL(1),
	EVALUATE_MODEL(2);
	
	private int value;
	
	private JobType(int value) {
		this.value = value;
	}
}
