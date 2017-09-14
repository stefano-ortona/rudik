package asu.edu.rule_miner.rudik.sparql.jena.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import asu.edu.rule_miner.rudik.configuration.ConfigurationFacility;
import asu.edu.rule_miner.rudik.configuration.Constant;
import asu.edu.rule_miner.rudik.model.rdf.graph.Edge;
import asu.edu.rule_miner.rudik.model.rdf.graph.Graph;
import asu.edu.rule_miner.rudik.sparql.jena.remote.QuerySparqlRemoteEndpoint;

/**
 * This test class aims to test whether an KB endpoint can be queried and returns expected results
 * @author ortona
 *
 */
public class QuerySparqlRemoteEndpointTest {

	QuerySparqlRemoteEndpoint endpoint;
	Graph<String> inputGraph;

	@Before
	public void bringUp() throws ConfigurationException{
		ConfigurationFacility.getConfiguration();
		//set in the configuration file the remote endpoint
		Configuration config = new XMLConfiguration("src/test/config/RemoteSparqlEndpointConfiguration.xml");
		this.endpoint = new QuerySparqlRemoteEndpoint(config);
		this.inputGraph = new Graph<String>();

	}

	@Test
	public void testNormalQuery() throws IOException {
		//read the input entity name
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the name of the entity:");
		String entityName = br.readLine();
		System.out.println("Enter the number of relationship where entity is subject:");
		int subjectRel = Integer.parseInt(br.readLine());
		System.out.println("Enter the number of relationship where entity is object:");
		int objectRel = Integer.parseInt(br.readLine());

		String entity = entityName;
		this.inputGraph.addNode(entity);
		this.endpoint.executeQuery(entity, this.inputGraph,null);

		Assert.assertTrue(this.inputGraph.getNodes().contains(entity));
		Set<Edge<String>> neighbours = this.inputGraph.getNeighbours(entity);
		Assert.assertNotNull(neighbours);
		Assert.assertEquals(subjectRel+objectRel, neighbours.size());
		int actualSubjectRel=0;
		int actualObjectRel=0;
		Set<String> totalNodes = Sets.newHashSet();
		totalNodes.add(entity);
		for(Edge<String> edge:neighbours){
			if(edge.isArtificial())
				actualObjectRel++;
			else
				actualSubjectRel++;
			totalNodes.add(edge.getNodeEnd());
		}
		Assert.assertEquals(subjectRel, actualSubjectRel);
		Assert.assertEquals(objectRel, actualObjectRel);
		Assert.assertEquals(totalNodes, this.inputGraph.getNodes());

		totalNodes.remove(entity);
		for(String node:totalNodes){
			Set<String> neighboursNodes = Sets.newHashSet();
			for(Edge<String> edge:this.inputGraph.getNeighbours(node))
				neighboursNodes.add(edge.getNodeEnd());
			Assert.assertEquals(1, neighboursNodes.size());
			Assert.assertEquals(entity, neighboursNodes.iterator().next());
		}			
	}

	@Test
	public void testLiteralQuery() throws IOException {
		//read the input entity name
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Write the name of two entites that have an number neighbour to evaluate separated by space:");
		String entityNames = br.readLine();
		String firstEntity = entityNames.split(" ")[0];
		String secondEntity = entityNames.split(" ")[1];
		System.out.println("Write the name of the two relationship that connects repsectively first entity " +
				"and second entity to a number separated by space:");
		String relNames = br.readLine();
		String firstRelation = relNames.split(" ")[0];
		String secondRelation = relNames.split(" ")[1];
		System.out.println("Write the relation that connects the first number with the second ("
				+Constant.EQUAL_REL+", "+Constant.GREATER_EQUAL_REL+", "+Constant.LESS_EQUAL_REL+", "+Constant.DIFF_REL+"):");
		String rel = br.readLine();

		String firstEntityNode =  firstEntity;
		this.inputGraph.addNode(firstEntityNode);
		this.endpoint.executeQuery(firstEntityNode, this.inputGraph,null);
		//get the first literal neighbour
		String firstEntityLiteral = null;
		for(Edge<String> edge:this.inputGraph.getNeighbours(firstEntityNode)){
			if(edge.getLabel().equals(firstRelation)){
				firstEntityLiteral = edge.getNodeEnd();
				break;
			}
		}
		Assert.assertNotNull(firstEntityLiteral);

		String secondEntityNode = secondEntity;
		this.inputGraph.addNode(secondEntityNode);
		this.endpoint.executeQuery(secondEntityNode, this.inputGraph,null);
		//get the second literal neighbour
		String secondEntityLiteral = null;
		for(Edge<String> edge:this.inputGraph.getNeighbours(secondEntityNode)){
			if(edge.getLabel().equals(secondRelation)){
				secondEntityLiteral = edge.getNodeEnd();
				break;
			}
		}
		Assert.assertNotNull(secondEntityLiteral);
		this.endpoint.executeQuery(firstEntityLiteral, this.inputGraph,null);

		//check there exists the path
		Edge<String> edge = new Edge<String>(firstEntityNode,firstEntityLiteral,firstRelation);
		Assert.assertTrue(this.inputGraph.getNeighbours(firstEntityNode).contains(edge));

		edge = new Edge<String>(firstEntityLiteral,secondEntityLiteral,rel);
		Assert.assertTrue(this.inputGraph.getNeighbours(firstEntityLiteral).contains(edge));

		edge = new Edge<String>(secondEntityLiteral,secondEntityNode,secondRelation);
		Assert.assertTrue(this.inputGraph.getNeighbours(secondEntityLiteral).contains(edge));
		Assert.assertTrue(edge.isArtificial());
	}

}
