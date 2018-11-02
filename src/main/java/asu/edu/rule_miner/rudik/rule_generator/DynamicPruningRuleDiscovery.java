package asu.edu.rule_miner.rudik.rule_generator;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.RuleMinerException;
import asu.edu.rule_miner.rudik.api.model.HornRuleInstantiation;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.MultipleGraphHornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.model.statistic.StatisticsContainer;
import asu.edu.rule_miner.rudik.rule_generator.examples_sampling.VariancePopularSampling;
import asu.edu.rule_miner.rudik.rule_generator.score.DynamicScoreComputation;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;

/**
 * Implements A* dynamic pruning discovery algorithm for rules generation
 * @author sortona
 */
public class DynamicPruningRuleDiscovery extends HornRuleDiscovery {

  private final static Logger LOGGER = LoggerFactory.getLogger(DynamicPruningRuleDiscovery.class.getName());


  public DynamicPruningRuleDiscovery() {
    super();
  }

  @Override
  public Map<HornRule, Double> discoverPositiveHornRules(final Set<Pair<String, String>> negativeExamples,
      final Set<Pair<String, String>> positiveExamples, final Set<String> relations, final String typeSubject,
      final String typeObject) {
    // switch positive and negative examples
    LOGGER.info(
        "Discovering positive rules for '{}' target predicates with {} positive examples, {} negative examples, '{}' subject type, '{}' object type.",
        relations, positiveExamples.size(), negativeExamples.size(), typeSubject, typeObject);
    final DynamicScoreComputation score = new DynamicScoreComputation(positiveExamples.size(), negativeExamples.size(),
        this.getSparqlExecutor(), relations, typeSubject, typeObject, negativeExamples, false);
    score.setMinePositive(true, false, false);
    final Map<HornRule, Double> rule2score = this.discoverHornRules(positiveExamples, negativeExamples, relations,
        score, -1);
    final int discoveredRules = rule2score != null ? rule2score.size() : 0;
    LOGGER.info("Discovered a total of {} output rules.", discoveredRules);
    return rule2score;

  }

  @Override
  public Map<HornRule, Double> discoverNegativeHornRules(final Set<Pair<String, String>> negativeExamples,
      final Set<Pair<String, String>> positiveExamples, final Set<String> relations, final String typeSubject,
      final String typeObject) {
    LOGGER.info(
        "Discovering negative rules for '{}' target predicates with {} positive examples, {} negative examples, '{}' subject type, '{}' object type.",
        relations, positiveExamples.size(), negativeExamples.size(), typeSubject, typeObject);
    final DynamicScoreComputation score = new DynamicScoreComputation(negativeExamples.size(), positiveExamples.size(),
        this.getSparqlExecutor(), relations, typeSubject, typeObject, positiveExamples, false);
    final Map<HornRule, Double> rule2score = this.discoverHornRules(negativeExamples, positiveExamples, relations,
        score, -1);
    final int discoveredRules = rule2score != null ? rule2score.size() : 0;
    LOGGER.info("Discovered a total of {} output rules.", discoveredRules);
    return rule2score;
  }

  @Override
  public Map<HornRule, Double> discoverAllPositiveHornRules(final Set<Pair<String, String>> negativeExamples,
      final Set<Pair<String, String>> positiveExamples, final Set<String> relations, final String typeSubject,
      final String typeObject, final int maxRulesNumber) {
    LOGGER.info(
        "Discovering top-{} positive rules for '{}' target predicates with {} positive examples, {} negative examples, '{}' subject type, '{}' object type.",
        maxRulesNumber, relations, positiveExamples.size(), negativeExamples.size(), typeSubject, typeObject);
    // switch positive and negative examples
    final DynamicScoreComputation score = new DynamicScoreComputation(positiveExamples.size(), negativeExamples.size(),
        this.getSparqlExecutor(), relations, typeSubject, typeObject, negativeExamples, true);
    score.setMinePositive(true, false, false);
    final Map<HornRule, Double> rule2score = this.discoverHornRules(positiveExamples, negativeExamples, relations,
        score, maxRulesNumber);
    final int discoveredRules = rule2score != null ? rule2score.size() : 0;
    LOGGER.info("Discovered a total of {} output rules.", discoveredRules);
    return rule2score;
  }

  @Override
  public Map<HornRule, Double> discoverAllNegativeHornRules(final Set<Pair<String, String>> negativeExamples,
      final Set<Pair<String, String>> positiveExamples, final Set<String> relations, final String typeSubject,
      final String typeObject, final int maxRulesNumber) {
    LOGGER.info(
        "Discovering top-{} negative rules for '{}' target predicates with {} positive examples, {} negative examples, '{}' subject type, '{}' object type.",
        maxRulesNumber, relations, positiveExamples.size(), negativeExamples.size(), typeSubject, typeObject);
    final DynamicScoreComputation score = new DynamicScoreComputation(negativeExamples.size(), positiveExamples.size(),
        this.getSparqlExecutor(), relations, typeSubject, typeObject, positiveExamples, true);
    final Map<HornRule, Double> rule2score = this.discoverHornRules(negativeExamples, positiveExamples, relations,
        score, maxRulesNumber);
    final int discoveredRules = rule2score != null ? rule2score.size() : 0;
    LOGGER.info("Discovered a total of {} output rules.", discoveredRules);
    return rule2score;
  }

  @Override
  public Map<HornRule, Double> discoverPositiveHornRules(final Set<Pair<String, String>> negativeExamples,
      final Set<Pair<String, String>> positiveExamples, final Set<String> relations, final String typeSubject,
      final String typeObject, final boolean subjectFunction, final boolean objectFunction) {
    // switch positive and negative examples
    final DynamicScoreComputation score = new DynamicScoreComputation(positiveExamples.size(), negativeExamples.size(),
        this.getSparqlExecutor(), relations, typeSubject, typeObject, negativeExamples, false);
    score.setMinePositive(true, subjectFunction, objectFunction);
    final Map<HornRule, Double> rule2score = this.discoverHornRules(positiveExamples, negativeExamples, relations,
        score, -1);
    return rule2score;
  }

  @Override
  public List<HornRuleInstantiation> instantiateRule(final Set<String> targetPredicates, final HornRule rule,
      final String subjType, final String objType, boolean positive, int maxInstantiationNumber) {
    if (rule == null) {
      return null;
    }
    final List<List<Pair<String, String>>> allBoundedVariables = this.getSparqlExecutor()
        .instantiateHornRule(targetPredicates, rule.getRules(), subjType, objType, positive, maxInstantiationNumber);
    final List<HornRuleInstantiation> allInstantiation = Lists.newArrayList();
    allBoundedVariables.forEach(bind -> allInstantiation.add(getOneRuleInstantiation(bind, rule)));
    return allInstantiation;
  }

  private HornRuleInstantiation getOneRuleInstantiation(final List<Pair<String, String>> createRuleInstantiation,
      HornRule rule) {
    final HornRuleInstantiation oneInst = new HornRuleInstantiation();
    rule.getRules().forEach(atom -> {
      final String subVariable = atom.getSubject();
      final String subjInstantiation = getVariableInstantiation(createRuleInstantiation, subVariable);
      setRuleSubjectObject(subVariable, subjInstantiation, oneInst);
      final String objVariable = atom.getObject();
      final String objInstantiation = getVariableInstantiation(createRuleInstantiation, objVariable);
      setRuleSubjectObject(objVariable, objInstantiation, oneInst);
      final RuleAtom instAtom = new RuleAtom(subjInstantiation, atom.getRelation(), objInstantiation);
      oneInst.addInstantiatedAtom(instAtom);
    });
    return oneInst;
  }

  private void setRuleSubjectObject(String variable, String instantiation, HornRuleInstantiation ruleInstantiation) {
    if (variable.equals(HornRule.START_NODE)) {
      ruleInstantiation.setRuleSubject(instantiation);
    }
    if (variable.equals(HornRule.END_NODE)) {
      ruleInstantiation.setRuleObject(instantiation);
    }
  }

  private String getVariableInstantiation(List<Pair<String, String>> variablesBinding, String variable) {
    // this should not never throw an exception as it should always find the variable binding
    return variablesBinding.stream().filter(b -> b.getLeft().equals(variable)).findFirst().get().getRight();
  }

  @SuppressWarnings("unchecked")
  private Map<HornRule, Double> discoverHornRules(final Set<Pair<String, String>> negativeExamples,
      final Set<Pair<String, String>> positiveExamples, final Set<String> relations,
      final DynamicScoreComputation score, final int maxRulesNumber) {
    if ((negativeExamples.size() == 0) || (positiveExamples.size() == 0)) {
      // cannot compute rules without examples
      return Maps.newHashMap();
    }

    StatisticsContainer.setStartTime(System.currentTimeMillis());

    final Map<String, Set<Pair<String, String>>> expandedNodes2examples = Maps.newHashMap();
    // for literals keep also track which examples expanded
    final Map<String, Set<Pair<String, String>>> expandedLiteral2examples = Maps.newHashMap();

    final Map<String, Set<String>> entity2types = Maps.newHashMap();
    final Set<MultipleGraphHornRule<String>> negativeHornRules = Sets.newHashSet();
    final Map<String, Set<Pair<String, String>>> relation2positiveExamples = Maps.newHashMap();

    final Graph<String> totalGraph = new Graph<String>();

    this.initialiseRules(negativeExamples, positiveExamples, expandedNodes2examples, entity2types, negativeHornRules,
        relation2positiveExamples, totalGraph);

    final int negativeCoverageThreshold = (negativeExamples.size() / 100) + 1;
    final int positiveCoverageThreshold = (positiveExamples.size() / 100) + 1;

    final Map<Set<RuleAtom>, Set<Pair<String, String>>> rule2relativePositiveCoverage = Maps.newHashMap();

    if (negativeHornRules.size() != 2) {
      throw new RuleMinerException(
          "Initalisation of negative and positive rules contains more or less than two empty rules with subject and object.",
          LOGGER);
    }

    MultipleGraphHornRule<String> currBestRule = negativeHornRules.iterator().next();
    double curBestScore = Double.MIN_VALUE;

    // the two initial empty rules relatively cover all positive examples
    rule2relativePositiveCoverage.put(currBestRule.getRules(), positiveExamples);

    final Set<Set<RuleAtom>> seenRules = Sets.newHashSet();

    final Map<HornRule, Double> outputRules = Maps.newHashMap();

    while (currBestRule != null) {

      boolean deleteRule = true;
      boolean expand = currBestRule.isExpandible(this.maxRuleLen);

      // if the rule is admissible (valid and does not cover many positive examples) outputs it
      if (score.isAdmissible(currBestRule)) {

        // if I can expand it more and find better rules I should try
        if (currBestRule.isExpandible(this.maxRuleLen) && score.isExpandible(currBestRule.getRules())) {
          deleteRule = false;
          score.setMandatoryExpandible(currBestRule.getRules());
        } else {
          LOGGER.info("Found a new valid rule: {} with score {}.", currBestRule, curBestScore);
          outputRules.put(currBestRule, curBestScore);
          // check maximum number of rules has been reached
          if ((maxRulesNumber >= 0) && (outputRules.size() >= maxRulesNumber)) {
            break;
          }
          expand = false;
        }
      }

      // do not delete it if it is a valid rule but can be further expanded and improved
      if (deleteRule) {
        negativeHornRules.remove(currBestRule);
      }

      final Set<Pair<String, String>> previousRelativeCoverage = rule2relativePositiveCoverage
          .get(currBestRule.getRules());

      // remove positive relative coverage only if it is not the empty rule and if it has to be deleted
      if ((currBestRule.getLen() > 0) && deleteRule) {
        rule2relativePositiveCoverage.remove(currBestRule.getRules());
      }

      // expand it only if it has less than maxRuleLen atoms
      if (expand) {

        LOGGER.debug("Computing next rules for current best rule...");
        // compute next plausible negative rules
        final Set<MultipleGraphHornRule<String>> newPlausibleRules = currBestRule.nextPlausibleRules(super.maxRuleLen,
            negativeCoverageThreshold);

        // destroy the materialised resources for the rule
        currBestRule.dematerialiseRule();

        LOGGER.debug("Computing for each new plausible relative positive support and total score...");
        for (final MultipleGraphHornRule<String> plausibleRule : newPlausibleRules) {

          if (seenRules.contains(plausibleRule.getRules())) {
            continue;
          }

          seenRules.add(plausibleRule.getRules());
          if (plausibleRule.isValid()) {
            seenRules.add(plausibleRule.getEquivalentRule());
          }

          final Set<Pair<String, String>> positiveRelativeCoverage = this.getPositiveRelativeCoverage(
              previousRelativeCoverage, currBestRule.getRules(), plausibleRule.getRules(), relation2positiveExamples);

          if (positiveRelativeCoverage.size() <= positiveCoverageThreshold) {
            continue;
          }

          // add the rule and its positive to the new rules to consider
          negativeHornRules.add(plausibleRule);
          // add the relative positive coverage of the rule
          rule2relativePositiveCoverage.put(plausibleRule.getRules(), positiveRelativeCoverage);

        }

      } else {
        currBestRule.dematerialiseRule();
      }

      LOGGER.debug("Computing next best rule according to score...");

      final Pair<MultipleGraphHornRule<String>, Double> ruleAndscore = score.getNextBestRule(negativeHornRules,
          rule2relativePositiveCoverage, this.maxRuleLen);
      if (ruleAndscore == null) {

        LOGGER.debug("Next best rule has a positive score, returning all the founded rules.");
        StatisticsContainer.setNodesNumber(totalGraph.getNodesNumber());
        StatisticsContainer.setEdgesNumber(totalGraph.getNodesEdges());

        break;
      }
      currBestRule = ruleAndscore.getLeft();
      curBestScore = ruleAndscore.getRight();

      LOGGER.debug(
          "Next best rule with best approximate score '{}' ({} covered negative examples, {} relative covered positive, {} positive coverage, score: {})",
          currBestRule, currBestRule.getCoveredExamples().size(),
          rule2relativePositiveCoverage.get(currBestRule.getRules()).size(),
          score.getRuleValidationScore(currBestRule.getRules()), ruleAndscore.getRight());

      // expand nodes for the current positive and negative examples only if the rule can be expanded
      if (currBestRule.getLen() < (maxRuleLen - 1)) {

        final Set<String> toAnalyse = Sets.newHashSet();
        toAnalyse.addAll(currBestRule.getCurrentNodes());
        // different expansions if the node is a literal
        final boolean isLiteral = totalGraph.isLiteral(toAnalyse.iterator().next());

        // special case if nodes to expand are literals
        if (isLiteral) {
          this.expandLiteral(toAnalyse, totalGraph, currBestRule, expandedLiteral2examples);
        } else {
          // expand graph only if nodes has not been expanded before
          toAnalyse.removeAll(expandedNodes2examples.keySet());
          if (toAnalyse.size() > 0) {
            LOGGER.debug("Expanding {} nodes for negative and positive examples...", toAnalyse.size());
            this.expandGraphs(toAnalyse, totalGraph, entity2types, numThreads);
            LOGGER.debug("...expansion completed.");
          }

          // add inequalities relations
          this.expandInequalityNodes(currBestRule.getCurrentNodes(), totalGraph, currBestRule, expandedNodes2examples);
        }

      }

    }

    StatisticsContainer.setEndTime(System.currentTimeMillis());
    // promove constants for each rule
    for (final HornRule rule : outputRules.keySet()) {
      // cast
      ((MultipleGraphHornRule<String>) rule).promoteConstant();
      ((MultipleGraphHornRule<String>) rule).dematerialiseRule();
    }

    StatisticsContainer.setOutputRules(Lists.newArrayList(outputRules.keySet()));
    return outputRules;

  }

  private void initialiseRules(final Set<Pair<String, String>> generationExamples,
      final Set<Pair<String, String>> validationExamples, final Map<String, Set<Pair<String, String>>> expandedNodes,
      final Map<String, Set<String>> entity2types, final Set<MultipleGraphHornRule<String>> hornRules,
      final Map<String, Set<Pair<String, String>>> relation2validationExample,
      final Graph<String> generationNodesGraph) {
    LOGGER.debug("Creating initial graphs and expanding all positive and negative examples...");
    if ((generationExamples == null) || (generationExamples.size() == 0) || (validationExamples == null)
        || (validationExamples.size() == 0)) {
      return;
    }
    StatisticsContainer.setValidationExamplesSize(validationExamples.size());

    // when adding new nodes, add also the corresponding example
    final Set<String> toAnalyse = Sets.newHashSet();
    Set<Pair<String, String>> coveredExamples;

    for (final Pair<String, String> example : generationExamples) {

      generationNodesGraph.addNode(example.getLeft());
      generationNodesGraph.addNode(example.getRight());
      toAnalyse.add(example.getLeft());
      toAnalyse.add(example.getRight());

      coveredExamples = expandedNodes.get(example.getLeft());
      if (coveredExamples == null) {
        coveredExamples = Sets.newHashSet();
        expandedNodes.put(example.getLeft(), coveredExamples);
      }
      coveredExamples.add(example);

      coveredExamples = expandedNodes.get(example.getRight());
      if (coveredExamples == null) {
        coveredExamples = Sets.newHashSet();
        expandedNodes.put(example.getRight(), coveredExamples);
      }
      coveredExamples.add(example);
    }

    this.expandGraphs(toAnalyse, generationNodesGraph, entity2types, numThreads);

    if ((generationSmartLimit != null) && (generationSmartLimit > 0)) {
      // double alpha = 0.1, beta = 0.1, gamma = 0.8, subWeight = 0.5, objWeight = 0.5;
      final VariancePopularSampling sampling = new VariancePopularSampling(alphaSmart, betaSmart, gammaSmart,
          subWeightSmart, objWeightSmart, this.getSparqlExecutor().getSubjectLimit(),
          this.getSparqlExecutor().getObjectLimit(), isTopK);
      final Set<Pair<String, String>> sampledExamples = sampling.sampleExamples(generationExamples, generationSmartLimit);
      generationExamples.clear();
      generationExamples.addAll(sampledExamples);
      StatisticsContainer.setGenerationSample(generationExamples);
    }

    generationNodesGraph.addExamples(generationExamples);
    final MultipleGraphHornRule<String> subjectRule = new MultipleGraphHornRule<String>(generationNodesGraph, true,
        generationExamples);
    hornRules.add(subjectRule);
    final MultipleGraphHornRule<String> objectRule = new MultipleGraphHornRule<String>(generationNodesGraph, false,
        generationExamples);
    hornRules.add(objectRule);

    final Graph<String> positiveNodesGraph = new Graph<String>();
    toAnalyse.clear();
    for (final Pair<String, String> example : validationExamples) {
      positiveNodesGraph.addNode(example.getLeft());
      positiveNodesGraph.addNode(example.getRight());
      toAnalyse.add(example.getLeft());
      toAnalyse.add(example.getRight());
    }
    this.expandGraphs(toAnalyse, positiveNodesGraph, entity2types, numThreads);

    // create map of positive relative coverage
    Set<Edge<String>> currentEdges;
    Set<Pair<String, String>> currentRelationExamples;
    final Set<String> permutations = Sets.newHashSet();
    permutations.add("subject");
    permutations.add("object");
    final Set<String> currentExampleRelation = Sets.newHashSet();

    for (final Pair<String, String> posExample : validationExamples) {
      currentExampleRelation.clear();

      for (final String onePermutation : permutations) {
        currentEdges = positiveNodesGraph.getNeighbours(posExample.getLeft());
        if (onePermutation.equals("object")) {
          currentEdges = positiveNodesGraph.getNeighbours(posExample.getRight());
        }

        for (final Edge<String> oneEdge : currentEdges) {
          String relation = oneEdge.getLabel();
          if (oneEdge.isArtificial()) {
            relation += "(_," + onePermutation + ")";
          } else {
            relation += "(" + onePermutation + ",_)";
          }
          if (currentExampleRelation.contains(relation)) {
            continue;
          }
          currentExampleRelation.add(relation);
          currentRelationExamples = relation2validationExample.get(relation);
          if (currentRelationExamples == null) {
            currentRelationExamples = Sets.newHashSet();
            relation2validationExample.put(relation, currentRelationExamples);
          }
          currentRelationExamples.add(posExample);
        }
      }

    }

    LOGGER.debug("Graph creation completed.");
  }

  private Set<Pair<String, String>> getPositiveRelativeCoverage(final Set<Pair<String, String>> previous,
      final Set<RuleAtom> previousRule, final Set<RuleAtom> newRule,
      final Map<String, Set<Pair<String, String>>> relation2examples) {
    final Set<RuleAtom> toVerify = Sets.newHashSet();
    toVerify.addAll(newRule);

    if (previousRule != null) {
      toVerify.removeAll(previousRule);
    }

    final Set<Pair<String, String>> newCoverage = Sets.newHashSet();
    newCoverage.addAll(previous);

    for (final RuleAtom oneAtom : toVerify) {
      if (oneAtom.getSubject().equals(HornRule.START_NODE)) {
        final String relation = oneAtom.getRelation() + "(subject,_)";
        if (relation2examples.containsKey(relation)) {
          newCoverage.retainAll(relation2examples.get(relation));
        } else {
          newCoverage.clear();
        }
      }

      if (oneAtom.getObject().equals(HornRule.START_NODE)) {
        final String relation = oneAtom.getRelation() + "(_,subject)";
        if (relation2examples.containsKey(relation)) {
          newCoverage.retainAll(relation2examples.get(relation));
        } else {
          newCoverage.clear();
        }
      }
      if (oneAtom.getSubject().equals(HornRule.END_NODE)) {
        final String relation = oneAtom.getRelation() + "(object,_)";
        if (relation2examples.containsKey(relation)) {
          newCoverage.retainAll(relation2examples.get(relation));
        } else {
          newCoverage.clear();
        }
      }

      if (oneAtom.getObject().equals(HornRule.END_NODE)) {
        final String relation = oneAtom.getRelation() + "(_,object)";
        if (relation2examples.containsKey(relation)) {
          newCoverage.retainAll(relation2examples.get(relation));
        } else {
          newCoverage.clear();
        }
      }
      if (newCoverage.size() == 0) {
        break;
      }
    }

    return newCoverage;
  }

  private void expandLiteral(final Set<String> toAnalyse, final Graph<String> totalGraph,
      final MultipleGraphHornRule<String> hornRule,
      final Map<String, Set<Pair<String, String>>> expandedLiteral2examples) {

    final Set<Pair<String, String>> literalCoveredExamplesToAnalyse = Sets.newHashSet();
    for (final String literalToAnalyse : toAnalyse) {
      literalCoveredExamplesToAnalyse.clear();
      // get the current covered examples of the literal
      if (hornRule.getCoveredExamples(literalToAnalyse) == null) {
        continue;
      }
      literalCoveredExamplesToAnalyse.addAll(hornRule.getCoveredExamples(literalToAnalyse));
      // remove the already coveredExamples
      Set<Pair<String, String>> previouslyCoveredExamples = expandedLiteral2examples.get(literalToAnalyse);
      if (previouslyCoveredExamples == null) {
        previouslyCoveredExamples = Sets.newHashSet();
        expandedLiteral2examples.put(literalToAnalyse, previouslyCoveredExamples);
      }
      literalCoveredExamplesToAnalyse.removeAll(previouslyCoveredExamples);

      if (literalCoveredExamplesToAnalyse.size() == 0) {
        continue;
      }
      previouslyCoveredExamples.addAll(literalCoveredExamplesToAnalyse);

      final Set<String> targetNodes = Sets.newHashSet();
      for (final Pair<String, String> oneExample : literalCoveredExamplesToAnalyse) {
        final String targetPartExample = hornRule.getStartingVariable().equals(HornRule.START_NODE)
            ? oneExample.getRight()
            : oneExample.getLeft();
        targetNodes.add(targetPartExample);
      }

      // for each covered examples, compare the current literal with all the literals of the covered example
      final String literalLexicalForm = totalGraph.getLexicalForm(literalToAnalyse);
      final Set<String> otherLiterals = totalGraph.getLiterals(targetNodes, this.maxRuleLen - hornRule.getLen() - 1);
      for (final String otherLiteral : otherLiterals) {
        final Set<String> outputRelations = SparqlExecutor.getLiteralRelation(literalLexicalForm,
            totalGraph.getLexicalForm(otherLiteral));

        if (outputRelations != null) {
          for (final String relation : outputRelations) {
            totalGraph.addEdge(literalToAnalyse, otherLiteral, relation, false);
            final String inverseRelation = SparqlExecutor.getInverseRelation(relation);
            if (inverseRelation != null) {
              totalGraph.addEdge(otherLiteral, literalToAnalyse, inverseRelation, false);
            }
          }
        }
      }

    }

  }

  /**
   * The creation of inequalities edges is allowed only for 1,000,000 examples, otherwise it gets too big
   * @param toAnalyse
   * @param totalGraph
   * @param hornRule
   * @param expandedInequalityNodes2examples
   */
  private void expandInequalityNodes(final Set<String> toAnalyse, final Graph<String> totalGraph,
      final MultipleGraphHornRule<String> hornRule,
      final Map<String, Set<Pair<String, String>>> expandedInequalityNodes2examples) {

    final Set<Pair<String, String>> nodeCoveredExamplesToAnalyse = Sets.newHashSet();

    int totEdgesCount = 0;

    for (final String nodeToAnalyse : toAnalyse) {
      nodeCoveredExamplesToAnalyse.clear();
      // get the current covered examples of the node
      if (hornRule.getCoveredExamples(nodeToAnalyse) == null) {
        continue;
      }
      nodeCoveredExamplesToAnalyse.addAll(hornRule.getCoveredExamples(nodeToAnalyse));
      // remove the already coveredExamples
      Set<Pair<String, String>> previouslyCoveredExamples = expandedInequalityNodes2examples.get(nodeToAnalyse);
      if (previouslyCoveredExamples == null) {
        previouslyCoveredExamples = Sets.newHashSet();
        expandedInequalityNodes2examples.put(nodeToAnalyse, previouslyCoveredExamples);
      }
      nodeCoveredExamplesToAnalyse.removeAll(previouslyCoveredExamples);

      // do not add inequalities if they have been already added or if the current rule does not allow inequality
      if ((nodeCoveredExamplesToAnalyse.size() == 0) || !hornRule.isInequalityExpandible()
          || (totalGraph.getTypes(nodeToAnalyse).size() == 0)) {
        continue;
      }

      previouslyCoveredExamples.addAll(nodeCoveredExamplesToAnalyse);

      final Set<String> targetNodes = Sets.newHashSet();
      // consider only allowed number of examples
      for (final Pair<String, String> oneExample : nodeCoveredExamplesToAnalyse) {
        final String targetPartExample = hornRule.getStartingVariable().equals(HornRule.START_NODE)
            ? oneExample.getRight()
            : oneExample.getLeft();
        targetNodes.add(targetPartExample);

      }

      final Set<String> sameTypesNodes = totalGraph.getSameTypesNodes(totalGraph.getTypes(nodeToAnalyse), targetNodes,
          this.maxRuleLen - hornRule.getLen() - 1);

      totEdgesCount += sameTypesNodes.size();

      // remove current node if exists
      sameTypesNodes.remove(nodeToAnalyse);
      for (final String otherNode : sameTypesNodes) {
        totalGraph.addEdge(nodeToAnalyse, otherNode, Constant.DIFF_REL, true);
      }

      if (totEdgesCount > 1000000) {
        return;
      }

    }

  }

}
