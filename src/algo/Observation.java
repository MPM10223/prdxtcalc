package algo;

import java.util.Map;

public class Observation {
	
	protected Map<Integer, Double> independentVariables;
	protected double dependentVariable;
	protected Double prediction;
	protected String detail;

	public Observation(Map<Integer, Double> independentVariables, double dependentVariable) {
		this(independentVariables, dependentVariable, null);
	}
	
	public Observation(Map<Integer, Double> independentVariables, double dependentVariable, String detail) {
		super();
		this.independentVariables = independentVariables;
		this.dependentVariable = dependentVariable;
		this.detail = detail;
	}
	
	public Map<Integer, Double> getIndependentVariables() {
		return independentVariables;
	}

	public void setIndependentVariables(Map<Integer, Double> independentVariables) {
		this.independentVariables = independentVariables;
	}

	public double getDependentVariable() {
		return dependentVariable;
	}

	public void setDependentVariable(double dependentVariable) {
		this.dependentVariable = dependentVariable;
	}

	public Double getPrediction() {
		return prediction;
	}

	public void setPrediction(Double prediction) {
		this.prediction = prediction;
	}
	
	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	protected Observation clone() {
		Observation o = new Observation(this.independentVariables, this.dependentVariable);
		o.setPrediction(this.prediction);
		return o;
	}
}
