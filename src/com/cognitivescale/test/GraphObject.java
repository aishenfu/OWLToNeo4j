package com.cognitivescale.test;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
import java.lang.reflect.Array;
import java.util.*;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;


public class GraphObject
{
	// User Defined GraphNode as per Ontology
	// Node Properties
	String name;
	String ID;
	String definition;
	ArrayList<String> AlternativeID;
	ArrayList<String> DbXRef;
	ArrayList<String> ExactSynonym;
	ArrayList<String> NarrowSynonym;

	// Relationships 
	ArrayList<String> ParentID;
	ArrayList<String> SubsetOf;
	//String OBONamespace;

	public GraphObject()
	{
		name = new String();
		ID = new String();
		definition = new String();
		AlternativeID = new ArrayList <String>();
		DbXRef = new ArrayList <String>();
		ExactSynonym = new ArrayList <String> ();
		NarrowSynonym = new ArrayList <String> ();
		ParentID = new ArrayList <String> ();
		SubsetOf = new ArrayList <String> ();
	}

	private static enum RelTypes implements RelationshipType
	{
		SubClassOf,
		SubsetOf
	}

	public void AddPropertyToGraphObject(String propertyString, String valueString)
	{
		if(propertyString.trim().equalsIgnoreCase("id"))
			ID = valueString.replace("DOID:", "");

		else if (propertyString.trim().equalsIgnoreCase("rdfs:label"))
			name = valueString;

		else if (propertyString.trim().equalsIgnoreCase("IAO_0000115"))
			definition = valueString;

		else if (propertyString.trim().equalsIgnoreCase("hasDbXref"))
			DbXRef.add(valueString);

		else if (propertyString.trim().equalsIgnoreCase("hasExactSynonym"))
			ExactSynonym.add(valueString);

		else if (propertyString.trim().equalsIgnoreCase("hasNarrowSynonym"))
			NarrowSynonym.add(valueString);

		else if (propertyString.trim().equalsIgnoreCase("subClassOf") & valueString!="owl:Thing")
			ParentID.add(valueString);

		else if (propertyString.trim().equalsIgnoreCase("inSubset"))
			SubsetOf.add(valueString);

		//else if (propertyString == "hasOBONamespace")
		//	OBONamespace = valueString;
	}

	public void printObject()
	{
		System.out.println("Class : " + ID);
		System.out.println("Name : " + name);

		if (definition.trim() != "")
		{
			System.out.println("Definition : " + definition);
		}

		if(!DbXRef.isEmpty())
		{
			Iterator<String> DX = DbXRef.iterator();
			while(DX.hasNext())
			{
				System.out.println("DbXRef : " + DX.next());				
			}
		}

		if(!ExactSynonym.isEmpty())
		{
			Iterator<String> ES = ExactSynonym.iterator();
			while(ES.hasNext())
			{
				System.out.println("ExactSynonym : " + ES.next());
			}
		}

		if(!NarrowSynonym.isEmpty())
		{
			Iterator<String> NS = NarrowSynonym.iterator();
			while (NS.hasNext())
			{
				System.out.println("NarrowSynonym : " + NS.next());
			}
		}

		// Set Relationships
		if(!ParentID.isEmpty())
		{
			Iterator<String> parID = ParentID.iterator();
			while (parID.hasNext())
			{
				System.out.println("ParentClass : " + parID.next());
			}
		}

		if(!SubsetOf.isEmpty())
		{
			Iterator<String> DTit = SubsetOf.iterator();
			while (DTit.hasNext())
			{
				System.out.println("SubsetOf : " + DTit.next());
			}	
		}	
	}

	private Node getOrCreateUniqueDiseaseNode(OWLGraph G, ExecutionEngine IDengine, String IDvalue)
	{
		Node n = null;
		ExecutionResult result;
		String queryString = "MERGE (n:Disease {ID: {ID}}) RETURN n";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put( "ID", IDvalue );
		//try(Transaction tx = G.graphDb.beginTx())
		//{
			result = IDengine.execute( queryString, parameters );
			Iterator<Node> n_column = result.columnAs( "n" );
			n = n_column.next();
			n.addLabel(DynamicLabel.label("Disease"));
			n.setProperty("ID",IDvalue);
		//	tx.success();
		//}
		return n;
	}

	private Node getOrCreateUniqueDiseaseTypeNode(OWLGraph G, ExecutionEngine DTEngine, String DTname)
	{

		Node n = null;

		ExecutionResult result;
		String queryString = "MERGE (n:DiseaseType {Type: {Type}}) return n";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("Type", DTname);

		//try(Transaction tx = G.graphDb.beginTx())
		//{
			result = DTEngine.execute(queryString, parameters);
			Iterator<Node> n_column = result.columnAs("n");
			n = n_column.next();
			n.addLabel(DynamicLabel.label("DiseaseType"));
			n.setProperty("Type", DTname);
		//	tx.success();
		//}
		return n;
	}

	public void createObjectNode(OWLGraph G, ExecutionEngine IDEngine, ExecutionEngine DTEngine)
	{
		try( Transaction tx = G.graphDb.beginTx() )
		{
			Node n = getOrCreateUniqueDiseaseNode(G, IDEngine, ID);

			// Set Node Properties
			if(definition.trim() != "")
			{
				n.setProperty("definition", definition);
			}
			
			n.setProperty("name", name);
			//n.setProperty("ID", ID);

			
			if(!DbXRef.isEmpty())
			{
				String [] DX = new String[DbXRef.size()];
				n.setProperty("DbXRef", DbXRef.toArray(DX));
			}

			if(!ExactSynonym.isEmpty())
			{
				String [] ES = new String[ExactSynonym.size()];
				n.setProperty("ExactSynonym", ExactSynonym.toArray(ES));
			}

			if(!NarrowSynonym.isEmpty())
			{
				String [] NS = new String[NarrowSynonym.size()];
				n.setProperty("NarrowSynonym", NarrowSynonym.toArray(NS));
			}
			
			// Set Relationships
			if(!ParentID.isEmpty())
			{
				Iterator<String> parID = ParentID.iterator();
				while (parID.hasNext())
				{
					Node parent = getOrCreateUniqueDiseaseNode(G, IDEngine, parID.next());
					n.createRelationshipTo(parent, RelTypes.SubClassOf);
				}

			}

			if(!SubsetOf.isEmpty())
			{
				Iterator<String> DTit = SubsetOf.iterator();
				while (DTit.hasNext())
				{
					Node DT = getOrCreateUniqueDiseaseTypeNode(G, DTEngine, DTit.next());
					n.createRelationshipTo(DT, RelTypes.SubsetOf);
				}

			}
			tx.success();
		}
	}
}