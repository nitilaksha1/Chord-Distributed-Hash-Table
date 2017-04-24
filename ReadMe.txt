TEAM INFORMATION:
--------------------

Member 1:
----------
Name : Akshay Mulkalwar
Id   : mulka002

Member2:
---------
Name : Nitilaksha Halakatti
Id   : halak004


RUNNING INSTRUCTIONS:
----------------------
-> Run compile.sh script to compile all the programs.
-> The compile.sh will also start and run all the chord nodes.
-> Next the script client.sh has to executed. This will run the dictionary loader to add all keys to DHT and it will launch the client.
-> Once the client is launched a menu-driven interface is presented
-> The options display 1 for lookup of a word 2 to exit the client.
-> On choosing 1 for lookup, enter a word that has to be queried in the DHT.
-> If the word is found the meaning is returned els Not Found is returned.


CUSTOMIZING CHORD NODES:
-------------------------

-> Execute the compile.sh script to compile and run the servers.
-> In case the predefined parametes in the script need to be changed the script can be modified.
-> The command to run the server is :
	- java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordServer <hostname> <portnumber> <nodename> <isMasterNode> <MasterURL>
	- <Hostname> is the hostname of the machine on which the above chord node in running
	- <portnumber> is the portnumber on the machine on which the above service runs
	- <nodename> is the name to be used for a chord node. This name information will be used to create logfile
	- <isMasterNode> This value is true or false. A true value indicates that this node will be used to coordinate all join operations. Only one node must     set this value to true.
	- <MasterURL> This value specifies the URL of the node0 or master node. The URL must be of the form "<hostname>:<portnumber>" inorder to be parsed cor     rectly by the program.
	

CUSTOMIZING DICTIONARY LOADER:
------------------------------------------------
-> The Dictionary loader is of the following structure:
	- java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" DictionaryLoader <MasterURL> <sample-file.txt>
	- <MasterURL> This values specifies the URL of the node0 or master node.The URL must be of the form "<hostname>:<portnumber>" inorder to be parsed cor     rectly by the program.
	- <sample-file.txt> This is the file which contains the word-meaning pairs that have to be distributed among different nodes in chord ring.

CUSTOMIZING CLIENT:
---------------------

-> If client parameters need to be modified, following is the structure of the client:
	- java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordClient <MasterURL>
	- <MasterURL> is the URL of the chord node which will be used to determine the node where keys are stored


LOGGING INFORMATION:
---------------------

-> The logs of each chord node are present in the file <Node-name>.txt where node name is the name that is given to the Chord node as a command line parameter during the start of the nodes.
-> The local logs of each node will contain information regarding finger tables of that node and the stack of operations for that particular node during lookup of a word.
