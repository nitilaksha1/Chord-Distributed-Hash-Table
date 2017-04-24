cd gen-java

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" DictionaryLoader localhost:9090 sample-data.txt

xterm -e java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordClient localhost:9090