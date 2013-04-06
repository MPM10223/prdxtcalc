package algo.linreg;

import java.util.Arrays;

import rcaller.RCaller;
import rcaller.RCode;
import sqlWrappers.Database;
import sqlWrappers.SQLDatabase;
import algo.Algorithm;
import algo.AlgorithmDAO;

public class ForwardStepwiseRegression extends Algorithm<RegressionModel> {

	protected double minPValue;
	
	public ForwardStepwiseRegression() {
		this(0.95);
	}
	
	public ForwardStepwiseRegression(double minPValue) {
		this.minPValue = minPValue;
	}

	@Override
	public RegressionModel buildModel(AlgorithmDAO dao) {
		RCaller rc = new RCaller();
		rc.setRscriptExecutable("C:\\Program Files\\R\\R-2.15.2\\bin\\Rscript.exe");
		rc.setRExecutable("C:\\Program Files\\R\\R-2.15.2\\bin\\R.exe");
		RCode r = rc.getRCode();
		
		/*
		> install.packages("RODBC")
		
		> library(RODBC)
		> conn <- odbcDriverConnect('Driver={SQL Server Native Client 10.0};Server=tcp:hwvhpv4cb1.database.windows.net,1433;Database=prdxt;Uid=mmonteiro@hwvhpv4cb1;Pwd=HVD2006a;Encrypt=yes;')
		> pd <- sqlQuery(conn, 'SELECT * FROM vw_problem_1') // make sure this has already dealt with missing data
		
		> model1 <- lm(formula = DV ~ `[best single correlation]`, data=pd)
		> step(object=model1, scope = DV ~ `Stochastic %K` + `Stochastic %D` + `Stochastic slow %D` + Momentum + `Rate of Change` + `LW %R` + `A/D Oscillator` + `Disparity 5d` + `Disparity 10d` + OSCP + CCI + RSI, direction="forward")
		 */
		
		// Connect to DB
		//r.addRCode(String.format("install.packages(\"RODBC\")"));
		r.addRCode(String.format("library(RODBC)"));
		SQLDatabase db = dao.getDb();
		String connString = Database.getAzureODBCConnectionString(db.getServer(), db.getPort(), db.getDatabase(), db.getUserName(), db.getPassword(), true);
		r.addRCode(String.format("conn <- odbcDriverConnect('%s')", connString));
		
		// Load problem data
		r.addRCode(String.format("pd <- sqlQuery(conn, '%s')", dao.getSourceDataQuery(true, true)));
		
		// Find best single-variable correlation as starting point
		// cor(pd[,c("Stochastic %K","Stochastic %D","Stochastic slow %D","Momentum","Rate of Change","LW %R", "A/D Oscillator","Disparity 5d","Disparity 10d","OSCP","CCI","RSI","DV")], use="complete.obs")["DV",]
		
		String ivDvList = dao.getColumnList(", ", "\"", "\"", false, true);
		r.addRCode(String.format("c <- cor(pd[,c(%s)], use=\"complete.obs\")[\"%s\",]", ivDvList, dao.getDvColumn())); //TODO: handle missing data better
		rc.runAndReturnResultOnline("c");
		double[] corr = rc.getParser().getAsDoubleArray("c");
		
		String bestIV = "";
		double bestCorr = 0.0;
		for(int i = 0; i < dao.getIvColumns().length; i++) {
			if(Math.abs(corr[i]) > Math.abs(bestCorr)) {
				bestCorr = corr[i];
				bestIV = dao.getIvColumns()[i];
			}
		}
		
		// Run 1-var regression
		rc.cleanRCode();
		r.addRCode(String.format("model1Var <- lm(formula = `%s` ~ `%s`, data=pd)", dao.getDvColumn(), bestIV));
		
		// Forward Stepwise
		String ivFormula = dao.getColumnList(" + ", "`", "`", false, false);
		r.addRCode(String.format("model <- step(object = model1Var, scope = `%s` ~ %s, direction = \"forward\")", dao.getDvColumn(), ivFormula));
		r.addRCode(String.format("coefficients <- model[\"coefficients\"]"));
		rc.runAndReturnResultOnline("coefficients");
		
		double[] coeff = rc.getParser().getAsDoubleArray("coefficients");
		
		rc.cleanRCode();
		r.addRCode(String.format("terms <- model[\"terms\"]"));
		rc.runAndReturnResultOnline("terms");

		String[] terms = rc.getParser().getAsStringArray("terms");
		String[] termsParsed = terms[0].split(", ");
		String[] vars = termsParsed[2].split(" \\+ ");
		
		int[] inputFeatures = new int[vars.length];
		for(int i = 0; i < vars.length; i++) {
			String var = vars[i].replaceAll("`", "");
			int featureID = dao.getFeatureID(var);
			inputFeatures[i] = featureID;
		}
		
		double[] coefficients = Arrays.copyOfRange(coeff, 1, coeff.length);
		
		double intercept = coeff[0];
		
		double[][] ivRanges = new double[vars.length][2];
		for(int i = 0; i < inputFeatures.length; i++) {
			ivRanges[i] = dao.getFeatureRange(inputFeatures[i]);
		}
		
		return new RegressionModel(inputFeatures, coefficients, intercept, ivRanges);
	}

}
