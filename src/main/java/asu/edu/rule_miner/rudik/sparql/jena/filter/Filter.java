package asu.edu.rule_miner.rudik.sparql.jena.filter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;


public interface Filter {
	public ArrayList<QuerySolution> doFilter(ResultSet results, String sub, String rel, String obj, String entity, int limitSubject, int limitObject);
	
}
