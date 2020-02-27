#!/bin/bash

HOST=$1
PORT=$2
USER=$3
PWD=$4
TIMEOUT_SEC=300
WAIT_SEC=10

START=$(date +%s)
echo "Start waiting for $HOST:$PORT..."
until curl --head --fail --anyauth -u $USER:$PWD http://$HOST:$PORT
do
  echo "curl return value is $?"
  if [ $(($(date +%s) - $START)) -gt $TIMEOUT_SEC ]; then
    echo "Service $HOST:$PORT did not start within $TIMEOUT_SEC seconds."
    exit 1
  fi
  echo "Waiting for $HOST:$PORT..."
  sleep $WAIT_SEC
done
