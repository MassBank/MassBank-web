#!/bin/sh
#------------------------------------------------
# MassBank System Installer
#------------------------------------------------

echo
echo
echo "*********************************************"
echo "* MassBank System Installer                 *"
echo "*  version 1.8.1                            *"
echo "*********************************************"
echo

# Install Files Path
INST_ROOT_PATH=./modules
INST_HTDOCS_PATH=$INST_ROOT_PATH/apache/htdocs
INST_ERROR_PATH=$INST_ROOT_PATH/apache/error
INST_CONF_PATH=$INST_ROOT_PATH/apache/conf
INST_MODULE_PATH=$INST_ROOT_PATH/apache/modules
INST_TOMCAT_PATH=$INST_ROOT_PATH/tomcat
INST_SQL_PATH=$INST_ROOT_PATH/sql/init.sql

# Apache Path
APACHE_HTDOCS_PATH=/var/www/html
APACHE_ERROR_PATH=/var/www/error

# Tomcat Path
DEST_TOMCAT_PATH=/var/lib/tomcat7


echo
echo ">> service stop"
service tomcat7 stop 
service apache2 stop

echo
echo ">> file copy"
cp -r $INST_HTDOCS_PATH/. $APACHE_HTDOCS_PATH
cp -r $INST_ERROR_PATH/. $APACHE_ERROR_PATH
cp $INST_ROOT_PATH/massbank.conf $APACHE_HTDOCS_PATH/MassBank
chown -R www-data:www-data /var/www/*
find /var/www/ -type d -exec chmod 755 {} \;
find /var/www/ -type f -exec chmod 644 {} \;

a2enmod rewrite
a2enmod authz_groupfile
a2enmod cgid
a2enmod jk

## mbadmin password
htpasswd -b -c /etc/apache2/.htpasswd massbank bird2006

install -m 644 -o root -g root $INST_CONF_PATH/010-a2site-massbank.conf /etc/apache2/sites-available
a2ensite 010-a2site-massbank

cp -rp $INST_TOMCAT_PATH $DEST_TOMCAT_PATH
cp -rp $INST_TOMCAT_PATH/webapps/* $DEST_TOMCAT_PATH/webapps/

echo 
echo "Compile Search.cgi"
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean ; make ) 
cp -p ./Apache/cgi-bin/Search.cgi/Search.cgi $APACHE_HTDOCS_PATH/MassBank/cgi-bin/


echo
echo ">> change file mode"
chmod 755 $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*.cgi \
          $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*/*.pl \
          $APACHE_HTDOCS_PATH/MassBank/script/*.pl \
          $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct_server
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct.dat
install -d -m 777 -o www-data -g www-data $APACHE_HTDOCS_PATH/MassBank/StructureSearch/temp

install -d -m 777 -o tomcat7 -g tomcat7 $DEST_TOMCAT_PATH/temp
chown -R tomcat7:tomcat7 $DEST_TOMCAT_PATH/webapps/MassBank/temp/
chown -R tomcat7:tomcat7 $APACHE_HTDOCS_PATH/MassBank/DB/
chown -R tomcat7:tomcat7 $APACHE_HTDOCS_PATH/MassBank/massbank.conf


#chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch

## Configure Tomcat if not already done
if ! grep '^<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />$' $DEST_TOMCAT_PATH/conf/server.xml ; then 
sed -i -e 's#<!-- Define an AJP 1.3 Connector on port 8009 -->#<!-- Define an AJP 1.3 Connector on port 8009 -->\n<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />#' $DEST_TOMCAT_PATH/conf/server.xml    
fi 


echo
echo ">> service start"
service mysql restart 
service tomcat7 restart 
service apache2 restart

echo
echo ">> create database (root authority)"

cat >~/.my.cnf <<EOF
[client]
user=root
password="bird2006"

[mysql]
user=root
password="bird2006"
EOF

mysql --user=root  < $INST_SQL_PATH

# echo
# echo ">> retrieving massbank.jp page and specifications"

# sudo wget -q www.massbank.jp/index.html?lang=en -O /var/lib/tomcat7/webapps/MassBank/jsp/index.jsp
# sudo wget -q -i ./imglist -P /var/www/html/MassBank/img
# sudo wget -q -i ./csslist -P /var/www/html/MassBank/css
# sudo mkdir /var/www/html/MassBank/en
# sudo wget -q -i ./pagelist -P /var/www/html/MassBank/en

echo
echo
echo
echo "Done."
echo
echo "Please edit \"FrontServer URL\" of \""$APACHE_HTDOCS_PATH"/MassBank/massbank.conf\" appropriately."
echo
echo
echo
