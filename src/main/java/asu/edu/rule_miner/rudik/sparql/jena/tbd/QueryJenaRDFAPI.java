package asu.edu.rule_miner.rudik.sparql.jena.tbd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.RuleMinerException;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.sparql.jena.QueryJenaLibrary;

public class QueryJenaRDFAPI extends QueryJenaLibrary{

	private Dataset dataset;

	private final static Logger LOGGER = LoggerFactory.getLogger(QueryJenaRDFAPI.class.getName());

	public QueryJenaRDFAPI(Configuration config) {
		super(config);
		if(!config.containsKey("parameters.directory"))
			throw new RuleMinerException("Cannot initialise Jena API without specifying the "
					+ "database directory in the configuration file.", LOGGER);
		String directory = config.getString("parameters.directory");
		Dataset dataset = TDBFactory.createDataset(directory) ;
		this.dataset = dataset;
	}

	@Override
	public ResultSet executeQuery(String sparqlQuery) {
		dataset.begin(ReadWrite.READ) ;
		QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, dataset);
		this.openResource = qExec;
		ResultSet results = qExec.execSelect() ;
		dataset.end();
		return results;
	}
	
	public static void main(String[] args) throws Exception{
		ConfigurationFacility.getConfiguration();
//		Dataset dataset = TDBFactory.createDataset("/Users/sortona/Documents/KDR/Data/DBPedia/jena") ;
//		long start = System.currentTimeMillis();
//		dataset.begin(ReadWrite.READ);
//		String entity = "http://dbpedia.org/resource/Spain";
//		QueryExecution qExec = QueryExecutionFactory.create("select * {{SELECT * where {?subject ?predicate <"+entity+">.} LIMIT 5000}"
//				+ " UNION {SELECT * where {<"+entity+"> ?predicate ?object.}}}", dataset);
//		ResultSet results = qExec.execSelect() ;
//		
//		Map<String,Set<String>> output = Maps.newHashMap();
//		
//		while(results.hasNext()){
//			QuerySolution solution = results.next();
//			String relation = solution.get("?predicate").toString();
//			String object = solution.get("?subject")!=null ? solution.get("?subject").toString() : solution.get("?object").toString();
//
//			Set<String> currentExamples = output.get(relation);
//			if(currentExamples==null){
//				currentExamples=Sets.newHashSet();
//				output.put(relation, currentExamples);
//			}
//			currentExamples.add(object);
//		}
//		dataset.end();
//		System.out.println((System.currentTimeMillis()-start)/1000.);
//		
//		
//		QueryJenaRDFAPI e = new QueryJenaRDFAPI(ConfigurationFacility.getConfiguration().subset("naive.sparql"));
//		//e.query();
	}
	
	public void query() throws Exception{

		int numThreads = 100;
		BufferedReader reader = new BufferedReader(new FileReader(new File("dbpedia_founder_neg_examples")));
		String line;
		String []lineSplit;

		int i=0;
		List<Set<String>> splitLists = Lists.newArrayList();
		while((line=reader.readLine())!=null){
			if(i==numThreads)
				i=0;

			if(splitLists.size()<=i){
				splitLists.add(new HashSet<String>());
			}
			Set<String> currentPair = splitLists.get(i);
			lineSplit=line.split("\t");
			currentPair.add(lineSplit[0]);
			i++;
		}
		reader.close();


		long start = System.currentTimeMillis();
		Map<String,Set<String>> output = Maps.newConcurrentMap();
		List<Thread> activeThreads = Lists.newArrayList();
		Dataset dataset = TDBFactory.createDataset("/Users/sortona/Documents/KDR/Data/DBPedia/jena") ;
		for(i=0;i<numThreads;i++){
			Thread current_thread = new Thread(new OneThread(splitLists.get(i),output,dataset), "Thread"+i);
			activeThreads.add(current_thread);
			current_thread.start();

		}
		for(Thread t:activeThreads){
			t.join();
		}

		System.out.println("End running time:"+(System.currentTimeMillis()-start)/1000.);
		System.out.println(output.size());

	}

	private class OneThread implements Runnable{


		private Set<String> examples;

		private Map<String,Set<String>> output;
		
		Dataset dataset;


		public OneThread(Set<String> examples, Map<String,Set<String>> output, Dataset dataset){
			this.examples = examples;
			this.output=output;
			this.dataset = dataset;
		}

		public void run(){
			try{
				QueryExecution qExec;
				for(String oneExample:examples){
					dataset.begin(ReadWrite.READ);
					long start=System.currentTimeMillis();
					//encodedExample = EncodingUtility.escapeSQLCharacter(oneExample);
					qExec = QueryExecutionFactory.create("SELECT * where {<"+oneExample+"> ?predicate ?object.}", dataset);
					ResultSet results = qExec.execSelect() ;
					
					while(results.hasNext()){
						QuerySolution solution = results.next();
						String relation = solution.get("?predicate").toString();
						String object = solution.get("?object").toString();

						Set<String> currentExamples = output.get(relation);
						if(currentExamples==null){
							currentExamples=Sets.newHashSet();
							output.put(relation, currentExamples);
						}
						currentExamples.add(object);
					}

					qExec = QueryExecutionFactory.create("SELECT * where {?subject ?predicate <"+oneExample+">.} LIMIT 5000", dataset);
					results = qExec.execSelect() ;
					
					while(results.hasNext()){
						QuerySolution solution = results.next();
						String relation = solution.get("?predicate").toString();
						String object = solution.get("?subject").toString();

						Set<String> currentExamples = output.get(relation);
						if(currentExamples==null){
							currentExamples=Sets.newHashSet();
							output.put(relation, currentExamples);
						}
						currentExamples.add(object);
					}
					dataset.end();
					LOGGER.debug(((System.currentTimeMillis()-start)/1000.)+"");
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally{
				LOGGER.debug("Thread {} done",Thread.currentThread().getId());
			}

		}

	}

}
