package asu.edu.rule_miner.rudik.api.model;

import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;

public class HornRuleInstantiation {
  private List<RuleAtom> instantiatedAtoms;
  private String ruleSubject;
  private String ruleObject;

  public HornRuleInstantiation() {
    this.instantiatedAtoms = Lists.newArrayList();
  }

  public List<RuleAtom> getInstantiatedAtoms() {
    return this.instantiatedAtoms;
  }

  public void setInstantiatedAtoms(List<RuleAtom> atoms) {
    this.instantiatedAtoms = atoms;
  }

  public void addInstantiatedAtom(RuleAtom atom) {
    this.instantiatedAtoms.add(atom);
  }

  public String getRuleSubject() {
    return ruleSubject;
  }

  public void setRuleSubject(String ruleSubject) {
    this.ruleSubject = ruleSubject;
  }

  public String getRuleObject() {
    return ruleObject;
  }

  public void setRuleObject(String ruleObject) {
    this.ruleObject = ruleObject;
  }

}
