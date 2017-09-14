package asu.edu.rule_miner.rudik.dbpedia;

import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.ClientTest;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;

public class DBPediaClient extends ClientTest{
  
  private final static String CONF_FILE = "src/main/config/DbpediaConfiguration.xml";
  
  @BeforeClass
  public static void bringUp(){
    //set config file
    ConfigurationFacility.setConfigurationFile(CONF_FILE);
  }
  
  
  @Test
  public void testSpouseNegative() {
    final Set<String> relations = Sets.newHashSet("http://dbpedia.org/ontology/spouse");
    final String typeSubject = "http://dbpedia.org/ontology/Person";
    final String typeObject = "http://dbpedia.org/ontology/Person";
    Assert.assertNotNull(super.executeRudikNegativeRules(relations, typeSubject, typeObject)); 
  }
  
  @Test
  public void testSpouseAllNegative() {
    //target relations to be discovered
    final Set<String> relations = Sets.newHashSet("http://dbpedia.org/ontology/spouse");
    //type of the subject according to the ontology
    final String typeSubject = "http://dbpedia.org/ontology/Person";
    //type of the object according to the ontology
    final String typeObject = "http://dbpedia.org/ontology/Person";
    //max number of rules to have in output. If set to a negative number, it will return all the ruls
    final int maxRulesNumber = 10;
    Assert.assertNotNull(super.executeRudikAllNegativeRules(relations, typeSubject, typeObject,maxRulesNumber)); 
  }

}
