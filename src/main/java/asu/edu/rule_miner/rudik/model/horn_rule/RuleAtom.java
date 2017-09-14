package asu.edu.rule_miner.rudik.model.horn_rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.RuleMinerException;

public class RuleAtom {

	private final static Logger LOGGER = LoggerFactory.getLogger(RuleAtom.class.getName());

	private String subject;
	private String object;
	private String relation;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public RuleAtom(String subject, String relation, String object){
		this.subject = subject;
		this.object= object;
		this.relation = relation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result
				+ ((relation == null) ? 0 : relation.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		RuleAtom other = (RuleAtom) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		} else if (!relation.equals(other.relation))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}

	public String toString(){
		return relation+"("+subject+","+object+")";
	}

	public static RuleAtom readRuleAtom(String ruleAtomString){
		String ruleAtomStringCopy = ruleAtomString;
		if(!ruleAtomStringCopy.contains("("))
			throw new RuleMinerException("The rule '"+ruleAtomString+"' cannot be parsed as rule atom",LOGGER);
		String relation = ruleAtomStringCopy.substring(0,ruleAtomStringCopy.indexOf("("));
		ruleAtomStringCopy=ruleAtomStringCopy.substring(relation.length()+1,ruleAtomStringCopy.length()-1);
		if(ruleAtomStringCopy.contains("(")||ruleAtomStringCopy.contains(")")||!ruleAtomStringCopy.contains(",")){
			throw new RuleMinerException("The rule '"+ruleAtomString+"' cannot be parsed as rule atom",LOGGER);
		}
		String subject = ruleAtomStringCopy.split(",")[0];
		String object = ruleAtomStringCopy.split(",")[1];
		return new RuleAtom(subject, relation, object);
	}

}
