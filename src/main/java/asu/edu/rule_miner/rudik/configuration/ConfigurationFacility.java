package asu.edu.rule_miner.rudik.configuration;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
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

  public synchronized static Configuration getConfiguration() {
    if (instance == null) {
      initialiseConfiguration(Constant.CONF_FILE);
    }
    return instance;
  }

  /**
   * Set the global configuration file to the input file name
   * @param confFileName
   */
  public static void setConfigurationFile(String confFileName) {
    initialiseConfiguration(confFileName);
  }

  private static void initialiseConfiguration(String confFileName) {
    BasicConfigurator.configure();

    Configuration config = null;
    String confLogMessage;
    try {
      // first try to read explictly set configuration
      config = new XMLConfiguration(confFileName);
      confLogMessage = StringUtils.join("Found xml configuration file at ", "'", confFileName, "'.");
    } catch (final ConfigurationException e) {
      // if unable, read package configuration
      try {
        config = new XMLConfiguration(ConfigurationFacility.class.getResource("Configuration.xml"));
        confLogMessage = "Did not find a explicit configuration file, using default packagae configuration.";
      } catch (final Exception e1) {
        throw new RuleMinerException("Unable to read configuration file at ''" + Constant.CONF_FILE, e1);
      }
    }
    instance = config;
    // read the logger properties
    final String logFile = config.getString(Constant.CONF_LOGGER);
    if (logFile != null) {
      PropertyConfigurator.configure(logFile);
    }
    LOGGER.info(confLogMessage);
    LOGGER.info("Log4j properties file to be specified at '{}'.", logFile);
  }

  public static SparqlExecutor getSparqlExecutor() {

    if (!ConfigurationFacility.getConfiguration().containsKey(Constant.CONF_SPARQL_ENGINE)) {
      throw new RuleMinerException("Sparql engine parameters not found in the configuration file.", LOGGER);
    }

    final Configuration subConf = ConfigurationFacility.getConfiguration().subset(Constant.CONF_SPARQL_ENGINE);

    if (!subConf.containsKey("class")) {
      throw new RuleMinerException("Need to specify the class implementing the Sparql engine "
          + "in the configuration file under parameter 'class'.", LOGGER);
    }

    SparqlExecutor endpoint;
    try {
      final Constructor<?> c = Class.forName(subConf.getString("class")).getDeclaredConstructor(Configuration.class);
      c.setAccessible(true);
      endpoint = (SparqlExecutor) c.newInstance(new Object[] { subConf });
    } catch (final Exception e) {
      throw new RuleMinerException("Error while instantiang the sparql executor enginge.", e, LOGGER);
    }
    return endpoint;
  }

  public static void resetConfiguration() {
    initialiseConfiguration(Constant.CONF_FILE);
  }

  public static void setGraphGenericTypes(final List<String> genericTypes) {
    ConfigurationParameterConfigurator.setGraphGenericTypes(genericTypes);
  }

  public static void setSparqlEndpoint(final String endpoint) {
    ConfigurationParameterConfigurator.setSparqlEndpoint(endpoint);
  }

  public static void setMaxRuleLength(int maxRuleLen) {
    ConfigurationParameterConfigurator.setMaxRuleLength(maxRuleLen);
  }

  public static void setAlphaBetaParameter(double alpha, double beta) {
    if ((alpha + beta) != 1) {
      LOGGER.warn("Trying to set values of alpha and beta to '{}' and '{}' that do not add up to 1.0, skipping.", alpha,
          beta);
    }
    ConfigurationParameterConfigurator.setAlphaParameter(alpha);
    ConfigurationParameterConfigurator.setBetaParameter(beta);
  }

  public static void setRelationToAvoid(List<String> avoidRelation) {
    ConfigurationParameterConfigurator.setRelationToAvoid(avoidRelation);
  }

  public static void setDisequalityRelation(int disequality) {
    ConfigurationParameterConfigurator.setDisequalityRelation(disequality);
  }

  public static void setIncomingEdgesLimit(int incomingEdgesLimit) {
    ConfigurationParameterConfigurator.setIncomingEdgesLimit(incomingEdgesLimit);
  }

  public static void setGraphIri(String graphIri) {
    ConfigurationParameterConfigurator.setGraphIri(graphIri);
  }

  public static void setIncludeLiteral(boolean includeLiteral) {
    ConfigurationParameterConfigurator.setIncludeLiteral(includeLiteral);
  }

  public static void setOutgoingEdgesLimit(int outgoingEdgesLimit) {
    ConfigurationParameterConfigurator.setOutgoingEdgesLimit(outgoingEdgesLimit);
  }

  /**
   * Set graph prefixes to be used when executing SPARQL queries
   * name2uri is a map where the key is the name of the prefix and the value is the URI of the prefix
   * @param name2uri
   */
  public static void setGraphPrefixes(Map<String, String> name2uri) {
    ConfigurationParameterConfigurator.setGraphPrefixes(name2uri);
  }

  public static void setRelationTargetPrefix(List<String> targetPrefix) {
    ConfigurationParameterConfigurator.setTargetPrefix(targetPrefix);
  }

  public static void setTypeRelationPrefixes(String typePrefix) {
    ConfigurationParameterConfigurator.setTypeRelationPrefix(typePrefix);
  }

}
