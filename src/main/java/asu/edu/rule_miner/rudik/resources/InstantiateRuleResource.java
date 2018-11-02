package asu.edu.rule_miner.rudik.resources;

import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.rudikUI.UIMethods;
import asu.edu.rule_miner.rudik.views.GraphView;
import asu.edu.rule_miner.rudik.views.InstantiateRuleView;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("/instantiate-rule")
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
//@Produces(MediaType.APPLICATION_JSON)

public class InstantiateRuleResource {

    //a dictionary to store the entities that composed a certain rule that will be used to show the surrounding graph. Keys and values are string. Entities are separeted by ;
    private Map<String, String> rules_entities_dict;
    //relations between nodes
    //store the atoms to use them to construct the graph
    private Map<String, List<RuleAtom>> rulesAtomsDict;

    public InstantiateRuleResource() {
        rules_entities_dict= new HashMap<>();
    }
    

    @GET
    @Timed
public InstantiateRuleView InstantiateRule(@QueryParam("instKg") String kg,
                                           @QueryParam("rule") String ruleToInst,
                                            @QueryParam("instEndpoint") String sparqlEndpoint) {

    final UIMethods UIfunctions = new UIMethods();
    //launching the API for the specific knowlegde graph with a maximum number of 10 instantiations and 20 secs of timeout
    UIfunctions.launchAPI(kg, 100, 5*60, sparqlEndpoint);
        List result = UIfunctions.instantiateRule(kg, ruleToInst);
        this.rules_entities_dict=UIfunctions.getRules_entities_dict();
        this.rulesAtomsDict=UIfunctions.getRulesAtomsDict();
        return new InstantiateRuleView(result, kg, ruleToInst);
  }

  @GET
  @Path("/surrounding-graph")
  public GraphView SurroundingGraph( @QueryParam("rule") String rule, @QueryParam("instantiatedRule") String instantiatedRule){
       String entities = rules_entities_dict.get(rule);
       List<RuleAtom> ruleAtoms = rulesAtomsDict.get(rule);
      String jsonGraph = UIMethods.createJsonGraph(entities,ruleAtoms);
      return new GraphView(jsonGraph, rule, instantiatedRule);
  }

}
