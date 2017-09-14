package asu.edu.rule_miner.rudik.rule_generator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;

public interface HornRuleDiscoveryInterface {

  /**
   * Discover positive horn rules by using union strategy as default
   * @param negativeExamples
   * @param positiveExamples
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @return
   */
  public List<HornRule> discoverPositiveHornRules(Set<Pair<String,String>> negativeExamples, Set<Pair<String,String>> positiveExamples,
      Set<String> relations, String typeSubject, String typeObject);

  /**
   * Discover positive horn rules by setting parameters for the validation query. If both subjectFuntion and objectFunction are true, then
   * intersection rescrtive setting is applied. If they are both false, then union setting is applied. If only one of them is true, negative
   * examples are queried with restriction either only on subject or object
   * 
   * @param negativeExamples
   * @param positiveExamples
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @param subjectFunction
   * @param objectFunction
   * @return
   */
  public List<HornRule> discoverPositiveHornRules(Set<Pair<String,String>> negativeExamples, Set<Pair<String,String>> positiveExamples,
      Set<String> relations, String typeSubject, String typeObject,boolean subjectFunction, boolean objectFunction);

  /**
   * Discover negative rules
   * 
   * @param negativeExamples
   * @param positiveExamples
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @return
   */
  public abstract List<HornRule> discoverNegativeHornRules(Set<Pair<String,String>> negativeExamples, Set<Pair<String,String>> positiveExamples,
      Set<String> relations, String typeSubject, String typeObject);

  /**
   * Generate set of positive examples for a given relation with subject and object
   * 
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @return
   */
  public Set<Pair<String,String>> generatePositiveExamples(
      Set<String> relations, String typeSubject, String typeObject);

  /**
   * Generate negative examples for the input relations
   * @param relations
   * 			name of input relations to generate negative examples for
   * @param typeObject
   * 			type of the object of the relation
   * @param typeSubject
   * 			type of the subject of the relation
   * @param filtered
   * 			decide whether returns only a subset of the negative examples, one for each relation
   * @return
   */
  public Set<Pair<String,String>> generateNegativeExamples(
      Set<String> relations, String typeSubject, String typeObject, boolean subjectFunction, boolean objectFunction);

  /**
   * By default generate negaitive examples with union strategy
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @return
   */
  public Set<Pair<String,String>> generateNegativeExamples(Set<String> relations, String typeSubject, String typeObject);
  /**
   * Get examples from the KB by executing the input query
   * @param query
   * @param subject
   * @param object
   * @return
   */
  public Set<Pair<String,String>> getKBExamples(String query, String subject, String object);

  /**
   * Read examples from an input file.
   * File must contain for each line a pair subject object separated with a tab character
   * @param inputFile
   * @return
   */
  public Set<Pair<String,String>> readExamples(File inputFile);

  /**
   * Discover the first maxRulesNumber positive rules according to a ranking based on score, and returns the rules with their score
   * 
   * @param negativeExamples
   * @param positiveExamples
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @param maxRulesNumber
   * @return
   */
  Map<HornRule,Double> discoverAllPositiveHornRules(Set<Pair<String, String>> negativeExamples,
      Set<Pair<String, String>> positiveExamples, Set<String> relations, String typeSubject, String typeObject,
      int maxRulesNumber);
  
  /**
   * Discover the first maxRulesNumber negative rules according to a ranking based on score, and returns the rules with their score
   * 
   * @param negativeExamples
   * @param positiveExamples
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @param maxRulesNumber
   * @return
   */
  Map<HornRule,Double> discoverAllNegativeHornRules(Set<Pair<String, String>> negativeExamples,
      Set<Pair<String, String>> positiveExamples, Set<String> relations, String typeSubject, String typeObject,
      int maxRulesNumber);

}
