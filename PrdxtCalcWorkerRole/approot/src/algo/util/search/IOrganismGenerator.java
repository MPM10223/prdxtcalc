package algo.util.search;

public interface IOrganismGenerator<T> {
	
	public T generate(byte[] dna);
	public int getDNASize();

}
