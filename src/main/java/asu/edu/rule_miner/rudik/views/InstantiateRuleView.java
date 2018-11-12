package asu.edu.rule_miner.rudik.views;

import java.util.*;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import io.dropwizard.views.View;

public class InstantiateRuleView extends View {
    private final List<String> output;
    private final String kg;
    private final String instantiatedRule;


    public InstantiateRuleView(List<String> result, String kg,String instantiatedRule) {
        super("instantiation.ftl");
	this.output=result;
       this.instantiatedRule=instantiatedRule;
        this.kg=kg;
    }


    
    public List<String> getOutput() {
        return output;
    } 

   public String getKg() {
        return kg;
    }
    public String getInstantiatedRule() {
        return instantiatedRule;
    }

}
