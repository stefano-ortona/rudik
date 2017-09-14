package asu.edu.rule_miner.rudik.rule_generator;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.model.statistic.StatisticsContainer;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;


/**
 * Query the knwoeldge base through sparql using multiple threads
 * 
 * @author ortona
 *
 */
public class OneExampleRuleDiscovery implements Runnable{

	private final static Logger LOGGER = LoggerFactory.getLogger(OneExampleRuleDiscovery.class.getName());

	private Set<String> input;

	private SparqlExecutor queryEndpoint;
	
	Graph<String> graph;
	
	Map<String,Set<String>> entity2types;


	public OneExampleRuleDiscovery(Set<String> input,Graph<String> graph,SparqlExecutor queryEndpoint,
			Map<String,Set<String>> entity2types){
		this.input = input;
		this.queryEndpoint = queryEndpoint;
		this.graph=graph;
		this.entity2types=entity2types;
	}

	public void run(){
		for(String entity:input){
			try{
				StatisticsContainer.increaseExpansionQuery();
				long startTime = System.currentTimeMillis();
				this.queryEndpoint.executeQuery(entity,this.graph,this.entity2types);
				long endTime = System.currentTimeMillis();
				StatisticsContainer.increaseTimeExpansionQuery(endTime-startTime);

			}

			catch(Exception e){
				LOGGER.error("Thread with entity '{}' was not able to complet its job.",entity.toString(),e);
			}
		}
	}

}
