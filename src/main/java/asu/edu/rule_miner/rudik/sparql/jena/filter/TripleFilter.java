package asu.edu.rule_miner.rudik.sparql.jena.filter;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;


public class TripleFilter implements Filter{
	public HashMap<String,ArrayList<QuerySolution>> hMap;
	public TripleFilter(){
		hMap= new HashMap<String,ArrayList<QuerySolution>>();
	}
	public ArrayList<QuerySolution> doFilter(ResultSet results, String sub, String rel, String obj, String entity, int limitSubject, int limitObject){
		if(limitSubject < 0)
			limitSubject = (int)Double.POSITIVE_INFINITY;
		if(limitObject < 0)
			limitObject = (int)Double.POSITIVE_INFINITY;
		while(results.hasNext()){
			QuerySolution oneResult = results.next();
			String subject, object;
			if(!oneResult.contains(sub))
				subject = entity;
			else
				subject = oneResult.get(sub).toString();
			if(!oneResult.contains(obj))
				object = entity;
			else
				object = oneResult.get(obj).toString();
			String relation = oneResult.get(rel).toString();
			String subRelKey = relation+"_sub";
			String objRelKey = relation+"_obj";
			if(subject.equals(entity)){
				if(hMap.containsKey(subRelKey)){
					ArrayList<QuerySolution> valSet = hMap.get(subRelKey);
					if(valSet.size() < limitSubject){
						valSet.add(oneResult);
						hMap.put(subRelKey, valSet);
					}
				}
				else{
					ArrayList<QuerySolution> valSet = new ArrayList<QuerySolution>();
					if(valSet.size() < limitSubject){
						valSet.add(oneResult);
						hMap.put(subRelKey, valSet);
					}
				}				
			}
			else if(object.equals(entity)){
				if(hMap.containsKey(objRelKey)){
					ArrayList<QuerySolution> valSet = hMap.get(objRelKey);
					if(valSet.size() < limitObject){
						valSet.add(oneResult);
						hMap.put(objRelKey, valSet);
					}
				}
				else{
					ArrayList<QuerySolution> valSet = new ArrayList<QuerySolution>();
					if(valSet.size() < limitObject){
						valSet.add(oneResult);
						hMap.put(objRelKey, valSet);
					}
				}
			}
		}
		ArrayList<QuerySolution> resultValues = new ArrayList<QuerySolution>();
		for(ArrayList<QuerySolution>valueSet : hMap.values())
			resultValues.addAll(valueSet);
		return resultValues;
	}
}
