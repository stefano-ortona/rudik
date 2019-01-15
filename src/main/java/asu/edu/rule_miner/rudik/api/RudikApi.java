package asu.edu.rule_miner.rudik.api;

import java.util.*;

import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.rule_generator.examples_sampling.VariancePopularSampling;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult.RuleType;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.predicate.analysis.KBPredicateSelector;
import asu.edu.rule_miner.rudik.predicate.analysis.SparqlKBPredicateSelector;
import asu.edu.rule_miner.rudik.rule_generator.DynamicPruningRuleDiscovery;
import asu.edu.rule_miner.rudik.rule_generator.HornRuleDiscoveryInterface;

import static asu.edu.rule_miner.rudik.configuration.ConfigurationFacility.getSparqlExecutor;

public class RudikApi {

    private HornRuleDiscoveryInterface ruleDiscovery;
    private KBPredicateSelector kbAnalysis;
    private SorroundingGraphGeneration graphGeneration;
    private RuleInstanceGeneration ruleInstantiation;
    private VariancePopularSampling sampling;
    // by default, timeout set to 10 minutes
    private final int timeout = 10 * 60;

    private int maxInstantiationNumber = 1000;

    /**
     * Initialize RudikApi with a specific configuration file that contains all parameters used by RuDiK
     *
     * @param filePath - the configuration file to be used
     * @param timeout  - timeout, in seconds, to specify the max waiting time for each operation. If an operation takes longer
     *                 than the timeout, then the operation is killed and it returns an empty result
     * @param sample   - if sampling should be used or not
     */
    public RudikApi(final String filePath, int timeout, boolean sample) {
        this(filePath, timeout, sample, 1000);
    }

    /**
     * Initialize RudikApi with a specific configuration file that contains all parameters used by RuDiK
     *
     * @param filePath - the configuration file to be used
     * @param timeout  - timeout, in seconds, to specify the max waiting time for each operation. If an operation takes longer
     *                 than the timeout, then the operation is killed and it returns an empty result
     * @param sample   - if sampling should be used or not
     * @param maxInstantiationNumber - that maximum number of instances that can be created
     */
    public RudikApi(final String filePath, int timeout, boolean sample, int maxInstantiationNumber) {

        XMLConfiguration config = null;
        try {
            config = new XMLConfiguration(filePath);
        } catch (ConfigurationException e) {
            System.err.println(String.format("No configuration file could be found at the path: %s", filePath));
            e.printStackTrace();
        }
        float alpha = config.getFloat("naive.runtime.score.alpha");
        float beta = config.getFloat("naive.runtime.score.beta");
        float gamma = config.getFloat("naive.runtime.score.gamma");
        String samplingPath = config.getString("sampling_path");

        ConfigurationFacility.setConfigurationFile(filePath);
        this.maxInstantiationNumber = maxInstantiationNumber;
        initialiseObjects(timeout);

        //initialize sampling with the parameters selected through the API with a subject and object weight of 0.5
        sampling = new VariancePopularSampling(alpha, beta, gamma,
                0.5, 0.5, getSparqlExecutor().getSubjectLimit(),
                getSparqlExecutor().getObjectLimit(), sample, samplingPath);
    }

    /**
     * "src/main/config/DbpediaConfiguration.xml"
     * Initialize RudikApi with a specific configuration file that contains all parameters used by RuDiK
     *
     * @param filePath
     * @param timeout  - timeout, in seconds, to specify the max waiting time for each operation. If an operation takes longer
     *                 than the timeout, then the operation is killed and it returns an empty result
     */
    public RudikApi(final String filePath,
                    int timeout,
                    float alpha,
                    float beta,
                    float gamma,
                    boolean sample,
                    final String samplePath) {
        ConfigurationFacility.setConfigurationFile(filePath);

        initialiseObjects(timeout);
        //initialize sampling with the parameters selected through the API with a subject and object weight of 0.5
        sampling = new VariancePopularSampling(alpha, beta, gamma,
                0.5, 0.5, getSparqlExecutor().getSubjectLimit(),
                getSparqlExecutor().getObjectLimit(), sample, samplePath);
    }

    //constructor for instantiating a rule
    public RudikApi(final String filePath, int timeout) {
        ConfigurationFacility.setConfigurationFile(filePath);
        initialiseObjects(timeout);
    }

    public RudikApi() {
        initialiseObjects(timeout);
    }

    private void initialiseObjects(int timeout) {
        this.ruleDiscovery = new DynamicPruningRuleDiscovery();
        this.kbAnalysis = new SparqlKBPredicateSelector();
        this.graphGeneration = new SorroundingGraphGeneration(ruleDiscovery.getSparqlExecutor(), timeout);
        this.ruleInstantiation = new RuleInstanceGeneration(ruleDiscovery, timeout);
    }

    /**
     * Compute positive rules for the target predicate and return a RudikResult
     *
     * @param targetPredicate
     * @return
     */
    public RudikResult discoverPositiveRules(final String targetPredicate, int nb_posEx, int nb_negEx) {
        final boolean isInstantiation = false;
        return discoverRules(targetPredicate, RuleType.positive, isInstantiation, nb_posEx, nb_negEx);
    }

    /**
     * Compute negative rules for the target predicate and return a RudikResult
     *
     * @param targetPredicate
     * @return
     */
    public RudikResult discoverNegativeRules(final String targetPredicate, int nb_posEx, int nb_negEx) {
        final boolean isInstantiation = false;
        return discoverRules(targetPredicate, RuleType.negative, isInstantiation, nb_posEx, nb_negEx);
    }

    private RudikResult discoverRules(final String targetPredicate, RuleType type, boolean isInstantiation, int nb_posEx, int nb_negEx) {
        final Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(targetPredicate);
        final String typeSubject = subjectObjectType.getLeft();
        final String typeObject = subjectObjectType.getRight();
        final Set<String> relations = Sets.newHashSet(targetPredicate);
        final Set<Pair<String, String>> positiveEx = ruleDiscovery.generatePositiveExamples(relations, typeSubject,
                typeObject);
        // take samples from the positive examples

        final Set<Pair<String, String>> positiveExamples = sampling.sampleExamples(positiveEx, nb_posEx);
        final Set<Pair<String, String>> negativeEx = ruleDiscovery.generateNegativeExamples(relations, typeSubject,
                typeObject);
        // sample the negative examples

        final Set<Pair<String, String>> negativeExamples = sampling.sampleExamples(negativeEx, nb_negEx);


        Map<HornRule, Double> outputRules = null;
        if (type == RuleType.positive) {
            outputRules = ruleDiscovery.discoverPositiveHornRules(negativeExamples, positiveExamples, relations, typeSubject,
                    typeObject);
        } else {
            outputRules = ruleDiscovery.discoverNegativeHornRules(negativeExamples, positiveExamples, relations, typeSubject,
                    typeObject);
        }
        return buildResult(outputRules.keySet(), targetPredicate, type, typeSubject, typeObject, isInstantiation);
    }

    /**
     * Instantiate a HornRule over the KB and return a Rudik result containing all the information of the rules instantiated
     *
     * @param rule
     * @param targetPredicate
     * @param type
     * @return
     */
//add isInstantiation to choose whether to retrieve or not the instantiations
    public RudikResult instantiateSingleRule(HornRule rule, String targetPredicate, RuleType type) {
        //to build the instantiations and surrounding graph in buikdIndividualresult()
        final boolean isInstantiation = true;
        final Pair<String, String> subjectObjectType = kbAnalysis.getPredicateTypes(targetPredicate);
        final String typeSubject = subjectObjectType.getLeft();
        final String typeObject = subjectObjectType.getRight();
        return buildResult(Lists.newArrayList(rule), targetPredicate, type, typeSubject, typeObject, isInstantiation);
    }

    private RudikResult buildResult(Collection<HornRule> allRules, String targetPredicate, RuleType type, String subType,
                                    String objType, boolean isInstantiation) {
        final RudikResult result = new RudikResult();
        allRules.forEach(rule -> result.addResult(buildIndividualResult(rule, targetPredicate, type, subType, objType, isInstantiation)));
        return result;
    }

    private HornRuleResult buildIndividualResult(HornRule oneRule, String targetPredicate, RuleType type, String subjType,
                                                 String objType, boolean isInstantiation) {
        final HornRuleResult result = new HornRuleResult();
        result.setOutputRule(oneRule);
        result.setTargetPredicate(targetPredicate);
        result.setType(type);
        if (isInstantiation == true) {
            result.setAllInstantiations(
                    ruleInstantiation.instantiateRule(targetPredicate, oneRule, subjType, objType, type, maxInstantiationNumber));
            final Set<String> targetEntities = Sets.newHashSet();
            result.getAllInstantiations().forEach(r -> {
                targetEntities.add(r.getRuleSubject());
                targetEntities.add(r.getRuleObject());
            });
            result.setSorroundingGraph(graphGeneration.generateSorroundingGraph(targetEntities));
        }
        return result;
    }

    public Graph<String> generateGraph(List<String> entities) {
        return graphGeneration.generateSorroundingGraph(entities);
    }

    public Collection<String> getAllKBRelations() {
        return kbAnalysis.getAllPredicates();
    }

    public void setMaxInstantiationNumber(int number) {
        this.maxInstantiationNumber = number;
    }

    public void setMaxRuleLength(int length) {
        this.ruleDiscovery.setMaxRuleLength(length);
    }

}
