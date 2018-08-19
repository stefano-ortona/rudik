package asu.edu.rule_miner.rudik.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult.RuleType;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.predicate.analysis.KBPredicateSelector;
import asu.edu.rule_miner.rudik.predicate.analysis.SparqlKBPredicateSelector;
import asu.edu.rule_miner.rudik.rule_generator.DynamicPruningRuleDiscovery;
import asu.edu.rule_miner.rudik.rule_generator.HornRuleDiscoveryInterface;

public class RudikApi {

  private HornRuleDiscoveryInterface ruleDiscovery;
  private KBPredicateSelector kbAnalysis;
  private SorroundingGraphGeneration graphGeneration;
  private RuleInstanceGeneration ruleInstantiation;

  // by default, timeout set to 10 minutes
  private final int timeout = 10 * 60;

  private int maxInstantiationNumber = 1000;

  /**
   * Initialize RudikApi with a specific configuration file that contains all parameters used by RuDiK
   *
   * @param filePath
   * @param timeout - timeout, in seconds, to specify the max waiting time for each operation. If an operation takes longer
   * than the timeout, then the operation is killed and it returns an empty result
   */
  public RudikApi(final String filePath, int timeout) {
    ConfigurationFacility.setConfigurationFile(filePath);
    initialiseObjects(timeout);
  }

  public RudikApi() {
    initialiseObjects(timeout);
  }

  private void initialiseObjects(int timeout) {
    this.ruleDiscovery = new DynamicPruningRuleDiscovery();
    this.kbAnalysis = new SparqlKBPredicateSelector();
    this.graphGeneration = new SorroundingGraphGeneration(ruleDiscovery.getSparqlExecutor(), timeout);
    this.ruleInstantiation = new RuleInstanceGeneration(ruleDiscovery, timeout);
  }

  /**
   * Compute positive rules for the target predicate and return a RudikResult
   *
   * @param targetPredicate
   * @return
   */
  public RudikResult discoverPositiveRules(final String targetPredicate) {
    return discoverRules(targetPredicate, RuleType.positive);
  }

  /**
   * Compute negative rules for the target predicate and return a RudikResult
   *
   * @param targetPredicate
   * @return
   */
  public RudikResult discoverNegativeRules(final String targetPredicate) {
    return discoverRules(targetPredicate, RuleType.negative);
  }

  private RudikResult discoverRules(final String targetPredicate, RuleType type) {
    final Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(targetPredicate);
    final String typeSubject = subjectObjectType.getLeft();
    final String typeObject = subjectObjectType.getRight();
    final Set<String> relations = Sets.newHashSet(targetPredicate);
    final Set<Pair<String, String>> positiveExamples = ruleDiscovery.generatePositiveExamples(relations, typeSubject,
        typeObject);
    final Set<Pair<String, String>> negativeExamples = ruleDiscovery.generateNegativeExamples(relations, typeSubject,
        typeObject);

    Map<HornRule, Double> outputRules = null;
    if (type == RuleType.positive) {
      outputRules = ruleDiscovery.discoverPositiveHornRules(negativeExamples, positiveExamples, relations, typeSubject,
          typeObject);
    } else {
      outputRules = ruleDiscovery.discoverNegativeHornRules(negativeExamples, positiveExamples, relations, typeSubject,
          typeObject);
    }
    return buildResult(outputRules.keySet(), targetPredicate, type, typeSubject, typeObject);
  }

  /**
   * Instantiate a HornRule over the KB and return a Rudik result containing all the information of the rules instantiated
   *
   * @param rule
   * @param targetPredicate
   * @param type
   * @return
   */
  public RudikResult instantiateSingleRule(HornRule rule, String targetPredicate, RuleType type) {
    final Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(targetPredicate);
    final String typeSubject = subjectObjectType.getLeft();
    final String typeObject = subjectObjectType.getRight();
    return buildResult(Lists.newArrayList(rule), targetPredicate, type, typeSubject, typeObject);
  }

  private RudikResult buildResult(Collection<HornRule> allRules, String targetPredicate, RuleType type, String subType,
      String objType) {
    final RudikResult result = new RudikResult();
    allRules.forEach(rule -> result.addResult(buildIndividualResult(rule, targetPredicate, type, subType, objType)));
    return result;
  }

  private HornRuleResult buildIndividualResult(HornRule oneRule, String targetPredicate, RuleType type, String subjType,
      String objType) {
    final HornRuleResult result = new HornRuleResult();
    result.setOutputRule(oneRule);
    result.setTargetPredicate(targetPredicate);
    result.setType(type);
    result.setAllInstantiations(
        ruleInstantiation.instantiateRule(targetPredicate, oneRule, subjType, objType, type, maxInstantiationNumber));
    final Set<String> targetEntities = Sets.newHashSet();
    result.getAllInstantiations().forEach(r -> {
      targetEntities.add(r.getRuleSubject());
      targetEntities.add(r.getRuleObject());
    });
    result.setSorroundingGraph(graphGeneration.generateSorroundingGraph(targetEntities));
    return result;
  }

  public Collection<String> getAllKBRelations() {
    return kbAnalysis.getAllPredicates();
  }

  public void setMaxInstantiationNumber(int number) {
    this.maxInstantiationNumber = number;
  }

}
