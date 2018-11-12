package asu.edu.rule_miner.rudik.views;

import io.dropwizard.views.View;

import java.util.*;

public class ExamplesView extends View {
  
   private final List<String> gen_examples;
   private final List<String> val_examples;
   private final String instantiatedRule;    

    public ExamplesView(List<String> gen_examples, List<String> val_examples, String instantiatedRule) {
        super("examples.ftl");
        this.gen_examples=gen_examples;
        this.val_examples=val_examples;
        this.instantiatedRule=instantiatedRule;
    }

    public List<String> getGen_examples() {
        return gen_examples;
    }
    public List<String> getVal_examples() {
        return val_examples;
    }  
    public String getInstantiatedRule() {
        return instantiatedRule;
    }

    
   }
