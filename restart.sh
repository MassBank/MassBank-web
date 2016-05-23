#!/bin/bash
sudo service apache2 stop
sudo service mysql restart 
sudo service tomcat7 restart
sudo service apache2 start
