package algo.neuralnetwork;

public class Neuron {
	
	protected ActivationFunction f;
	
	public Neuron(ActivationFunction f) {
		this.f = f;
	}
	
	public Neuron(double threshhold) {
		this.f = new BinaryActivationFunction(threshhold);
	}
	
	public double process(double[] input) {
		return f.process(input);
	}

	@Override
	public int hashCode() {
		return f.hashCode();
	}
}
