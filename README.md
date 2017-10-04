# RuDiK

This repository contains a Java implementation of RuDiK, a system for discovering of positive and negative logical rules over RDF knowledge graphs.
The prototype was first developed as a research project under the database group of Prof. Paolo Papotti at Ira A. Fulton Schools of Engineering, Arizona State University, and now moved to the Data Science group of EURECOM.

The current implementation works against a Knowledge Graph specified as a http SPARQL endpoint, please have a look at the src/main/config/Configuration.xml on how to specify the http endpoint and various parameter of the system.

The folder src/test/java/asu.edu.rule_miner.rudik.dbpedia contains some example tests on how to run the system against DBPedia Knowledge Graph.

System Requirements: Maven 3.2 or above, Java 1.8 or above.

For a full technical report of RuDik and the algorithms involved, please refer to: http://www.eurecom.fr/en/publication/5321.


# Contacts

Stefano Ortona	<stefano.ortona@gmail.com>

Paolo Papotti	<paolo.papotti@eurecom.fr>

Vamsi Meduri	<vamsikrishna1902@gmail.com>