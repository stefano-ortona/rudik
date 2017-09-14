package asu.edu.rule_miner.rudik;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.rule_generator.DynamicPruningRuleDiscovery;

/**
 * Unit test for simple App.
 */
public class ClientTest 
{
  private final DynamicPruningRuleDiscovery rudik = new DynamicPruningRuleDiscovery();
  private final static Logger LOGGER = LoggerFactory.getLogger(ClientTest.class.getName());
  
  protected List<HornRule> executeRudikNegativeRules(final Set<String> relationNames, final String subjectType, final String objectType){
    final Instant startTime = Instant.now();
    
    //get positive and negative examples
    final Set<Pair<String,String>> negativeExamples = rudik.
        generateNegativeExamples(relationNames, subjectType, objectType, false, false);
    final Set<Pair<String,String>> positiveExamples = rudik.
        generatePositiveExamples(relationNames, subjectType, objectType);

    //compute outputs
    final List<HornRule> outputRules = rudik.
        discoverNegativeHornRules(negativeExamples, positiveExamples, relationNames, subjectType, objectType);
    
    final Instant endTime = Instant.now();
    LOGGER.info("----------------------------COMPUTATION ENDED----------------------------");
    LOGGER.info("Final computation time: {} seconds.",(endTime.toEpochMilli()-startTime.toEpochMilli())/1000.);
    LOGGER.info("----------------------------Final output rules----------------------------");
    for(final HornRule oneRule:outputRules){
      LOGGER.info("{}",oneRule);
    }
    return outputRules;
  }
  
  protected Map<HornRule,Double> executeRudikAllNegativeRules(final Set<String> relationNames, final String subjectType, final String objectType, final int maxRulesNumber){
    final Instant startTime = Instant.now();
    
    //get positive and negative examples
    final Set<Pair<String,String>> negativeExamples = rudik.
        generateNegativeExamples(relationNames, subjectType, objectType, false, false);
    final Set<Pair<String,String>> positiveExamples = rudik.
        generatePositiveExamples(relationNames, subjectType, objectType);

    //compute outputs
    final Map<HornRule,Double> outputRules = rudik.
        discoverAllNegativeHornRules(negativeExamples, positiveExamples, relationNames, subjectType, objectType,maxRulesNumber);
    
    final Instant endTime = Instant.now();
    LOGGER.info("----------------------------COMPUTATION ENDED----------------------------");
    LOGGER.info("Final computation time: {} seconds.",(endTime.toEpochMilli()-startTime.toEpochMilli())/1000.);
    LOGGER.info("----------------------------Final output rules----------------------------");
    for(final HornRule oneRule:outputRules.keySet()){
      LOGGER.info("Rule:{}\tScore:{}",oneRule,outputRules.get(oneRule));
    }
    return outputRules;
  }
}
