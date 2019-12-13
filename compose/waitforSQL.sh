#!/bin/sh
maxcounter=300
 
counter=1
while ! mysql --protocol TCP -uroot -p123blah321 -e "show databases;" > /dev/null 2>&1; do
    echo 'Waiting for MariaDB ['$counter'/'$maxcounter'].'
    sleep 1
    counter=`expr $counter + 1`
    if [ $counter -gt $maxcounter ]; then
        >&2 echo "We have been waiting for MariaDB too long already; failing."
        exit 1
    fi;
done
