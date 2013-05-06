#!/bin/bash

JAVA_HOME=/usr/local/jdk/jdk1.6.0_21
export JAVA_HOME
echo $JAVA_HOME
PATH=/usr/local/jdk/jdk1.6.0_21/bin:${PATH}
echo $PATH

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls ./libs/*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done

THE_CLASSPATH=${THE_CLASSPATH}:./resources

echo $THE_CLASSPATH



nohup java -Xms512m -Xmx2048m -cp ".:${THE_CLASSPATH}"  org.ithaka.phx.archive.BatchController &