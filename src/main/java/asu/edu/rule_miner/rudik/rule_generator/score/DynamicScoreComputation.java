package asu.edu.rule_miner.rudik.rule_generator.score;

import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.model.horn_rule.MultipleGraphHornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.statistic.StatisticsContainer;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;

/**
 * Implements score computation based on coverge over generation set, coverage over validation set and unbouned coverage over validation set
 * @author sortona
 *
 */
public class DynamicScoreComputation {

  private final static Logger LOGGER = LoggerFactory.getLogger(DynamicScoreComputation.class.getName());

  private double alpha = 0.3;
  private double beta = 0.6;
  private double gamma = 0.1;

  // by default set to 0.2
  private double validationExamplesThreshold = 0.2;

  private final double totalGenerationExamples;
  private final double totalValidationExamples;

  private final Set<Pair<String, String>> solutionCoveredGenerationExamples;
  private final Set<Pair<String, String>> solutionCoveredValidationExamples;

  private final Map<Set<RuleAtom>, Integer> rule2validationCoverage;

  private final SparqlExecutor executor;

  private final Set<String> relations;

  private final String typeSubject;

  private final String typeObject;

  private final Set<Pair<String, String>> validationExamples;

  // keep track of valid expanded rules
  private final Map<Set<RuleAtom>, Boolean> validRules2canBeExpanded;

  // true if goal is to discover positive rules
  private boolean minePositive = false;

  private boolean subjectFunction = true;

  private boolean objectFunction = true;

  // if mining positive rules with target relation should not be mined
  private Set<String> notAdmissibleRules;

  private boolean includeAllRules = false;

  public DynamicScoreComputation(final int totalGenerationExamples, final int totalValidationExamples,
      final SparqlExecutor executor, final Set<String> relations, final String typeSubject, final String typeObject,
      final Set<Pair<String, String>> validationExamples, final boolean includeAllRules) {

    this.totalGenerationExamples = totalGenerationExamples;
    this.totalValidationExamples = totalValidationExamples;
    this.solutionCoveredGenerationExamples = Sets.newHashSet();
    this.solutionCoveredValidationExamples = Sets.newHashSet();
    this.rule2validationCoverage = Maps.newHashMap();
    this.executor = executor;
    this.relations = relations;
    this.typeSubject = typeSubject;
    this.typeObject = typeObject;
    this.validationExamples = validationExamples;
    this.validRules2canBeExpanded = Maps.newHashMap();

    // read parameters from config file
    final Configuration config = ConfigurationFacility.getConfiguration();

    // read validationExamplesThreshold
    if (config.containsKey(Constant.CONF_VALIDATION_THRESHOLD)) {
      try {
        validationExamplesThreshold = config.getDouble(Constant.CONF_VALIDATION_THRESHOLD);
      } catch (final Exception e) {
        LOGGER.error("Error while trying to read the " + "validation threshold configuration parameter. Set to 0.2.");
      }
    }

    // read alpha, beta, gamma
    if (config.containsKey(Constant.CONF_SCORE_ALPHA) && config.containsKey(Constant.CONF_SCORE_BETA)
        && config.containsKey(Constant.CONF_SCORE_GAMMA)) {
      try {
        alpha = config.getDouble(Constant.CONF_SCORE_ALPHA);
        beta = config.getDouble(Constant.CONF_SCORE_BETA);
        gamma = config.getDouble(Constant.CONF_SCORE_GAMMA);
      } catch (final Exception e) {
        LOGGER.error("Error while trying to read the "
            + "alpha, beta, gamma score configuration parameters. Set to 0.3, 0.6, 0.1.");
      }
    }
    this.includeAllRules = includeAllRules;
  }

  public double getScore(final MultipleGraphHornRule<String> hornRule,
      final Set<Pair<String, String>> validationRelativeCoverage, final int validationCoverage, final int maxAtomLen) {

    final Set<Pair<String, String>> retainCoveredNegative = Sets.newHashSet();
    retainCoveredNegative.addAll(hornRule.getCoveredExamples());
    retainCoveredNegative.removeAll(solutionCoveredGenerationExamples);

    final Set<Pair<String, String>> retainCoveredRelativePositive = Sets.newHashSet();
    retainCoveredRelativePositive.addAll(validationRelativeCoverage);
    retainCoveredRelativePositive.removeAll(solutionCoveredValidationExamples);

    // compute the score
    double score = -alpha * (retainCoveredNegative.size() / totalGenerationExamples);

    // if computing all output rules, more permissing scores to avoid score>+beta
    final int denominator = this.includeAllRules ? validationRelativeCoverage.size()
        : retainCoveredRelativePositive.size();

    score += (beta * validationCoverage) / denominator;

    score -= (gamma * retainCoveredRelativePositive.size()) / totalValidationExamples;

    if (retainCoveredRelativePositive.size() == 0) {
      // to avoid division by 0
      score = (beta * validationCoverage) / totalValidationExamples;
    }

    return score;
  }

  public void addCoveredGeneration(final Set<Pair<String, String>> examples) {
    this.solutionCoveredGenerationExamples.addAll(examples);
  }

  public void addCoveredRelativeValidation(final Set<Pair<String, String>> examples) {
    this.solutionCoveredValidationExamples.addAll(examples);
  }

  public Pair<MultipleGraphHornRule<String>, Double> getNextBestRule(final Set<MultipleGraphHornRule<String>> rules,
      final Map<Set<RuleAtom>, Set<Pair<String, String>>> rule2relativeValidationCoverage, final int maxAtomLen) {

    double bestScore = Integer.MAX_VALUE;
    MultipleGraphHornRule<String> bestRule = null;

    // when computing the next best score, rules with score >=0 can be deleted
    final Set<MultipleGraphHornRule<String>> positiveScoreRules = Sets.newHashSet();
    for (final MultipleGraphHornRule<String> plausibleRule : rules) {

      double currentScore = this.getScore(plausibleRule, rule2relativeValidationCoverage.get(plausibleRule.getRules()),
          0, maxAtomLen);
      if (!includeAllRules && (currentScore >= 0)) {
        positiveScoreRules.add(plausibleRule);
        continue;
      }

      if (plausibleRule.isValid()) {
        if (!this.rule2validationCoverage.containsKey(plausibleRule.getRules())) {
          final int positiveCoverage = this.getRuleMatchingValidationExamples(plausibleRule);

          this.rule2validationCoverage.put(plausibleRule.getRules(), positiveCoverage);
          if (plausibleRule.isExpandible(maxAtomLen)) {
            if (positiveCoverage == 0) {
              this.validRules2canBeExpanded.put(plausibleRule.getRules(), false);
            } else {
              this.validRules2canBeExpanded.put(plausibleRule.getRules(), true);
            }
          }
        }
        final int positiveCoverage = this.rule2validationCoverage.get(plausibleRule.getRules());
        // check if the rule is elegible to be further expanded
        if ((this.validRules2canBeExpanded.get(plausibleRule.getRules()) != null)
            && this.validRules2canBeExpanded.get(plausibleRule.getRules())) {
          currentScore = this.getScore(plausibleRule, rule2relativeValidationCoverage.get(plausibleRule.getRules()), 0,
              maxAtomLen);
        } else {
          currentScore = this.getScore(plausibleRule, rule2relativeValidationCoverage.get(plausibleRule.getRules()),
              positiveCoverage, maxAtomLen);
        }
      }

      if (!includeAllRules && (currentScore >= 0)) {
        positiveScoreRules.add(plausibleRule);
        continue;
      }

      if (currentScore < bestScore) {
        bestScore = currentScore;
        bestRule = plausibleRule;
      }
    }

    // remove rules with positive scores from the set of plausible rules, only if not all the rules need to be in output
    if (!includeAllRules) {
      rules.removeAll(positiveScoreRules);
    }

    // if the rule has a negative score, update covered negative and covered relative positive
    if ((bestRule != null) && ((bestScore < 0) || includeAllRules)) {
      // if the rule has to be avoided, remove it and recompute the next best rule
      if (containsForbiddenAtoms(bestRule)) {
        rules.remove(bestRule);
        rule2relativeValidationCoverage.remove(bestRule.getRules());
        return getNextBestRule(rules, rule2relativeValidationCoverage, maxAtomLen);
      }

      // update solution coverage only if the rule is admissible and it should not be further expanded
      if (this.isAdmissible(bestRule) && (!this.validRules2canBeExpanded.containsKey(bestRule.getRules())
          || !this.validRules2canBeExpanded.get(bestRule.getRules()))) {
        this.solutionCoveredGenerationExamples.addAll(bestRule.getCoveredExamples());
        this.solutionCoveredValidationExamples.addAll(rule2relativeValidationCoverage.get(bestRule.getRules()));
      }
      return Pair.of(bestRule, bestScore);
    }

    return null;
  }

  public boolean isExpandible(final Set<RuleAtom> rule) {
    return this.validRules2canBeExpanded.get(rule);
  }

  public void setMandatoryExpandible(final Set<RuleAtom> rule) {
    this.validRules2canBeExpanded.put(rule, false);
  }

  /**
   * A rule is admissible if it is valid and it covers less or equal validation_threshold of validation set
   * @return
   */
  public boolean isAdmissible(final MultipleGraphHornRule<String> rule) {
    if (!rule.isValid() || (this.rule2validationCoverage.get(rule.getRules()) == null)) {
      return false;
    }

    // check the validation threshold coverage
    final int validationCoverage = this.rule2validationCoverage.get(rule.getRules());
    if ((validationCoverage / totalValidationExamples) > this.validationExamplesThreshold) {
      return false;
    }

    // check covered validation examples are not more than covered generation examples
    if ((validationCoverage + 1) >= rule.getCoveredExamples().size()) {
      return false;
    }

    return true;
  }

  private boolean containsForbiddenAtoms(final MultipleGraphHornRule<String> rule) {
    if (notAdmissibleRules != null) {
      if (notAdmissibleRules.contains(rule.toString())) {
        return true;
      }
      for (final String oneNotAdmissibleRule : notAdmissibleRules) {
        if (rule.toString().contains(oneNotAdmissibleRule)) {
          return true;
        }
      }
    }

    return false;
  }

  public void setMinePositive(final boolean minePositive, final boolean subjectFunction, final boolean objectFunction) {
    this.minePositive = minePositive;

    // if trying to mine positive, target relations connecting subjects and objects are not plausible rules
    if (this.minePositive == true) {
      this.notAdmissibleRules = Sets.newHashSet();
      final String argumentRelation = "(" + MultipleGraphHornRule.START_NODE + "," + MultipleGraphHornRule.END_NODE
          + ")";
      for (final String relation : this.relations) {
        notAdmissibleRules.add(relation + argumentRelation);
      }
      this.subjectFunction = subjectFunction;
      this.objectFunction = objectFunction;
    }
  }

  /**
   * Execute validation query
   * @param rules
   * @return
   */
  public int getRuleMatchingValidationExamples(final MultipleGraphHornRule<String> rule) {
    StatisticsContainer.increaseValidationQuery();

    final long startingTime = System.currentTimeMillis();
    int coverage = -1;
    Set<Pair<String, String>> matchedExamples = null;
    try {
      if (minePositive) {
        matchedExamples = this.executor.getMatchingNegativeExamples(rule.getRules(), relations, typeSubject, typeObject,
            validationExamples, subjectFunction, objectFunction);
        coverage = matchedExamples.size();
      } else {
        matchedExamples = this.executor.getMatchingPositiveExamples(rule.getRules(), relations, typeSubject, typeObject,
            validationExamples);
        coverage = matchedExamples.size();
      }
      if (coverage == -1) {
        coverage = Integer.MAX_VALUE;
      }
    } catch (final Exception e) {
      coverage = Integer.MAX_VALUE;
    }
    if (matchedExamples != null) {
      rule.setValidationCoveredExamples(matchedExamples);
    }
    final long endingTime = System.currentTimeMillis();
    StatisticsContainer.increaseTimeValidationQuery(endingTime - startingTime);

    return coverage;
  }

  public int getRuleValidationScore(final Set<RuleAtom> rule) {
    final Integer coverage = this.rule2validationCoverage.get(rule);
    return coverage != null ? coverage : 0;
  }

}
