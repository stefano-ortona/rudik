package asu.edu.rule_miner.rudik.sparql.jena;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;
import asu.edu.rule_miner.rudik.sparql.jena.filter.TripleFilterFunctional;
import asu.edu.rule_miner.rudik.sparql.jena.remote.QuerySparqlRemoteEndpoint;

public abstract class QueryJenaLibrary extends SparqlExecutor {

  Map<String, Set<String>> predicate2positiveExamplesSupport;

  protected QueryExecution openResource;

  public QueryJenaLibrary(final Configuration config) {
    super(config);
  }

  private final static Logger LOGGER = LoggerFactory.getLogger(QuerySparqlRemoteEndpoint.class.getName());

  @Override
  public void executeQuery(final String entity, final Graph<String> graph,
                           final Map<String, Set<String>> entity2types) {
    // different query if the entity is a literal
    if (graph.isLiteral(entity)) {
      // this.compareLiterals(entity, graph);
      // if it's a literal do not return any neighbours because they might change based on the graph
      return;
    }

    String sparqlQuery = "SELECT DISTINCT ?sub ?rel ?obj";
    if ((this.graphIri != null) && (this.graphIri.length() > 0)) {
      sparqlQuery += " FROM " + this.graphIri;
    }
    sparqlQuery += " WHERE {" + "{<" + entity + "> ?rel ?obj.} " + "UNION " + "{?sub ?rel <" + entity + ">.}}";

    long startTime = System.currentTimeMillis();

    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(sparqlQuery);

    if (!results.hasNext()) {
      LOGGER.debug("Query '{}' returned an empty result.", sparqlQuery);
    }

    final long totalTime = System.currentTimeMillis() - startTime;
    final String logMessage = "Query '{}' took {} seconds to complete.";
    if (totalTime > MAX_QUERY_RUN_TIME) {
      LOGGER.warn(logMessage, sparqlQuery, totalTime / 1000.);
    }

    final TripleFilterFunctional tripFil = new TripleFilterFunctional();

    // TO DO: read from config file
    final String sub = "sub", rel = "rel", obj = "obj";

    final ArrayList<QuerySolution> resultTriples = tripFil.doFilter(results, sub, rel, obj, entity, this.subjectLimit,
            this.objectLimit);

    final Set<String> currentTypes = Sets.newHashSet();
    entity2types.put(entity, currentTypes);
    for (final QuerySolution oneResult : resultTriples) {

      final String relation = oneResult.get("rel").toString();

      // check the relation is a type relation
      if (relation.equals(this.typePrefix) && (oneResult.get("obj") != null)) {
        graph.addType(entity, oneResult.get("obj").toString());
      }

      if ((this.relationToAvoid != null) && this.relationToAvoid.contains(relation)) {
        continue;
      }

      boolean isTargetRelation = this.targetPrefix == null;
      if (this.targetPrefix != null) {
        for (final String targetRelation : this.targetPrefix) {
          if (relation.startsWith(targetRelation)) {
            isTargetRelation = true;
            break;
          }
        }
      }
      if (!isTargetRelation) {
        continue;
      }

      String nodeToAdd = null;
      Edge<String> edgeToAdd = null;
      String lexicalForm = null;

      final RDFNode subject = oneResult.get("sub");
      if (subject != null) {
        nodeToAdd = subject.toString();
        edgeToAdd = new Edge<String>(nodeToAdd, entity, relation);
        if (subject.isLiteral()) {
          lexicalForm = subject.asLiteral().getLexicalForm();
        }
      } else {
        final RDFNode object = oneResult.get("obj");
        if (object != null) {
          nodeToAdd = object.toString();
          edgeToAdd = new Edge<String>(entity, nodeToAdd, relation);
          if (object.isLiteral()) {
            if ((object.asLiteral() == null) || (object.asLiteral().getLexicalForm() == null)) {
              lexicalForm = object.toString();
            } else {
              lexicalForm = object.asLiteral().getLexicalForm();
            }
          }
        }
      }

      if (lexicalForm == null) {
        graph.addNode(nodeToAdd);
      } else {
        if (includeLiterals) {
          graph.addLiteralNode(nodeToAdd, lexicalForm);
        } else {
          continue;
        }
      }

      final boolean addEdge = graph.addEdge(edgeToAdd, true);
      if (!addEdge) {
        LOGGER.warn("Not able to insert the edge '{}' in the graph '{}'.", edgeToAdd, graph);
      }

    }

    startTime = System.currentTimeMillis();

    this.closeResources();
  }

  @Override
  public Set<Pair<String, String>> generateUnionNegativeExamples(final Set<String> relations, final String typeSubject,
                                                                 final String typeObject, final boolean subjectFunction, final boolean objectFunction) {
    String negativeCandidateQuery = super.generateNegativeExampleUnionQuery(relations, typeSubject, typeObject,
            subjectFunction, objectFunction, true);
    final Set<Pair<String, String>> negativeExamples = Sets.newHashSet();
    if (negativeCandidateQuery == null) {
      return negativeExamples;
    }

    LOGGER.debug("Executing negative candidate query selection '{}' on Sparql Endpoint...", negativeCandidateQuery);
    final long startTime = System.currentTimeMillis();
    if (this.openResource != null) {
      this.openResource.close();
    }

    // try to execute normal query
    ResultSet results = null;
    try {
      results = this.executeQuery(negativeCandidateQuery);
    } catch (final Exception e) {
      // if not able, put a limit of 5000 records
      if (super.negativeExampleLimit >= 0) {
        // already set a limit, remove the random part of the limit
        if (negativeCandidateQuery.contains("ORDER BY RAND()")) {
          LOGGER.debug("Cannot execute query with random constraints, removing the random part.");
          negativeCandidateQuery = negativeCandidateQuery.replace("ORDER BY RAND()", "");
          results = this.executeQuery(negativeCandidateQuery);
        } else {
          throw e;
        }
      } else {
        LOGGER.debug("Not able to execute normal query, setting a limit of 5000 results");
        results = this.executeQuery(negativeCandidateQuery + " LIMIT 5000");
      }
    }
    LOGGER.debug("Query executed in {} seconds.", (System.currentTimeMillis() - startTime) / 1000.0);

    while (results.hasNext()) {
      final QuerySolution oneResult = results.next();
      final String secondResult = oneResult.get("object").toString();
      final String firstResult = oneResult.get("subject").toString();
      if (!negativeExamples.contains(Pair.of(secondResult, firstResult))) {
        final Pair<String, String> negativeExample = Pair.of(firstResult, secondResult);
        negativeExamples.add(negativeExample);

      }
    }

    this.closeResources();
    LOGGER.debug("{} negative examples retrieved.", negativeExamples.size());

    return negativeExamples;
  }

  @Override
  public Set<Pair<String, String>> generatePositiveExamples(final Set<String> relations, final String typeSubject,
                                                            final String typeObject) {
    final String positiveCandidateQuery = super.generatePositiveExampleQuery(relations, typeSubject, typeObject, true);
    final Set<Pair<String, String>> positiveExamples = Sets.newHashSet();
    if (positiveCandidateQuery == null) {
      return positiveExamples;
    }

    LOGGER.debug("Executing positive candidate query selection '{}' on Sparql Endpoint...", positiveCandidateQuery);
    final long startTime = System.currentTimeMillis();
    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(positiveCandidateQuery);
    LOGGER.debug("Query executed in {} seconds.", (System.currentTimeMillis() - startTime) / 1000.0);

    while (results.hasNext()) {
      final QuerySolution oneResult = results.next();

      final String secondResult = oneResult.get("object").toString();
      final String firstResult = oneResult.get("subject").toString();
      if (!positiveExamples.contains(Pair.of(secondResult, firstResult))) {
        final Pair<String, String> positiveExample = Pair.of(firstResult, secondResult);
        positiveExamples.add(positiveExample);
      }

    }

    this.closeResources();
    LOGGER.debug("{} positive examples retrieved.", positiveExamples.size());

    return positiveExamples;
  }

  @Override
  public Set<Pair<String, String>> getKBExamples(final String query, final String subject, final String object,
                                                 final boolean includeInverted) {

    LOGGER.debug("Executing negative candidate query selection '{}' on Sparql Endpoint...", query);
    final long startTime = System.currentTimeMillis();
    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(query);
    LOGGER.debug("Query executed in {} seconds.", (System.currentTimeMillis() - startTime) / 1000.0);

    final Set<Pair<String, String>> examples = Sets.newHashSet();
    while (results.hasNext()) {
      final QuerySolution oneResult = results.next();
      String secondResult = oneResult.get(object).toString();
      // check object is not a literal
      if (oneResult.get(object).isLiteral()) {
        secondResult = oneResult.get(object).asLiteral().getLexicalForm();
      }
      final String firstResult = oneResult.get(subject).toString();
      if (includeInverted || !examples.contains(Pair.of(secondResult, firstResult))) {
        final Pair<String, String> currentExample = Pair.of(firstResult, secondResult);
        examples.add(currentExample);
      }
    }

    this.closeResources();
    LOGGER.debug("{} negative examples retrieved.", examples.size());

    return examples;
  }

  public abstract ResultSet executeQuery(String sparqlQuery);

  public void closeResources() {
    if (this.openResource != null) {
      this.openResource.close();
      this.openResource = null;
    }
  }

  @Override
  public int getSupportivePositiveExamples(final Set<RuleAtom> rules, final Set<String> relations,
                                           final String typeSubject, final String typeObject, final Set<Pair<String, String>> positiveExamples) {

    if (rules.size() == 0) {
      return 0;
    }
    final String positiveExamplesCountQuery = super.generateRulesPositiveExampleQuery(rules, relations, typeSubject,
            typeObject);

    return executeSubjectObjectQuery(positiveExamplesCountQuery, positiveExamples).size();
  }

  @Override
  public Set<Pair<String, String>> getMatchingPositiveExamples(final Set<RuleAtom> rules, final Set<String> relations,
                                                               final String typeSubject, final String typeObject, final Set<Pair<String, String>> positiveExamples) {

    if (rules.size() == 0) {
      return Sets.newHashSet();
    }
    final String positiveExamplesCountQuery = super.generateRulesPositiveExampleQuery(rules, relations, typeSubject,
            typeObject);

    return executeSubjectObjectQuery(positiveExamplesCountQuery, positiveExamples);

  }

  @Override
  public Set<Pair<String, String>> getMatchingNegativeExamples(final Set<RuleAtom> rules, final Set<String> relations,
                                                               final String typeSubject, final String typeObject, final Set<Pair<String, String>> negativeExamples,
                                                               final boolean subjectFuntion, final boolean objectFunction) {

    if (rules.size() == 0) {
      return Sets.newHashSet();
    }
    final String negativeExamplesCountQuery = super.generateRulesNegativeExampleQuery(rules, relations, typeSubject,
            typeObject, subjectFuntion, objectFunction);

    return executeSubjectObjectQuery(negativeExamplesCountQuery, negativeExamples);

  }

  @Override
  public Set<Pair<String, String>> executeHornRuleQuery(final Set<RuleAtom> rules, final String typeSubject,
                                                        final String typeObject) {
    if (rules.size() == 0) {
      return Sets.newHashSet();
    }
    final String hornRuleQuery = super.generateHornRuleQuery(rules, typeSubject, typeObject, false);

    return executeSubjectObjectQuery(hornRuleQuery, null);
  }

  @Override
  public List<List<Pair<String, String>>> instantiateHornRule(Set<String> targetPredicates, Set<RuleAtom> rules,
                                                              final String subjType, final String objType, boolean positive, int maxInstantiationNumber) {
    if (rules.size() == 0) {
      return Lists.newArrayList();
    }
    final String hornRuleQuery = super.generateHornRuleQueryInstantiation(targetPredicates, rules, subjType, objType,
            true, positive, maxInstantiationNumber);
    return executeStarQuery(hornRuleQuery);
  }

  @Override
  public Set<Pair<String, String>> executeRestrictiveHornRuleQuery(final String queryRestriction,
                                                                   final Set<RuleAtom> rules, final String typeSubject, final String typeObject) {
    if (rules.size() == 0) {
      return Sets.newHashSet();
    }
    String hornRuleQuery = super.generateHornRuleQuery(rules, typeSubject, typeObject, false);
    hornRuleQuery = hornRuleQuery.replaceAll("WHERE \\{", "WHERE {" + queryRestriction + " ");

    return executeSubjectObjectQuery(hornRuleQuery, null);

  }

  private Set<Pair<String, String>> executeSubjectObjectQuery(final String query,
                                                              final Set<Pair<String, String>> examples) {
    final Set<Pair<String, String>> matchingPositiveExamples = Sets.newHashSet();
    if (query == null) {
      return matchingPositiveExamples;
    }

    LOGGER.debug("Executing sparql rule query '{}' on Sparql Endpoint...", query);
    final long startTime = System.currentTimeMillis();
    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(query);
    final long totalTime = System.currentTimeMillis() - startTime;
    final String logMessage = "Query '{}' took {} seconds to complete.";
    if (totalTime > MAX_QUERY_RUN_TIME) {
      LOGGER.warn(logMessage, query, totalTime / 1000.);
    } else {
      LOGGER.debug(logMessage, query, totalTime / 1000.);
    }

    while (results.hasNext()) {

      final QuerySolution oneResult = results.next();
      final String subject = oneResult.get("subject").toString();
      final String object = oneResult.get("object").toString();
      if ((subject != null) && (object != null)
              && ((examples == null) || examples.contains(Pair.of(subject, object)))) {
        matchingPositiveExamples.add(Pair.of(subject, object));
      }
    }
    this.closeResources();

    return matchingPositiveExamples;

  }

  private List<List<Pair<String, String>>> executeStarQuery(final String query) {
    if (query == null) {
      return Lists.newArrayList();
    }

    LOGGER.debug("Executing sparql rule query '{}' on Sparql Endpoint...", query);
    final long startTime = System.currentTimeMillis();
    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(query);
    LOGGER.debug("Query executed in {} seconds.", (System.currentTimeMillis() - startTime) / 1000.0);
    final List<List<Pair<String, String>>> allBindings = Lists.newLinkedList();
    while (results.hasNext()) {
      final List<Pair<String, String>> currentBinding = Lists.newArrayList();
      final QuerySolution oneResult = results.next();
      final Iterator<String> variables = oneResult.varNames();
      while (variables.hasNext()) {
        String oneVar = variables.next();
        final String value = oneResult.get(oneVar).toString();
        // if it is a constant, add binding constant to constant
        if (oneVar.startsWith(CONSTANT_SUBSTITUTION_VARIABLE)) {
          oneVar = value;
        }
        currentBinding.add(Pair.of(oneVar, value));
      }
      allBindings.add(currentBinding);
    }
    this.closeResources();
    return allBindings;
  }

  @Override
  public Set<String> getAllPredicates() {
    final String query = super.allPredicatesQuery("rel");
    LOGGER.debug("Executing sparql rule query '{}' on Sparql Endpoint...", query);
    final long startTime = System.currentTimeMillis();
    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(query);
    LOGGER.debug("Query executed in {} seconds.", (System.currentTimeMillis() - startTime) / 1000.0);
    final Set<String> allPredicates = Sets.newHashSet();
    while (results.hasNext()) {
      final QuerySolution oneResult = results.next();
      final String relation = oneResult.get("rel").toString();
      if ((relation != null) && !relation.isEmpty()
              && ((targetPrefix == null) || targetPrefix.stream().anyMatch(prefix -> {
        return relation.startsWith(prefix);
      }))) {
        allPredicates.add(relation);
      }
    }
    this.closeResources();
    return allPredicates;
  }

  @Override
  public Pair<String, String> getPredicateTypes(final String inputPredicate) {
    String query = super.predicateSubjectTypeQuery(inputPredicate, "type");
    final String subjType = getType("type", query);
    query = super.predicateObjectTypeQuery(inputPredicate, "type");
    final String objType = getType("type", query);
    this.closeResources();
    return Pair.of(subjType, objType);
  }

  private String getType(final String typeName, final String query) {
    if (query == null) {
      return null;
    }
    LOGGER.debug("Executing sparql rule query '{}' on Sparql Endpoint...", query);
    final long startTime = System.currentTimeMillis();
    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(query);
    LOGGER.debug("Query executed in {} seconds.", (System.currentTimeMillis() - startTime) / 1000.0);
    // types ordered by popularity
    while (results.hasNext()) {
      final QuerySolution oneResult = results.next();
      final String type = oneResult.get(typeName).toString();
      if ((type != null) && !type.isEmpty() && !genericTypes.contains(type)
              && ((targetPrefix == null) || targetPrefix.stream().anyMatch(prefix -> {
        return type.startsWith(prefix);
      }))) {
        return type;
      }
    }
    return null;
  }

  @Override
  public int executeCountQuery(final String inputQuery) {
    // get the count variable
    if (!inputQuery.contains("count") && !inputQuery.contains("COUNT")) {
      return -1;
    }
    LOGGER.debug("Executing sparql rule query '{}' on Sparql Endpoint...", inputQuery);
    if (this.openResource != null) {
      this.openResource.close();
    }
    final ResultSet results = this.executeQuery(inputQuery);

    int result = -1;
    final List<String> resultsVariable = results.getResultVars();
    if (results.hasNext() && (resultsVariable.size() > 0)) {
      final QuerySolution oneResult = results.next();
      try {
        result = oneResult.getLiteral(resultsVariable.get(0)).getInt();
      } catch (final Exception e) {
        LOGGER.warn("Not able to parse as integer the result of the count query {}.", inputQuery);
      }
    }
    this.closeResources();

    return result;
  }

  @Override
  public Map<String, Set<Pair<String, String>>> getRulePositiveSupport(
          final Set<Pair<String, String>> positiveExamples) {

    final Map<String, Set<Pair<String, String>>> predicate2support = Maps.newHashMap();
    final Set<String> totalPermutations = Sets.newHashSet();
    totalPermutations.add("subject");
    totalPermutations.add("object");

    final Map<String, Set<String>> entity2predicate = Maps.newHashMap();
    LOGGER.debug("Compute predicate support for all positive examples...");
    int exampleCount = 0;

    for (final Pair<String, String> oneExample : positiveExamples) {
      for (final String permutation : totalPermutations) {

        exampleCount++;
        if ((exampleCount % 100) == 0) {
          LOGGER.debug("Computed {} examples out of {}", exampleCount, positiveExamples.size() * 2);
        }

        String currentExample = oneExample.getLeft().toString();
        if (permutation.equals("object")) {
          currentExample = oneExample.getRight().toString();
        }

        if (entity2predicate.containsKey(currentExample)) {
          final Set<String> currentPredicates = entity2predicate.get(currentExample);
          for (final String predicate : currentPredicates) {
            Set<Pair<String, String>> currentPredicateSupport = predicate2support
                    .get(predicate.replaceAll("entity", permutation));
            if (currentPredicateSupport == null) {
              currentPredicateSupport = Sets.newHashSet();
              predicate2support.put(predicate.replaceAll("entity", permutation), currentPredicateSupport);
            }

            currentPredicateSupport.add(oneExample);
          }
        } else {
          String sparqlQuery = "SELECT ?sub ?rel ?obj";
          if ((this.graphIri != null) && (this.graphIri.length() > 0)) {
            sparqlQuery += " FROM " + this.graphIri;
          }
          sparqlQuery += " WHERE {" + "{<" + currentExample.toString() + "> ?rel ?obj.} " + "UNION " + "{?sub ?rel <"
                  + currentExample.toString() + ">.}}";

          final long startTime = System.currentTimeMillis();

          if (this.openResource != null) {
            this.openResource.close();
          }

          try {
            final ResultSet results = this.executeQuery(sparqlQuery);

            if (!results.hasNext()) {
              LOGGER.debug("Query '{}' returned an empty result!", sparqlQuery);
            }

            final long totalTime = System.currentTimeMillis() - startTime;
            if (totalTime > 50000) {
              LOGGER.debug("Query '{}' took {} seconds to complete.", sparqlQuery, totalTime / 1000.);
            }

            final Set<String> currentEntitySupport = Sets.newHashSet();
            entity2predicate.put(currentExample, currentEntitySupport);
            while (results.hasNext()) {
              final QuerySolution oneResult = results.next();

              final String relation = oneResult.get("rel").toString();

              if ((this.relationToAvoid != null) && this.relationToAvoid.contains(relation)) {
                continue;
              }

              boolean isTargetRelation = this.targetPrefix == null;
              if (this.targetPrefix != null) {
                for (final String targetRelation : this.targetPrefix) {
                  if (relation.startsWith(targetRelation)) {
                    isTargetRelation = true;
                    break;
                  }
                }
              }
              if (!isTargetRelation) {
                continue;
              }
              final boolean inverse = oneResult.get("sub") != null;
              String predicate = relation + "(" + permutation + ",_)";
              if (inverse) {
                predicate = relation + "(_," + permutation + ")";
              }

              Set<Pair<String, String>> currentSupportExamples = predicate2support.get(predicate);
              if (currentSupportExamples == null) {
                currentSupportExamples = Sets.newHashSet();
                predicate2support.put(predicate, currentSupportExamples);
              }
              currentSupportExamples.add(oneExample);

              currentEntitySupport.add(predicate.replaceAll(permutation, "entity"));
            }

          } catch (final Exception e) {
            LOGGER.warn("Unable to execute query '{}'.", sparqlQuery);
          }
          this.closeResources();
        }
      }
    }

    return predicate2support;
  }

}