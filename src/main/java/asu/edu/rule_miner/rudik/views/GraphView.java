package asu.edu.rule_miner.rudik.views;


import io.dropwizard.views.View;
import org.json.simple.JSONObject;
import java.util.*;

public class GraphView extends View {
     //private JSONObject graph;
     private String graph;
     private String example;


    private String instantiatedRule;

    public GraphView(String graph, String example) {
       super("graph.ftl");
       this.graph=graph;
       this.example=example;
    }

    public GraphView(String graph, String example, String instantiatedRule) {
        super("vis_graph.ftl");
       this.graph=graph;
       this.example=example;
	this.instantiatedRule=instantiatedRule;
    }
   
   public String getGraph(){return graph;}
   public String getExample(){return example;}
    public String getInstantiatedRule() {
        return instantiatedRule;
    }


}
