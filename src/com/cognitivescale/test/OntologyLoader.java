package com.cognitivescale.test;
import java.io.File;
import java.util.List;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;



public class OntologyLoader {

	OWLOntologyManager manager;
	OWLOntology ontology;
	IRI documentIRI;
	OWLReasoner reasoner;


	public void ParseOntology(String DB_PATH)
	{
		System.out.println("Creating New OWLGraph object");
		OWLGraph G = new OWLGraph(DB_PATH);
		System.out.println("Creating IDEngine");
		ExecutionEngine IDEngine = G.createExecutionEngine("Disease","ID");
		System.out.println("Creating DTEngine");
		ExecutionEngine DTEngine = G.createExecutionEngine("DiseaseType", "Type");
				

		for (OWLClass ontClass : ontology.getClassesInSignature(true))
		{
			System.out.println("-------------------------");
			//System.out.println("Class :" + ontClass.getIRI().getFragment());

			GraphObject Obj = new GraphObject();

			NodeSet<OWLClass> superclasses = reasoner.getSuperClasses(ontClass, true);
			if (superclasses.isEmpty())
			{
				System.out.println("No Super Class");
			}
			else
			{
				for (Node<OWLClass> parentOWLNode: superclasses)
				{

					OWLClassExpression parent =
							parentOWLNode.getRepresentativeElement();
					String parentString = new String();
					if(parent.isOWLThing())
					{
						parentString = "owl:Thing";    		            	
					}
					else
					{
						OWLClass parentClass = parent.asOWLClass();
						IRI parentClassIRI = parentClass.getIRI();
						parentString = parentClassIRI.getFragment();
					}
					//System.out.println("ParentClass :" + parentString.replace("DOID_",""));
					//Obj.AddPropertyToGraphObject("subClassOf", parentString);
					Obj.ParentID.add(parentString.replace("DOID_",""));
				}
			}

			java.util.Set<OWLAnnotation> annotations = ontClass.getAnnotations(ontology);
			for (OWLAnnotation a : annotations)
			{
				OWLAnnotationProperty property = a.getProperty();
				OWLAnnotationValue value = a.getValue();

				String propertyString = "";
				String valueString = "";

				if(value instanceof OWLLiteral)
				{
					valueString = ((OWLLiteral) value).getLiteral();
				}
				else if (value instanceof IRI)
				{
					valueString = ((IRI) value).getFragment();
				}
				else valueString = "";


				if(property.isLabel())
				{
					propertyString = "rdfs:label";
				}
				else if (!property.isLabel() & !property.isComment() & !property.isDeprecated())
				{
					propertyString = property.getIRI().getFragment();
				}
				else propertyString = "";


				if (valueString != "" && propertyString != "")
				{
					//System.out.println(propertyString + " : " + valueString);
					Obj.AddPropertyToGraphObject(propertyString, valueString);					
				}
			}
			Obj.printObject();
			System.out.println("Creating Object Node ...");
			Obj.createObjectNode(G, IDEngine, DTEngine);
			System.out.println("Done.");
			
		}	 
		G.graphDb.shutdown();
	}

	public OntologyLoader(String FilePath)
	{
		File localOwlFile = new File(FilePath);
		this.manager = OWLManager.createOWLOntologyManager();
		try
		{
			this.ontology = this.manager.loadOntologyFromOntologyDocument(localOwlFile);
			this.documentIRI = this.manager.getOntologyDocumentIRI(this.ontology);
			this.reasoner = new Reasoner(this.ontology);

			System.out.println("Loaded ontology: " + this.ontology);
			System.out.println(" from: " + this.documentIRI);			
		}
		catch (OWLOntologyCreationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

		String FilePath = "/home/apoorv/diseaseontology/HumanDO.owl";
		String DB_PATH = "/home/apoorv/Downloads/neo4j-community-2.1.4/data/DO2.db";
		
		OntologyLoader OwlLoader = new OntologyLoader(FilePath);
		OwlLoader.ParseOntology(DB_PATH);
		
		
	}
}