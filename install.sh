#!/bin/bash
CURRENT_UID=$(id -u):$(id -g)
export CURRENT_UID


case $2 in
[0-9])
	TAG=$2
	export TAG
        ;;
*)
        echo "Usage: install.sh <operation> <instance>"
        echo "         <operation> ... start, stop, deploy or refresh"
        echo "         <instance>  ... 0 to 9"
        exit 1
        ;;
esac

case $1 in
start)
	docker-compose -f compose/full-service.yml -p $TAG pull 
	docker-compose -f compose/full-service.yml -p $TAG run --rm maven mvn -Duser.home=/var/maven -f /project package
	docker-compose -f compose/full-service.yml -p $TAG up -d tomcat
	echo "Wait 20s for database to be ready..."
	sleep 20
	docker-compose -f compose/full-service.yml -p $TAG \
		run --rm dbupdate \
		/project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1/bin/RefreshDatabase
	;;
stop)
	docker-compose -f compose/full-service.yml -p $TAG rm -s
	;;
refresh)
	docker-compose -f compose/full-service.yml -p $TAG \
                run --rm dbupdate \
                /project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1/bin/RefreshDatabase
	;;
redeploy)
	docker-compose -f compose/full-service.yml -p $TAG pull
	docker-compose -f compose/full-service.yml -p $TAG run --rm maven mvn -Duser.home=/var/maven -f /project package
	docker-compose -f compose/full-service.yml -p $TAG rm -s tomcat
	docker-compose -f compose/full-service.yml -p $TAG up -d tomcat
	;;

*)
	echo "Usage: install.sh <operation> <instance>"
	echo "         <operation> ... start, stop, deploy or refresh"
	echo "         <instance>  ... 0 to 9"
	exit 1
	;;
esac

