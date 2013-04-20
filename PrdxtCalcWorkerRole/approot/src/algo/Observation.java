package algo;

import java.util.Map;

public class Observation {
	
	protected String identifier;
	protected Map<Integer, Double> independentVariables;
	protected Double dependentVariable;
	protected Double prediction;
	protected String detail;

	public Observation(Map<Integer, Double> independentVariables, Double dependentVariable) {
		this(null, independentVariables, dependentVariable); // missing detail and identifier
	}
	
	public Observation(String identifier, Map<Integer, Double> independentVariables, Double dependentVariable) {
		this(identifier, independentVariables, dependentVariable, null); // missing detail
	}
	
	public Observation(Map<Integer, Double> independentVariables, Double dependentVariable, String detail) {
		this(null, independentVariables, dependentVariable, detail); // missing identifier
	}
	
	public Observation(String identifier, Map<Integer, Double> independentVariables, Double dependentVariable, String detail) {
		super();
		this.identifier = identifier;
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

	public void setDependentVariable(Double dependentVariable) {
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
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	protected Observation clone() {
		Observation o = new Observation(this.identifier, this.independentVariables, this.dependentVariable);
		o.setPrediction(this.prediction);
		return o;
	}
}
