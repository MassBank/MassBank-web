#!/bin/bash
cd MassBank-Project
mvn build-helper:parse-version versions:set -DnewVersion=$1
mvn versions:set-property -Dproperty=timestamp -DnewVersion=`date --iso-8601=seconds`
