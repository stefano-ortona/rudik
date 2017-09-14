/*
Copyright (c) 2014, DIADEM Team (http://diadem.cs.ox.ac.uk)

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project. 
 */

package asu.edu.rule_miner.rudik.model.rdf.graph;

import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.configuration.Constant;

/**
 * 
 * 
 * @author Stefano Ortona (stefano dot ortona at cs dot ox dot ac dot uk) -
 *         Department of Computer Science - University of Oxford
 * 
 *         Utility class to model a RDF Graph.
 *         Each Node contains a label of type T
 */

public class Graph<T>{

	int equality_types_number = -1;
	
	private int edgesNumber;

	public Graph(){
		this.neighbours=Maps.newConcurrentMap();
		this.literal2lexicalForm = Maps.newHashMap();
		this.examples = Sets.newHashSet();
		this.node2types = Maps.newHashMap();

		Configuration config = ConfigurationFacility.getConfiguration();
		//read number of threads if specified in the conf file
		if(config.containsKey(Constant.CONF_NUM_THREADS)){
			try{
				equality_types_number = config.getInt(Constant.CONF_EQUALITY_TYPES_NUMBER);
			}
			catch(Exception e){
				//do not set it
			}
		}

	}

	public Map<T,Set<Edge<T>>> neighbours;

	public Map<T,String> literal2lexicalForm;

	private Set<Pair<T,T>> examples;

	private Map<T,Set<String>> node2types;

	public void addNode(T label){
		if(this.neighbours.containsKey(label))
			return;
		Set<Edge<T>> neighbors = Sets.newHashSet();
		this.neighbours.put(label, neighbors);

	}

	public void addLiteralNode(T label,String lexicalForm){
		if(this.neighbours.containsKey(label))
			return;
		Set<Edge<T>> neighbors = Sets.newHashSet();
		this.neighbours.put(label, neighbors);
		this.literal2lexicalForm.put(label, lexicalForm);

	}

	public boolean addEdge(T source, T end, String label, boolean bidirectional){
		Edge<T> edge = new Edge<T>(source, end, label);
		return this.addEdge(edge, bidirectional);
	}

	/**
	 * Return true if the operation completed succesfully
	 * @param edge
	 * @param bidirectional
	 * @return
	 */
	public boolean addEdge(Edge<T> edge,boolean bidirectional){
		//cannot add an edge if neither node source nor source edge are in the graph
		if(!this.neighbours.containsKey(edge.getNodeSource())||!this.neighbours.containsKey(edge.getNodeEnd())){
			return false;
		}

		T source = edge.getNodeSource();
		Set<Edge<T>> neighbours = this.neighbours.get(source);

		boolean added = neighbours.add(edge);
		if(added)
			edgesNumber++;

		if(bidirectional){
			T end = edge.getNodeEnd();
			Edge<T> inverseEdge = new Edge<T>(end, source, edge.getLabel());
			inverseEdge.setIsArtificial(true);
			neighbours = this.neighbours.get(end);
			added = neighbours.add(inverseEdge);
			if(added)
				edgesNumber++;
		}

		return true;
	}

	public Set<Edge<T>> getNeighbours(T node){
		return this.neighbours.get(node);
	}

	public Set<T> getNodes(){
		return Sets.newHashSet(this.neighbours.keySet());
	}

	public boolean isLiteral(T label){
		return this.literal2lexicalForm.containsKey(label);
	}

	public String getLexicalForm(T node){
		return this.literal2lexicalForm.get(node);
	}

	public Set<T> getLiteralNodes(){
		return this.literal2lexicalForm.keySet();
	}

	public Set<Pair<T,T>> getExamples(){
		return this.examples;
	}

	public void addExamples(Set<Pair<T,T>> examples){
		this.examples.addAll(examples);
	}

	public boolean containsEdge(T nodeSource, T nodeEnd, String label){
		Edge<T> tempEdge = new Edge<T>(nodeSource,nodeEnd,label);
		return this.neighbours.containsKey(nodeSource) && 
				this.neighbours.get(nodeSource).contains(tempEdge);
	}

	public Set<T> getLiteralsExamples(Set<Pair<T,T>> examples){
		Set<T> analysedNodes = Sets.newHashSet();
		Set<T> nodesToAnalyse = Sets.newHashSet();
		for(Pair<T,T> oneExample:examples){
			nodesToAnalyse.add(oneExample.getLeft());
			nodesToAnalyse.add(oneExample.getRight());
		}

		Set<T> literalOutputNodes = Sets.newHashSet();
		while(nodesToAnalyse.size()>0){
			T currentNode = nodesToAnalyse.iterator().next();
			analysedNodes.add(currentNode);
			nodesToAnalyse.remove(currentNode);

			//if it is not a literal analyse all its neighbours
			Set<Edge<T>> neighbours = this.getNeighbours(currentNode);
			for(Edge<T> oneNeighbour:neighbours){
				T endNode = oneNeighbour.getNodeEnd();
				if(this.isLiteral(endNode)){
					literalOutputNodes.add(endNode);
					continue;
				}
				if(!analysedNodes.contains(endNode))
					nodesToAnalyse.add(endNode);
			}

		}

		return literalOutputNodes;
	}

	/**
	 * Return all literal nodes that are at most distance from at least one of the starting nodes
	 * @param startingNodes
	 * @param distance
	 * @return
	 */
	public Set<T> getLiterals(Set<T> startingNodes, int distance){

		Set<T> outputLiterals = Sets.newHashSet();
		Set<T> toAnalyse = Sets.newHashSet();
		Set<T> analysedNodes = Sets.newHashSet();
		for(T oneStartingNode:startingNodes){
			analysedNodes.clear();
			if(this.isLiteral(oneStartingNode))
				outputLiterals.add(oneStartingNode);
			analysedNodes.add(oneStartingNode);
			toAnalyse.add(oneStartingNode);
			for(int i=0;i<distance;i++){
				Set<T> nextNeighboursToAnalyse = Sets.newHashSet();
				for(T nodeToAnalyse:toAnalyse){
					analysedNodes.add(nodeToAnalyse);
					Set<Edge<T>> oneHopEdges = this.getNeighbours(nodeToAnalyse);
					for(Edge<T> currentOneHopEdge:oneHopEdges){
						T endingNode = currentOneHopEdge.getNodeEnd();
						if(this.isLiteral(endingNode)){
							outputLiterals.add(endingNode);
							continue;
						}
						if(!analysedNodes.contains(endingNode) && i+1 <distance)
							nextNeighboursToAnalyse.add(endingNode);
					}
				}
				toAnalyse.clear();
				toAnalyse.addAll(nextNeighboursToAnalyse);
			}
		}

		return outputLiterals;
	}

	public void addType(T node, String type){
		if(!this.neighbours.containsKey(node))
			return;

		Set<String> previousType = this.node2types.get(node);
		if(previousType==null){
			previousType = Sets.newHashSet();
			this.node2types.put(node, previousType);
		}
		previousType.add(type);
	}

	public Set<String> getTypes(T node){
		Set<String> returningTypes = Sets.newHashSet();
		if(!node2types.containsKey(node) || node2types.get(node)==null)
			return returningTypes;
		returningTypes.addAll(this.node2types.get(node));
		return returningTypes;
	}

	/**
	 * Get set of nodes at distance <= maxDistance from one of the startingNodes that have at least 2 types in common with inputTypes
	 * @param types
	 * @param startingNodes
	 * @param distance
	 * @return
	 */
	public Set<T> getSameTypesNodes(Set<String> inputTypes, Set<T> startingNodes, int maxDistance){

		Set<T> outputNodes = Sets.newHashSet();
		if(inputTypes==null || inputTypes.size()==0 || this.equality_types_number==-1)
			return outputNodes;

		Set<T> toAnalyse = Sets.newHashSet();
		Set<T> analysedNodes = Sets.newHashSet();
		Set<String> otherNodeTypes;

		for(T oneStartingNode:startingNodes){

			otherNodeTypes = this.getTypes(oneStartingNode);
			otherNodeTypes.retainAll(inputTypes);
			if(otherNodeTypes.equals(inputTypes) || 
					(this.equality_types_number>0 && otherNodeTypes.size()>=this.equality_types_number))
				outputNodes.add(oneStartingNode);

			analysedNodes.add(oneStartingNode);
			toAnalyse.add(oneStartingNode);

			for(int i=0;i<maxDistance;i++){
				Set<T> nextNeighboursToAnalyse = Sets.newHashSet();
				for(T nodeToAnalyse:toAnalyse){
					analysedNodes.add(nodeToAnalyse);
					Set<Edge<T>> oneHopEdges = this.getNeighbours(nodeToAnalyse);
					for(Edge<T> currentOneHopEdge:oneHopEdges){
						//avoid != relation
						if(currentOneHopEdge.getLabel().equals(Constant.DIFF_REL))
							continue;
						T endingNode = currentOneHopEdge.getNodeEnd();
						//avoid literals
						if(this.isLiteral(endingNode))
							continue;
						otherNodeTypes = this.getTypes(endingNode);
						otherNodeTypes.retainAll(inputTypes);
						if(otherNodeTypes.equals(inputTypes) || 
								(this.equality_types_number>0 && otherNodeTypes.size()>=this.equality_types_number))
							outputNodes.add(endingNode);

						if(!analysedNodes.contains(endingNode) && i+1 <maxDistance)
							nextNeighboursToAnalyse.add(endingNode);
					}
				}
				toAnalyse.clear();
				toAnalyse.addAll(nextNeighboursToAnalyse);
			}
		}

		return outputNodes;
	}
	
	public int getNodesNumber(){
		return node2types.size() + literal2lexicalForm.size();
	}
	
	public int getNodesEdges(){
		return edgesNumber;
		
	}
}
