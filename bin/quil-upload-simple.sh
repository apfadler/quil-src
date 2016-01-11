#!/bin/sh


curl -s -X POST --data-binary @$1 -H "Content-Type: text/plain" http://$QUIL_SERVER:$QUIL_PORT/api/simplecache/$2/addFromCSV
