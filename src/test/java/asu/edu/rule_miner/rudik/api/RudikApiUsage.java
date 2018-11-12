package asu.edu.rule_miner.rudik.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import asu.edu.rule_miner.rudik.api.model.HornRuleInstantiation;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult.RuleType;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.configuration.ConfigurationParameterConfigurator;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import junit.framework.Assert;

public class RudikApiUsage {

  private static RudikApi API;

  @BeforeClass
  public static void bringUp() {
    // initialise the API object by passing the configuration file to be used and the timeout (in seconds) for the
    // maximum waiting time for an operation
    // if not conf file is specified, the default one will be used and the default 10minutes timeout will be used
    API = new RudikApi("src/main/config/DbpediaConfiguration.xml", 5 * 60); // timeout set to 5 minutes
    // set the max number of instantiated facts to generate when executing rules against the KB
    API.setMaxInstantiationNumber(500);
  }

  @Before
  public void initialiseParameters() {
    // set some of the parameters through the ConfigurationParameterConfigurator
    // NOTE: after you set the parameters, the parameters will keep the new set values
    ConfigurationParameterConfigurator.setAlphaParameter(0.3);
    ConfigurationParameterConfigurator.setBetaParameter(0.7);
    ConfigurationParameterConfigurator.setMaxRuleLength(2);

    // set maximum number of positive/negative examples - randomly selected
    ConfigurationParameterConfigurator.setPositiveExamplesLimit(15);
    ConfigurationParameterConfigurator.setNegativeExamplesLimit(15);

    // specify number max number of incoming and outgoing edges
    ConfigurationParameterConfigurator.setIncomingEdgesLimit(1000);
    ConfigurationParameterConfigurator.setOutgoingEdgesLimit(1000);

    // check new values have been set
    Assert.assertEquals(15, ConfigurationFacility.getConfiguration().getInt("naive.sparql.limits.examples.positive"));
    Assert.assertEquals(15, ConfigurationFacility.getConfiguration().getInt("naive.sparql.limits.examples.negative"));
  }

  @Test
  public void testComputeRules() {
    // like this we can compute positive rules giving a target predicate - target predicate must contain the full name
    final String targetPredicate = "http://dbpedia.org/ontology/spouse";
    // use discoverNegativeRules to discover negative rules
    final RudikResult result = API.discoverPositiveRules(targetPredicate,20,20);

    // The following shows how to inspect the result
    // iterate all over discovered results
    for (final HornRuleResult oneResult : result.getResults()) {
      // get target predicate of the result = http://dbpedia.org/ontology/spouse
      oneResult.getTargetPredicate();
      // get type of the rule = positive
      oneResult.getType();

      // 1) get the output Horn Rule
      final HornRule rule = oneResult.getOutputRule();
      // iterate over all atoms of the rule
      for (final RuleAtom atom : rule.getRules()) {
        // get <subject,relation,object> of one atom - this could be something like <subject,child,v0> (with variables)
        atom.getSubject();
        atom.getRelation();
        atom.getObject();
      }

      // 2) get all instantiation of the rule over the KB
      final List<HornRuleInstantiation> ruleInst = oneResult.getAllInstantiations();
      // iterate over all instantiation
      for (final HornRuleInstantiation oneInst : ruleInst) {
        // get <subject,object> of the instantiation - this could be something like <Barack_Obama,Michelle_Obama>
        oneInst.getRuleSubject();
        oneInst.getRuleObject();
        // iterate over all instantiated atoms of the rule
        for (final RuleAtom atom : oneInst.getInstantiatedAtoms()) {
          // get <subject,relation,object> of one atom - this could be something like
          // <Barack_Obama,child,Sasha_Obama> (now the atoms are instantiated, so they contain actual values and not
          // variables)
          atom.getSubject();
          atom.getRelation();
          atom.getObject();
        }
      }

      // 3) get the sorrounding graphs of all entities involved in the rule
      final Graph<String> sorroundingGraph = oneResult.getSorroundingGraph();
      // get all the edges sorrounding Barack Obama
      final Set<Edge<String>> allBarackEdges = sorroundingGraph.getNeighbours("Barack_Obama");
      // iterate over all edges
      if (allBarackEdges != null) {
        for (final Edge<String> oneEdge : allBarackEdges) {
          // get <nodeStart,edgeName,nodeEnd> of the edge. This could be something like <Barack_Obama,bornIn,USA> or
          // <Sasha_Obama,parent,Barack_Obama>
          oneEdge.getNodeStart();
          oneEdge.getLabel();
          oneEdge.getNodeEnd();
        }
      }
      // get the ontology type of an entity. This could be something like Person, President, Politician
      final Set<String> types = sorroundingGraph.getTypes("Barack_Obama");

      // 4) get covered examples of the rule (NOTE: this will be null in case of instantiateRule API)
      // get the generation set examples covered
      final Set<Pair<String, String>> genExamples = oneResult.getGenerationExamples();
      for (final Pair<String, String> oneExample : genExamples) {
        // get one example - things like <Barack_Obama,Michelle_Obama>
        final String subject = oneExample.getLeft();
        final String object = oneExample.getRight();
      }
      // get the validation set examples covered - these could be potential errors
      final Set<Pair<String, String>> valExamples = oneResult.getValidationExamples();
      for (final Pair<String, String> oneExample : valExamples) {
        final String subject = oneExample.getLeft();
        final String object = oneExample.getRight();
      }
    }
  }

  @Test
  public void instantiateRule() {
    // this reads a rule in the form of string, converts it to a HornRule, and instantiate over the KB
    // the rule in the form of string
    final String ruleString = "http://dbpedia.org/ontology/birthPlace(object,v0) & http://dbpedia.org/ontology/deathPlace(subject,v0)";
    // the target predicate
    final String targetPredicate = "http://dbpedia.org/ontology/spouse";
    // the rule type
    final RuleType type = RuleType.positive;
    // parse and create the HornRule
    final HornRule rule = HornRule.createHornRule(ruleString);
    // instantiate rule and create the result
    final RudikResult result = API.instantiateSingleRule(rule, targetPredicate, type);
    Assert.assertNotNull(result);
    // see testComputeRules on how to inspect and use the result
    final Graph<String> sorGraph = result.getResults().get(0).getSorroundingGraph();
  }

  @Test
  public void getAllKBRelations() {
    // get all relations from DBpedia KB
    Collection<String> allRelations = API.getAllKBRelations();
    // iterate over all relations
    for (final String oneRel : allRelations) {
      System.out.println("Found a relation from the KB: " + oneRel);
    }
    // get all relations from Yago, need to create a new API object with a different conf file
    final RudikApi newApi = new RudikApi("src/main/config/YagoConfiguration.xml", 5 * 60);
    allRelations = newApi.getAllKBRelations();
    // iterate over all relations
    for (final String oneRel : allRelations) {
      System.out.println("Found a relation from the KB: " + oneRel);
    }
  }

}
