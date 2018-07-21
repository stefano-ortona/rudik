package asu.edu.rule_miner.rudik.api;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.sparql.SparqlExecutor;

public class SorroundingGraphGeneration {

  private final static Logger LOG = LoggerFactory.getLogger(RuleInstanceGeneration.class.getName());

  private final SparqlExecutor executor;
  private final ScheduledExecutorService doubleThreadExecutor = Executors.newScheduledThreadPool(2);
  private final int timeout;

  public SorroundingGraphGeneration(SparqlExecutor executor, int timeout) {
    this.executor = executor;
    this.timeout = timeout;
  }

  public Graph<String> generateSorroundingGraph(Collection<String> targetEntities) {
    // start a thread that instantiate the rule
    LOG.info("Generating sorrounding graph for {} total entities...", targetEntities.size());
    final Future<Graph<String>> generationHandler = doubleThreadExecutor.submit(new GraphGeneration(targetEntities));
    // start a second thread that controls the rule timeout
    final ScheduledFuture<?> cancellationHandler = doubleThreadExecutor.schedule(() -> {
      LOG.error("Generation of the sorrounding graph was taking more than {} seconds, killing its execution.", timeout);
      generationHandler.cancel(true);
    }, timeout, TimeUnit.SECONDS);
    try {
      final Graph<String> graph = generationHandler.get();
      LOG.info("Sorrounding graph generated for {} total nodes.", targetEntities.size());
      // remove the cancellation thread
      cancellationHandler.cancel(true);
      return graph;
    } catch (InterruptedException | ExecutionException e) {
      LOG.error("Problem while executing the generation of the graph.", e);
    } catch (final CancellationException e2) {
      // cancellation can happen if the timeout is exceeded
    }
    // return an empty graph if failed
    return new Graph<String>();
  }

  class GraphGeneration implements Callable<Graph<String>> {

    Collection<String> targetEntites;

    public GraphGeneration(Collection<String> targetEntities) {
      this.targetEntites = targetEntities;
    }

    @Override
    public Graph<String> call() throws Exception {
      final long start = System.currentTimeMillis();
      final Graph<String> sorroundingGraph = new Graph<String>();
      // generate sorroudning graph for all target entities
      for (final String en : targetEntites) {
        // stop if there are less than 1 second left
        if (((timeout * 1000) - (System.currentTimeMillis() - start)) < 1000) {
          LOG.warn("Just one second left for graph generation, stopping here with {} total nodes retrieved.",
              sorroundingGraph.getNodesNumber());
          return sorroundingGraph;
        }
        sorroundingGraph.addNode(en);
        executor.executeQuery(en, sorroundingGraph, Maps.newHashMap());
      }
      return sorroundingGraph;
    }
  }

}
