package asu.edu.rule_miner.rudik.model.horn_rule;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;

/**
 * Implements a HornRule structure that keeps tracks of a graph for each input generation example
 * @author sortona
 *
 * @param <T>
 */
public class MultipleGraphHornRule<T> extends HornRule {

  private final static Logger LOGGER = LoggerFactory.getLogger(MultipleGraphHornRule.class.getName());

  private Graph<T> g;

  Map<T, Map<Pair<T, T>, String>> node2example2variable;
  public Map<T, Set<Pair<T, T>>> currentNode2examples;

  Set<Pair<T, T>> coveredExamples;
  Set<Pair<T, T>> validationExamples;

  /**
   * For each supported graph, get the set of current nodes identified by the rule
   * @return
   */
  public Set<T> getCurrentNodes() {
    if ((node2example2variable == null) || (currentNode2examples == null)) {
      final boolean succ = initialiseBoundingVariable();
      if (!succ) {
        LOGGER.warn("Error while trying to instantiate rule {}. Cannot compute current nodes.", this.toString());
        return Sets.newHashSet();

      }
    }
    return currentNode2examples.keySet();
  }

  @Override
  public Set<Pair<String, String>> getCoveredExamples() {
    final Set<Pair<String, String>> stringExamples = Sets.newHashSet();
    this.coveredExamples.forEach(ex -> stringExamples.add(Pair.of(ex.getLeft().toString(), ex.getRight().toString())));
    return stringExamples;
  }

  @Override
  public Set<Pair<String, String>> getValidationCoveredExamples() {
    final Set<Pair<String, String>> stringExamples = Sets.newHashSet();
    this.validationExamples
        .forEach(ex -> stringExamples.add(Pair.of(ex.getLeft().toString(), ex.getRight().toString())));
    return stringExamples;
  }

  public void setValidationCoveredExamples(Set<Pair<T, T>> validationExamples) {
    this.validationExamples = validationExamples;
  }

  /**
   * Return a new Rule Atom that is supported by at least threshold graphs
   * @param threshold
   * @return
   */
  public Set<MultipleGraphHornRule<T>> nextPlausibleRules(int maxAtomThreshold, int minRuleSupport) {

    final Set<MultipleGraphHornRule<T>> nextPlausibleRules = Sets.newHashSet();

    final boolean succ = this.initialiseBoundingVariable();
    if (!succ) {
      LOGGER.warn("Error while trying to instantiate rule {}. Rule cannot be expanded.", this.toString());
      return nextPlausibleRules;
    }

    final boolean isLast = this.getLen() == (maxAtomThreshold - 1);
    String obligedVariable = null;
    if (isLast) {
      boolean containsStart = false;
      boolean containsEnd = false;
      for (final RuleAtom atom : this.rules) {
        if (!containsStart) {
          containsStart = atom.getSubject().equals(START_NODE) || atom.getObject().equals(START_NODE);
        }
        if (!containsEnd) {
          containsEnd = atom.getSubject().equals(END_NODE) || atom.getObject().equals(END_NODE);
        }
      }

      if (!containsStart && !containsEnd) {
        return nextPlausibleRules;
      }
      if (!containsStart) {
        obligedVariable = START_NODE;
      }
      if (!containsEnd) {
        obligedVariable = END_NODE;
      }

      if ((obligedVariable != null) && (obligedVariable.equals(START_NODE) || obligedVariable.equals(END_NODE))) {
        return this.nextOneHopPlausibleRules(maxAtomThreshold, minRuleSupport, obligedVariable);
      }
    }

    final Map<RuleAtom, Boolean> rule2newVariable = Maps.newHashMap();
    final Map<RuleAtom, Set<Pair<T, T>>> rule2coveredExamples = Maps.newHashMap();

    for (final T currentNode : this.currentNode2examples.keySet()) {
      // get the examples where the current node is
      final Set<Pair<T, T>> currentCoveredExamples = currentNode2examples.get(currentNode);

      final Set<Edge<T>> neighbors = g.getNeighbours(currentNode);
      for (final Edge<T> e : neighbors) {
        final boolean isArtifical = e.isArtificial();
        final T endNode = e.getNodeBottom();
        final String label = e.getLabel();
        for (final Pair<T, T> oneCoveredExample : currentCoveredExamples) {
          boolean isNewVariable = false;
          String newVariable = null;
          if (node2example2variable.containsKey(endNode)) {
            newVariable = node2example2variable.get(endNode).get(oneCoveredExample);
          }

          if (newVariable == null) {
            if (isLast) {
              continue;
            }
            newVariable = LOOSE_VARIABLE_NAME + variableCount;
            isNewVariable = true;
          }
          if ((obligedVariable != null) && !newVariable.equals(obligedVariable)) {
            continue;
          }
          RuleAtom newRule = null;
          if (isArtifical) {
            newRule = new RuleAtom(newVariable, label, currentVariable);
          } else {
            newRule = new RuleAtom(currentVariable, label, newVariable);
          }
          if (rules.contains(newRule)) {
            continue;
          }

          // TO DO: different check
          if (currentVariable.equals(newVariable) && !(endNode.equals(e.getNodeSource()))) {
            continue;
          }

          if (!rule2newVariable.containsKey(newRule)) {
            rule2newVariable.put(newRule, isNewVariable);
          }

          Set<Pair<T, T>> newNodeCoveredExamples = rule2coveredExamples.get(newRule);
          if (newNodeCoveredExamples == null) {
            newNodeCoveredExamples = Sets.newHashSet();
            rule2coveredExamples.put(newRule, newNodeCoveredExamples);
          }
          newNodeCoveredExamples.add(oneCoveredExample);
        }
      }
    }

    // return only rules which cover something above the threshold
    for (final RuleAtom oneAtom : rule2newVariable.keySet()) {
      if (rule2coveredExamples.get(oneAtom).size() <= minRuleSupport) {
        continue;
      }

      final MultipleGraphHornRule<T> newHornRule = this.duplicateRule();
      newHornRule.addRuleAtom(oneAtom, rule2newVariable.get(oneAtom), rule2coveredExamples.get(oneAtom));
      nextPlausibleRules.add(newHornRule);

    }
    return nextPlausibleRules;
  }

  /**
   * Add a rule atome to the current rule and modify the covered examples
   *
   * @param rule
   * @param newVariable
   * @param newCoveredExamples
   */

  public void addRuleAtom(RuleAtom rule, boolean newVariable, Set<Pair<T, T>> newCoveredExamples) {

    this.addRuleAtom(rule, newVariable);

    this.coveredExamples.clear();
    this.coveredExamples.addAll(newCoveredExamples);
  }

  /**
   * Initialise a Horn rule by giving a map containing a pair start-node as a key, and the corresponding graph as a value
   * @param example2graph
   */
  public MultipleGraphHornRule(Graph<T> g, boolean startsSubject, Set<Pair<T, T>> coveredExamples) {
    this();

    this.g = g;

    if (startsSubject) {
      this.startingVariable = START_NODE;
    } else {
      this.startingVariable = END_NODE;
    }

    this.coveredExamples.addAll(coveredExamples);
  }

  private MultipleGraphHornRule() {
    super();
    this.coveredExamples = Sets.newHashSet();
  }

  /**
   * Duplicate the current rule WITHOUT COPYING THE CURRENT COVERED EXAMPLES
   * @return
   */
  public MultipleGraphHornRule<T> duplicateRule() {
    final MultipleGraphHornRule<T> newRule = new MultipleGraphHornRule<T>();

    final List<RuleAtom> rules = Lists.newArrayList(this.rules);
    newRule.rules = rules;

    newRule.variableCount = variableCount;
    newRule.startingVariable = this.startingVariable;

    newRule.g = this.g;

    return newRule;
  }

  private boolean initialiseBoundingVariable() {

    if ((this.currentVariable != null) && (this.node2example2variable != null) && (this.currentNode2examples != null)) {
      return true;
    }

    final Set<Pair<T, T>> examples = g.getExamples();
    // build initial node2example2variable and currentNode2examples
    this.node2example2variable = Maps.newHashMap();
    this.currentNode2examples = Maps.newHashMap();
    for (final Pair<T, T> oneExample : examples) {
      final T subject = oneExample.getLeft();
      final T object = oneExample.getRight();

      Map<Pair<T, T>, String> currentExample2variable = this.node2example2variable.get(subject);
      if (currentExample2variable == null) {
        currentExample2variable = Maps.newHashMap();
        this.node2example2variable.put(subject, currentExample2variable);
      }
      currentExample2variable.put(oneExample, START_NODE);

      currentExample2variable = this.node2example2variable.get(object);
      if (currentExample2variable == null) {
        currentExample2variable = Maps.newHashMap();
        this.node2example2variable.put(object, currentExample2variable);
      }
      currentExample2variable.put(oneExample, END_NODE);

      final T currentNode = startingVariable.equals(START_NODE) ? subject : object;
      Set<Pair<T, T>> currentCoveredExamples = this.currentNode2examples.get(currentNode);
      if (currentCoveredExamples == null) {
        currentCoveredExamples = Sets.newHashSet();
        this.currentNode2examples.put(currentNode, currentCoveredExamples);
      }
      currentCoveredExamples.add(oneExample);
    }

    String currentVariable = startingVariable;
    final Set<String> seenVariables = Sets.newHashSet();
    seenVariables.add(START_NODE);
    seenVariables.add(END_NODE);
    final Set<Pair<T, T>> notCoveredExamples = Sets.newHashSet();
    this.currentVariable = startingVariable;

    // to avoid out of memory issues
    // current implementation can hold around 10M edges for each Giga of memory
    int totEdgesCount = 0;
    long maxEdgesCount = Runtime.getRuntime().maxMemory() / 100;
    // remove 1 giga from the maximum memory for further processing
    maxEdgesCount -= 10000000;
    for (final RuleAtom oneAtom : rules) {
      final Map<T, Set<Pair<T, T>>> newCurrentNodes = Maps.newHashMap();
      final String relation = oneAtom.getRelation();
      final boolean isInverse = !oneAtom.getSubject().equals(currentVariable);
      final String newVariable = isInverse ? oneAtom.getSubject() : oneAtom.getObject();

      for (final T oneNode : currentNode2examples.keySet()) {

        notCoveredExamples.clear();
        notCoveredExamples.addAll(currentNode2examples.get(oneNode));
        final Set<Edge<T>> neighbours = g.getNeighbours(oneNode);

        // need to keep track of available meory to avoid out of memory issue
        totEdgesCount += neighbours.size() * currentNode2examples.get(oneNode).size();

        if ((totEdgesCount > maxEdgesCount) && (rules.size() >= 2)) {
          return false;
        }

        for (final Edge<T> oneNeighbour : neighbours) {

          if (!oneNeighbour.getLabel().equals(relation) || (oneNeighbour.isArtificial() != isInverse)) {
            continue;
          }

          final T endNode = oneNeighbour.getNodeBottom();
          // for the current node get the not covered examples
          for (final Pair<T, T> oneExample : currentNode2examples.get(oneNode)) {
            final String currentNodeVariable = node2example2variable.get(endNode) != null
                ? node2example2variable.get(endNode).get(oneExample)
                : null;
            // check if the variable is the same
            boolean isGoodNode = false;
            if (seenVariables.contains(newVariable)
                && ((currentNodeVariable != null) && currentNodeVariable.equals(newVariable))) {
              isGoodNode = true;
            }
            if (!seenVariables.contains(newVariable) && (currentNodeVariable == null)) {
              Map<Pair<T, T>, String> example2variable = node2example2variable.get(endNode);
              if (example2variable == null) {
                example2variable = Maps.newHashMap();
                node2example2variable.put(endNode, example2variable);
              }
              example2variable.put(oneExample, newVariable);
              isGoodNode = true;
            }

            if (isGoodNode) {
              notCoveredExamples.remove(oneExample);
              Set<Pair<T, T>> newCurrentNodeCoveredExamples = newCurrentNodes.get(endNode);
              if (newCurrentNodeCoveredExamples == null) {
                newCurrentNodeCoveredExamples = Sets.newHashSet();
                newCurrentNodes.put(endNode, newCurrentNodeCoveredExamples);
              }
              newCurrentNodeCoveredExamples.add(oneExample);
            }
          }

        }

        // remove the not covered examples
        if (node2example2variable.containsKey(oneNode)) {
          node2example2variable.get(oneNode).keySet().removeAll(notCoveredExamples);
          if (node2example2variable.get(oneNode).size() == 0) {
            node2example2variable.remove(oneNode);
          }
        }
      }

      currentVariable = newVariable;
      seenVariables.add(newVariable);
      currentNode2examples = newCurrentNodes;
      this.currentVariable = newVariable;
    }

    return true;
  }

  public void dematerialiseRule() {
    this.node2example2variable = null;
    this.currentNode2examples = null;
    this.currentVariable = null;
  }

  public Set<Pair<T, T>> getCoveredExamples(T specifiNode) {
    if (this.currentNode2examples == null) {
      final boolean succ = this.initialiseBoundingVariable();
      if (!succ) {
        LOGGER.warn("Error while trying to instantiate rule {}. Cannot compute covered examples.", this.toString());
        return Sets.newHashSet();
      }
    }
    return this.currentNode2examples.get(specifiNode);
  }

  public Set<MultipleGraphHornRule<T>> nextOneHopPlausibleRules(int maxAtomThreshold, int minRuleSupport,
      String variable) {

    final Set<MultipleGraphHornRule<T>> nextPlausibleRules = Sets.newHashSet();
    if (!variable.equals(START_NODE) && !variable.equals(END_NODE)) {
      return nextPlausibleRules;
    }

    if (this.currentNode2examples == null) {

      final boolean succ = this.initialiseBoundingVariable();
      if (!succ) {
        LOGGER.warn("Error while trying to instantiate rule {}. Rule cannot be expanded.", this.toString());
        return nextPlausibleRules;
      }

    }

    final Set<T> obligedEndingNodes = Sets.newHashSet();
    for (final Pair<T, T> oneExample : this.coveredExamples) {
      if (variable.equals(START_NODE)) {
        obligedEndingNodes.add(oneExample.getLeft());
      } else {
        obligedEndingNodes.add(oneExample.getRight());
      }
    }

    // keep only variables for ending node
    this.node2example2variable.keySet().retainAll(obligedEndingNodes);

    final Map<RuleAtom, Set<Pair<T, T>>> rule2coveredExamples = Maps.newHashMap();
    // examine all obliged end nodes to see if they are connected to one or more currentNodes
    Set<Edge<T>> neighbours;
    Set<Pair<T, T>> currentExamples;
    for (final T singleObligedNode : obligedEndingNodes) {
      neighbours = this.g.getNeighbours(singleObligedNode);

      for (final Edge<T> oneNeighbour : neighbours) {
        final T endNode = oneNeighbour.getNodeBottom();
        if (!this.currentNode2examples.containsKey(endNode)) {
          continue;
        }

        // get current examples
        currentExamples = this.currentNode2examples.get(endNode);
        for (final Pair<T, T> oneExample : currentExamples) {
          // check the node is actually the desired node in the current example
          if ((node2example2variable.get(singleObligedNode).get(oneExample) == null)
              || !node2example2variable.get(singleObligedNode).get(oneExample).equals(variable)) {
            continue;
          }

          RuleAtom newRule = null;
          if (oneNeighbour.isArtificial()) {
            newRule = new RuleAtom(currentVariable, oneNeighbour.getLabel(), variable);
          } else {
            newRule = new RuleAtom(variable, oneNeighbour.getLabel(), currentVariable);
          }
          if (rules.contains(newRule)) {
            continue;
          }

          // TO DO: different check
          if (currentVariable.equals(variable)
              && !(oneNeighbour.getNodeBottom().equals(oneNeighbour.getNodeSource()))) {
            continue;
          }

          Set<Pair<T, T>> newNodeCoveredExamples = rule2coveredExamples.get(newRule);
          if (newNodeCoveredExamples == null) {
            newNodeCoveredExamples = Sets.newHashSet();
            rule2coveredExamples.put(newRule, newNodeCoveredExamples);
          }
          newNodeCoveredExamples.add(oneExample);
        }

      }
    }

    // create new rules
    for (final RuleAtom oneAtom : rule2coveredExamples.keySet()) {
      if (rule2coveredExamples.get(oneAtom).size() <= minRuleSupport) {
        continue;
      }

      final MultipleGraphHornRule<T> newHornRule = this.duplicateRule();
      newHornRule.addRuleAtom(oneAtom, false, rule2coveredExamples.get(oneAtom));
      nextPlausibleRules.add(newHornRule);

    }
    return nextPlausibleRules;
  }

  public void promoteConstant() {
    final boolean succ = this.initialiseBoundingVariable();
    if (!succ) {
      return;
    }
    final Map<String, T> variable2constant = Maps.newHashMap();
    final Set<String> allVariables = Sets.newHashSet();
    for (final RuleAtom oneAtom : this.rules) {
      allVariables.add(oneAtom.getSubject());
      allVariables.add(oneAtom.getObject());
    }

    final Set<String> currentVariables = Sets.newHashSet();
    for (final T oneNode : node2example2variable.keySet()) {
      final Map<Pair<T, T>, String> currentExamples = node2example2variable.get(oneNode);
      currentVariables.clear();
      currentVariables.addAll(currentExamples.values());
      for (final String oneVariable : currentVariables) {
        if (!allVariables.contains(oneVariable)) {
          continue;
        }

        if (variable2constant.containsKey(oneVariable) && !variable2constant.get(oneVariable).equals(oneNode)) {
          allVariables.remove(oneVariable);
        }

        if (allVariables.size() == 0) {
          return;
        }

        variable2constant.put(oneVariable, oneNode);
      }
    }

    if (allVariables.size() > 0) {
      final List<RuleAtom> newAtoms = Lists.newLinkedList();
      for (final RuleAtom atom : rules) {
        final String subj = allVariables.contains(atom.getSubject())
            && (variable2constant.get(atom.getSubject()) != null) ? variable2constant.get(atom.getSubject()).toString()
                : atom.getSubject();

        final String obj = allVariables.contains(atom.getObject()) && (variable2constant.get(atom.getObject()) != null)
            ? variable2constant.get(atom.getObject()).toString()
            : atom.getObject();

        newAtoms.add(new RuleAtom(subj, atom.getRelation(), obj));
      }
      this.rules = newAtoms;
    }
  }

}
