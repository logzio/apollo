#!/usr/bin/env bash

JAVA_OPTS=""
if ! [[ -z $APOLLO_LOGBACK_XML_PATH ]]; then
    JAVA_OPTS="-Dlogback.configurationFile=$APOLLO_LOGBACK_XML_PATH"
fi


nginx &> /dev/null
java -jar /hawtio-app-1.4.67.jar --port 8083 &
java $JAVA_OPTS -jar /apollo-backend-jar-with-dependencies.jar
