#!/bin/sh

if [ -z "$QUIL_HOME" ]; then
    echo "Defaulting QUIL_HOME to" `pwd`
    export QUIL_HOME=`pwd`
fi 

export QUIL_SERVER_STANDALONE=true
export QUIL_WARPATH=$QUIL_HOME/libs/webapp-1.0-SNAPSHOT.war
$QUIL_HOME/bin/quil-server.sh