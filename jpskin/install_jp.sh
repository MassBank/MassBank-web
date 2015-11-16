#!/bin/bash
sudo cp /vagrant/jpskin/htmlfiles/index.jsp /var/lib/tomcat7/webapps/MassBank/jsp/index.jsp;
sudo cp /vagrant/jpskin/images/* -t /var/www/html/MassBank/img;
sudo mkdir /var/www/html/MassBank/en;
sudo cp /vagrant/jpskin/htmlfiles/*.html -t /var/www/html/MassBank/en;
sudo cp /vagrant/jpskin/cssfiles/* -t /var/www/html/MassBank/css;
sudo cp /vagrant/jpskin/scriptfiles/* -t /var/www/html/MassBank/script;