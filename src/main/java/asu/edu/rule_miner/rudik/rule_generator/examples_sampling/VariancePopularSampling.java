package asu.edu.rule_miner.rudik.rule_generator.examples_sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;



public class VariancePopularSampling {
	
	double alpha, beta, gamma, subWeight, objWeight;
	int subjectLimit, objectLimit;
	boolean isTopK; // to indicate whether it is topK sampling or uniform sampling
	
	public VariancePopularSampling(double alpha, double beta, double gamma, double subWeight, double objWeight, int subjectLimit, int objectLimit, boolean isTopK){
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.subWeight = subWeight;
		this.objWeight = objWeight;
		this.isTopK = isTopK;
		if(subjectLimit < 0)
			this.subjectLimit = 2; // only for the computation of functionality, doesn't effect the example generation for actual in-degree & out-degree
//			this.subjectLimit = (int)Double.POSITIVE_INFINITY;
		else
			this.subjectLimit = subjectLimit;
		if(objectLimit < 0)
			this.objectLimit = 2; // only for the computation of functionality, doesn't effect the example generation for actual in-degree & out-degree
//			this.objectLimit = (int)Double.POSITIVE_INFINITY;
		else
			this.objectLimit = objectLimit;
	}
	
	public ArrayList<Double> computeEntityStats(String entity, Set<Edge<String>> edges, int limit){
		ArrayList<Double> toReturn = new ArrayList<Double>();
		HashMap<String,ArrayList<Double>> diversePopularity = new HashMap<String,ArrayList<Double>>();
		ArrayList<String> inverseFunctionalSet = new ArrayList<String>(); 
		ArrayList<String> labelSet = new ArrayList<String>();
		for(Edge<String> oneEdge : edges){
			String label = oneEdge.getLabel();
			ArrayList<Double> values = new ArrayList<Double>();
			if(diversePopularity.containsKey(label) && diversePopularity.get(label).get(0)!=null)
				values.add(diversePopularity.get(label).get(0)+1.0);
			else {
				values.add(1.0);
				labelSet.add(label); //new label added
			}
			if(diversePopularity.containsKey(label) && diversePopularity.get(label).get(0)>limit){
				if(!inverseFunctionalSet.contains(label))
					inverseFunctionalSet.add(label);
			}
			
	/*		if(diversePopularity.containsKey(label) && diversePopularity.get(label).get(0)>limit){
				if(diversePopularity.get(label).get(1)!=null)
					values.add(diversePopularity.get(label).get(1)+1.0);
				else
					values.add(1.0);
			}
			else if(diversePopularity.containsKey(label) && diversePopularity.get(label).get(0)<=limit){
				if(diversePopularity.containsKey(label) && diversePopularity.get(label).get(1)!=null)
					values.add(diversePopularity.get(label).get(1));
				else
					values.add(0.0);
			}
			else if(!diversePopularity.containsKey(label))
				values.add(0.0);
	*/		diversePopularity.put(label, values);
		}
		toReturn.add(new Double(diversePopularity.keySet().size())); //added diversity as the number of relations an entity is connected to
		Collection<ArrayList<Double>> valueSet = diversePopularity.values();
		double aggPopularity = 0.0, aggInverseFunctionality = 0.0;
		for(ArrayList<Double> values : valueSet){
			aggPopularity += values.get(0);
		//	aggInverseFunctionality += values.get(1);
		}
		aggInverseFunctionality = (double)inverseFunctionalSet.size() / (double)labelSet.size();
		toReturn.add(aggPopularity);
		toReturn.add(aggInverseFunctionality);
		return toReturn;
	}
	
//	public double computePopularity(String entity, Set<Edge<String>> edges){
//		return 0.0;
//	}
//	
//	public double computeInverseFunctionality(String entity, Set<Edge<String>> edges){
//		return 0.0;
//	}
	
	private static HashMap sortByValues(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }

	public Set<Pair<String,String>> sampleExamples(Set<Pair<String,String>> allExamples, Graph<String> graph, 
			int genExamplesLimit){
		//compute a weighted score for each pair of entities. alpha * diversity + beta * popularity - gamma * inverse_functionality
		//sort by the weighted score
		if(genExamplesLimit < 0)
			genExamplesLimit = (int)Double.POSITIVE_INFINITY;
		Set<Pair<String,String>> sampleSet = Sets.newHashSet();
		HashMap<String,Double> scorePerEntity = new HashMap<String, Double>();
		HashMap<Pair<String,String>,Double> scorePerPair = new HashMap<Pair<String,String>,Double>();
		
		double maxDiversity = 0.0, maxPopularity = 0.0, maxFunctionality = 0.0;
		HashMap<String,Double> diversityMap = new HashMap<String,Double>();
		HashMap<String,Double> popularityMap = new HashMap<String,Double>();
		HashMap<String,Double> functionalityMap = new HashMap<String,Double>();
		for(Pair<String,String> example:allExamples){
			String subject = example.getLeft();
			String object = example.getRight();
			Set<Edge<String>> subIncidentEdges = graph.getNeighbours(subject);
			Set<Edge<String>> objIncidentEdges = graph.getNeighbours(object);
			double diversity=0.0, popularity=0.0, functionality=0.0;
			ArrayList<Double> entityStats;
			if(!diversityMap.containsKey(subject) && diversityMap.get(subject) == null){
				entityStats = this.computeEntityStats(subject, subIncidentEdges,subjectLimit);
				diversity = entityStats.get(0);
				popularity = entityStats.get(1);
				functionality = 1.0 - entityStats.get(2);
				if(diversity > maxDiversity)
					maxDiversity = diversity;
				if(popularity > maxPopularity)
					maxPopularity = popularity;
				if(functionality > maxFunctionality)
					maxFunctionality = functionality;
				diversityMap.put(subject, diversity);
				popularityMap.put(subject, popularity);
				functionalityMap.put(subject, functionality);
			}
			if(!diversityMap.containsKey(object) && diversityMap.get(object) == null){
				entityStats = this.computeEntityStats(object, objIncidentEdges, objectLimit);
				diversity = entityStats.get(0);
				popularity = entityStats.get(1);
				functionality = 1.0 - entityStats.get(2);
				if(diversity > maxDiversity)
					maxDiversity = diversity;
				if(popularity > maxPopularity)
					maxPopularity = popularity;
				if(functionality > maxFunctionality)
					maxFunctionality = functionality;
				diversityMap.put(object, diversity);
				popularityMap.put(object, popularity);
				functionalityMap.put(object, functionality);
			}
			
			
		}
		
		int i=0;
		for(Pair<String,String> example:allExamples){
			String subject = example.getLeft();
			String object = example.getRight();
			Set<Edge<String>> subIncidentEdges = graph.getNeighbours(subject);
			Set<Edge<String>> objIncidentEdges = graph.getNeighbours(object);
			double diversity=diversityMap.get(subject), popularity = popularityMap.get(subject), functionality = functionalityMap.get(subject), 
					subScore, objScore, pairScore;
			ArrayList<Double> entityStats;
			subScore = 0.0;
			if(!scorePerEntity.containsKey(subject) && scorePerEntity.get(subject) == null){
				if(maxDiversity>0)
					diversity /= maxDiversity;
				if(maxPopularity > 0)
					popularity /= maxPopularity;
				if(maxFunctionality > 0)
					functionality /= maxFunctionality;
				subScore = alpha * diversity + beta * popularity + gamma * functionality;
				scorePerEntity.put(subject, subScore);
			}
			else
				subScore = scorePerEntity.get(subject);
			
			
			diversity=diversityMap.get(object);
			popularity = popularityMap.get(object);
			functionality = functionalityMap.get(object);
			
			objScore = 0.0;
			if(!scorePerEntity.containsKey(object) && scorePerEntity.get(object) == null){
				if(maxDiversity > 0)
					diversity /= maxDiversity;
				if(maxPopularity > 0)
					popularity /= maxPopularity;
				if(maxFunctionality > 0)
					functionality /= maxFunctionality;
				objScore = alpha * diversity + beta * popularity + gamma * functionality;
				scorePerEntity.put(object, objScore);
			}
			else
				objScore = scorePerEntity.get(object);

			
			pairScore = this.subWeight * subScore + this.objWeight * objScore;
			scorePerPair.put(example, pairScore);
		}
		
		HashMap<Pair<String,String>,Double> sortedScorePerPair = sortByValues(scorePerPair);
		int count = 0;
		for(Double value:sortedScorePerPair.values()){
			if(value < 0.0)
				count++;
		}
		int keyNum = 0;
		HashSet<String> seenSet = new HashSet<String>();
		//Adding code for uniform sampling
		if(!isTopK){
			int totalSize = sortedScorePerPair.size();
			int sampleInterval = totalSize/genExamplesLimit;
			if(sampleInterval ==0)
				sampleInterval = 1;
			int pairIndex = -1;
			boolean gotSamplePoint = true;
			for(Pair<String,String> key:sortedScorePerPair.keySet()){
				pairIndex = (pairIndex + 1) % sampleInterval;
				if(pairIndex == 0)
					gotSamplePoint = false;
				if(gotSamplePoint)
					continue;
				if(keyNum >= genExamplesLimit)
					break;
				if(key.getLeft() == key.getRight() || seenSet.contains(key.getLeft()) || seenSet.contains(key.getRight()))
					continue;
				sampleSet.add(key);
				seenSet.add(key.getLeft());
				seenSet.add(key.getRight());
				gotSamplePoint = true;
				keyNum++;
			}
		}
		else{
			for(Pair<String,String> key:sortedScorePerPair.keySet()){
				if(keyNum >= genExamplesLimit)
					break;
				if(key.getLeft() == key.getRight() || seenSet.contains(key.getLeft()) || seenSet.contains(key.getRight()))
					continue;
				sampleSet.add(key);
				seenSet.add(key.getLeft());
				seenSet.add(key.getRight());
				keyNum++;
			}
		}

		return sampleSet;
	}


	public static void main(String[] args){

		Set<Pair<String,String>> examples = Sets.newHashSet();

		//how to use the graph
		Graph<String> graph = new Graph<String>();

		String entity = "http://dbpedia.org/resource/Barack_Obama";

		Set<Edge<String>> edges = graph.getNeighbours(entity);
		VariancePopularSampling vps = new VariancePopularSampling(0.5, 0.4, 0.1, 0.5, 0.5, -1, -1, true); //alpha, beta, gamma, subWeight, objWeight, isTopK
	//	vps.sampleExamples(allExamples, graph, genExamplesLimit);

		for(Edge<String> oneEdge : edges){
			oneEdge.getNodeSource();

			oneEdge.getNodeEnd();

			//name of the relation
			oneEdge.getLabel();

			//if it is false-> edge is outgoing, if it is true, edge is incoming
			oneEdge.isArtificial();
		}
	}

}
