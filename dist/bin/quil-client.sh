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


if [ $1 == "documentcache" ]; then
	if [ $3 != "get" ]; then
		if [ $3 != "put" ]; then
			curl -s -X POST --data-binary @$4 -H "Content-Type: text/plain" http://$QUIL_SERVER:$QUIL_PORT/api/$1/$2/$3
		else
			curl -s -X POST --data-binary @$5 -H "Content-Type: text/plain" http://$QUIL_SERVER:$QUIL_PORT/api/$1/$2/$3/$4
		fi
	else
		curl -s -X GET -H "Content-Type: text/plain" http://$QUIL_SERVER:$QUIL_PORT/api/$1/$2/$3/$4
	fi
fi

if [ $1 == "simplecache" ]; then
	if [ $3 != "get" ]; then
		if [ $3 != "put" ]; then
			curl -s -X POST --data-binary @$4 -H "Content-Type: text/plain" http://$QUIL_SERVER:$QUIL_PORT/api/$1/$2/$3
		else
			curl -s -X POST --data-binary @$5 -H "Content-Type: text/plain" http://$QUIL_SERVER:$QUIL_PORT/api/$1/$2/$3/$4
		fi
	else
		curl -s -X GET -H "Content-Type: text/plain" http://$QUIL_SERVER:$QUIL_PORT/api/$1/$2/$3/$4
	fi
fi

if [ $1 == "compute" ]; then
	if [ $2 == "tasks" ]; then
		if [ -z "$3" ]; then
			curl -s -X GET  -H "Content-Type: application/json" http://$QUIL_SERVER:$QUIL_PORT/api/compute/tasks
		else
			if [ -z "$4" ]; then
				curl -s -X GET  -H "Content-Type: application/json" http://$QUIL_SERVER:$QUIL_PORT/api/compute/tasks/$3
			else
				curl -s -X GET  -H "Content-Type: application/json" http://$QUIL_SERVER:$QUIL_PORT/api/compute/tasks/$3/$4
			fi
		fi
	else
		curl -s -X POST --data-binary @$4 -H "Content-Type: application/json" http://$QUIL_SERVER:$QUIL_PORT/api/$1/$2/$3
	fi
fi



