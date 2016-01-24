#!/bin/sh

if [ -z "$QUIL_HOME" ]; then
    echo "Defaulting QUIL_HOME to" `pwd`
    export QUIL_HOME=`pwd`
fi 

export QUIL_WORKER=true
$QUIL_HOME/bin/quil-server.sh