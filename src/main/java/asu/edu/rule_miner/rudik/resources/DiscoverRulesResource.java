package asu.edu.rule_miner.rudik.resources;

import asu.edu.rule_miner.rudik.rudikUI.DiscoverNewRules;
import asu.edu.rule_miner.rudik.rudikUI.UIMethods;
import org.json.simple.JSONObject;
import asu.edu.rule_miner.rudik.views.SankeyDiagView;
import asu.edu.rule_miner.rudik.views.DiscoverRulesView;
import asu.edu.rule_miner.rudik.views.ExamplesView;
import asu.edu.rule_miner.rudik.views.GraphView;
import asu.edu.rule_miner.rudik.api.RudikApi;
import asu.edu.rule_miner.rudik.api.SorroundingGraphGeneration;
import asu.edu.rule_miner.rudik.api.model.HornRuleInstantiation;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.configuration.ConfigurationParameterConfigurator;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.tuple.Pair;
import javax.json.JsonValue;
import javax.ws.rs.GET;
import java.util.*;
import java.io.*;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;
import junit.framework.Assert;
import java.io.FileWriter;
import java.io.IOException;

@Path("/discover-rules")
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
//@Produces(MediaType.APPLICATION_JSON)
public class DiscoverRulesResource {

   /* private final AtomicLong counter; */
    private static RudikApi API;
    //dictionary to store the generation and validation examples: key=rule & value=list of examples
    private Map<String, List<String>> gen_examples_dict= new HashMap<String, List<String>>();
    private Map<String, List<String>> val_examples_dict= new HashMap<String, List<String>>();
    private String predicate;
    private Map<String, List<RuleAtom>> rulesAtomsDict= new HashMap<>();


    public DiscoverRulesResource() {

    }

    @GET
    @Timed
public DiscoverRulesView discoverNewRules(@QueryParam("rulesType") String rulesType,
                      @QueryParam("predicate") String predicate,
                      @QueryParam("kg") String kg,
                      @QueryParam("alpha") String alphaS,
                      @QueryParam("gamma") String gammaS,
                      @QueryParam("endpoint") String endpoint,
                      @QueryParam("nb_pos") String nb_posS,
                      @QueryParam("nb_neg") String nb_negS,
                      @QueryParam("sub_edges") String sub_edgesS,
                      @QueryParam("obj_edges") String obj_edgesS,
                      @QueryParam("max_rule_length") String max_rule_lengthS,
                      @QueryParam("includeLiterals") String includeLiteralsS,
                      @QueryParam("sampling") String sampling) {

        //launching the API for the specific knowlegde graph with a maximum number of 10 instantiations and 5 mins of timeout
        final DiscoverNewRules functions = new DiscoverNewRules(kg, 10, 5*60, Float.valueOf(alphaS),1-Float.valueOf(alphaS),Float.valueOf(gammaS),sampling);
        //update the config file by setting the new parameters values
        functions.setConfigurationParameters(alphaS, nb_posS, nb_negS, max_rule_lengthS, sub_edgesS, obj_edgesS, endpoint, includeLiteralsS);

        //retrieve the examples to be used in the next methods
        gen_examples_dict=functions.getGen_examples_dict();
        val_examples_dict=functions.getVal_examples_dict();
        API=functions.getAPI();
        this.predicate=functions.getPredicate();
        rulesAtomsDict=functions.getRulesAtomsDict();

        return new DiscoverRulesView(functions.discoverRules(kg, rulesType, predicate,Integer.valueOf(nb_posS),Integer.valueOf(nb_negS)), kg, endpoint);
 }

@GET 
@Path("/examples")
//@Produces(MediaType.APPLICATION_JSON)
public ExamplesView getExamples(@QueryParam("instantiatedRule") String rule) {
    Map<String,List<String>> gen_examples=   this.gen_examples_dict;
        Map<String, List<String>> val_examples=this.val_examples_dict;
         
         return new ExamplesView(gen_examples.get(rule), val_examples.get(rule), rule);
        }

@GET
@Path("/sankey-diagram")
//@Produces(MediaType.APPLICATION_JSON)
public SankeyDiagView showDiag() {
        Map<String,List<String>> gen_examples= this.gen_examples_dict;
        Map<String, List<String>> val_examples=this.val_examples_dict;
    List<List> gen_exampleSankey;
    List <List> val_exampleSankey;

    //construct the arrays from the dicts
    gen_exampleSankey=UIMethods.constructSankeyArray(gen_examples);
    val_exampleSankey=UIMethods.constructSankeyArray(val_examples);

         return new SankeyDiagView(gen_exampleSankey.toString(), val_exampleSankey.toString());
        }



private String caption;
private String source;
private String target;

@GET
@Path("/surrounding-graph")
//@Produces(MediaType.APPLICATION_JSON)
public GraphView surroundingGraph (@QueryParam("example") String example, @QueryParam("instantiatedRule") String instantiatedRule) throws java.io.IOException {

        //graph_framework can either be "vis" or "alchemy"
    String graph_framework="vis";
    //List<String> entitiesList= Arrays.asList(example.split(";"));
    //final Graph<String> surroundingGraph = API.generateGraph(entitiesList);
    String jsonGraph = DiscoverNewRules.createJsonGraph(example, rulesAtomsDict.get(instantiatedRule));
    if (graph_framework.equals("alchemy")){ return new GraphView(jsonGraph, example);}
        else {return new GraphView(jsonGraph, example, instantiatedRule);}
           }

    }
