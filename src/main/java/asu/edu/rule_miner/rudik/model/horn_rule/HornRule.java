package asu.edu.rule_miner.rudik.model.horn_rule;

import java.util.List;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Lists;

import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.configuration.Constant;

/**
 * A HornRule is made of a set of RuleAtom
 * A RuleAtom contains two variables, each variable can be either START_NODE, END_NODE or a LOOSE_VARIABLES
 * A HornRule is valid if contains START_NODE and END_NODE at least once and each other variable at least twice
 * @author sortona
 *
 */
public class HornRule {

	public static final String START_NODE = "subject";
	public static final String END_NODE = "object";

	public static final String LOOSE_VARIABLE_NAME = "v";

	protected String startingVariable;

	protected String currentVariable;

	protected List<RuleAtom> rules;

	protected int variableCount = 0;

	public HornRule(){
		this.rules = Lists.newArrayList();
		this.variableCount = 0;
	}

	/**
	 * addRuleAtom
	 * @param rule
	 * @param newVariable
	 */
	public void addRuleAtom(RuleAtom rule, boolean newVariable){

		if(rules.contains(rule))
			return;

		rules.add(rule);

		//increment variable count if it is a new variable
		if(newVariable)
			this.variableCount++;
	}

	public void addRuleAtom(RuleAtom rule){

		if(rules.contains(rule))
			return;

		//check if it is a new variable
		boolean newSubject = true;
		boolean newObject = true;
		for(RuleAtom atom:rules){
			if(atom.getSubject().equals(rule.getSubject()) || atom.getObject().equals(rule.getSubject()))
				newSubject = false;
			if(atom.getSubject().equals(rule.getObject()) || atom.getObject().equals(rule.getObject()))
				newObject = false;
		}
		if(newSubject)
			this.variableCount++;

		if(newObject)
			this.variableCount++;

		rules.add(rule);
	}

	/**
	 * If they have an empty set of rules check the start variable
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rules == null || rules.size()==0) ? startingVariable.hashCode() : rules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HornRule other = (HornRule) obj;
		if (rules == null) {
			if (other.rules != null)
				return false;
		} else {
			if (!rules.equals(other.rules))
				return false;
			else 
				if(rules.size()==0) 
					return startingVariable.equals(other.startingVariable); 
		}
		return true;
	}

	/**
	 * Get the number of rule atoms in the rule
	 * @return
	 */
	public int getLen(){
		return this.rules.size();
	}

	@Override
	public String toString(){
		if(this.rules.size()==0)
			return "Empty Rule";
		String hornRule = "";
		for(RuleAtom rule:rules){
			hornRule+=rule+" & ";
		}
		return hornRule.substring(0,hornRule.length()-3);
	}

	/**
	 * A HornRule is valid iff each variable appear at least twice and start and end at least once
	 * @return
	 */
	public boolean isValid(){

		Set<String> seenVariables = Sets.newHashSet();
		seenVariables.add(START_NODE);
		seenVariables.add(END_NODE);
		Set<String> countOneVariable = Sets.newHashSet();
		countOneVariable.add(START_NODE);
		countOneVariable.add(END_NODE);
		for(RuleAtom rule:rules){
			String firstVariable = rule.getSubject();
			String secondVariable = rule.getObject();

			if(seenVariables.contains(firstVariable)){
				countOneVariable.remove(firstVariable);
			}
			else{
				seenVariables.add(firstVariable);
				countOneVariable.add(firstVariable);
			}

			if(seenVariables.contains(secondVariable)){
				countOneVariable.remove(secondVariable);
			}
			else{
				seenVariables.add(secondVariable);
				countOneVariable.add(secondVariable);
			}
		}
		return countOneVariable.size()==0;

	}

	/**
	 * Return thes set of rule atoms
	 * @return
	 */
	public Set<RuleAtom> getRules(){
		return Sets.newHashSet(this.rules);
	}

	/**
	 * A rule is expandible only if it contains less than maxAtomLen atoms
	 * @return
	 */
	public boolean isExpandible(int maxAtomLen){
		if(this.getLen()<maxAtomLen)
			return true;
		return false;
	}

	public Set<RuleAtom> getEquivalentRule(){
		Set<RuleAtom> alternativeRulesAtom = Sets.newHashSet();

		//get the total number of variables different from start and node
		Set<String> looseVariables = Sets.newHashSet();
		for(RuleAtom oneAtom:this.rules){
			if(!oneAtom.getSubject().equals(MultipleGraphHornRule.START_NODE)&&
					!oneAtom.getSubject().equals(MultipleGraphHornRule.END_NODE))
				looseVariables.add(oneAtom.getSubject());
			if(!oneAtom.getObject().equals(MultipleGraphHornRule.START_NODE)&&
					!oneAtom.getObject().equals(MultipleGraphHornRule.END_NODE))
				looseVariables.add(oneAtom.getObject());
		}

		int variablesSize = looseVariables.size()-1;
		if(variablesSize<=1){
			alternativeRulesAtom.addAll(rules);
			return alternativeRulesAtom;
		}

		for(RuleAtom rule:this.rules){
			String subject = rule.getSubject();
			if(!subject.equals(HornRule.START_NODE)&&!subject.equals(HornRule.END_NODE)){
				subject = "v"+(variablesSize-Integer.parseInt(subject.replaceAll("v", "")));
			}
			String object = rule.getObject();
			if(!object.equals(HornRule.START_NODE)&&!object.equals(HornRule.END_NODE)){
				object = "v"+(variablesSize-Integer.parseInt(object.replaceAll("v", "")));
			}
			alternativeRulesAtom.add(new RuleAtom(subject, rule.getRelation(), object));
		}

		return alternativeRulesAtom;


	}

	public String getStartingVariable(){
		return this.startingVariable;
	}

	public boolean isInequalityExpandible(){
		if(this.rules.size()==0)
			return false;

		//check current variable different from start and end
		if(this.currentVariable.equals(START_NODE)||this.currentVariable.equals(END_NODE))
			return false;

		//check it already exists and inequality or contains both start and end
		boolean containsStart = false;
		boolean containsEnd = false;
		for(RuleAtom oneAtom:this.rules){
			if(oneAtom.getRelation().equals(Constant.DIFF_REL))
				return false;
			if(oneAtom.getSubject().equals(START_NODE)||oneAtom.getObject().equals(START_NODE))
				containsStart = true;
			if(oneAtom.getSubject().equals(END_NODE)||oneAtom.getObject().equals(END_NODE))
				containsEnd = true;
		}

		if(containsStart&&containsEnd)
			return false;

		//elegible
		return true;
	}

	/**
	 * Read a rule atom from a string representation
	 * @param ruleString
	 * @return
	 */
	public static Set<RuleAtom> readHornRule(String ruleString){
		String []atomString = ruleString.split(" & ");
		Set<RuleAtom> hornRule = Sets.newHashSet();
		for(String oneAtomString:atomString){
			String relation = oneAtomString.substring(0,oneAtomString.indexOf("("));
			oneAtomString = oneAtomString.substring(relation.length()+1,oneAtomString.length()-1);
			RuleAtom oneAtom = new RuleAtom(oneAtomString.split(",")[0], relation, oneAtomString.split(",")[1]);
			hornRule.add(oneAtom);
		}

		return hornRule;
	}

}
