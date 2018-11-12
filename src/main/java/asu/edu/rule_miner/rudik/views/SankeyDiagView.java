package asu.edu.rule_miner.rudik.views;

import io.dropwizard.views.View;

import java.util.*;

public class SankeyDiagView extends View {

   private final String gen_examples;
   private final String val_examples;
   

    public SankeyDiagView(String gen_examples, String val_examples) {
        super("sankeyDiag.ftl");
        this.gen_examples=gen_examples;
        this.val_examples=val_examples;     
    }

    public String getGen_examples() {
        return gen_examples;
    }
    public String getVal_examples() {
        return val_examples;
    }
    

   }
