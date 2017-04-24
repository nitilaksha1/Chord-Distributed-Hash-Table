#!/bin/sh


thrift -r --gen java ChordNodeService.thrift

cd gen-java

javac -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" *.java

if [ $? -eq 0 ]
then
  echo "Done Compilation!"
fi

java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" ChordDHTServer localhost 9090 node0 true localhost:9090 &
#source compile1.sh &
#source compile2.sh &
#source compile3.sh &
#source compile4.sh &
cd ..
