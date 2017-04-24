cd gen-java

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 10000 node1 false localhost:9090 &

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 15000 node2 false localhost:9090 &

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 20000 node3 false localhost:9090 &

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 25000 node4 false localhost:9090 &

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 30000 node5 false localhost:9090 &

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 35000 node6 false localhost:9090 &

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 40000 node7 false localhost:9090 &

cd ..
