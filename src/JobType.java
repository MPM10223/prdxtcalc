
public enum JobType {
	BUILD_MODEL(1),
	EVALUATE_MODEL(2);
	
	private int value;
	
	private JobType(int value) {
		this.value = value;
	}
	
	public static JobType fromInt(int value) {
		switch(value) {
		case 1:
			return BUILD_MODEL;
		case 2:
			return EVALUATE_MODEL;
		default:
			throw new RuntimeException();
		}
	}
}
