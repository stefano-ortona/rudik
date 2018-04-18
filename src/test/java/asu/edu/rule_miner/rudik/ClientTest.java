package asu.edu.rule_miner.rudik;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.statistic.StatisticsContainer;
import asu.edu.rule_miner.rudik.rule_generator.DynamicPruningRuleDiscovery;

/**
 * Unit test for simple App.
 */
public class ClientTest {
  protected final DynamicPruningRuleDiscovery rudik = new DynamicPruningRuleDiscovery();
  private final static Logger LOGGER = LoggerFactory.getLogger(ClientTest.class.getName());

  protected Map<HornRule, Double> executeRudikNegativeRules(final Set<String> relationNames, final String subjectType,
      final String objectType, Set<Pair<String, String>> posExamples, Set<Pair<String, String>> negExamples) {
    final Instant startTime = Instant.now();

    // get positive and negative examples
    if (negExamples == null) {
      negExamples = rudik.generateNegativeExamples(relationNames, subjectType, objectType, false, false);
    }
    if (posExamples == null) {
      posExamples = rudik.generatePositiveExamples(relationNames, subjectType, objectType);
    }
    StatisticsContainer.setGenerationSample(negExamples);

    // compute outputs
    final Map<HornRule, Double> outputRules = rudik.discoverNegativeHornRules(negExamples, posExamples, relationNames,
        subjectType, objectType);

    final Instant endTime = Instant.now();
    LOGGER.info("----------------------------COMPUTATION ENDED----------------------------");
    LOGGER.info("Final computation time: {} seconds.", (endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000.);
    LOGGER.info("----------------------------Final output rules----------------------------");
    for (final HornRule oneRule : outputRules.keySet()) {
      LOGGER.info("{}", oneRule);
    }
    return outputRules;
  }

  protected Map<HornRule, Double> executeRudikPositiveRules(final Set<String> relationNames, final String subjectType,
      final String objectType, Set<Pair<String, String>> positiveExamples, Set<Pair<String, String>> negativeExamples) {
    final Instant startTime = Instant.now();

    // get positive and negative examples
    if (negativeExamples == null) {
      negativeExamples = rudik.generateNegativeExamples(relationNames, subjectType, objectType, false, false);
    }
    if (positiveExamples == null) {
      positiveExamples = rudik.generatePositiveExamples(relationNames, subjectType, objectType);
    }
    StatisticsContainer.setGenerationSample(positiveExamples);

    // compute outputs
    final Map<HornRule, Double> outputRules = rudik.discoverPositiveHornRules(negativeExamples, positiveExamples,
        relationNames, subjectType, objectType);

    final Instant endTime = Instant.now();
    LOGGER.info("----------------------------COMPUTATION ENDED----------------------------");
    LOGGER.info("Final computation time: {} seconds.", (endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000.);
    LOGGER.info("----------------------------Final output rules----------------------------");
    for (final HornRule oneRule : outputRules.keySet()) {
      LOGGER.info("{}", oneRule);
    }
    return outputRules;
  }

  protected Map<HornRule, Double> executeRudikAllNegativeRules(final Set<String> relationNames,
      final String subjectType, final String objectType, final int maxRulesNumber) {
    final Instant startTime = Instant.now();

    // get positive and negative examples
    final Set<Pair<String, String>> negativeExamples = rudik.generateNegativeExamples(relationNames, subjectType,
        objectType, false, false);
    final Set<Pair<String, String>> positiveExamples = rudik.generatePositiveExamples(relationNames, subjectType,
        objectType);

    // compute outputs
    final Map<HornRule, Double> outputRules = rudik.discoverAllNegativeHornRules(negativeExamples, positiveExamples,
        relationNames, subjectType, objectType, maxRulesNumber);

    final Instant endTime = Instant.now();
    LOGGER.info("----------------------------COMPUTATION ENDED----------------------------");
    LOGGER.info("Final computation time: {} seconds.", (endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000.);
    LOGGER.info("----------------------------Final output rules----------------------------");
    for (final HornRule oneRule : outputRules.keySet()) {
      LOGGER.info("Rule:{}\tScore:{}", oneRule, outputRules.get(oneRule));
    }
    return outputRules;
  }

  protected Set<Pair<String, String>> readExamples(final InputStream stream) throws IOException {
    // read positive examples
    final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String line;
    final Set<Pair<String, String>> examples = Sets.newHashSet();
    while ((line = reader.readLine()) != null) {
      // create the pair
      try {
        final Pair<String, String> curPair = Pair.of(line.split("\t")[0], line.split("\t")[1]);
        if (examples.contains(curPair)) {
          LOGGER.info("Pair '{}' repeated in the input examples file.", curPair);
        } else {
          examples.add(curPair);
        }
      } catch (final Exception e) {
        System.out.println("TROUBLE:" + line);
      }
    }
    return examples;
  }
}
