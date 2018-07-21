package asu.edu.rule_miner.rudik.api.model;

import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

/**
 * This class represents results of discovering Horn Rules with rudik.
 * Rudik result is a set of HornRuleResult, each representing a different rule discovered over the KB
 * with some additional information
 *
 * @author Stefano Ortona <stefano.ortona@meltwater.com>
 *
 */
public class RudikResult {

  private List<HornRuleResult> results;

  public RudikResult() {
    this.results = Lists.newLinkedList();
  }

  public List<HornRuleResult> getResults() {
    return results;
  }

  public void setResults(List<HornRuleResult> results) {
    this.results = results;
  }

  public void addResult(HornRuleResult result) {
    this.results.add(result);
  }

}
