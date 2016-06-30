#!/bin/sh

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

$QUIL_HOME/bin/quil-client.sh documentcache ExampleTrades createFromJSONArray $QUIL_HOME/sampledata/OGExampleData/Trades.json

$QUIL_HOME/bin/quil-client.sh compute task submit $QUIL_HOME/sampledata/OGExampleData/Task.OGTest.json

