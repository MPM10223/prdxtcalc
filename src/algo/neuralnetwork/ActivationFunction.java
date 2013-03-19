package algo.neuralnetwork;

public abstract class ActivationFunction {
	
	public abstract double process(double[] input);
	
	protected double getLinearSum(double[] input) {
		double sum = 0;
		
		for(int i = 0; i < input.length; i++) {
			sum += input[i];
		}
		
		return sum;
	}

}
