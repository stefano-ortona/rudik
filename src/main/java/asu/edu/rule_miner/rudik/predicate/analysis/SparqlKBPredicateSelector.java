package asu.edu.rule_miner.rudik.predicate.analysis;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;

public class SparqlKBPredicateSelector implements KBPredicateSelector{
  
  private final static Logger LOGGER = LoggerFactory.getLogger(SparqlKBPredicateSelector.class.getName());
  
  final SparqlExecutor executor = ConfigurationFacility.getSparqlExecutor();

  @Override
  public Pair<String, String> getPredicateTypes(String inputPredicate) {
    LOGGER.info("Retrieving predicate types for predicate '{}'...",inputPredicate);
    final Pair<String,String> predicates = executor.getPredicateTypes(inputPredicate);
    LOGGER.info("Predicates retrieved: '{}'.",predicates);
    return predicates;
  }

  @Override
  public Set<String> getAllPredicates() {
    LOGGER.info("Retrieving all predicates from the KG...");
    final Set<String> predicates =  executor.getAllPredicates();
    LOGGER.info("Retrieved a total of {} predicates.",predicates.size());
    return predicates;
  }

  @Override
  public Map<String, Pair<String, String>> getAllPredicateTypes() {
    LOGGER.info("Retrieving most common subject and object types for all predicates in the KG...");
    final Set<String> allPredicates = getAllPredicates();
    final Map<String,Pair<String,String>> predicate2types = Maps.newHashMap();
    for(String onePredicate:allPredicates) {
      final Pair<String,String> types = getPredicateTypes(onePredicate);
      //do not include null types
      if(types != null && types.getLeft() != null && types.getRight() != null) {
        predicate2types.put(onePredicate, types);
      }
    }
    LOGGER.info("Retrieved a total of {} meaningful predicate types.",predicate2types.size());
    return predicate2types;
  }

}
