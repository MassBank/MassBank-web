#!/bin/bash
sudo mv /vagrant/jpskin/htmlfiles/index.jsp /var/lib/tomcat7/webapps/MassBank/jsp/index.jsp
sudo mv /vagrant/jpskin/images/* -t /var/www/html/MassBank/img
sudo mkdir /var/www/html/MassBank/en
sudo mv /vagrant/jpskin/htmlfiles/*.html -t /var/www/html/MassBank/en
sudo mv /vagrant/jpskin/cssfiles/* -t /var/www/html/MassBank/css
sudo mv /vagrant/jpskin/scriptfiles/* -t /var/www/html/MassBank/script