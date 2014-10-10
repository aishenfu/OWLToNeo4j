package com.cognitivescale.test;
import java.io.*;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.shell.kernel.apps.Set;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;

public class OWLGraph
{
		String DB_PATH;
		public GraphDatabaseService graphDb;
		
		public OWLGraph(String DB_PATH)
		{
			this.DB_PATH = DB_PATH;
			clearDb();
			this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( this.DB_PATH );
			registerShutdownHook( this.graphDb );			
		}
		
		private void clearDb()
		{
			try
			{
				FileUtils.deleteRecursively(new File (DB_PATH));
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		private void registerShutdownHook( final GraphDatabaseService graphDb )
		{
		    // Registers a shutdown hook for the Neo4j instance so that it
		    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
		    // running application).
		    Runtime.getRuntime().addShutdownHook( new Thread()
		    {
		        @Override
		        public void run()
		        {
		            graphDb.shutdown();
		        }
		    } );
		}
		
		public ExecutionEngine createExecutionEngine(String labelName, String uniqueProperty)
		{
			
			try ( Transaction tx = graphDb.beginTx() )
			{
			    graphDb.schema()
			            .constraintFor( DynamicLabel.label( labelName ) )
			            .assertPropertyIsUnique( uniqueProperty )
			            .create();
			    tx.success();
			}

			ExecutionEngine engine = new ExecutionEngine(graphDb);
			return engine;
		}
}