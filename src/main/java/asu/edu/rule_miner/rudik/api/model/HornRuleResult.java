package asu.edu.rule_miner.rudik.api.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;

/**
 * This class wraps the result returned by rudik APIs when discovering Horn Rule
 * A result is made of a HornRule, a target predicate, a type (positive/negative),
 * the instantiation of the rule over the KB, and the sorrounding graph (1-hop steps)
 * of all the entities that match the instantiated rule
 *
 * @author Stefano Ortona <stefano.ortona@gmail.com>
 *
 */
public class HornRuleResult {
  public enum RuleType {
    positive, negative;
  }

  private HornRule outputRule;
  private String targetPredicate;
  private RuleType type;
  private List<HornRuleInstantiation> allInstantiations;
  private Graph<String> sorroundingGraph;

  public HornRule getOutputRule() {
    return outputRule;
  }

  public void setOutputRule(HornRule outputRule) {
    this.outputRule = outputRule;
  }

  public String getTargetPredicate() {
    return targetPredicate;
  }

  public void setTargetPredicate(String targetPredicate) {
    this.targetPredicate = targetPredicate;
  }

  public RuleType getType() {
    return type;
  }

  public void setType(RuleType type) {
    this.type = type;
  }

  public List<HornRuleInstantiation> getAllInstantiations() {
    return allInstantiations;
  }

  public void setAllInstantiations(List<HornRuleInstantiation> allInstantiations) {
    this.allInstantiations = allInstantiations;
  }

  public Graph<String> getSorroundingGraph() {
    return sorroundingGraph;
  }

  public void setSorroundingGraph(Graph<String> sorroundingGraph) {
    this.sorroundingGraph = sorroundingGraph;
  }

  public Set<Pair<String, String>> getGenerationExamples() {
    return this.outputRule.getCoveredExamples();
  }

  public Set<Pair<String, String>> getValidationExamples() {
    return this.outputRule.getValidationCoveredExamples();
  }

}
