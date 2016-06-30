#!/bin/bash


if [ -z "$QUIL_HOME" ]; then
    echo "Defaulting QUIL_HOME to" `pwd`
    export QUIL_HOME=`pwd`
fi 

if [ -z "$QUIL_SERVER" ]; then
    echo "Defaulting QUIL_SERVER to localhost"
    export QUIL_SERVER=localhost
fi 

if [ -z "$QUIL_PORT" ]; then
    echo "Defaulting QUIL_SERVER to 8081"
    export QUIL_PORT=8081
fi 

curl -X POST -H "Content-type: text/plain" -d "$1" http://$QUIL_SERVER:$QUIL_PORT/api/objects/query
