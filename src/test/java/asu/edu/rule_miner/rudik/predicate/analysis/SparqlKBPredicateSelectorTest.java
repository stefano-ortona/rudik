package asu.edu.rule_miner.rudik.predicate.analysis;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;

public class SparqlKBPredicateSelectorTest {

  //test with dbpedia
  private final static String CONF_FILE = "src/main/config/DbpediaConfiguration.xml";
  private final SparqlKBPredicateSelector selector = new SparqlKBPredicateSelector();

  @BeforeClass
  public static void bringUp(){
    //set config file
    ConfigurationFacility.setConfigurationFile(CONF_FILE);
  }

  @Test
  public void testGetPredicateTypes() {
    final String predicate = "http://dbpedia.org/ontology/ethnicGroup";
    final Pair<String,String> types = selector.getPredicateTypes(predicate);
    Assert.assertNotNull(types);
    Assert.assertEquals(types.getLeft(), "http://dbpedia.org/ontology/Place");
    Assert.assertEquals(types.getRight(), "http://dbpedia.org/ontology/EthnicGroup");
  }

  @Test
  public void testGetAllPredicates() {
    final Set<String> predicates = selector.getAllPredicates();
    Assert.assertNotNull(predicates);
    Assert.assertTrue(predicates.size() > 10);
  }
  
  @Test
  public void testGetAllPredicateTypes() {
    final Map<String,Pair<String,String>> type2predicates = selector.getAllPredicateTypes();
    Assert.assertNotNull(type2predicates);
    Assert.assertTrue(type2predicates.size() > 10);
    Assert.assertTrue(type2predicates.keySet().stream().allMatch(
        predicate -> {return type2predicates.get(predicate).getLeft() != null &&
        type2predicates.get(predicate).getLeft() != null;}));
    //print the results
    for(Entry<String,Pair<String,String>> entry:type2predicates.entrySet()) {
      System.out.println(entry.getKey()+"\t"+entry.getValue().getLeft()+"\t"+entry.getValue().getRight());
    }
  }
  
  

}
