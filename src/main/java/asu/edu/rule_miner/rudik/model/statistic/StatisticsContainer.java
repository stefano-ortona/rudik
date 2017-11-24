package asu.edu.rule_miner.rudik.model.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;

public class StatisticsContainer {

  private static long startTime;
  private static long endTime;

  private static int numberValidationQuery;
  private static long totalTimeValidationQuery;

  private static int nodeNumber;
  private static int edgesNumber;

  private static int numberExpansionQuery;
  private static long totalTimeExpansionQuery;

  private static File outputFile;

  private static String id;

  private static double negativeSetTime;

  private static double positiveSetTime;

  private static List<HornRule> outputRules;

  private static Set<Pair<String, String>> generationSample;

  private static int validationSampleSize;

  public static void initialiseContainer(String currentId) {

    numberValidationQuery = 0;
    totalTimeValidationQuery = 0;
    totalTimeExpansionQuery = 0;
    nodeNumber = 0;
    edgesNumber = 0;
    id = currentId;
    startTime = 0;
    endTime = 0;
    negativeSetTime = 0;
    positiveSetTime = 0;
    validationSampleSize = 0;
    outputRules = null;

  }

  public static void setFileName(File file) {
    outputFile = file;
  }

  public static void setNegativeSetTime(double time) {
    negativeSetTime = time;
  }

  public static void setPositiveSetTime(double time) {
    positiveSetTime = time;
  }

  public static void increaseValidationQuery() {
    numberValidationQuery++;
  }

  public static void increaseExpansionQuery() {
    numberExpansionQuery++;
  }

  public static void setNodesNumber(int nodesNumber) {
    nodeNumber = nodesNumber;
  }

  public static void setEdgesNumber(int edgesNumberInput) {
    edgesNumber = edgesNumberInput;
  }

  public static void increaseTimeValidationQuery(long newTime) {
    totalTimeValidationQuery += newTime;
  }

  public static void increaseTimeExpansionQuery(long newTime) {
    totalTimeExpansionQuery += newTime;
  }

  public static void setStartTime(long currentStartTime) {
    startTime = currentStartTime;
  }

  public static void setEndTime(long currentEndTime) {
    endTime = currentEndTime;
  }

  public static void setOutputRules(List<HornRule> currentOutputRules) {
    outputRules = currentOutputRules;
  }

  public static void setGenerationSample(Set<Pair<String, String>> generationExamples) {
    generationSample = generationExamples;
  }

  public static void printStatistics() throws IOException {
    if (outputFile == null) {
      return;
    }

    final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));

    final Date startDate = new Date(startTime);
    final DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
    final String startDateFormatted = formatter.format(startDate);
    final Date endDate = new Date(endTime);
    final String endDateFormatted = formatter.format(endDate);

    // writer.write("-------------------------"+id+"_"+ConfigurationFacility.getNegativeExampleLimit()+"_"+
    // startDateFormatted+"-------------------------\n");
    writer.write("-------------------------" + id + "_" + startDateFormatted + "-------------------------\n");
    writer.write("Number of Nodes: " + nodeNumber + "\n");
    writer.write("Number of Edges: " + edgesNumber + "\n");
    writer.write("Total time for validation queries: " + (totalTimeValidationQuery / 1000.) + " seconds.\n");
    writer.write("Average time for validation queries: "
        + (((totalTimeValidationQuery + 0.) / numberValidationQuery) / 1000.) + " seconds.\n");
    writer.write("Number of validation queries: " + numberValidationQuery + "\n");
    writer.write("Total time for expansion queries: " + (totalTimeExpansionQuery / 1000.) + " seconds.\n");
    writer.write("Average time for expansion queries: "
        + (((totalTimeExpansionQuery + 0.) / numberExpansionQuery) / 1000.) + " seconds.\n");
    writer.write("Number of expansion queries: " + numberExpansionQuery + "\n");
    writer.write("Positive examples generation time: " + positiveSetTime + " seconds.\n");
    writer.write("Negative examples generation time: " + negativeSetTime + " seconds.\n");
    writer.write("Total running time: " + ((endTime - startTime) / 1000.) + " seconds.\n");
    writer.write("Generation sample examples: " + generationSample + "\n");
    writer.write("Validation sample examples size: " + validationSampleSize + "\n");
    writer.write("Output rules: " + outputRules + "\n");
    // writer.write("-------------------------"+id+"_"+ConfigurationFacility.getNegativeExampleLimit()+"_"+
    // endDateFormatted+"-------------------------");
    writer.write("-------------------------" + id + "_" + endDateFormatted + "-------------------------");
    writer.write("\n");

    writer.flush();
    writer.close();
  }

  public static void setValidationExamplesSize(final int valSampleExamplesSize) {
    validationSampleSize = valSampleExamplesSize;
  }

  public static Double getExpansionAvgTime() {
    return ((totalTimeExpansionQuery + 0.) / numberExpansionQuery) / 1000.;
  }

  public static Double getExpanasionTotTime() {
    return (totalTimeExpansionQuery / 1000.);
  }

  public static Double getNegativeExamplesGenerationTime() {
    return negativeSetTime;
  }

  public static Double getPositiveExamplesGenerationTime() {
    return positiveSetTime;
  }

  public static Double getTotatlRuntime() {
    return ((endTime - startTime) / 1000.);
  }

  public static Integer getSubgraphEdges() {
    return edgesNumber;
  }

  public static Integer getSubgraphNodes() {
    return nodeNumber;
  }

  public static Double getValidationAvgTime() {
    return (((totalTimeValidationQuery + 0.) / numberValidationQuery) / 1000.);
  }

  public static Double getValidationTotTime() {
    return (totalTimeValidationQuery / 1000.);
  }

  public static Integer getNumberValidationQueries() {
    return numberValidationQuery;
  }

  public static Integer getNumberExpansionQueries() {
    return numberExpansionQuery;
  }

}
