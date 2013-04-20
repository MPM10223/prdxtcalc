package algo;

import algo.util.dao.ILog;

public abstract class Algorithm<T extends PredictiveModel> {
	
	protected ILog log;
	
	public ILog getLog() {
		return log;
	}

	public void setLog(ILog log) {
		this.log = log;
	}

	public abstract T buildModel(AlgorithmDAO dao);

}
