#!/bin/bash
mvn -f MassBank-Project/pom.xml build-helper:parse-version versions:set -DnewVersion=$1
mvn -f MassBank-Project/pom.xml versions:set-property -Dproperty=timestamp -DnewVersion=`date --iso-8601=seconds`
