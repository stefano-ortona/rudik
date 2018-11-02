package asu.edu.rule_miner.rudik.rudikUI;

import asu.edu.rule_miner.rudik.api.RudikApi;
import asu.edu.rule_miner.rudik.api.model.HornRuleInstantiation;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.configuration.ConfigurationParameterConfigurator;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import junit.framework.Assert;
import org.json.simple.JSONObject;


import java.io.File;
import java.util.*;

import static asu.edu.rule_miner.rudik.rudikUI.DiscoverNewRules.createEdge;
import static asu.edu.rule_miner.rudik.rudikUI.DiscoverNewRules.createNode;
import static asu.edu.rule_miner.rudik.rudikUI.DiscoverNewRules.isInList;
import static org.apache.jena.atlas.lib.StrUtils.str;

public class UIMethods {

    private static RudikApi API;


    //a dictionary to store the entities that composed a certain rule that will be used to show the surrounding graph. Keys and values are string. Entities are separeted by ;
    private Map<String, String> rules_entities_dict;



    //store the atoms to use them to construct the graph
    private Map<String, List<RuleAtom>> rulesAtomsDict;

    //a string of relations between the nodes in the instantiation
    private Map<String, String> relationBetweenNodes;

    public Map<String, String> getRelationBetweenNodes() {
        return relationBetweenNodes;
    }
    public Map<String, String> getRules_entities_dict() {
        return rules_entities_dict;
    }
    public Map<String, List<RuleAtom>> getRulesAtomsDict() {
        return rulesAtomsDict;
    }

    public void launchAPI(String endpoint, int maxInst, int timeout, String sparqlEndpoint) {
        String filepath;
        if ("dbpedia".equals(endpoint)) {
            filepath= "src/main/config/Configuration.xml";
        } else {filepath="src/main/config/YagoConfiguration.xml"; }
        API = new RudikApi(filepath, timeout);
        // set the max number of instantiated facts to generate when executing rules against the KB
        API.setMaxInstantiationNumber(maxInst);
        ConfigurationParameterConfigurator.setSparqlEndpoint(sparqlEndpoint);
        if ("Virtuoso on localhost".equals(sparqlEndpoint) & "yago".equals(endpoint)){
            ConfigurationParameterConfigurator.setGraphIri("http://yago-knowledge.org/");
        }
        rules_entities_dict = new HashMap<>();
        rulesAtomsDict = new HashMap<>();
    }

    //a function to retrieve the target predicate, rule and type of rule from the given rule to feed the function instantiate
    public List<String> instantiateRuleparams(String rad, String ruleToInst){
        String var="";
        String ruleString ;
        String targetPredicate;
        String type;
        List<String> output=new ArrayList<String>();
        String[] words = ruleToInst.split(" ");
        for (String word : words){
            if ( word.equals("&") || word.equals("=>") || word.equals("¬") || word.equals("not") || word.substring(0, 1).equals(">") || word.substring(0, 1).equals("<")) { var+=word+" ";}
            else {var+=rad+word+" ";}
        }
        //get the first part of the rule to instantiate
        ruleString = var.split("=>")[0];
        if (var.split("=>")[1].trim().split(" ").length==1){
            targetPredicate=var.split("=>")[1].trim();
            type="pos";}
        else{
            targetPredicate=var.split("=>")[1].trim().split(" ")[1];
            type="neg";
        }
        output.add(ruleString.trim());
        output.add(targetPredicate.replace("(subject,object)",""));
        output.add(type);
        return output ;
  }

//remove all the radicals i.e from http://dbpedia.org/resource/Virginia to Virginia
    public static String removeRad(String stringWithRad){
        List<String> radToRemove= new ArrayList<>();
        String stringWithoutRad=stringWithRad;
        radToRemove.add("^^http://www.w3.org/2001/XMLSchema#date");
        radToRemove.add("^^http://www.w3.org/2001/XMLSchema#gYear");
        radToRemove.add("^^http://www.w3.org/2001/XMLSchema#integer");
        radToRemove.add("http://dbpedia.org/resource/");
        radToRemove.add("http://yago-knowledge.org/resource/");
        radToRemove.add("http://dbpedia.org/ontology/");
        for (String rad : radToRemove){
            stringWithoutRad=stringWithoutRad.replace(rad,"");
        }
        return stringWithoutRad;
    }

    public List<String> instantiateRule(String kg, String ruleToInst) {

        final String ruleString;
        final String targetPredicate;
        final HornRuleResult.RuleType type;
        final String typeString;
        final String rad;
        List<String> params;


        //retrieve the targetpredicate, rule and type of rule from the rule to instantiate
        if ("dbpedia".equals(kg)){
            rad="http://dbpedia.org/ontology/";
            params=instantiateRuleparams(rad, ruleToInst);
            ruleString= params.get(0);
            targetPredicate=params.get(1);
            typeString=params.get(2);
            if (typeString.equals("pos")){type= HornRuleResult.RuleType.positive;}
            else{type= HornRuleResult.RuleType.negative;}


        }
        else {

            rad="http://yago-knowledge.org/resource/";
            params=instantiateRuleparams(rad, ruleToInst);
            ruleString= params.get(0);
            targetPredicate=params.get(1).replace("(subject,object)","");
            typeString=params.get(2);
            if (typeString.equals("pos")){type= HornRuleResult.RuleType.positive;}
            else{type= HornRuleResult.RuleType.negative;}

        }

        String sep;
        String temp;
        //String of entities that composed a specific instantiation. Each entity is separated by ;
        String ruleEntities="";
        //separator of entities
        String entitieSep;
        temp = "";

        List<String> returnResult= new ArrayList<>();
        // parse and create the HornRule
        final HornRule rule = HornRule.createHornRule(ruleString);
        // instantiate rule and create the result
        final RudikResult result = API.instantiateSingleRule(rule, targetPredicate, type);
        Assert.assertNotNull(result);
        // iterate all over discovered results
        for (final HornRuleResult oneResult : result.getResults()) {

            // 2) get all instantiation of the rule over the KB
            final List<HornRuleInstantiation> ruleInst = oneResult.getAllInstantiations();
            // iterate over all instantiation
            for (final HornRuleInstantiation oneInst : ruleInst) {
                // get <subject,object> of the instantiation - this could be something like <Barack_Obama,Michelle_Obama>

                sep="";
                entitieSep="";
                temp="";
                ruleEntities="";
                //store the previous atom to compare with the new one and remove the instantiation if two following atoms are the same
                List<String> ruleAtoms= new ArrayList<String>();
                int i=0;
                List<RuleAtom> ruleAtomsList = new LinkedList<>();

                // iterate over all instantiated atoms of the rule
                for (final RuleAtom atom : oneInst.getInstantiatedAtoms()) {
                    //list of atoms composing a rule
                    // get <subject,relation,object> of one atom - this could be something like
                    // <Barack_Obama,child,Sasha_Obama> (now the atoms are instantiated, so they contain actual values and not
                    // variables)

                    File relation=new File(atom.getRelation());
                    //if the current atoms is already in the list of atoms set i to 1
                    if (isInList(ruleAtoms, relation.getName()+"("+atom.getSubject()+","+ atom.getObject()+")")){i++;}
                    temp+=sep+relation.getName()+"("+atom.getSubject()+","+ atom.getObject()+")";
                    sep=" & ";
                    ruleAtoms.add(relation.getName()+"("+atom.getSubject()+","+ atom.getObject()+")");
                    ruleAtomsList.add(atom);
                    //construct the string of entities that compose the instantiated rule
                    if(!ruleEntities.contains(atom.getSubject())){
                    ruleEntities+=entitieSep+atom.getSubject();
                    entitieSep=";";
                    }
                    if (!ruleEntities.contains(atom.getObject())) {
                        ruleEntities+=entitieSep+atom.getObject();
                        entitieSep=";";

                    }
                }
                rulesAtomsDict.put(removeRad(temp), ruleAtomsList);
                rules_entities_dict.put(removeRad(temp), ruleEntities);
                if (!isInList(returnResult,removeRad(temp)) & i==0) {
                    returnResult.add(removeRad(temp));
                }
            }
        }

        return returnResult;
    }


    public static List<List> constructSankeyArray(Map<String,List<String>> examples_dict){
        List<List> sankeyArray = new ArrayList<>();
        List temp;

        for (String key : examples_dict.keySet() ) {

            for (String value : examples_dict.get(key)) {
                temp= new ArrayList();
                temp.add('"'+key+'"');
                String subject=value.split(";")[0];
                String object=value.split(";")[1];
                String RuleInstantiated = key.replace("subject", subject).replace("object",object);
                temp.add('"'+RuleInstantiated+'\"');
                temp.add(5);
                sankeyArray.add(temp);
            }
        }
        return sankeyArray;

    }

    public static List<List<String>> createNodesAndEdgesGraph(List<String>entitiesList,
                                                              Graph<String> surroundingGraph,
                                                              List<RuleAtom> rulesAtoms,
                                                              String type) {

        //result list of list
        List<List<String>> result = new ArrayList<>();
        Map<String, Integer> nodesAdded = new HashMap<>();
        List<String> edgesAdded = new ArrayList<>();
        //Map<String, Integer> edgesAdded = new HashMap<>();
        //store the edges and nodes
        List nodes_list = new LinkedList();
        List edges_list = new LinkedList();

        String labelWithoutRad;
        String nodeEndWithoutRad;
        String nodeStartWithoutRad;
        int i = 0;
        int j;

        int clusterId = 0;
        String color = "#00aeff";

        //list of nodes we want to be added
        List<String> nodesToAdd = new ArrayList<String>(Arrays.asList("activeYearsEndYear","wikiPageExternalLink", "wikiPageDisambiguates", "publisher", "occupation", "nationality", "office", "party", "birthDate", "birthPlace", "deathDate", "deathPlace", "parent", "spouse", "successor", "predecessor", "religion", "relative", "child", "founder", "founders", "locationCountry", "author", "knownFor", "foundationPlace"));
        List<String> relationsToAvoid = new ArrayList<String>(Arrays.asList("abstract", "subject", "type"));
        List<String> nodesThatCanBeDuplicated = new ArrayList<String>(Arrays.asList("child", "parent", "spouse", "foundedBy", "founder", "deathPlace"));
        for (final RuleAtom atom : rulesAtoms) {
            nodesThatCanBeDuplicated.add(removeRad(atom.getRelation()));
            nodesToAdd.add(removeRad(atom.getRelation()));
        }

        for (String ent : entitiesList) {
            j = 0;
            //this list contains the edges added for a specific edge! This is to avoid having many nodes with the same predicate. Ex: a city that is the birthPlace of many person
            List<String> currentEntityEdgesAdded = new ArrayList<>();
            String entity = removeRad(ent);
            //create colors for the different clusters
            if (clusterId == 0 | clusterId > 2) {
                color = "#00aeff";
            } else if (clusterId == 1) {
                color = "#90fc3b";
            } else if (clusterId == 2) {
                color = "#ffffff";
            }

            if (!nodesAdded.containsKey(entity)) {
                nodesAdded.put(entity, i);
                nodes_list.add(createNode(entity, i, clusterId, "#ff0b0b"));
            }
            i++;
            //get the neighbours
            final Set<Edge<String>> allEdges = surroundingGraph.getNeighbours(ent);
            // iterate over all subject edges
            if (allEdges != null) {
                for (final Edge<String> oneEdge : allEdges) {
                    labelWithoutRad = removeRad(oneEdge.getLabel());
                    nodeEndWithoutRad = removeRad(oneEdge.getNodeEnd());
                    nodeStartWithoutRad = removeRad(oneEdge.getNodeStart());
                    //add only the nodes that are in the list of desired nodes and that have not been added yet.
                    if (isInList(nodesToAdd, removeRad(oneEdge.getLabel())) & !isInList(currentEntityEdgesAdded, removeRad(oneEdge.getLabel()))) {
                        if (!isInList(nodesThatCanBeDuplicated, labelWithoutRad)) {
                            currentEntityEdgesAdded.add(labelWithoutRad);
                        }
                        if (entity.equals(nodeStartWithoutRad)) {
                            if (nodesAdded.containsKey(nodeEndWithoutRad)) {
                                //add the edge+str(node1ID)+str(node2ID) to make sure that there won't be any duplicates
                                if (!isInList(edgesAdded, labelWithoutRad + str(nodesAdded.get(nodeEndWithoutRad)) + str(nodesAdded.get(entity))) & !isInList(edgesAdded, labelWithoutRad + str(nodesAdded.get(entity)) + str(nodesAdded.get(nodeEndWithoutRad)))) {
                                    edges_list.add(createEdge(labelWithoutRad, nodesAdded.get(nodeEndWithoutRad), nodesAdded.get(entity)));
                                    edgesAdded.add(labelWithoutRad + str(nodesAdded.get(nodeEndWithoutRad)) + str(nodesAdded.get(entity)));
                                    edgesAdded.add(labelWithoutRad + str(nodesAdded.get(entity)) + str(nodesAdded.get(nodeEndWithoutRad)));
                                    //currentEntityEdgesAdded.add(labelWithoutRad+str(nodesAdded.get(nodeEndWithoutRad))+str(nodesAdded.get(entity)));
                                }
                            } else {
                                if (isInList(entitiesList, oneEdge.getNodeEnd())) {
                                    //set color to red if entity
                                    nodes_list.add(createNode(nodeEndWithoutRad, i, clusterId, "#ff0b0b"));
                                } else {
                                    nodes_list.add(createNode(nodeEndWithoutRad, i, clusterId, color));
                                }
                                nodesAdded.put(nodeEndWithoutRad, i);
                                if (!isInList(edgesAdded, labelWithoutRad + str(i) + str(nodesAdded.get(entity))) & !isInList(edgesAdded, labelWithoutRad + str(nodesAdded.get(entity)) + str(i))) {
                                    edges_list.add(createEdge(labelWithoutRad, i, nodesAdded.get(entity)));
                                    edgesAdded.add(labelWithoutRad + str(i) + str(nodesAdded.get(entity)));
                                    edgesAdded.add(labelWithoutRad + str(nodesAdded.get(entity)) + str(i));
                                    //currentEntityEdgesAdded.add(labelWithoutRad+str(i)+str(nodesAdded.get(entity)));
                                }
                                i++;
                            }
                        } else if (entity.equals(nodeEndWithoutRad)) {
                            if (nodesAdded.containsKey(nodeStartWithoutRad)) {
                                if (!isInList(edgesAdded, labelWithoutRad + str(nodesAdded.get(entity)) + str(nodesAdded.get(nodeStartWithoutRad))) & !isInList(edgesAdded, labelWithoutRad + str(nodesAdded.get(nodeStartWithoutRad)) + str(nodesAdded.get(entity)))) {
                                    edges_list.add(createEdge(labelWithoutRad, nodesAdded.get(entity), nodesAdded.get(nodeStartWithoutRad)));
                                    edgesAdded.add(labelWithoutRad + str(nodesAdded.get(entity)) + str(nodesAdded.get(nodeStartWithoutRad)));
                                    edgesAdded.add(labelWithoutRad + str(nodesAdded.get(nodeStartWithoutRad)) + str(nodesAdded.get(entity)));
                                    //currentEntityEdgesAdded.add(labelWithoutRad+str(nodesAdded.get(entity))+str(nodesAdded.get(nodeStartWithoutRad)));
                                }
                            } else {
                                if (isInList(entitiesList, nodeEndWithoutRad)) {
                                    nodes_list.add(createNode(nodeStartWithoutRad, i, clusterId, "#ff0b0b"));
                                } else {
                                    nodes_list.add(createNode(nodeStartWithoutRad, i, clusterId, color));
                                }
                                nodesAdded.put(nodeStartWithoutRad, i);
                                if (!isInList(entitiesList, labelWithoutRad + str(nodesAdded.get(entity)) + str(i)) & !isInList(entitiesList, labelWithoutRad + str(i) + str(nodesAdded.get(entity)))) {
                                    edges_list.add(createEdge(labelWithoutRad, nodesAdded.get(entity), i));
                                    edgesAdded.add(labelWithoutRad + str(nodesAdded.get(entity)) + str(i));
                                    edgesAdded.add(labelWithoutRad + str(i) + str(nodesAdded.get(entity)));
                                }
                                i++;
                            }

                        }

                    }
                }
            }
            clusterId++;
        }
        if ("inst".equals(type)) {
            for (RuleAtom atom : rulesAtoms) {
                edges_list.add(createEdge(removeRad(atom.getRelation()), nodesAdded.get(removeRad(atom.getSubject())), nodesAdded.get(removeRad(atom.getObject()))));
            }
        }
        result.add(nodes_list);
        result.add(edges_list);
        return result;
    }
    public static String createJsonGraph(String entities, List<RuleAtom> ruleAtoms){
        //create a list of entities
        List<String> entitiesList= Arrays.asList(entities.split(";"));
        Graph<String> surroundingGraph= API.generateGraph(entitiesList);

        //store the result
        JSONObject result = new JSONObject();



        //add the relations found in the rule body
        List<List<String>> nodesAndEdgeslist= createNodesAndEdgesGraph(entitiesList, surroundingGraph,ruleAtoms, "inst");
        result.put("nodes", nodesAndEdgeslist.get(0));
        result.put("edges", nodesAndEdgeslist.get(1));
        String jsonGraph = result.toJSONString();
        return jsonGraph;
    }
}
