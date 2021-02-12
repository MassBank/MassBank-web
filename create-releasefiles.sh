#!/bin/bash
docker-compose -f compose/mariadb-docker-compose.yml stop
docker-compose -f compose/mariadb-docker-compose.yml rm
docker-compose -f compose/mariadb-docker-compose.yml pull
docker-compose -f compose/mariadb-docker-compose.yml up -d
cd MassBank-Project
mvn package
./MassBank-lib/target/MassBank-lib/MassBank-lib/bin/RefreshDatabase
echo Create sql dump...
mysqldump -u root -h 127.0.0.1 -p --databases MassBank > MassBank.sql
./MassBank-lib/target/MassBank-lib/MassBank-lib/bin/RecordExporter -f RIKEN_MSP -o MassBank_RIKEN.msp ../../MassBank-data/*
./MassBank-lib/target/MassBank-lib/MassBank-lib/bin/RecordExporter -f NIST_MSP -o MassBank_NIST.msp ../../MassBank-data/*
