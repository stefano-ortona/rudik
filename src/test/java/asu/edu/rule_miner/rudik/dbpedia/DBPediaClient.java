package asu.edu.rule_miner.rudik.dbpedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.ClientTest;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.model.statistic.StatisticsContainer;

public class DBPediaClient extends ClientTest {

  private final static Logger LOGGER = LoggerFactory.getLogger(DBPediaClient.class.getName());

  private final static String CONF_FILE = "src/main/config/DbpediaConfiguration.xml";

  @BeforeClass
  public static void bringUp() {
    // set config file
    ConfigurationFacility.setConfigurationFile(CONF_FILE);
  }

  @Test
  public void testSpouseNegative() {
    final Set<String> relations = Sets.newHashSet("http://dbpedia.org/ontology/spouse");
    final String typeSubject = "http://dbpedia.org/ontology/Person";
    final String typeObject = "http://dbpedia.org/ontology/Person";
    StatisticsContainer.setFileName(new File("local_log"));
    Assert.assertNotNull(super.executeRudikNegativeRules(relations, typeSubject, typeObject, null, null));
  }

  @Test
  public void testSpouseAllNegative() {
    // target relations to be discovered
    final Set<String> relations = Sets.newHashSet("http://dbpedia.org/ontology/spouse");
    // type of the subject according to the ontology
    final String typeSubject = "http://dbpedia.org/ontology/Person";
    // type of the object according to the ontology
    final String typeObject = "http://dbpedia.org/ontology/Person";
    // max number of rules to have in output. If set to a negative number, it will return all the ruls
    final int maxRulesNumber = -1;
    Assert.assertNotNull(super.executeRudikAllNegativeRules(relations, typeSubject, typeObject, maxRulesNumber));
  }

  @Test
  public void testMultipleRelationsNegative() throws IOException {
    testMultipleRelations(false, "statisticsFileNegative");
  }

  @Test
  public void testMultipleRelationsPositive() throws IOException {
    testMultipleRelations(true, "statisticsFilePositive");
  }

  /**
   * Run rudik to discover rules for multiple relations read from a input text file
   * @param positive
   * @throws IOException
   */
  private void testMultipleRelations(boolean positive, final String statisticsFile) throws IOException {
    StatisticsContainer.setFileName(new File(statisticsFile));
    // read multiple relations from a file with type subject/object
    final BufferedReader reader = new BufferedReader(
        new InputStreamReader(DBPediaClient.class.getResourceAsStream("all_relations.txt")));
    final Map<String, Pair<String, String>> relation2types = Maps.newHashMap();
    final List<String> orderedRelations = Lists.newLinkedList();
    String line;
    while ((line = reader.readLine()) != null) {
      final String[] component = line.split("\t");
      relation2types.put(component[0], Pair.of(component[1], component[2]));
      orderedRelations.add(component[0]);
    }
    reader.close();
    final Set<String> allRelations = Sets.newHashSet();
    final AtomicInteger count = new AtomicInteger(0);
    final double startTime = System.currentTimeMillis();
    final double[] timeToSubtract = new double[] { 0 };
    orderedRelations.forEach(relation -> {
      final double curStartTime = System.currentTimeMillis();
      count.incrementAndGet();
      LOGGER.info("Starting computation for relation '{}', {} out of {}.", relation, count.get(),
          orderedRelations.size());
      final String id = relation;
      StatisticsContainer.initialiseContainer(id);
      allRelations.clear();
      allRelations.add(relation);
      final Pair<String, String> subjectObject = relation2types.get(relation);
      try {
        if (positive) {
          Assert.assertNotNull(super.executeRudikPositiveRules(allRelations, subjectObject.getLeft(),
              subjectObject.getRight(), null, null));
        } else {
          Assert.assertNotNull(super.executeRudikNegativeRules(allRelations, subjectObject.getLeft(),
              subjectObject.getRight(), null, null));
        }
        try {
          StatisticsContainer.printStatistics();
        } catch (final IOException e) {
          LOGGER.error("Error", e);
        }
      } catch (final Exception e) {
        LOGGER.error("Error while executing relation '{}', with stack trace '{}'.", relation, e.getMessage());
        timeToSubtract[0] += (System.currentTimeMillis() - curStartTime);
      }
      LOGGER.info("Computation ended, {} relations left to go.", orderedRelations.size() - count.get());
      LOGGER.info("Current running time: '{}' seconds.",
          (System.currentTimeMillis() - startTime - timeToSubtract[0]) / 1000.);
    });
  }

  @Test
  public void testMineRulesFromFile() throws IOException {
    // read pos and negative examples from file
    final String relation = "http://dbpedia.org/ontology/successor";
    final String subjType = "http://dbpedia.org/ontology/Person";
    final String objType = "http://dbpedia.org/ontology/Person";

    for (int i = 0; i <= 10; i++) {
      final Set<Pair<String, String>> posExamples = super.readExamples(
          DBPediaClient.class.getResourceAsStream(relation.replaceAll("/", "_") + "_pos" + i));
      final Set<Pair<String, String>> negExamples = super.readExamples(
          DBPediaClient.class.getResourceAsStream(relation.replaceAll("/", "_") + "_neg" + i));

      super.executeRudikNegativeRules(Sets.newHashSet(relation), subjType, objType, posExamples, negExamples);
    }

  }

}
