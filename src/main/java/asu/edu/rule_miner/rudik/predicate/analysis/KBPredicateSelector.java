package asu.edu.rule_miner.rudik.predicate.analysis;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public interface KBPredicateSelector {
  
  /**
   * Get the most common types for subject and object, for the given input predicate
   * 
   * @param inputPredicate
   * 
   * @return A pair, where the left element is the most common type for the subject, right element is the most common type for the object.
   * If not such types can be retrieved, the left/right element is set to null
   */
  Pair<String,String> getPredicateTypes(final String inputPredicate);
  
  /**
   * Get all predicate names from the given KB
   * 
   * @return
   */
  Set<String> getAllPredicates();
  
  /**
   * Get all most common types for subject and object for all the predicates in the KB
   * 
   * @return
   */
  Map<String,Pair<String,String>> getAllPredicateTypes();
  
 

}
