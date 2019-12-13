#!/bin/bash
mvn -f MassBank-Project/pom.xml build-helper:parse-version versions:set -DnewVersion=$1
