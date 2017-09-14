package asu.edu.rule_miner.rudik.model.rdf.graph;

/**
 * 
 * @author ortona
 * 
 * Utility class to model an edge in a RDF graph.
 * Each edge contains a node source and node end with label T, and a String label for the edge
 *
 * @param <T>
 */
public class Edge <T> {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isArtificial ? 1231 : 1237);
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((nodeEnd == null) ? 0 : nodeEnd.hashCode());
		result = prime * result + ((nodeSource == null) ? 0 : nodeSource.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge<T> other = (Edge<T>) obj;
		if (isArtificial != other.isArtificial)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (nodeEnd == null) {
			if (other.nodeEnd != null)
				return false;
		} else if (!nodeEnd.equals(other.nodeEnd))
			return false;
		if (nodeSource == null) {
			if (other.nodeSource != null)
				return false;
		} else if (!nodeSource.equals(other.nodeSource))
			return false;
		return true;
	}

	public T nodeSource;

	private boolean isArtificial;

	public Edge(T source, T end, String label){
		this.label=label;
		this.nodeSource = source;
		this.nodeEnd = end;
		this.isArtificial = false;
	}

	public void setIsArtificial(boolean isArtificial){
		this.isArtificial = isArtificial;
	}

	public boolean isArtificial(){
		return this.isArtificial;
	}


	public T getNodeSource() {
		return nodeSource;
	}

	public void setNodeSource(T nodeSource) {
		this.nodeSource = nodeSource;
	}

	public T getNodeEnd() {
		return nodeEnd;
	}

	public void setNodeEnd(T nodeEnd) {
		this.nodeEnd = nodeEnd;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public T nodeEnd;

	public String label;

	public String toString() {
		return (this.nodeSource!=null ? this.nodeSource.toString() : "") +
				(this.label!=null ? this.label.toString() : "") +
				(this.nodeEnd!=null ? this.nodeEnd.toString() : "");
	}


}
