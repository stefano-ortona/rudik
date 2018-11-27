# RuDiK

This repository contains a Java implementation of RuDiK, a system for discovering positive and negative logical rules over RDF knowledge graphs.
The prototype was first developed as a research project under the database group of Prof. Paolo Papotti at Ira A. Fulton Schools of Engineering, Arizona State University, and now moved to the Data Science group of EURECOM.

The current implementation works against a Knowledge Graph specified as a http SPARQL endpoint, please have a look at the src/main/config/Configuration.xml on how to specify the http endpoint and various parameters of the system.

The folder src/test/java/asu.edu.rule_miner.rudik.dbpedia contains some example tests on how to run the system against DBPedia Knowledge Graph.

# Technical Report

For a full technical report of RuDik and the algorithms involved, please refer to: http://www.eurecom.fr/en/publication/5321

# Run DBPedia Examples
 
 In this example, we are going to mine negative rules from DBPedia
 
## System Requirements

- Maven 3.2 or above
- Java 1.8 or above

## Installation

- Checkout git project `git clone git@github.com:stefano-ortona/rudik.git`
- Install all maven dependency:
  - Inside the project folder, run `mvn -U clean install -DskipTests`

If the above commands run without errors, then you are ready to run your first example

## Run Spouse Example

We are going to run the test case `testSpouseNegative` of the test class `asu.edu.rule_miner.rudik.dbpedia.DBPediaClient`. This test case will mine negative rules for the target predicate _spouse_

### Configuration parameter

All configuration parameters are defined in an xml file. For this test case, we are using the specific file for DBPedia `src/main/config/DbpediaConfiguration.xml`. Have a look at this file, and in particular pay attention to the following parameters:

- **max_rule_lenght** -> Maximum number of atoms of the body of the output rules. The higher is this parameter, the bigger will be the search space (hence the slower the computation)
- **sparql_endpoint** -> This parameter defines the endpoint to query the target Knowledge Graph. In this example, the endpoint is set to the online accessible endpoint `http://dbpedia.org/sparql`. This parameter is necessary to query the external database
- **examples.positive(.negative)** -> These two parameters define the number of input positive (negative) examples to be used for the mining (currently set to 10 and 10). The higher are these two numbers, the bigger is the search space, but also the higher the chance to find valid rules. We found that an acceptable compromise between runtime and output quality can always be achieved with 500-1K examples, however in this spouse example we can mine qualitative rules with just 10 examples in input
- **include_literals** -> decide whether to include literal values or not in the output rules

For a full explanation of all parameters, check out the technical report.

### Run the example

Run the test case `testSpouseNegative` of the test class `asu.edu.rule_miner.rudik.dbpedia.DBPediaClient`. The computation, for this particular example, should take around 50 seconds (depending also on your network speed). At the end of the computation you should see an output like this:

```edu.rule_miner.rudik.ClientTest [INFO] ----------------------------COMPUTATION ENDED----------------------------
edu.rule_miner.rudik.ClientTest [INFO] Final computation time: 49.982 seconds.
edu.rule_miner.rudik.ClientTest [INFO] ----------------------------Final output rules----------------------------
edu.rule_miner.rudik.ClientTest [INFO] http://dbpedia.org/ontology/spouse(object,v0) & http://dbpedia.org/ontology/parent(subject,v0)
edu.rule_miner.rudik.ClientTest [INFO] http://dbpedia.org/ontology/successor(subject,object)
```

In the above log messages we found two negative output rules for the target predicate spouse. The first rule has 2 atoms in the body, while the second one has just 1 atom.

**NOTE:** The 10 examples we use in this test are randomly selected from a set of bigger examples, so it is possible that your output is different or empty. If that is the case and you want to see some real rules, try to re-run the test multiple times, or increase the number of input examples.

# Mine from your own Knowledge Graph

The above example mines rules against the publicly available version of DBPedia. If you wish to mine rules from your own Knowledge Graph, or simply you want to have a local version of DBPedia, you need to change the sparql endpoint in the configuration file, along with few other parameters.

Look inside the folder `src/main/config`, there you can find other configuration files for knowledge graphs such as _Yago_, _Freebase_, and _Wikidata_. We tested several RDF triple store and SPARQL engines, and we found that the fastest freely available is [Virtuoso](https://virtuoso.openlinksw.com/). To install Virtuoso on a Mac, simply use the brew package manager:

- brew install virtuoso (this will install Virtuoso in `/usr/local/Cellar/virtuoso`)
- use the Virtuoso bulk loader to insert your graph triples in Virtuoso http://vos.openlinksw.com/owiki/wiki/VOS/VirtBulkRDFLoader
- after loading the triples, access the Sparql endpoint at http://localhost:8890/sparql
- update the configuration file with the new Sparql endpoint and run your test cases

# Install Virtuoso with DBpedia files

The following steps may prompt for a sudo password and may need to be written in a virtuoso SQL client terminal.

- STEP 1: Installation of virtuoso server. (Borrowed from http://vos.openlinksw.com/owiki/wiki/VOS/VOSUbuntuNotes)

```sudo aptitude install  virtuoso-opensource```

- The above command triggers the download as well as the installation. It also prompts for a dba password

- STEP 2: Download the dbpedia ttl files. Let us say that the ttl files are downloaded in the user's home directory

```cd ~
mkdir dbpedia_ttl
cd dbpedia_ttl
wget http://downloads.dbpedia.org/2016-10/core/geo_coordinates_en.ttl.bz2
wget http://downloads.dbpedia.org/2016-10/core/persondata_en.ttl.bz2
wget http://downloads.dbpedia.org/2016-10/core/instance_types_transitive_en.ttl.bz2
wget http://downloads.dbpedia.org/2016-10/core/specific_mappingbased_properties_en.ttl.bz2
wget http://downloads.dbpedia.org/2016-10/core/infobox_properties_en.ttl.bz2
wget http://downloads.dbpedia.org/2016-10/core/mappingbased_objects_en.ttl.bz2
wget http://downloads.dbpedia.org/2016-10/core/mappingbased_literals_en.ttl.bz2
```

- STEP 3 : Uncompress the files. In case you don't have bzip2 install it using "sudo apt install bzip2" The following commands will delete the bz2 files after uncompressing. In case you want to preserve the zip files, issue the command as bzip2 -dk <filename>.bz2

```bzip2 -d geo_coordinates_en.ttl.bz2
bzip2 -d persondata_en.ttl.bz2
bzip2 -d instance_types_transitive_en.ttl.bz2
bzip2 -d specific_mappingbased_properties_en.ttl.bz2
bzip2 -d infobox_properties_en.ttl.bz2
```

 - STEP 4 : Modify the virtuoso.ini file with the filepath to the ttl files and set the number of buffers based on available RAM. Copy the virtuoso.ini file into the virtuoso db directory

```sudo cp /etc/virtuoso-opensource-6.1/virtuoso.ini /var/lib/virtuoso-opensource-6.1/db/virtuoso.ini
 cd /var/lib/virtuoso-opensource-6.1/db
sudo vi virtuoso.ini
```

Copy the path to the ttl files and append that to the DirsAllowed. For example, if home directory is /home/rudik, copy the ttl files as following:  DirsAllowed = ., /usr/share/virtuoso-opensource-6.1/vad, /home/rudik/dbpedia_ttls

The default number of buffers is 10000 and the maximum dirty buffers are set to 6000. Comment them depending on the available RAM, uncomment the appropriate list of options available in the virtuoso.ini file. Save the settings and close the file

- STEP 5: Stop the virtuoso server if it is already running by searching for the virtuoso server as "ps -ef | grep virtuoso-t". Start the virtuoso server with the updated settings in virtuoso.ini

```sudo /usr/bin/virtuoso-t```

- STEP 6: In a new terminal tab, start the virtuoso client

```sudo /usr/bin/isql-vt```

- STEP 7: In the SQL prompt, issue the following sparql command to load the triples from the ttl files into the virtuoso server

```OpenLink Interactive SQL (Virtuoso), version 0.9849b.
Type HELP; for help and EXIT; to exit.
SQL> ld_dir('/home/rudik/Documents/dbpedia_ttls', '*.ttl', 'http://dbpedia.org');
Connected to OpenLink Virtuoso
Driver: 06.01.3127 OpenLink Virtuoso ODBC Driver

Done. -- 17 msec.
SQL> rdf_loader_run();
```
## Some useful tips

### Checking file loading status
To make sure that the loading succeeded check the table DB.DBA.load_list: 
“ select * from DB.DBA.load_list; “
The table DB.DBA.load_list can be used to check the list of data sets registered for loading, and the graph IRIs into which they will be or have been loaded. The ```ll_state field``` can have three values: 0 indicating the data set is to be loaded; 1 the data set load is in progress; or 2 the data set load is complete.

On a multi-core machine, it is recommended that data sets be split into multiple files, and that these be registered in the DB.DBA.load_list table with the ld_dir() function. Since it’s the case here, once registered for load, the rdf_loader_run() function can be run multiple times, it is recommended a maximum of no cores / 2.5, to optimally parallelize the data load and hence maximize load speed.  
It may also be useful to run regularly a checkpoint to commit all transactions to the database. 
```SQL> checkpoint;```
This is done automatically at the end of each data load (after a ```rdf_loader_run();```). However, it may append that Virtuoso crashes and that all the transactions are lost.

### Restarting file loading 
To force the loading of a file which ll_state is 1, we must return the ll_state field to 0 with the following command and then execute again ‘rdf_loader_run();’
“update load_list set ll_state=0 where ll_file='[adresse/du/fichier]'; “
Useful links about how to load Dbpedia data sets in virtuoso: http://vos.openlinksw.com/owiki/wiki/VOS/VirtBulkRDFLoaderExampleDbpedia
http://fr.dbpedia.org/doc/chargementVirtuoso.html

Once the above steps are completed, the loader finishes and you are set to issue sparql queries in the client SQL terminal.


# RuDiK APIs

The APIs we currently expose are defined in the interface `asu.edu.rule_miner.rudik.rule_generator.HornRuleDiscoveryInterface`. The current implementation, explained in details in the technical report, is defined in the class `asu.edu.rule_miner.rudik.rule_generatorDynamicPruningRuleDiscovery`

# RuDiK-GUI
The following GUI is a <a href="https://www.dropwizard.io/1.3.5/docs/">Dropwizard </a> application and therefore follows a Dropwizard application structure. 

## Getting started
These instructions will get you a copy of the project up and running on your local machine for testing purposes.
Please note that some browsers like ```Mozilla Firefox``` and ```Microsoft Edge``` do not support all the tools used to build the GUI. Therefore, to have a good experience with this version of the GUI, please use ```Google Chrome```.

### STEP 1: 
Clone the git project: ```git clone https://github.com/lionelsome/RuDiK-GUI.git```;

### STEP 2: 
Build the project. Inside the project folder: run ```mvn -U clean package -DskipTests```;

### STEP 3: 
The following Web application works primarily with Virtuoso installed locally and available at localhost:8890/sparql (the parameter "Sparql endpoint" allows to choose the endpoint we want to query). Therefore, make sure the Virtuoso server is started (see above):
 ```cd /var/lib/virtuoso-opensource-6.1/db
 sudo /usr/bin/virtuoso-t -f 
 ```
 
### STEP 4: 
Compile and laugh the application. Inside the project folder run:
```java -jar target/rule_miner-0.0.1-SNAPSHOT.jar server rudik.yml``` where rudik.yml is the application’s configuration file. 
In your web browser, go to ```localhost:9090/rudik``` and start the adventure with Rudik!

## How to use it ?

### Discover new rules : 
It allows to run RuDiK against the selected knowledge base in order to find new rules related to the selected predicate.
Select first the ```Type of rule``` and then the ```Knowledge Graph``` (KG). Depending on the KG, a list of predicates are suggested : you can either select one of them or enter another predicate that exists in the KG in ```Target predicate```(since no check is made on the correctness of the typed predicate, make sure it does exist).<br>
In the ```More options```, select the ```Sparql endpoint``` you want to use based on your installation (whether you have Virtuoso installed or not) and the KG that you selected. There are other parameters that can be changed there. Please refer to github mentioned above for more details.<br>
Click on ```Run``` in the ```Main parameters``` card and wait for RuDiK to make the job. <br>
You can see the Generation and Validation set (refer to the technical report for more details) of examples of each rule and also a Sankey diagram with all the rules and their corresponding Generation and validation Set. For each example, the ```Graph``` button allows to show a surrounding graph.
This GUI also gives you the possibility to instantiate the discovered rules. 

N.B: You can hover over each parameter to get a description.

### Instantiate a rule
Select a KG and the corresponding ```Sparql endpoint```. According to the selected KG, a list of rules to instantiate are suggested. Select one of them or enter your own rule you want to instantiate.<br>
Here, you can also have a surrounding graph for the literals involved in the instantiation. 

## Built With

* [Dropwizard](http://www.dropwizard.io/1.3.5/docs/) (1.3.5 or above) - The web framework used
* [Maven](https://maven.apache.org/) (3.2 or above) - Dependency Management


# Contacts

1. [Stefano Ortona](mailto:stefano.ortona@gmail.com)
2. [Paolo Papotti](mailto:change-with.last-name_@eurecom.fr)
3. [Vamsi Meduri](mailto.vamsikrishna1902@gmail.com)
