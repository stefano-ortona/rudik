package asu.edu.rule_miner.rudik.sparql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import asu.edu.rule_miner.rudik.RuleMinerException;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;

/**
 * Abstract class to model a Sparql engine endpoint The method executeQuery must executed a select query on the input
 * entity and write the results on the input graph The method generateNegativeExamples has to produce negative examples
 * as pairs of RDFNodes
 * @author ortona
 */
public abstract class SparqlExecutor {

  private final static Logger LOGGER = LoggerFactory.getLogger(SparqlExecutor.class.getName());

  protected Set<String> prefixQuery;

  protected Set<String> targetPrefix;

  protected Set<String> relationToAvoid;

  protected Set<String> genericTypes;

  protected String graphIri;

  protected String typePrefix;

  protected int subjectLimit = -1;

  protected int objectLimit = -1;

  protected int negativeExampleLimit = -1;

  protected int positiveExampleLimit = -1;

  protected boolean includeLiterals = true;

  // max runtime for a sparql query in milliseconds, make it configurable
  protected long MAX_QUERY_RUN_TIME = 20000;

  protected final static String CONSTANT_SUBSTITUTION_VARIABLE = "constantVariable_";

  @SuppressWarnings("unchecked")
  public SparqlExecutor(final Configuration config) {

    if (config.containsKey(Constant.CONF_RELATION_PREFIX)) {
      this.prefixQuery = Sets.newHashSet();
      try {
        final HierarchicalConfiguration hierarchicalConfig = (HierarchicalConfiguration) config;
        final List<HierarchicalConfiguration> prefixes = hierarchicalConfig
                .configurationsAt(Constant.CONF_RELATION_PREFIX);
        for (final HierarchicalConfiguration prefix : prefixes) {
          final String name = prefix.getString(Constant.CONF_RELATION_PREFIX_NAME);
          final String uri = prefix.getString(Constant.CONF_RELATION_PREFIX_URI);
          if ((name != null) && (name.length() > 0) && (uri != null) && (uri.length() > 0)) {
            prefixQuery.add("PREFIX " + name + ": <" + uri + ">");
          }
        }

      } catch (final Exception e) {
        LOGGER.error("Error while reading " + Constant.CONF_RELATION_PREFIX + " parameter from the configuration file.",
                e);
      }
    }

    if (!config.containsKey(Constant.CONF_TYPE_PREFIX) || (config.getString(Constant.CONF_TYPE_PREFIX).length() == 0)) {
      throw new RuleMinerException("No " + Constant.CONF_TYPE_PREFIX + " specific in the Configuration file.", LOGGER);
    }
    typePrefix = config.getString(Constant.CONF_TYPE_PREFIX);

    if (config.containsKey(Constant.CONF_RELATION_TARGET_REFIX)) {
      this.targetPrefix = Sets.newHashSet();
      final List<String> objects = config.getList(Constant.CONF_RELATION_TARGET_REFIX);
      for (final String object : objects) {
        this.targetPrefix.add(object);
      }
    }

    if (config.containsKey(Constant.CONF_GRAPH_IRI) && (config.getString(Constant.CONF_GRAPH_IRI).length() > 0)) {
      this.graphIri = "<" + config.getString(Constant.CONF_GRAPH_IRI) + ">";
    }

    this.relationToAvoid = Sets.newHashSet();
    if (config.containsKey(Constant.CONF_RELATION_TO_AVOID)) {
      final List<String> objects = config.getList(Constant.CONF_RELATION_TO_AVOID);
      for (final String object : objects) {
        this.relationToAvoid.add(object);
      }
    }

    // read limit from config file
    if (config.containsKey(Constant.CONF_SUBJECT_LIMIT)) {
      try {
        this.subjectLimit = config.getInt(Constant.CONF_SUBJECT_LIMIT);
      } catch (final Exception e) {
        LOGGER.error("Error while setting " + Constant.CONF_SUBJECT_LIMIT + " configuration parameter.", e);
      }
    }

    if (config.containsKey(Constant.CONF_OBJECT_LIMIT)) {
      try {
        this.objectLimit = config.getInt(Constant.CONF_OBJECT_LIMIT);
      } catch (final Exception e) {
        LOGGER.error("Error while setting " + Constant.CONF_OBJECT_LIMIT + " configuration parameter.", e);
      }
    }

    if (config.containsKey(Constant.CONF_POS_EXAMPLES_LIMIT)) {
      try {
        this.positiveExampleLimit = config.getInt(Constant.CONF_POS_EXAMPLES_LIMIT);
      } catch (final Exception e) {
        LOGGER.error("Error while setting " + Constant.CONF_POS_EXAMPLES_LIMIT + " configuration parameter.", e);
      }
    }

    if (config.containsKey(Constant.CONF_NEG_EXAMPLES_LIMIT)) {
      try {
        this.negativeExampleLimit = config.getInt(Constant.CONF_NEG_EXAMPLES_LIMIT);
      } catch (final Exception e) {
        LOGGER.error("Error while setting " + Constant.CONF_NEG_EXAMPLES_LIMIT + " configuration parameter.", e);
      }
    }

    if (config.containsKey(Constant.CONF_INCLUDE_LITERALS)) {
      try {
        this.includeLiterals = config.getBoolean(Constant.CONF_INCLUDE_LITERALS);
      } catch (final Exception e) {
        LOGGER.error("Error while setting " + Constant.CONF_INCLUDE_LITERALS + " configuration parameter.", e);
      }
    }

    this.genericTypes = Sets.newHashSet();
    if (config.containsKey(Constant.CONF_GENERIC_TYPES)) {
      final List<String> objects = config.getList(Constant.CONF_GENERIC_TYPES);
      for (final String object : objects) {
        this.genericTypes.add(object);
      }
    }

  }

  public abstract void executeQuery(String entity, Graph<String> inputGraphs, Map<String, Set<String>> entity2types);

  public abstract Set<Pair<String, String>> generateUnionNegativeExamples(Set<String> relations, String typeSubject,
                                                                          String typeObject, boolean subjectFunction, boolean objectFunction);

  public abstract Set<Pair<String, String>> generatePositiveExamples(Set<String> relations, String typeSubject,
                                                                     String typeObject);

  public abstract Set<String> getAllPredicates();

  public abstract Pair<String, String> getPredicateTypes(final String inputPredicate);

  /**
   * @param query
   * @param subject
   * @param object
   * @param includeInverted if set to false, the output will not include the pair <a,b> if the output already contains
   *          the pair <b,a>
   * @return
   */
  public abstract Set<Pair<String, String>> getKBExamples(String query, String subject, String object,
                                                          boolean includeInverted);

  public String generatePositiveExampleQuery(final Set<String> relations, final String typeSubject,
                                             final String typeObject, final boolean includeLimitation) {

    if ((relations == null) || (relations.size() == 0)) {
      return null;
    }

    // create the RDF
    final StringBuilder query = new StringBuilder();

    final Iterator<String> relationIterator = relations.iterator();
    final StringBuilder filterRelation = new StringBuilder();
    while (relationIterator.hasNext()) {
      final String currentRelation = relationIterator.next();
      filterRelation.append("?targetRelation = <" + currentRelation + ">");
      if (relationIterator.hasNext()) {
        filterRelation.append(" || ");
      }
    }

    if ((this.prefixQuery != null) && (this.prefixQuery.size() > 0)) {
      for (final String prefix : this.prefixQuery) {
        query.append(prefix + " ");
      }
    }
    query.append("SELECT DISTINCT ?subject ?object");

    if ((this.graphIri != null) && (graphIri.length() > 0)) {
      query.append(" FROM " + this.graphIri);
    }

    query.append(" WHERE {");

    if (typeObject != null) {
      query.append("  ?object <" + typePrefix + "> <" + typeObject + ">.");
    }
    if (typeSubject != null) {
      query.append("  ?subject <" + typePrefix + "> <" + typeSubject + ">.");
    }
    query.append("  ?subject ?targetRelation ?object. " + "  FILTER (" + filterRelation.toString() + ") }");

    if (includeLimitation) {
      if (this.positiveExampleLimit >= 0) {
        query.append(" ORDER BY RAND() LIMIT " + this.positiveExampleLimit*2);
      }
    }

    return query.toString();
  }

  public String generateRulesPositiveExampleQuery(final Set<RuleAtom> rules, final Set<String> relations,
                                                  final String typeSubject, final String typeObject) {

    if (!(rules.size() > 0)) {
      return null;
    }

    String query = this.generatePositiveExampleQuery(relations, typeSubject, typeObject, false);
    query = query.substring(0, query.length() - 1) + " " + this.getHornRuleAtomQuery(rules) + " }";

    return query;
  }

  public String allPredicatesQuery(final String predicateVariable) {
    String query = "select distinct ?" + predicateVariable + " ";
    if (this.graphIri != null) {
      query += "from " + this.graphIri + " ";
    }
    query += "where {?sub ?" + predicateVariable + " ?obj.}";
    return query;
  }

  public String generateRulesNegativeExampleQuery(final Set<RuleAtom> rules, final Set<String> relations,
                                                  final String typeSubject, final String typeObject, final boolean subjectFunction, final boolean objectFunction) {

    if (!(rules.size() > 0)) {
      return null;
    }

    String query = this.generateNegativeExampleUnionQuery(relations, typeSubject, typeObject, subjectFunction,
            objectFunction, false);
    query = query.substring(0, query.length() - 1) + " " + this.getHornRuleAtomQuery(rules) + " }";

    return query;
  }

  public String getHornRuleAtomQuery(final Set<RuleAtom> rules) {
    final StringBuilder atomFilterBuilder = new StringBuilder();
    RuleAtom inequalityAtom = null;
    final Map<String, String> constant2variable = Maps.newHashMap();
    for (final RuleAtom atom : rules) {
      final String subject = getVariableSubstitution(atom.getSubject(), constant2variable, rules);
      final String object = getVariableSubstitution(atom.getObject(), constant2variable, rules);
      if (atom.getRelation().equals(Constant.GREATER_EQUAL_REL) || atom.getRelation().equals(Constant.LESS_EQUAL_REL)
              || atom.getRelation().equals(Constant.GREATER_REL) || atom.getRelation().equals(Constant.LESS_REL)
              || atom.getRelation().equals(Constant.EQUAL_REL)) {
        atomFilterBuilder.append("FILTER (?" + subject + atom.getRelation() + "?" + object + ") ");
        continue;
      }
      if (atom.getRelation().equals("!=")) {
        inequalityAtom = atom;
        continue;
      }
      atomFilterBuilder.append("?" + subject + " <" + atom.getRelation() + "> ?" + object + ". ");
    }
    atomFilterBuilder.toString();
    if (inequalityAtom != null) {
      atomFilterBuilder.append(this.inequalityFilter(rules, inequalityAtom)).append(" ");
    }
    // for each constant variable, impose the equality to constants
    constant2variable.forEach((k, v) -> {
      atomFilterBuilder.append("FILTER(?").append(v).append("=<").append(k).append(">) ");
    });
    return atomFilterBuilder.toString();
  }

  private String getVariableSubstitution(String constant, Map<String, String> constant2variable, Set<RuleAtom> atoms) {
    if (constant2variable.containsKey(constant)) {
      return constant2variable.get(constant);
    }
    // check it it a constant
    if (constant.startsWith("http")) {
      // do variable substitution
      String createdVariable = null;
      // if subject or object are missing, that is the variable. Cannot be both
      if (isSubjectMissing(atoms)) {
        createdVariable = HornRule.START_NODE;
      }
      if (isObjectMissing(atoms)) {
        if (createdVariable != null) {
          throw new RuleMinerException("Rule atoms contains neither " + HornRule.START_NODE + " nor "
                  + HornRule.END_NODE + " variables, cannot instantiate rule.", LOGGER);
        }
        createdVariable = HornRule.END_NODE;
      }
      if (createdVariable == null) {
        // need to create a loose variable
        if (constant2variable.containsValue(HornRule.START_NODE)
                || constant2variable.containsValue(HornRule.END_NODE)) {
          // cannot instantiate rule if subject (object) is missing and there is another constant
          throw new RuleMinerException("Rule atoms do not contain either " + HornRule.START_NODE + " or "
                  + HornRule.END_NODE + " variables, and there is another constant in the rule. Cannot instantiate rule.",
                  LOGGER);
        }
        createdVariable = CONSTANT_SUBSTITUTION_VARIABLE + constant2variable.size();
      }
      constant2variable.put(constant, createdVariable);
      return createdVariable;
    } else {
      // return the variable and do nothing
      return constant;
    }
  }

  private boolean isSubjectMissing(Set<RuleAtom> atoms) {
    return isVariableMissing(atoms, HornRule.START_NODE);
  }

  private boolean isObjectMissing(Set<RuleAtom> atoms) {
    return isVariableMissing(atoms, HornRule.END_NODE);
  }

  private boolean isVariableMissing(Set<RuleAtom> atoms, final String variableTarget) {
    for (final RuleAtom oneAtom : atoms) {
      if (oneAtom.getSubject().equals(variableTarget) || oneAtom.getObject().equals(variableTarget)) {
        return false;
      }
    }
    return true;
  }

  private String inequalityFilter(final Set<RuleAtom> rules, final RuleAtom inequalityAtom) {
    final StringBuilder inequalityFilter = new StringBuilder();
    inequalityFilter.append("FILTER NOT EXISTS {");
    String variableToSubstitue = inequalityAtom.getSubject();
    String replacementVariable = inequalityAtom.getObject();
    if (variableToSubstitue.equals(HornRule.START_NODE) || variableToSubstitue.equals(HornRule.END_NODE)) {
      variableToSubstitue = inequalityAtom.getObject();
      replacementVariable = inequalityAtom.getSubject();
    }

    if (!replacementVariable.equals(HornRule.START_NODE) && !replacementVariable.equals(HornRule.END_NODE)) {
      replacementVariable = "other" + replacementVariable;
    }

    for (final RuleAtom atom : rules) {
      if (atom.equals(inequalityAtom)) {
        continue;
      }

      String subject = atom.getSubject();
      if (subject.equals(variableToSubstitue)) {
        subject = replacementVariable;
      } else {
        if (!subject.equals(HornRule.START_NODE) && !subject.equals(HornRule.END_NODE)) {
          subject = "other" + subject;
        }
      }

      String object = atom.getObject();
      if (object.equals(variableToSubstitue)) {
        object = replacementVariable;
      } else {
        if (!object.equals(HornRule.START_NODE) && !object.equals(HornRule.END_NODE)) {
          object = "other" + object;
        }
      }

      if (atom.getRelation().equals(Constant.GREATER_EQUAL_REL) || atom.getRelation().equals(Constant.LESS_EQUAL_REL)
              || atom.getRelation().equals(Constant.GREATER_REL) || atom.getRelation().equals(Constant.LESS_REL)
              || atom.getRelation().equals(Constant.EQUAL_REL)) {
        inequalityFilter.append("FILTER (?" + subject + atom.getRelation() + "?" + object + ") ");
        continue;
      }
      inequalityFilter.append("?" + subject + " <" + atom.getRelation() + "> ?" + object + ". ");
    }
    inequalityFilter.append("}");

    return inequalityFilter.toString();
  }

  public String generateOneSideExampleQuery(final Set<String> relations, final String type, final String variable) {

    final Iterator<String> relationIterator = relations.iterator();
    final StringBuilder filterRelation = new StringBuilder();
    while (relationIterator.hasNext()) {
      final String currentRelation = relationIterator.next();
      filterRelation.append("?targetRelation = <" + currentRelation + ">");
      if (relationIterator.hasNext()) {
        filterRelation.append(" || ");
      }
    }

    String oneSideExampleQuery = "";
    if ((this.prefixQuery != null) && (this.prefixQuery.size() > 0)) {
      for (final String prefix : this.prefixQuery) {
        oneSideExampleQuery += prefix + " ";
      }
    }

    oneSideExampleQuery += "SELECT DISTINCT ?" + variable + " ";
    // if(this.graphIri!=null&&graphIri.length()>0)
    // oneSideExampleQuery+=" FROM "+this.graphIri;

    oneSideExampleQuery += " WHERE " + "{ ?" + variable + " <" + typePrefix + "> <" + type + ">.";

    oneSideExampleQuery += " ?subject ?targetRelation ?object. ";

    oneSideExampleQuery += "  FILTER (" + filterRelation.toString() + ") }";

    return oneSideExampleQuery;
  }

  public String generateNegativeExampleUnionQuery(final Set<String> relations, final String typeSubject,
                                                  final String typeObject, final boolean subjectFunction, final boolean objectFunction,
                                                  final boolean includeLimit) {
    if ((relations == null) || (relations.size() == 0)) {
      return null;
    }

    // create the RDF
    final StringBuilder filterRelation = new StringBuilder();
    final StringBuilder filterNotRelation = new StringBuilder();
    final StringBuilder differentRelation = new StringBuilder();
    final Iterator<String> relationIterator = relations.iterator();

    while (relationIterator.hasNext()) {
      final String currentRelation = relationIterator.next();
      filterRelation.append("?targetRelation = <" + currentRelation + ">");
      filterNotRelation.append("?otherRelation != <" + currentRelation + ">");
      if (relationIterator.hasNext()) {
        filterRelation.append(" || ");
        filterNotRelation.append(" && ");
      }
      differentRelation.append("  FILTER NOT EXISTS {?subject <" + currentRelation + "> ?object.} ");
    }

    String negativeCandidateQuery = "";
    if ((this.prefixQuery != null) && (this.prefixQuery.size() > 0)) {
      for (final String prefix : this.prefixQuery) {
        negativeCandidateQuery += prefix + " ";
      }
    }

    negativeCandidateQuery += "SELECT DISTINCT ?subject ?object ";
    if ((this.graphIri != null) && (graphIri.length() > 0)) {
      negativeCandidateQuery += " FROM " + this.graphIri;
    }

    negativeCandidateQuery += " WHERE {";
    if (typeObject != null) {
      negativeCandidateQuery += " ?object <" + typePrefix + "> <" + typeObject + ">.";
    }
    if (typeSubject != null) {
      negativeCandidateQuery += "  ?subject <" + typePrefix + "> <" + typeSubject + ">.";
    }

    // if both true or false, include both
    if ((subjectFunction == false) && (objectFunction == false)) {
      negativeCandidateQuery += " {{?subject ?targetRelation ?realObject.} UNION "
              + " {?realSubject ?targetRelation ?object.}} ";
    } else {
      if (subjectFunction) {
        negativeCandidateQuery += " ?subject ?targetRelation ?realObject. ";
      }
      if (objectFunction) {
        negativeCandidateQuery += " ?realSubject ?targetRelation ?object. ";
      }
    }

    negativeCandidateQuery += "  ?subject ?otherRelation ?object. " + "  FILTER (" + filterRelation.toString() + ") "
            + "  FILTER (" + filterNotRelation.toString() + ") " + differentRelation.toString();

    negativeCandidateQuery += "}";

    if (includeLimit) {
      if (this.negativeExampleLimit >= 0) {
        negativeCandidateQuery += " ORDER BY RAND() LIMIT " + this.negativeExampleLimit;
      }
    }

    return negativeCandidateQuery;
  }

  /**
   * If switchSubjectObject is set to true, looks for pair where object is in relation with subject rather than subject
   * in relation with object
   * @param relations
   * @param typeSubject
   * @param typeObject
   * @param switchSubjectObject
   * @return
   */
  public String generateNegativeExampleRestrictedQuery(final Set<String> relations, final String typeSubject,
                                                       final String typeObject, final boolean switchSubjectObject) {
    if ((relations == null) || (relations.size() == 0)) {
      return null;
    }

    // create the RDF
    final StringBuilder filterRelation = new StringBuilder();
    final StringBuilder filterNotRelation = new StringBuilder();
    final StringBuilder differentRelation = new StringBuilder();
    final Iterator<String> relationIterator = relations.iterator();

    while (relationIterator.hasNext()) {
      final String currentRelation = relationIterator.next();
      filterRelation.append("?targetRelation = <" + currentRelation + ">");
      filterNotRelation.append("?otherRelation != <" + currentRelation + ">");
      if (relationIterator.hasNext()) {
        filterRelation.append(" || ");
        filterNotRelation.append(" && ");
      }
      differentRelation.append("  FILTER NOT EXISTS {?subject <" + currentRelation + "> ?object.} ");
    }

    String negativeCandidateQuery = "";
    if ((this.prefixQuery != null) && (this.prefixQuery.size() > 0)) {
      for (final String prefix : this.prefixQuery) {
        negativeCandidateQuery += prefix + " ";
      }
    }

    negativeCandidateQuery += "SELECT DISTINCT ?subject ?object ";
    if ((this.graphIri != null) && (graphIri.length() > 0)) {
      negativeCandidateQuery += " FROM " + this.graphIri;
    }

    negativeCandidateQuery += " WHERE " + "{ ?object <" + typePrefix + "> <" + typeObject + ">." + "  ?subject <"
            + typePrefix + "> <" + typeSubject + ">." + "  ?subject ?targetRelation ?realObject. "
            + "  ?realSubject ?targetRelation ?object. ";
    if (switchSubjectObject) {
      negativeCandidateQuery += "  ?object ?otherRelation ?subject. ";
    } else {
      negativeCandidateQuery += "  ?subject ?otherRelation ?object. ";
    }

    negativeCandidateQuery += "  FILTER (" + filterRelation.toString() + ") " + "  FILTER ("
            + filterNotRelation.toString() + ") " + differentRelation.toString();

    negativeCandidateQuery += "}";

    return negativeCandidateQuery;
  }

  public String generateNegativeExtendedExampleQuery(final Set<String> relations, final String typeSubject,
                                                     final String typeObject) {
    if ((relations == null) || (relations.size() == 0)) {
      return null;
    }

    // create the RDF
    final StringBuilder filterRelation = new StringBuilder();
    final StringBuilder differentRelation = new StringBuilder();
    final Iterator<String> relationIterator = relations.iterator();

    while (relationIterator.hasNext()) {
      final String currentRelation = relationIterator.next();
      filterRelation.append("?targetRelation = <" + currentRelation + ">");
      if (relationIterator.hasNext()) {
        filterRelation.append(" || ");
      }
    }

    String negativeCandidateQuery = "";
    if ((this.prefixQuery != null) && (this.prefixQuery.size() > 0)) {
      for (final String prefix : this.prefixQuery) {
        negativeCandidateQuery += prefix + " ";
      }
    }

    negativeCandidateQuery += "SELECT DISTINCT ?subject ?object ";
    if ((this.graphIri != null) && (graphIri.length() > 0)) {
      negativeCandidateQuery += " FROM " + this.graphIri;
    }

    negativeCandidateQuery += " WHERE " + "{ ?object <" + typePrefix + "> <" + typeObject + ">." + "  ?subject <"
            + typePrefix + "> <" + typeSubject + ">." + "  ?subject ?targetRelation ?realObject. "
            + "  ?realSubject ?targetRelation ?object. " + "  FILTER (" + filterRelation.toString() + ") "
            + "  FILTER NOT EXISTS {?subject ?relation ?object.} " + differentRelation.toString();
    negativeCandidateQuery += "}";

    return negativeCandidateQuery;
  }

  public String generateExampleWithRelationsQuery(final Set<String> relations, final String typeSubject,
                                                  final String typeObject) {
    if ((relations == null) || (relations.size() == 0)) {
      return null;
    }

    // create the RDF
    final StringBuilder filterRelation = new StringBuilder();
    final Iterator<String> relationIterator = relations.iterator();

    while (relationIterator.hasNext()) {
      final String currentRelation = relationIterator.next();
      filterRelation.append("?targetRelation = <" + currentRelation + ">");
      if (relationIterator.hasNext()) {
        filterRelation.append(" || ");
      }
    }

    String negativeCandidateQuery = "";
    if ((this.prefixQuery != null) && (this.prefixQuery.size() > 0)) {
      for (final String prefix : this.prefixQuery) {
        negativeCandidateQuery += prefix + " ";
      }
    }

    negativeCandidateQuery += "SELECT DISTINCT ?subject ?object ";

    // if(this.graphIri!=null&&graphIri.length()>0)
    // negativeCandidateQuery+=" FROM "+this.graphIri;

    negativeCandidateQuery += " WHERE " + "{ ?object <" + typePrefix + "> <" + typeObject + ">." + "  ?subject <"
            + typePrefix + "> <" + typeSubject + ">." + "  ?subject ?targetRelation ?realObject. "
            + "  ?realSubject ?targetRelation ?object. " + "  FILTER (" + filterRelation.toString() + ") "
            + "  ?subject ?relation ?object.}";

    return negativeCandidateQuery;
  }

  public abstract int getSupportivePositiveExamples(Set<RuleAtom> rules, Set<String> relations, String typeSubject,
                                                    String typeObject, Set<Pair<String, String>> subject2objectConstant);

  public abstract Set<Pair<String, String>> getMatchingPositiveExamples(Set<RuleAtom> rules, Set<String> relations,
                                                                        String typeSubject, String typeObject, Set<Pair<String, String>> positiveExamples);

  public abstract Set<Pair<String, String>> getMatchingNegativeExamples(Set<RuleAtom> rules, Set<String> relations,
                                                                        String typeSubject, String typeObject, Set<Pair<String, String>> negativeExamples, boolean subjectFunction,
                                                                        boolean objectFunction);

  public abstract Map<String, Set<Pair<String, String>>> getRulePositiveSupport(
          Set<Pair<String, String>> positiveExamples);

  public abstract Set<Pair<String, String>> executeHornRuleQuery(Set<RuleAtom> rules, String typeSubject,
                                                                 String typeObject);

  public abstract Set<Pair<String, String>> executeRestrictiveHornRuleQuery(String queryRestriction,
                                                                            Set<RuleAtom> rules, String typeSubject, String typeObject);

  public abstract int executeCountQuery(String inputQuery);

  public String generateHornRuleQuery(final Set<RuleAtom> rules, final String typeSubject, final String typeObject,
                                      boolean selectAll) {
    return generateHornRuleQueryInstantiation(null, rules, typeSubject, typeObject, selectAll, false, -1);
  }

  public String generateHornRuleQueryInstantiation(final Set<String> targetPredicates, final Set<RuleAtom> rules,
                                                   final String typeSubject, final String typeObject, boolean selectAll, boolean positive,
                                                   int maxInstantiationNumber) {

    if (!(rules.size() > 0)) {
      return null;
    }
    // create the RDF
    final StringBuilder query = new StringBuilder();

    if ((this.prefixQuery != null) && (this.prefixQuery.size() > 0)) {
      for (final String prefix : this.prefixQuery) {
        query.append(prefix + " ");
      }
    }

    final String subject = typeSubject != null ? "?subject" : "";
    final String object = typeObject != null ? "?object" : "";
    if (selectAll) {
      query.append("SELECT DISTINCT *");
    } else {
      query.append("SELECT DISTINCT " + subject + " " + object);
    }

    /**
     * Jena does not work with count and nested query with from
     */
    if ((this.graphIri != null) && (graphIri.length() > 0)) {
      query.append(" FROM " + this.graphIri);
    }

    query.append(" WHERE {");
    if ((subject.length() > 0) && (typePrefix != null)) {
      query.append(subject + " <" + typePrefix + "> <" + typeSubject + ">. ");
    }
    if ((object.length() > 0) && (typePrefix != null)) {
      query.append(object + " <" + typePrefix + "> <" + typeObject + ">. ");
    }

    // check if the query contains an inequality

    query.append(this.getHornRuleAtomQuery(rules));

    // attach target predicates
    if (targetPredicates != null) {
      if (positive) {
        query.append(" FILTER NOT EXISTS {");
        query.append(appendMultiTargetPredicate(targetPredicates, "subject", "object"));
        query.append("}");

      } else {
        if (targetPredicates.size() > 1) {
          query.append("{");
        }
        query.append(appendMultiTargetPredicate(targetPredicates, "subject", "object"));
        if (targetPredicates.size() > 1) {
          query.append("}");
        }
      }
    }
    query.append("}");
    // check if there is a limit to set
    if (maxInstantiationNumber > 0) {
      query.append(" LIMIT ").append(maxInstantiationNumber);
    }
    return query.toString();
  }

  private String appendMultiTargetPredicate(final Collection<String> predicates, String subject, String object) {
    final StringBuilder queryBuilder = new StringBuilder();
    if (predicates.size() == 1) {
      queryBuilder.append(StringUtils.join("?", subject, " <", predicates.iterator().next(), "> ?", object, "."));
    } else {
      queryBuilder.append(StringUtils.join("{?", subject, " <", predicates.iterator().next(), "> ?", object, ".}"));
      final Iterator<String> predicatesIt = predicates.iterator();
      predicatesIt.next();
      while (predicatesIt.hasNext()) {
        queryBuilder.append(StringUtils.join(" UNION {?", subject, " <", predicatesIt.next(), "> ?", object, ".}"));
      }
    }
    return queryBuilder.toString();

  }

  /**
   * Negative examples must be separated with a tab
   * @return
   * @throws IOException
   */
  public Set<Pair<String, String>> readExamplesFromFile(final File inputFile) throws IOException {
    final Set<Pair<String, String>> examples = Sets.newHashSet();
    final BufferedReader reader = new BufferedReader(new FileReader(inputFile));

    String line = reader.readLine();
    while (line != null) {
      final String firstNode = line.split("\t")[0];
      final String secondNode = line.split("\t")[1];
      final Pair<String, String> example = Pair.of(firstNode, secondNode);
      if (!examples.contains(Pair.of(secondNode, firstNode))) {
        examples.add(example);
      }
      line = reader.readLine();
    }

    reader.close();
    LOGGER.debug("Read {} examples from input file.", examples.size());
    return examples;
  }

  /**
   * If the entity is literal compare it with all others literals
   * @param entity
   * @return
   */
  protected void compareLiterals(final String literal, final Graph<String> graph) {

    final String literalLexicalForm = graph.getLexicalForm(literal);
    // get only the literals that belong to the same example. It returns all literals if entity covers a null set of
    // examples
    final Set<String> otherLiterals = graph.getLiteralNodes();

    for (final String node : otherLiterals) {
      if (node.equals(literal)) {
        continue;
      }
      final Set<String> outputRelations = getLiteralRelation(literalLexicalForm, graph.getLexicalForm(node));
      if (outputRelations != null) {
        for (final String relation : outputRelations) {
          if (!graph.containsEdge(node, literal, getInverseRelation(relation))) {
            graph.addEdge(literal, node, relation, true);
          }
        }
      }
    }

  }

  public static Set<String> getLiteralRelation(final String stringLiteralOne, final String stringLiteralTwo) {

    final Set<String> outputRelations = Sets.newHashSet();

    if ((stringLiteralOne == null) || (stringLiteralTwo == null)) {
      return null;
    }

    // compare them as integer
    Double firstDouble = null;
    try {
      firstDouble = Double.parseDouble(stringLiteralOne);
    } catch (final Exception e) {
      // just continue
    }
    Double secondDouble = null;
    try {
      secondDouble = Double.parseDouble(stringLiteralTwo);
      if (firstDouble == null) {
        return null;
      }
      if (firstDouble.equals(secondDouble)) {
        outputRelations.add(Constant.EQUAL_REL);
        outputRelations.add(Constant.LESS_EQUAL_REL);
        outputRelations.add(Constant.GREATER_EQUAL_REL);
        return outputRelations;
      }
      if (firstDouble < secondDouble) {
        outputRelations.add(Constant.LESS_REL);
      } else {
        outputRelations.add(Constant.GREATER_REL);
      }
      return outputRelations;
    } catch (final Exception e) {
      if (firstDouble != null) {
        return null;
      }
    }

    // compare them as date
    final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
    final DateTimeFormatter formatterTime = DateTimeFormatter.ISO_DATE_TIME;
    LocalDate firstDate = null;
    try {
      firstDate = LocalDate.parse(stringLiteralOne, formatter);
    } catch (final Exception e) {
      try {
        firstDate = LocalDate.parse(stringLiteralOne, formatterTime);
      } catch (final Exception e1) {
        // just continue
      }
    }
    LocalDate secondDate = null;
    try {
      secondDate = LocalDate.parse(stringLiteralTwo, formatter);
      if (firstDate == null) {
        return null;
      }
    } catch (final Exception e) {
      try {
        secondDate = LocalDate.parse(stringLiteralTwo, formatterTime);
        if (firstDate == null) {
          return null;
        }
      } catch (final Exception e1) {
        if (firstDate != null) {
          return null;
        }
      }
    }
    if ((firstDate != null) && (secondDate != null)) {
      if (firstDate.compareTo(secondDate) == 0) {
        outputRelations.add(Constant.EQUAL_REL);
        outputRelations.add(Constant.LESS_EQUAL_REL);
        outputRelations.add(Constant.GREATER_EQUAL_REL);
        return outputRelations;
      }
      if (firstDate.compareTo(secondDate) < 0) {
        outputRelations.add(Constant.LESS_REL);
      } else {
        outputRelations.add(Constant.GREATER_REL);
      }
      return outputRelations;
    }

    // compare them as a string
    if (stringLiteralOne.equals(stringLiteralTwo)) {
      outputRelations.add(Constant.EQUAL_REL);
      return outputRelations;
    }
    return null;
  }

  public String predicateSubjectTypeQuery(final String inputPredicate, final String typeName) {
    return typeQuery(inputPredicate, typeName, true);
  }

  public String predicateObjectTypeQuery(final String inputPredicate, final String typeName) {
    return typeQuery(inputPredicate, typeName, false);
  }

  private String typeQuery(final String inputPredicate, final String typeName, final boolean subject) {
    if ((this.typePrefix == null) || this.typePrefix.isEmpty()) {
      return null;
    }
    final String variable = subject ? "sub" : "obj";
    String query = StringUtils.join("select ?", typeName, " (count(?", variable, ") AS ?count) ");
    if ((this.graphIri != null) && !this.graphIri.isEmpty()) {
      query = StringUtils.join(query, "from ", this.graphIri, " ");
    }
    query = StringUtils.join(query, "where {?sub <", inputPredicate, "> ?obj. ?", variable,
            " <" + this.typePrefix + "> ?", typeName, ".} GROUP BY ?", typeName, " ORDER BY DESC(?count)");
    return query;
  }

  public static Integer compareLiteral(final String stringLiteralOne, final String stringLiteralTwo) {

    final Set<String> outputRelations = Sets.newHashSet();

    if ((stringLiteralOne == null) || (stringLiteralTwo == null)) {
      return null;
    }

    // compare them as integer
    Double firstDouble = null;
    try {
      firstDouble = Double.parseDouble(stringLiteralOne);
    } catch (final Exception e) {
      // just continue
    }
    Double secondDouble = null;
    try {
      secondDouble = Double.parseDouble(stringLiteralTwo);
      if (firstDouble == null) {
        return null;
      }
      if (firstDouble == secondDouble) {
        return 0;
      }
      if (firstDouble < secondDouble) {
        return -1;
      } else {
        return +1;
      }
    } catch (final Exception e) {
      if (firstDouble != null) {
        return null;
      }
    }

    // compare them as date
    final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
    final DateTimeFormatter formatterTime = DateTimeFormatter.ISO_DATE_TIME;
    LocalDate firstDate = null;
    try {
      firstDate = LocalDate.parse(stringLiteralOne, formatter);
    } catch (final Exception e) {
      try {
        firstDate = LocalDate.parse(stringLiteralOne, formatterTime);
      } catch (final Exception e1) {
        // just continue
      }
    }
    LocalDate secondDate = null;
    try {
      secondDate = LocalDate.parse(stringLiteralTwo, formatter);
      if (firstDate == null) {
        return null;
      }
    } catch (final Exception e) {
      try {
        secondDate = LocalDate.parse(stringLiteralTwo, formatterTime);
        if (firstDate == null) {
          return null;
        }
      } catch (final Exception e1) {
        if (firstDate != null) {
          return null;
        }
      }
    }
    if ((firstDate != null) && (secondDate != null)) {
      if (firstDate.compareTo(secondDate) == 0) {
        outputRelations.add(Constant.EQUAL_REL);
        outputRelations.add(Constant.LESS_EQUAL_REL);
        outputRelations.add(Constant.GREATER_EQUAL_REL);
        return 0;
      }
      if (firstDate.compareTo(secondDate) < 0) {
        return -1;
      } else {
        outputRelations.add(Constant.GREATER_REL);
      }
      return +1;
    }

    // compare them as a string
    if (stringLiteralOne.equals(stringLiteralTwo)) {
      outputRelations.add(Constant.EQUAL_REL);
      return 0;
    }
    return null;
  }

  public boolean isLiteralNumber(final String literalValue) {
    try {
      Double.parseDouble(literalValue);
      return true;
    } catch (final Exception e) {
      try {
        DateTimeFormatter.ISO_DATE.parse(literalValue);
        return true;
      } catch (final Exception e1) {
        try {
          DateTimeFormatter.ISO_DATE_TIME.parse(literalValue);
          return true;
        } catch (final Exception e2) {

        }
      }
    }
    return false;
  }

  public static String getInverseRelation(final String rel) {
    if (rel.equals(Constant.GREATER_EQUAL_REL)) {
      return Constant.LESS_EQUAL_REL;
    }

    if (rel.equals(Constant.LESS_EQUAL_REL)) {
      return Constant.GREATER_EQUAL_REL;
    }

    if (rel.equals(Constant.GREATER_REL)) {
      return Constant.LESS_REL;
    }

    if (rel.equals(Constant.LESS_REL)) {
      return Constant.GREATER_REL;
    }

    if (rel.equals(Constant.EQUAL_REL)) {
      return rel;
    }

    return null;

  }

  public String getGraphIri() {
    return this.graphIri;
  }

  public String getTypePrefix() {
    return this.typePrefix;
  }

  public void setSubjectLimit(final int limit) {
    this.subjectLimit = limit;
  }

  public int getSubjectLimit() {
    return this.subjectLimit;
  }

  public void setObjectLimit(final int limit) {
    this.objectLimit = limit;
  }

  public int getObjectLimit() {
    return this.objectLimit;
  }

  public void setPosExamplesLimit(final int limit) {
    this.positiveExampleLimit = limit;
  }

  public void setNegExamplesLimit(final int limit) {
    this.negativeExampleLimit = limit;
  }

  public void setGenericTypes(final Collection<String> types) {
    this.genericTypes = Sets.newHashSet(types);
  }

  public abstract List<List<Pair<String, String>>> instantiateHornRule(Set<String> targetPredicates,
                                                                       Set<RuleAtom> rules, String subjType, String objType, boolean positive, int maxInstantiationNumber);

}