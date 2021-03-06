## Building the DHT:
* The crux of the DHT functionality is implemented by the chord ring design. The following are key operations for the chord ring:
* Join: In our design the first chord node to be started with parameter4 set to "true" becomes the master node or node-0. This node handles the join operations for all the subsequent nodes that will join the chord ring. The node0 will allow only one node to join at a time. This is enforced via usage of locks in the join operations which would allow only one join request thread to complete at any instance. This operation initializes the finger table and predecessor for a newly joining node.
* Finding successor node : This operation is required  when information regarding where a particular key is stored is required at any stage. A key is always stored in the node that follows it in the chord ring. This method is also used by Dictionary loader in locating the node that will be used to store the key extracted from the sample file.
* Updating of other nodes: This operation is performed by a new node everytime it has completed the join operation. This operation involves updating the finger tables of the predecessor nodes to include information regarding the newly joined node. 


## Inserting Data Into DHT:
* The dictionary loader program handles the task of adding all the keys to their respective nodes in the Chord ring.
* The dicitionary loader takes the input of a sample file of key-value pairs and the URL of a existing chordNode to assist in the insertion process.
* For each line in the sample file the following steps:
	* Compute the hash of the word to be added to the DHT.
	* Using the hash find the node that will store the key (The find_node method will handle this task).
	* Once the node where the key has to be stored is found, the word-meaning pair is inserted into the chord node computed in the previous step.
* The dictionary loader will exit once all the key-value pairs in the file have been inserted.


## Data Query by the Client:
* The client takes the URL of a particular ChordNode as a parameter.
* The client queries the DHT for a word and the meaning is looked up from the dictionary and returned. If the word is not found then Not found is returned.
* For every word the client queries, the following operations are performed:
	* Compute the hash of the word
	* Using the URL of the ChordNode and the hash of the word, find the URL of the chord node that stores the particular word.
	* Using the URL returned in the previous step, perform the lookup operation on the node returned in the URL.
* The client runs as long as words have to be queried


