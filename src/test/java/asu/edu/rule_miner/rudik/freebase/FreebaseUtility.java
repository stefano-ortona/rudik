package asu.edu.rule_miner.rudik.freebase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.Maps;

public class FreebaseUtility {

  private final static int MAX_ENTITIES = 170;

  private final String freeBasePrefix = "http://rdf.freebase.com/ns/";
  private final static String BASE_PATH = "//media/psf/Home/Desktop/freebase_results/";

  @Test
  public void testCreateSpouseData() throws IOException{
    final String outputFolder = StringUtils.join(BASE_PATH,"Spouse_(or_domestic_partner)");
    this.testCreateUIData(outputFolder);
  }

  @Test
  public void testCreateChildrenData() throws IOException{
    final String outputFolder = StringUtils.join(BASE_PATH,"Children");
    this.testCreateUIData(outputFolder);
  }

  @Test
  public void testCreateAcademicAdvisorData() throws IOException{
    final String outputFolder = StringUtils.join(BASE_PATH,"AcademicAdvisor");
    this.testCreateUIData(outputFolder);
  }

  @Test
  public void testCreateSuccessorData() throws IOException{
    final String outputFolder = StringUtils.join(BASE_PATH,"Successor");
    this.testCreateUIData(outputFolder);
  }
  
  @Test
  public void testCreateSiblingData() throws IOException{
    final String outputFolder = StringUtils.join(BASE_PATH,"Sibling");
    this.testCreateUIData(outputFolder);
  }
  
  @Test
  public void testCreateCountyData() throws IOException{
    final String outputFolder = StringUtils.join(BASE_PATH,"County");
    this.testCreateUIData(outputFolder);
  }

  private void testCreateUIData(final String outputFolder) throws IOException{
    //read rules first
    final Map<String,String> rules = readRules(StringUtils.join(outputFolder,"/rules.lst"));
    //print rules
    System.out.println("var rules = [");
    int i=1;
    for(final String oneRule:rules.values()){
      System.out.print("\"Rule "+i+" : "+oneRule+"\"");
      if(i!=rules.size()){
        System.out.print(",");
      }
      System.out.println();
      i++;
    }
    System.out.println("];");
    System.out.println();
    computeUIOutput(rules.keySet(), outputFolder, "gen_set_coverage.lst");
    System.out.println();
    computeUIOutput(rules.keySet(), outputFolder, "val_set_coverage.lst");
  }


  private void computeUIOutput(final Set<String> allRules,final String outputFolder,final String targetFileName) throws IOException{
    final Map<String,Set<String>> entity2ruleCover = Maps.newHashMap();
    final Map<String,StringBuilder> rules2output = Maps.newTreeMap();
    for(final String oneRule:allRules){
      incrementRuleCoverage(entity2ruleCover, StringUtils.join(outputFolder, "/",oneRule,"/",targetFileName),oneRule);
    }
    //first insert all entities covered by more than one rule
    final Map<String,Integer> rule2newEntitiesCount = Maps.newHashMap();
    int insertedEntities = 0;
    double totEntities = 0.;
    final Map<String,List<String>> remainingEntities = Maps.newHashMap();
    for(final String oneEntity:entity2ruleCover.keySet()){
      final Set<String> coveredRules = entity2ruleCover.get(oneEntity);
      if(coveredRules.size()>1 && insertedEntities<=MAX_ENTITIES*3/4 ){
        insertedEntities++;
      }
      for(final String oneRule:coveredRules){
        if(!rule2newEntitiesCount.containsKey(oneRule)){
          rule2newEntitiesCount.put(oneRule, 0);
        }
        rule2newEntitiesCount.put(oneRule, rule2newEntitiesCount.get(oneRule)+1);
        totEntities++;
        if(coveredRules.size()>1 && insertedEntities<=MAX_ENTITIES*3/4){
          if(!rules2output.containsKey(oneRule)){
            rules2output.put(oneRule, new StringBuilder());
          }
          rules2output.get(oneRule).append(StringUtils.join("[\"", oneRule,"\",\"",oneEntity,"\",\"1\",\"1\"],\n"));
        } 
        else{
          if(!remainingEntities.containsKey(oneRule)){
            remainingEntities.put(oneRule, new ArrayList<String>());
          }
          remainingEntities.get(oneRule).add(oneEntity);
        }
      }
    }
    //insert remaining entities sampling the data
    for(final String oneRule:rule2newEntitiesCount.keySet()){
      long newCount = Math.round(rule2newEntitiesCount.get(oneRule)/totEntities
          *(MAX_ENTITIES-insertedEntities));
      newCount = newCount==0 ? 1 : newCount;
      rule2newEntitiesCount.put(oneRule, (int)newCount);
    }
    
    for(final String oneRule:remainingEntities.keySet()){
      final List<String> lastEntities = remainingEntities.get(oneRule);
      Collections.shuffle(lastEntities, new Random());
      for(int i=0;i<rule2newEntitiesCount.get(oneRule) && i<lastEntities.size();i++){
        if(!rules2output.containsKey(oneRule)){
          rules2output.put(oneRule, new StringBuilder());
        }
        rules2output.get(oneRule).append(StringUtils.join("[\"", oneRule,"\",\"",lastEntities.get(i),"\",\"1\",\"1\"],\n"));
      }
    }
    

    //print final results
    System.out.println("var data = [");
    int curRule = 0;
    for(final String oneRule:rules2output.keySet()){
      if(curRule == rules2output.size()-1){
        System.out.println(rules2output.get(oneRule).substring(0,rules2output.get(oneRule).length()-2));
      }else{
        System.out.print(rules2output.get(oneRule));
      }
      curRule++;
    }
    System.out.println("];");
  }

  private void incrementRuleCoverage(final Map<String,Set<String>> entity2rules, final String inputFile, final String ruleName) throws IOException{
    final File ruleFile = new File(inputFile);
    final BufferedReader reader = new BufferedReader(new FileReader(ruleFile));
    //forget first line
    reader.readLine();
    String line;
    while((line = reader.readLine()) != null){
      final String []subObj = line.split("\t");
      if(subObj.length != 2){
        reader.close();
        throw new RuntimeException("Error!");
      }
      final String finalEntity = StringUtils.join("<", subObj[0].replace(freeBasePrefix, ""),", ",
          subObj[1].replace(freeBasePrefix, ""),">");
      if(!entity2rules.containsKey(finalEntity)){
        entity2rules.put(finalEntity, new TreeSet<String>());
      } 
      entity2rules.get(finalEntity).add(ruleName);
    }
    reader.close();
  }

  private Map<String,String> readRules(final String rulesFile) throws IOException{
    final BufferedReader reader = new BufferedReader(new FileReader(new File(rulesFile)));
    String line = null;
    final Map<String,String> outputRules = Maps.newTreeMap();

    while((line = reader.readLine()) != null){
      final String rule = line.substring(0,5);
      line = line.substring(6);
      //delete last atom
      line = line.substring(0, line.lastIndexOf(" & "));
      outputRules.put(rule,line.replaceAll(freeBasePrefix, ""));
    }
    reader.close();
    return outputRules;
  }

}
