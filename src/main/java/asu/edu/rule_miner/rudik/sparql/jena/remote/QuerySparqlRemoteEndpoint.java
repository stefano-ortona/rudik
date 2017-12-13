package asu.edu.rule_miner.rudik.sparql.jena.remote;

import org.apache.commons.configuration.Configuration;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.RuleMinerException;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.sparql.jena.QueryJenaLibrary;

public class QuerySparqlRemoteEndpoint extends QueryJenaLibrary {

  private final static Logger LOGGER = LoggerFactory.getLogger(QuerySparqlRemoteEndpoint.class.getName());

  private final String sparqlEndpoint;

  /**
   * prefixRelation can be equal to null
   * @param endpoint
   * @param prefixQuery
   * @param input
   */
  public QuerySparqlRemoteEndpoint(Configuration config) {
    super(config);
    if (!config.containsKey(Constant.CONF_SPARQL_ENDPOINT)) {
      throw new RuleMinerException("Cannot initialise Virtuoso engine without specifying the "
          + "sparql url endpoint in the configuration file.", LOGGER);
    }
    this.sparqlEndpoint = config.getString(Constant.CONF_SPARQL_ENDPOINT);
  }

  @Override
  public ResultSet executeQuery(String sparqlQuery) {
    final QueryExecution qexec = QueryExecutionFactory.sparqlService(this.sparqlEndpoint, sparqlQuery);
    qexec.setTimeout(MAX_QUERY_RUN_TIME);
    final ResultSet results = qexec.execSelect();
    this.openResource = qexec;
    return results;
  }

}
