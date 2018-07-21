package asu.edu.rule_miner.rudik.api;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.api.model.HornRuleInstantiation;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult.RuleType;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.rule_generator.HornRuleDiscoveryInterface;

/**
 * Class responsible of instantiating a rule over the KB.
 * The instantiation is done with a separated thread, so that if the instantiation exceeds a given timeout, then the instantiation
 * thread is killed and an empty result is returned
 *
 * @author Stefano Ortona <stefano.ortona@gmail.com>
 *
 */
public class RuleInstanceGeneration {

  private final static Logger LOG = LoggerFactory.getLogger(RuleInstanceGeneration.class.getName());

  HornRuleDiscoveryInterface discovery;

  private final ScheduledExecutorService doubleThreadExecutor = Executors.newScheduledThreadPool(2);
  private final int timeout;

  /**
   *
   * @param discovery
   * @param timeout - in seconds, to execute the instantiation of the rule
   */
  public RuleInstanceGeneration(HornRuleDiscoveryInterface discovery, int timeout) {
    this.discovery = discovery;
    this.timeout = timeout;
  }

  public List<HornRuleInstantiation> instantiateRule(String predicate, HornRule rule, String subjType, String objType,
      RuleType type, int maxInstantiationNumber) {
    final boolean positive = type == RuleType.positive;
    // start a thread that instantiate the rule
    LOG.info("Instantiating rule '{}' over the KB...", rule.getRules());
    final Future<List<HornRuleInstantiation>> instantiationHandles = doubleThreadExecutor
        .submit(new Instantiate(predicate, rule, subjType, objType, positive, maxInstantiationNumber));
    // start a second thread that controls the rule instantiation timeout
    final ScheduledFuture<?> cancellationHandler = doubleThreadExecutor.schedule(() -> {
      LOG.error("Instantiation of the rule '{}' was taking more than {} seconds, killing its execution.",
          rule.getRules(), timeout);
      instantiationHandles.cancel(true);
    }, timeout, TimeUnit.SECONDS);
    try {
      final List<HornRuleInstantiation> insts = instantiationHandles.get();
      LOG.info("Rule '{}' succesfully instantiated with {} output results.", rule.getRules(), insts.size());
      // remove the cancellation thread
      cancellationHandler.cancel(true);
      return insts;
    } catch (InterruptedException | ExecutionException e) {
      LOG.error("Problem while executing the instantiation of the rule.", e);
    } catch (final CancellationException e2) {
      // cancellation can happen if the timeout is exceeded
    }
    return Lists.newArrayList();
  }

  class Instantiate implements Callable<List<HornRuleInstantiation>> {
    String predicate;
    HornRule rule;
    String subjType;
    String objType;
    boolean positive;
    int maxInstantiationNumber;

    public Instantiate(String predicate, HornRule rule, String subjType, String objType, boolean positive,
        int maxInstantiationNumber) {
      this.predicate = predicate;
      this.rule = rule;
      this.subjType = subjType;
      this.objType = objType;
      this.positive = positive;
      this.maxInstantiationNumber = maxInstantiationNumber;
    }

    @Override
    public List<HornRuleInstantiation> call() throws Exception {
      return discovery.instantiateRule(Sets.newHashSet(predicate), rule, subjType, objType, positive,
          maxInstantiationNumber);
    }

  }

}
