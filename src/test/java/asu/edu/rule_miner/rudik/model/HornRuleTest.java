package asu.edu.rule_miner.rudik.model;


import org.junit.Before;
import org.junit.Test;

import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import junit.framework.Assert;

public class HornRuleTest {

	private HornRule hornRule;
	@Before
	public void bringUp(){
		hornRule = new HornRule();
		int count=0;
		RuleAtom r1 = new RuleAtom(HornRule.START_NODE,"r1",HornRule.LOOSE_VARIABLE_NAME+count);
		hornRule.addRuleAtom(r1);

		RuleAtom r2 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+count,"r2",HornRule.LOOSE_VARIABLE_NAME+(++count));
		hornRule.addRuleAtom(r2);

		RuleAtom r3 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+count,"r3",HornRule.LOOSE_VARIABLE_NAME+(++count));
		hornRule.addRuleAtom(r3);

		RuleAtom r4 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+count,"r4",HornRule.END_NODE);
		hornRule.addRuleAtom(r4);

	}

	@Test
	public void testHornRuleValidity(){
		Assert.assertTrue(hornRule.isValid());

		HornRule otherRule = new HornRule();

		RuleAtom r1 = new RuleAtom(HornRule.START_NODE,"r1",HornRule.LOOSE_VARIABLE_NAME+0);
		otherRule.addRuleAtom(r1);

		RuleAtom r2 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+0,"r2",HornRule.LOOSE_VARIABLE_NAME+1);
		otherRule.addRuleAtom(r2);

		Assert.assertFalse(otherRule.isValid());

		RuleAtom r4 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+2,"r4",HornRule.END_NODE);
		otherRule.addRuleAtom(r4);

		Assert.assertFalse(otherRule.isValid());


		RuleAtom r3 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+1,"r3",HornRule.LOOSE_VARIABLE_NAME+2);
		otherRule.addRuleAtom(r3);

		Assert.assertTrue(otherRule.isValid());
	}

	@Test
	public void testHornRuleEquality(){
		HornRule otherRule = new HornRule();

		RuleAtom r1 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+0,"r4",HornRule.END_NODE);
		RuleAtom r2 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+1,"r3",HornRule.LOOSE_VARIABLE_NAME+0);
		RuleAtom r3 = new RuleAtom(HornRule.LOOSE_VARIABLE_NAME+2,"r2",HornRule.LOOSE_VARIABLE_NAME+1);
		RuleAtom r4 = new RuleAtom(HornRule.START_NODE,"r1",HornRule.LOOSE_VARIABLE_NAME+2);
		RuleAtom r5 = new RuleAtom(HornRule.START_NODE,"r1",HornRule.LOOSE_VARIABLE_NAME+1);
		
		//different number of atoms, they cannot be equal
		otherRule.addRuleAtom(r1);
		Assert.assertFalse(hornRule.equals(otherRule));
		
		//different number of counting variables, they cannot be equals
		otherRule.addRuleAtom(r2);
		otherRule.addRuleAtom(r3);
		otherRule.addRuleAtom(r5);
		Assert.assertFalse(hornRule.equals(otherRule));
		
		//same number of atoms and of variables, plus exchanging variables result in the same rule
		otherRule = new HornRule();
		otherRule.addRuleAtom(r1);
		otherRule.addRuleAtom(r2);
		otherRule.addRuleAtom(r3);
		otherRule.addRuleAtom(r4);
		Assert.assertTrue(hornRule.equals(otherRule));
		
		
		

	}

}
