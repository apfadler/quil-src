#!/bin/bash

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set. Exiting..." 
    exit
fi 


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


QUIL_CLASSPATH="."

function addEachJarInDir(){
  if [[ -d "${1}" ]]; then
    for jar in $(find -L "${1}" -maxdepth 1 -name '*jar'); do
      QUIL_CLASSPATH="$jar:$QUIL_CLASSPATH"
    done
  fi
}

function addEachJarInDirWinCygwin(){
  if [[ -d "${1}" ]]; then
    for jar in $(find -L "${1}" -maxdepth 1 -name '*jar'); do
      QUIL_CLASSPATH="$jar;$QUIL_CLASSPATH"
    done
  fi
}


cd $QUIL_HOME

if [ "$(uname)" == "Darwin" ]; then
    addEachJarInDir "libs/"  
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    addEachJarInDir "libs/"
elif [ "$(expr substr $(uname -s) 1 4)" == "MING" ]; then
    addEachJarInDirWinCygwin "libs/"
fi

echo $QUIL_CLASSPATH
java -cp "$QUIL_CLASSPATH" org.quil.server.QuilServer

