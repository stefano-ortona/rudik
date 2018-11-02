package asu.edu.rule_miner.rudik.rudikUI;

import asu.edu.rule_miner.rudik.api.RudikApi;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.configuration.ConfigurationParameterConfigurator;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.*;

import static asu.edu.rule_miner.rudik.rudikUI.UIMethods.createNodesAndEdgesGraph;

public class DiscoverNewRules {



    private static String predicate;



    private static RudikApi API;
    private Map<String, List<String>> gen_examples_dict;
    private static String kg;



    //store the atoms to use them to construct the graph
    private Map<String, List<RuleAtom>> rulesAtomsDict;


    private Map<String, List<String>> val_examples_dict;

    public DiscoverNewRules(String endpoint, int maxInst, int timeout, float alpha, float beta, float gamma,String sampling) {
        String filepath;
        if ("dbpedia".equals(endpoint)) {
            filepath= "src/main/config/Configuration.xml";
        } else {filepath="src/main/config/YagoConfiguration.xml"; }
        API = new RudikApi(filepath, timeout, alpha, beta, gamma, sampling);
        // set the max number of instantiated facts to generate when executing rules against the KB
        API.setMaxInstantiationNumber(maxInst);
        gen_examples_dict = new HashMap<>();
        val_examples_dict = new HashMap<>();
        rulesAtomsDict = new LinkedHashMap();
    }

    public Map<String, List<String>> getGen_examples_dict(){return gen_examples_dict;}

    public Map<String, List<String>> getVal_examples_dict() {
        return val_examples_dict;
    }
    public String getPredicate() {
        return predicate;
    }

    public RudikApi getAPI() {
        return API;
    }
    public Map<String, List<RuleAtom>> getRulesAtomsDict() {
        return rulesAtomsDict;
    }
    //set the configuration parameters to discover new rules
    public void setConfigurationParameters(String alphaS, String nb_posS, String nb_negS, String max_rule_lengthS, String sub_edgesS, String obj_edgesS, String endpoint, String includeLiteralsS){
        // if virtuoso on localhost is chosen, then set the graph IRI corresponding to the graph name set while loading yago files. Keep empty otherwise
        if (endpoint.equals("Virtuoso on localhost")){
            ConfigurationParameterConfigurator.setGraphIri("http://yago-knowledge.org/");
        }

        boolean includeLiterals;

        if (includeLiteralsS.equals("yes")){
            includeLiterals = true;
        } else {includeLiterals = false;}
        //set the parameters
        ConfigurationParameterConfigurator.setAlphaParameter(Float.valueOf(alphaS));
        ConfigurationParameterConfigurator.setBetaParameter(1- Float.valueOf(alphaS));
        // set maximum rules length
        API.setMaxRuleLength(Integer.valueOf(max_rule_lengthS));
        ConfigurationParameterConfigurator.setIncludeLiteral(includeLiterals);
        ConfigurationParameterConfigurator.setSparqlEndpoint(endpoint);

        // set maximum number of positive/negative examples - randomly selected
        ConfigurationParameterConfigurator.setPositiveExamplesLimit( Integer.valueOf(nb_posS));
        ConfigurationParameterConfigurator.setNegativeExamplesLimit( Integer.valueOf(nb_negS));

        // specify number max number of incoming and outgoing edges
        ConfigurationParameterConfigurator.setIncomingEdgesLimit(Integer.valueOf(sub_edgesS));
        ConfigurationParameterConfigurator.setOutgoingEdgesLimit(Integer.valueOf(obj_edgesS));

        // check new values have been set
        //Assert.assertEquals(1000, ConfigurationFacility.getConfiguration().getInt("naive.sparql.limits.examples.positive"));
        //Assert.assertEquals(1000, ConfigurationFacility.getConfiguration().getInt("naive.sparql.limits.examples.negative"));
        //Assert.assertEquals(max_rule_length, ConfigurationFacility.getConfiguration().getInt(Constant.CONF_MAX_RULE_LEN));
    }

    public List<String> discoverRules(String kg, String rulesType, String predicate, int nb_posEx, int nb_negEx){

        final String targetPredicate;
        final RudikResult result;
        DiscoverNewRules.kg =kg;
        if (kg.equals("dbpedia")){
            targetPredicate = "http://dbpedia.org/ontology/"+ predicate;
        } else {targetPredicate = "http://yago-knowledge.org/resource/"+ predicate;}

        //a variable to store the constructed output rule
        String constructedRule;
        List<String> returnResult= new ArrayList<>();


        if ("pos".equals(rulesType)){
            // use discoverNegativeRules to discover negative rules
            result = API.discoverPositiveRules(targetPredicate,nb_posEx,nb_negEx);
            //save the predicate to be used in /surrounding-graph
            this.predicate=predicate;
        }
        else {
            result = API.discoverNegativeRules(targetPredicate,nb_posEx,nb_negEx);
            //save the predicate to be used in /surrounding-graph
            this.predicate="not "+predicate;
        }

        // The following shows how to inspect the result
        // iterate all over discovered results
        String sep;
        sep = "";
        //store the previous atom to be compared with the actual one to avoid repetitions
        String previousAtom="";


        for (final HornRuleResult oneResult : result.getResults()) {

            // 1) get the output Horn Rule
            final HornRule rule = oneResult.getOutputRule();
            sep="";
            String temp = "";
            //counter to recognize rules with same atoms in the body
            int i=0;
            //list of atoms composing a rule
            List<RuleAtom> ruleAtoms = new LinkedList<>();
            // iterate over all atoms of the rule
            for (final RuleAtom atom : rule.getRules()) {
                ruleAtoms.add(atom);
                // get <subject,relation,object> of one atom - this could be something like <subject,child,v0> (with variables)
                //extract the relation from the URI
                //construct the rule based on its type: positive or negative
                File relation=new File(atom.getRelation());
                temp+=sep+relation.getName()+"("+atom.getSubject()+","+ atom.getObject()+")";
                sep=" & ";
                if (previousAtom.equals(temp)){i++;}
                previousAtom=temp;
            }

            constructedRule=temp+" => "+this.predicate+"(subject,object)";
            //add the key rule and value list of atoms in the dict
            rulesAtomsDict.put(constructedRule, ruleAtoms);
        if (i==0){returnResult.add(constructedRule);}

            //list to store the examples for a specific rule
            List<String> gen_examples= new ArrayList<>();
            List<String> val_examples= new ArrayList<>();

            // 4) get covered examples of the rule (NOTE: this will be null in case of instantiateRule API)
            // get the generation set examples covered
            Set<Pair<String, String>> genExamples = oneResult.getGenerationExamples();
            for (final Pair<String, String> oneExample : genExamples) {
                // get one example - things like <Barack_Obama,Michelle_Obama>
                String subject = UIMethods.removeRad(oneExample.getLeft());
                String object = UIMethods.removeRad(oneExample.getRight());
                gen_examples.add(object+";"+subject);

            }
            this.gen_examples_dict.put(constructedRule, gen_examples);
            // get the validation set examples covered - these could be potential errors
            final Set<Pair<String, String>> valExamples = oneResult.getValidationExamples();
            for (final Pair<String, String> oneExample : valExamples) {
                String subject = UIMethods.removeRad(oneExample.getLeft());
                String object = UIMethods.removeRad(oneExample.getRight());
                val_examples.add(object+";"+subject);
            }
            this.val_examples_dict.put(constructedRule, val_examples);

        }
        return returnResult;
    }
    private static String caption;
    private static String source;
    private static String target;

    //create a node with color
    public static Map createNode(String caption, int id, int cluster, String color ) {
        Map node = new LinkedHashMap();
        //remove the prefix
        node.put("label", UIMethods.removeRad(caption));
        node.put("color", color);
        node.put("id", id);
        node.put("cluster", cluster);
    return node;
    }
    //create a node without color
    public static Map createNode(String caption, int id, int cluster) {
        return createNode(caption, id, cluster, "#00aeff");
    }
    //create a edge
    public static Map createEdge(String caption, int source, int target ) {
        Map edge = new LinkedHashMap();
        //create the edge
        edge.put("from", source);
        edge.put("to", target);
        //remove the prefix
        edge.put("label", UIMethods.removeRad(caption));

        return edge;
    }

    //get ontology prefix
    public static String getOntologyPrefix(){
        final Configuration config = ConfigurationFacility.getConfiguration().subset(Constant.CONF_SPARQL_ENGINE);
        return config.getString(Constant.CONF_RELATION_TARGET_REFIX);
    }

    //get most generic Types
    public static String getMostGenericType(){
        final Configuration config = ConfigurationFacility.getConfiguration().subset(Constant.CONF_SPARQL_ENGINE);
        return config.getString(Constant.CONF_GENERIC_TYPES);
    }

    //check if string is in list
    public static boolean isInList(List<String> myList, String stringToCheck){
        for(String str: myList) {
            if(stringToCheck.contains(str.trim()))
                return true;
        }
        return false;
    }
        //create the nodes and edges for subject and object from the example. The subject's nodes will have even numbers and odd//one for object's nodes (this is defined by type which may be subject or object
    public static List<List<String>> createGraph(Graph<String> surroundingGraph, List<String> entities, List<RuleAtom> ruleAtoms){

            caption="label";
            source="from";
            target="to";
            List<List<String>> result = createNodesAndEdgesGraph(entities, surroundingGraph, ruleAtoms, "examples");
        
        return result;
    }

    public static String createJsonGraph(String example, List<RuleAtom> ruleAtoms){

        //graph_framework can either be "vis" or "alchemy"
        String graph_framework="vis";
        List<String> entities= new ArrayList<String>();
        String subject=example.split(";")[0];
        String subject_withoutPrefix=UIMethods.removeRad(subject);
        String object=example.split(";")[1];
        String object_withoutPrefix=UIMethods.removeRad(object);
        //add the radical
        if ("dbpedia".equals(kg)) {
            subject="http://dbpedia.org/resource/"+subject;
            object="http://dbpedia.org/resource/"+object;
        } else {
            subject="http://yago-knowledge.org/resource/"+subject;
            object="http://yago-knowledge.org/resource/"+object;
        }

        //generate the graph
        entities.add(subject);
        entities.add(object);
        final Graph<String> sorroundingGraph = API.generateGraph(entities);
    
        //create the resulting json variable
        JSONObject result = new JSONObject();
        result.put("nodes", createNodesAndEdgesGraph(entities, sorroundingGraph, ruleAtoms, "examples").get(0));
        result.put("edges", createNodesAndEdgesGraph(entities, sorroundingGraph, ruleAtoms,"examples").get(1));
        String jsonGraph = result.toJSONString();

        return jsonGraph;
    }


}
