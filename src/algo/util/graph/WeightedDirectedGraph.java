package algo.util.graph;

public class WeightedDirectedGraph<TNode> {
	
	TNode[] nodes;
	double[][] edges;
	
	public WeightedDirectedGraph(TNode[] nodes, double[][] edges) {
		int n = nodes.length;
		
		if(edges.length != n) throw new IllegalArgumentException();
		for(int i = 0; i < n; i++) {
			if(edges[i].length != n) throw new IllegalArgumentException();
		}
		
		this.nodes = nodes;
		this.edges = edges;
	}
	
	public TNode[] getNodes() {
		return this.nodes;
	}
	
	public double getEdgeWeight(int node1, int node2) {
		return edges[node1][node2];
	}

	public TNode getNodeAt(int nodeIndex) {
		return this.nodes[nodeIndex];
	}
	
	@Override
	public int hashCode() {
		double product = 1;
		for(int i = 0; i < edges.length; i++) {
			for(int j = 0; j < edges[i].length; j++) {
				product *= edges[i][j];
			}
		}
		
		for(int i = 0; i < nodes.length; i++) {
			product *= nodes[i].hashCode();
		}
		
		return (int)Math.rint(product);
	}
}
