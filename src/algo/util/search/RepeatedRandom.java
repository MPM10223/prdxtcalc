package algo.util.search;

import java.util.Random;

import algo.util.dao.ILog;

public class RepeatedRandom<TOrganism> extends Search<TOrganism> {
	
	protected int iterationLimit;
	
	public RepeatedRandom(ILog log, IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int iterationLimit) {
		super(log, generator, fitness);
		this.iterationLimit = iterationLimit;
	}
	
	public RepeatedRandom(ILog log, IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int iterationLimit, int seed) {
		super(log, generator, fitness, seed);
		this.iterationLimit = iterationLimit;
	}
	
	public RepeatedRandom(ILog log, IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int iterationLimit, Random r) {
		super(log, generator, fitness, r);
		this.iterationLimit = iterationLimit;
	}
	
	@Override
	public TOrganism search() {
		return search(this.iterationLimit);
	}
	
	public TOrganism search(int iterationLimit) {
		double best = Double.NEGATIVE_INFINITY;
		TOrganism winner = null;
		
		log.logStepSequenceStarted(iterationLimit);
		
		for(int i = 0; i < iterationLimit; i++) {
			
			byte[] dna = new byte[this.generator.getDNASize()];
			r.nextBytes(dna);
			
			TOrganism organism = this.generator.generate(dna);
			double fitness = this.fitness.evaluate(organism);
			
			//System.out.println(String.format("Iteration %d - Fitness: %f, Champion: %f", i+1, fitness, best));
			
			if(fitness > best) {
				log.logMessage(String.format("Iteration %d - New Champion @ Fitness: %f", i+1, fitness));
				winner = organism;
				best = fitness;
			}
			
			log.logStepCompleted();
		}
		
		log.logStepSequenceCompleted();
		
		return winner;
	}
}
