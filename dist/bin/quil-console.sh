#!/bin/bash

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set. Exiting..." 
    exit
fi 


if [ -z "$QUIL_HOME" ]; then
    echo "Defaulting QUIL_HOME to" `pwd`
    export QUIL_HOME=`pwd`
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
	java -cp "$QUIL_CLASSPATH" org.quil.console.ScalaConsole "$@"
	
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then

    addEachJarInDir "libs/"	
	java -cp "$QUIL_CLASSPATH" org.quil.console.ScalaConsole "$@"
	
elif [ "$(expr substr $(uname -s) 1 4)" == "MING" ]; then
    
	addEachJarInDirWinCygwin "libs/"
	stty -icanon min 1 -echo > /dev/null 2>&1
	java -cp "$QUIL_CLASSPATH" -Djline.terminal=jline.UnixTerminal -Dlog4j.configuration=file:config/java.util.logging.properties org.quil.console.ScalaConsole "$@"
	stty icanon echo > /dev/null 2>&1
fi



