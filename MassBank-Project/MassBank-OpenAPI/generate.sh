#!/bin/bash
#wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/5.4.0/openapi-generator-cli-5.4.0.jar -O openapi-generator-cli.jar
java -jar openapi-generator-cli.jar generate -g spring -i src/main/resources/openapi.yaml -c openapi-generator-config.json -o ../spring-boot-codegenerator
