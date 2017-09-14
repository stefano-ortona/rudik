package asu.edu.rule_miner.rudik.rule_generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.RuleMinerException;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.model.statistic.StatisticsContainer;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;

public abstract class HornRuleDiscovery implements HornRuleDiscoveryInterface{

	protected int numThreads = 1;

	protected int maxRuleLen = 3;

	Integer subjectLimit = null; 

	Integer objectLimit = null; 
	
	Integer generationSmartLimit = null;
	
	Double alphaSmart = null;
	
	Double betaSmart = null;
	
	Double gammaSmart = null;
	
	Double subWeightSmart = null;
	
	Double objWeightSmart = null;
	
	Boolean isTopK = null;

	private final static Logger LOGGER = 
			LoggerFactory.getLogger(OneExampleRuleDiscovery.class.getName());

	/**
	 * Read config parameters
	 */
	public HornRuleDiscovery(){
		Configuration config = ConfigurationFacility.getConfiguration();
		//read number of threads if specified in the conf file
		if(config.containsKey(Constant.CONF_NUM_THREADS)){
			try{
				numThreads = config.getInt(Constant.CONF_NUM_THREADS);
			}
			catch(Exception e){
				LOGGER.error("Error while trying to read the numbuer of "
						+ "threads configuration parameter. Set to 1.");
			}
		}


		//read maxRuleLength if specified in the conf file
		if(ConfigurationFacility.getConfiguration().containsKey(Constant.CONF_MAX_RULE_LEN)){
			try{
				maxRuleLen = config.getInt(Constant.CONF_MAX_RULE_LEN);
			}
			catch(Exception e){
				LOGGER.error("Error while trying to read the "
						+ "maximum rule length configuration parameter. Set to 3.");
			}
		}

	}

	public abstract List<HornRule> discoverPositiveHornRules(Set<Pair<String,String>> negativeExamples, Set<Pair<String,String>> positiveExamples,
			Set<String> relations, String typeSubject, String typeObject);

	public abstract List<HornRule> discoverPositiveHornRules(Set<Pair<String,String>> negativeExamples, Set<Pair<String,String>> positiveExamples,
			Set<String> relations, String typeSubject, String typeObject,boolean subjectFunction, boolean objectFunction);

	public abstract List<HornRule> discoverNegativeHornRules(Set<Pair<String,String>> negativeExamples, Set<Pair<String,String>> positiveExamples,
			Set<String> relations, String typeSubject, String typeObject);


	/**
	 * Utlity method to expand graphs for an input set of nodes to analyse
	 * @param toAnalyse
	 * @param graph
	 * @param node2neghbours
	 * @param entity2types
	 * @param numThreads
	 */
	protected void expandGraphs(Set<String> toAnalyse, Graph<String> graph, Map<String,Set<String>> entity2types,
			int numThreads){

		List<Thread> activeThreads = Lists.newLinkedList();
		List<Set<String>> currentInputs = 
				this.splitNodesThreads(toAnalyse, numThreads);

		int i=0;
		for(Set<String> currentInput:currentInputs){

			i++;
			// Create the thread and add it to the list
			SparqlExecutor executor = this.getSparqlExecutor();
			final Thread current_thread = new Thread(new OneExampleRuleDiscovery(currentInput,graph,
					executor,entity2types), "Thread"+i);
			activeThreads.add(current_thread);

			// start the thread and querydbpedia
			try {
				current_thread.start();
			}
			// if thread is not able to finish its job, just continue and save
			// into the log the problem
			catch (final IllegalThreadStateException e) {
				LOGGER.error("Thread with id '{}' encountered a problem.", current_thread.getId(), e);
				continue;
			}
		}

		for (final Thread t : activeThreads) {
			try {

				t.join(0);

			} catch (final InterruptedException e) {
				LOGGER.error("Thread with id '{}' was unable to complete its job.", t.getId());
				continue;
			}
		}
	}

	private List<Set<String>> splitNodesThreads(Set<String> input, int numThread){
		List<Set<String>> outputThreads = Lists.newLinkedList();

		int i=0;
		for(String currentNode:input){
			if(i==numThread)
				i=0;
			if(i>=outputThreads.size()){
				Set<String> currentSet = Sets.newHashSet();
				outputThreads.add(currentSet);
			}
			Set<String> currentSet = outputThreads.get(i);
			currentSet.add(currentNode);
			i++;
		}
		return outputThreads;
	}


	public Set<Pair<String,String>> generatePositiveExamples(
			Set<String> relations, String typeSubject, String typeObject){
		long startTime = System.currentTimeMillis();
		Set<Pair<String,String>> examples =  this.getSparqlExecutor().generatePositiveExamples(relations, typeSubject, typeObject);
		long endTime = System.currentTimeMillis();
		StatisticsContainer.setPositiveSetTime((endTime-startTime)/1000.);
		return examples;

	}

	/**
	 * Generated limited set of positive examples
	 * @param relations
	 * @param typeSubject
	 * @param typeObject
	 * @param limit
	 * @return
	 */
	public Set<Pair<String,String>> generatePositiveExamples(
			Set<String> relations, String typeSubject, String typeObject, int limit){
		long startTime = System.currentTimeMillis();
		SparqlExecutor executor = this.getSparqlExecutor();
		executor.setPosExamplesLimit(limit);
		Set<Pair<String,String>> examples =  executor.generatePositiveExamples(relations, typeSubject, typeObject);
		long endTime = System.currentTimeMillis();
		StatisticsContainer.setPositiveSetTime((endTime-startTime)/1000.);
		return examples;

	}

	public Set<Pair<String,String>> generateNegativeExamples(
			Set<String> relations, String typeSubject, String typeObject, 
			boolean subjectFunction, boolean objectFunction){
		long startTime = System.currentTimeMillis();
		Set<Pair<String,String>> examples = this.getSparqlExecutor().generateUnionNegativeExamples(relations, 
				typeSubject, typeObject,subjectFunction,objectFunction);
		long endTime = System.currentTimeMillis();
		StatisticsContainer.setNegativeSetTime((endTime-startTime)/1000.);
		return examples;

	}

	public Set<Pair<String,String>> generateNegativeExamples(
			Set<String> relations, String typeSubject, String typeObject){
		return this.getSparqlExecutor().generateUnionNegativeExamples(relations, 
				typeSubject, typeObject,false,false);

	}

	/**
	 * Generate limited negative examples
	 * @param relations
	 * @param typeSubject
	 * @param typeObject
	 * @param limit
	 * @return
	 */
	public Set<Pair<String,String>> generateNegativeExamples(
			Set<String> relations, String typeSubject, String typeObject, int limit){
		SparqlExecutor executor = this.getSparqlExecutor();
		executor.setNegExamplesLimit(limit);
		return executor.generateUnionNegativeExamples(relations, 
				typeSubject, typeObject,false,false);

	}

	public Set<Pair<String,String>> getKBExamples(String query, String subject, 
			String object){
		return this.getSparqlExecutor().getKBExamples(query, subject, object, false);
	}

	public Set<Pair<String,String>> readExamples(File inputFile){
		try{
			return this.getSparqlExecutor().readExamplesFromFile(inputFile);
		}
		catch(IOException e){
			LOGGER.error("Error while reading the input examples file '{}'",
					inputFile.getAbsolutePath(),e);
		}
		return null;

	}

	/**
	 * Read the sparql executor from configuration file and instantiate it
	 * @return
	 */
	protected SparqlExecutor getSparqlExecutor(){

		if(!ConfigurationFacility.getConfiguration().containsKey(Constant.CONF_SPARQL_ENGINE))
			throw new RuleMinerException("Sparql engine parameters not found in the configuration file.", 
					LOGGER);

		Configuration subConf = ConfigurationFacility.getConfiguration().subset(Constant.CONF_SPARQL_ENGINE);

		if(!subConf.containsKey("class"))
			throw new RuleMinerException("Need to specify the class implementing the Sparql engine "
					+ "in the configuration file under parameter 'class'.", LOGGER);

		SparqlExecutor endpoint;
		try{
			Constructor<?> c = Class.forName(subConf.getString("class")).
					getDeclaredConstructor(Configuration.class);
			c.setAccessible(true);
			endpoint = (SparqlExecutor) 
					c.newInstance(new Object[] {subConf});
		}
		catch(Exception e){
			throw new RuleMinerException("Error while instantiang the sparql executor enginge.", e,LOGGER);
		}
		
		if(subjectLimit!=null)
			endpoint.setSubjectLimit(subjectLimit);
		if(objectLimit!=null)
			endpoint.setObjectLimit(objectLimit);
		
		return endpoint;
	}

	public void setSubjectLimit(int limit){
		this.subjectLimit = limit;
	}

	public void setObjectLimit(int limit){
		this.objectLimit = limit;
	}
	
	public void setGenerationSmartLimit(int limit){
		this.generationSmartLimit = limit;
	}

	public void setSmartWeights(double alpha_smart, double beta_smart, double gamma_smart, double sub_weight, double obj_weight){
		this.alphaSmart = alpha_smart;
		this.betaSmart = beta_smart;
		this.gammaSmart = gamma_smart;
		this.subWeightSmart = sub_weight;
		this.objWeightSmart = obj_weight;
	}
	
	public void setIsTopK(boolean isTopKValue){
		this.isTopK = isTopKValue;
	}

}
