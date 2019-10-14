#!/bin/bash
CURRENT_UID=$(id -u):$(id -g)
export CURRENT_UID

usage () {
	echo "Usage: install.sh <operation> <instance>"
	echo "         <operation> ... start, stop, deploy or refresh"
	echo "         <instance>  ... 0 to 9"
}

case $2 in
	[0-9])
		TAG=$2
		export TAG
	;;
	*)
		usage
		exit 1
	;;
esac


case $1 in
	start)
		docker-compose -f compose/full-service.yml -p $TAG pull 
		docker-compose -f compose/full-service.yml -p $TAG run --rm maven mvn -Duser.home=/var/maven -f /project clean package
		docker-compose -f compose/full-service.yml -p $TAG up -d mariadb
		echo "Wait 30s for database to be ready..."
                sleep 30
		docker-compose -f compose/full-service.yml -p $TAG up -d tomcat
		docker-compose -f compose/full-service.yml -p $TAG \
			run --rm dbupdate \
			/project/MassBank-lib/target/MassBank-lib/MassBank-lib/bin/RefreshDatabase
	;;
	stop)
		docker-compose -f compose/full-service.yml -p $TAG rm -s
	;;
	refresh)
		docker-compose -f compose/full-service.yml -p $TAG \
			run --rm dbupdate \
			/project/MassBank-lib/target/MassBank-lib/MassBank-lib/bin/RefreshDatabase
	;;
	deploy)
		docker-compose -f compose/full-service.yml -p $TAG pull
		docker-compose -f compose/full-service.yml -p $TAG run --rm maven mvn -Duser.home=/var/maven -f /project clean package
		docker-compose -f compose/full-service.yml -p $TAG rm -s tomcat
		docker-compose -f compose/full-service.yml -p $TAG up -d tomcat
	;;
	*)
		usage
		exit 1
	;;
esac
