This is code for importing an OWL Disease Ontology file into a Neo4j DB.

The Disease Ontology file can be found here :
http://do-wiki.nubic.northwestern.edu/do-wiki/index.php/Download_DO

For customizing the code to load your own Ontology File, please follow the steps given below:

1) The class OntologyLoader loads the ontology file into memory and extracts the Classes and its properties i.e name, ID etc. It calls methods from the Class GraphObject to store these properties into a Neo4j Node in the Class OWLGraph.

2) The Class OWLGraph need not be edited.

3) The Clas GraphObject defines what needs to be captured from the Ontology File i.e the Class name, Class ID, and other properties.

A few things that may need to be edited to suit your needs are as follows : a) members of the class. b) Methods such as printObject (for Debugging), getOrCreateUniqueNodes
