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
        echo "         <operation> ... start, stop or refresh"
        echo "         <instance>  ... 0 to 9"
        exit 1
        ;;
esac

case $1 in
start)
	docker-compose -f compose/full-service.yml -p $2 pull 
	docker-compose -f compose/full-service.yml -p $2 run --rm maven mvn -Duser.home=/var/maven -f /project package
	docker-compose -f compose/full-service.yml -p $2 up -d tomcat
	echo "Wait 20s for database to be ready..."
	sleep 20
	docker-compose -f compose/full-service.yml -p $2 \
		run --rm dbupdate \
		/project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1/bin/RefreshDatabase
	;;
stop)
	docker-compose -f compose/full-service.yml -p $2 stop
	docker-compose -f compose/full-service.yml -p $2 rm
	;;
refresh)
	docker-compose -f compose/full-service.yml -p $2 \
                run --rm dbupdate \
                /project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1/bin/RefreshDatabase
	;;
*)
	echo "Usage: install.sh <operation> <instance>"
	echo "         <operation> ... start, stop or refresh"
	echo "         <instance>  ... 0 to 9"
	exit 1
	;;
esac

