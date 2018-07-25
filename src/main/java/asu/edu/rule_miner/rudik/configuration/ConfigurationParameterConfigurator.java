package asu.edu.rule_miner.rudik.configuration;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Stefano Ortona <stefano.ortona@gmail.com>
 *
 * Utility class to set at runtime some of the parameters to be specified in the configuration
 * file
 */
public class ConfigurationParameterConfigurator {

  public static void setGraphGenericTypes(final Collection<String> genericTypes) {
    if (genericTypes == null) {
      return;
    }
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_GENERIC_TYPES), genericTypes);
  }

  public static void setSparqlEndpoint(String endpoint) {
    if ((endpoint == null) || endpoint.isEmpty()) {
      return;
    }
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_SPARQL_ENDPOINT), endpoint);
  }

  public static void setMaxRuleLength(int maxRuleLen) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(Constant.CONF_MAX_RULE_LEN, maxRuleLen);
  }

  public static void setAlphaParameter(double alpha) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(Constant.CONF_SCORE_ALPHA, alpha);
  }

  public static void setBetaParameter(double beta) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(Constant.CONF_SCORE_BETA, beta);
  }

  public static void setRelationToAvoid(Collection<String> avoidRelation) {
    if (avoidRelation == null) {
      return;
    }
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_RELATION_TO_AVOID),
        avoidRelation);
  }

  public static void setDisequalityRelation(int disequality) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(Constant.CONF_EQUALITY_TYPES_NUMBER, disequality);
  }

  public static void setIncomingEdgesLimit(int incomingEdgesLimit) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_SUBJECT_LIMIT),
        incomingEdgesLimit);
  }

  public static void setOutgoingEdgesLimit(int outgoingEdgesLimit) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_OBJECT_LIMIT),
        outgoingEdgesLimit);
  }

  public static void setGraphIri(String graphIri) {
    if (graphIri == null) {
      return;
    }
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_GRAPH_IRI), graphIri);
  }

  public static void setIncludeLiteral(boolean includeLiteral) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_INCLUDE_LITERALS),
        includeLiteral);
  }

  public static void setTargetPrefix(Collection<String> targetPrefix) {
    if (targetPrefix == null) {
      return;
    }
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_RELATION_TARGET_REFIX),
        targetPrefix);
  }

  public static void setTypeRelationPrefix(String typePrefix) {
    if (typePrefix == null) {
      return;
    }
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_TYPE_PREFIX), typePrefix);
  }

  public static void setGraphPrefixes(Map<String, String> name2uri) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    final AtomicInteger counter = new AtomicInteger(0);
    final int totCount = config
        .getStringArray(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_RELATION_PREFIX)).length;
    if (totCount > name2uri.size()) {
      // clear previously defined prefixes, not strictly necessary
      for (int i = name2uri.size(); i < totCount; i++) {
        config.clearProperty(
            StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_RELATION_PREFIX, "(", i + "", ").name"));
        config.clearProperty(
            StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_RELATION_PREFIX, "(", i + "", ").uri"));
      }
    }
    name2uri.forEach((k, v) -> {
      final String curCount = counter.getAndIncrement() + "";
      config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_RELATION_PREFIX, "(",
          curCount, ").", Constant.CONF_RELATION_PREFIX_NAME), k);
      config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_RELATION_PREFIX, "(",
          curCount, ").", Constant.CONF_RELATION_PREFIX_URI), v);
    });

  }

  public static void setPositiveExamplesLimit(int limit) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_POS_EXAMPLES_LIMIT), limit);
  }

  public static void setNegativeExamplesLimit(int limit) {
    final Configuration config = ConfigurationFacility.getConfiguration();
    config.setProperty(StringUtils.join(Constant.CONF_SPARQL_ENGINE, ".", Constant.CONF_NEG_EXAMPLES_LIMIT), limit);
  }

}
