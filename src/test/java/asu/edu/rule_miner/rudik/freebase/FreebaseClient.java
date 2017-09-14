package asu.edu.rule_miner.rudik.freebase;

import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.ClientTest;
import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;

public class FreebaseClient extends ClientTest{
  
  private final static String CONF_FILE = "src/main/config/FreebaseConfiguration.xml";
  
  @BeforeClass
  public static void bringUp(){
    //set config file
    ConfigurationFacility.setConfigurationFile(CONF_FILE);
  }
  
  
  @Test
  public void testSpouseNegative() {
    final Set<String> relations = Sets.newHashSet("http://rdf.freebase.com/ns/Spouse_(or_domestic_partner)");
    final String typeSubject = "http://rdf.freebase.com/ns/Person";
    final String typeObject = "http://rdf.freebase.com/ns/Person";
    Assert.assertNotNull(super.executeRudikNegativeRules(relations, typeSubject, typeObject)); 
  }

}
