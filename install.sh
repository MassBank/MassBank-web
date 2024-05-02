#!/bin/bash
OUT=$(docker compose version --short  2> /dev/null)
RET=$?
if [ $RET -ne 0 ]; then
        echo "Docker Compose plugin for docker not found. Trying 'docker-compose'."
	OUT=$(docker-compose version --short  2> /dev/null)
	RET2=$?
	if [ $RET2 -ne 0 ]; then
		echo "'docker-compose' not found."
		echo "Expecting Docker Compose plugin for docker or docker-compose"
	else
		if [[ $OUT = 1* ]] || [[ $OUT = 2* ]]
		then
			echo "'docker-compose' version "$OUT" found. Using command 'docker-compose'." 
			COMPOSE_COMMAND="docker-compose"
		else
			echo "'docker-compose' version "$OUT" found. Version 1.X or 2.X expected."
			exit
		fi
	fi
else
	if [[ $OUT = 2* ]]
	then
		echo "Docker Compose plugin for docker version "$OUT" found. Using command 'docker compose'." 
		COMPOSE_COMMAND="docker compose"
	else
		echo "Docker Compose plugin for docker version "$OUT" found. Version 2.X expected."
		exit
	fi
fi

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
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG pull 
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG up -d mariadb
		CURRENT_UID=$(id -u):$(id -g) $COMPOSE_COMMAND -f compose/full-service.yml -p $TAG run --rm maven mvn -q -Duser.home=/var/maven clean package
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG up -d tomee
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG \
			run --rm dbupdate \
			/project/MassBank-lib/target/MassBank-lib/MassBank-lib/bin/RefreshDatabase
	;;
	stop)
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG rm -s
	;;
	refresh)
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG \
			run --rm dbupdate \
			/project/MassBank-lib/target/MassBank-lib/MassBank-lib/bin/RefreshDatabase
	;;
	deploy)
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG pull
		CURRENT_UID=$(id -u):$(id -g) $COMPOSE_COMMAND -f compose/full-service.yml -p $TAG run --rm maven mvn -q -Duser.home=/var/maven clean package
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG rm -s tomee
		$COMPOSE_COMMAND -f compose/full-service.yml -p $TAG up -d tomee
	;;
	*)
		usage
		exit 1
	;;
esac
