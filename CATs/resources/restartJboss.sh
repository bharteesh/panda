#!/bin/bash

# HOST=0.0.0.0
HOST=cats.ithaka.org
JAVA_HOME=/cmjp/java/jdk1.6.0_21
export PATH=${JAVA_HOME}/bin:$PATH
JBOSS_BIN=/cmjp/jboss/jboss-5.1.0.GA/bin

JBOSS_PID=`jps | egrep '^[0-9]+ Main$' | cut -f 1 -d ' ' `

if [ "z$JBOSS_PID" = "z" ] ; then
	printf "%s: failed to find a running JBOSS!\n" $0
else
	kill -9 $JBOSS_PID
	sleep 10
fi


/usr/bin/nohup $JBOSS_BIN/run.sh -b $HOST 1>/dev/null 2>&1 &

