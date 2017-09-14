package asu.edu.rule_miner.rudik.configuration;

import java.lang.reflect.Constructor;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.RuleMinerException;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;

public class ConfigurationFacility {

	private static Configuration instance;

	private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationFacility.class.getName());

	public synchronized static Configuration getConfiguration(){
		if(instance == null)
			initialiseConfiguration(Constant.CONF_FILE);
		return instance;
	}

	/**
	 * Set the global configuration file to the input file name
	 * @param confFileName
	 */
	public static void setConfigurationFile(String confFileName){
		initialiseConfiguration(confFileName);
	}


	private static void initialiseConfiguration(String confFileName){
		BasicConfigurator.configure(new NullAppender());

		Configuration config = null;
		try {
			config = new XMLConfiguration(confFileName);
		} catch (ConfigurationException e) {
			throw new RuleMinerException("Unable to read conf file at ''"+
					Constant.CONF_FILE, e);
		}
		instance = config;
		//read the logger properties
		String logFile = config.getString(Constant.CONF_LOGGER);
		if(logFile != null)
			PropertyConfigurator.configure(logFile);
	}

	public static SparqlExecutor getSparqlExecutor(){

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
		return endpoint;
	}
	
}
