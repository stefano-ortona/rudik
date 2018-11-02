package asu.edu.rule_miner.rudik.views;

import java.util.*;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import io.dropwizard.views.View;

public class DiscoverRulesView extends View {
    private final List<String> output;
    private final String kg;



    private final String endpoint;

    public DiscoverRulesView(List<String> result, String kg, String endpoint) {
        super("output.ftl");
	this.output=result;
        this.kg=kg;
        this.endpoint=endpoint;
    }


    
    public List<String> getOutput() {
        return output;
    }
 
    public String getKg() {
        return kg;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
