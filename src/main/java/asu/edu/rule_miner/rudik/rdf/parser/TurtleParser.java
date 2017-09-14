package asu.edu.rule_miner.rudik.rdf.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;

public class TurtleParser {

  private final static Logger LOGGER = LoggerFactory.getLogger(TurtleParser.class.getName());

  public static void main(final String[] args) throws Exception{
    ConfigurationFacility.getConfiguration();
    final String fileName = "freebase_5";
    //set negative numbers to analyse the whole file
    final long startLine = -1;
    final long endLine = -1;
    new TurtleParser().parseRDF(fileName,startLine,endLine);
  }

  public void parseRDF(final String fileName, final long startLine,final long endLine) throws IOException{
    Model model = ModelFactory.createDefaultModel();
    final BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
    String line;
    final List<String> goodLines = Lists.newLinkedList();
    final List<String> badLines = Lists.newLinkedList();
    long goodLinesCount = 0;
    long badLinesCount = 0;
    long i=0;
    while((line = reader.readLine()) != null){
      if(startLine>=0 && i<startLine){
        i++;
        continue;
      }
      if(endLine>=0 && i>=endLine){
        break;
      }
      if(i%100000==0){
        LOGGER.info("Analysed {} rows of the input file ({} good lines, {} bad lines so far).",i,
            goodLinesCount,badLinesCount);
      }
      //flush content every 200000 lines read
      if(i%200000==0){
        flushOutput(goodLines, badLines, fileName);
      }
      i++;
      final String cleanTriple = cleanTriple(line);
      if(cleanTriple == null){
        LOGGER.error("Line could not been transformed into a good triple: '{}'.",line);
        badLines.add(line);
        badLinesCount++;
        continue;
      }
      final InputStream in = IOUtils.toInputStream(cleanTriple, "UTF-8");
      try{
        model.read(in,null, "N-TRIPLE");
        goodLines.add(cleanTriple);
        goodLinesCount++;
      } catch(final Exception e){
        badLines.add(line);
        badLinesCount++;
      }
      in.close();
      model.removeAll();
      model = ModelFactory.createDefaultModel();
    }	
    flushOutput(goodLines, badLines, fileName);
    reader.close();
    LOGGER.info("Found a total of {} good lines and {} bad lines.",goodLinesCount,badLinesCount);
  }

  private void flushOutput(final List<String> goodLines, final List<String> badLines, final String fileName) throws IOException{
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(fileName+"_good"), true));
    for(final String line:goodLines){
      bufferedWriter.write(line+"\n");
    }
    bufferedWriter.flush();
    bufferedWriter.close();
    goodLines.clear();
    bufferedWriter = new BufferedWriter(new FileWriter(new File(fileName+"_bad"), true));
    for(final String line:badLines){
      bufferedWriter.write(line+"\n");
    }
    bufferedWriter.flush();
    bufferedWriter.close();
    badLines.clear();
  }


  private String cleanTriple(final String tripleLine){
    //split subject predicate object
    if(!tripleLine.contains("> <")){
      return null;
    }
    final String []entities = tripleLine.split("> <");
    if(entities.length>3){
      return null;
    }
    final String subject = replaceBadCharacter(entities[0]);
    String object;
    String predicate;
    boolean objectEntity = false;
    if(entities.length==3){
      object = "<"+entities[2];
      predicate = entities[1];
      objectEntity = true;
    }
    else{
      final int index = entities[1].indexOf('>');
      predicate = entities[1].substring(0, index);
      object = entities[1].substring(index+2);
    }
    object = object.substring(0, object.length()-2);
    predicate = replaceBadCharacter(predicate);
    if(objectEntity){
      object = replaceBadCharacter(object);
    }
    return subject+"> <"+predicate+"> "+object+" .";

  }

  private String replaceBadCharacter(String entity){
    entity = entity.replaceAll(" ", "_");
    entity = entity.replaceAll("\"", "'");
    entity = entity.replaceAll("\\^", "_");
    return entity;
  }

}
