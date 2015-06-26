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
APACHE_CONF_PATH=/etc/apache2/
APACHE_MODULE_PATH=/usr/lib/httpd/modules

# Tomcat Path
#DEST_TOMCAT_PATH=/usr/local
DEST_TOMCAT_PATH=/var/lib/tomcat7


echo
echo ">> service stop"

if [ -x /etc/init.d/xvfb ] ; then /etc/init.d/xvfb stop ; fi 
service tomcat7 stop 
service apache2 stop

echo
echo ">> file copy"
cp -rp $INST_HTDOCS_PATH/. $APACHE_HTDOCS_PATH
cp -fp $INST_ROOT_PATH/massbank.conf $APACHE_HTDOCS_PATH/MassBank

mkdir -p $APACHE_ERROR_PATH
cp -fp $INST_ERROR_PATH/40?.html $APACHE_ERROR_PATH
cp -fp $INST_ERROR_PATH/50?.html $APACHE_ERROR_PATH

#cp -rip $INST_CONF_PATH/* $APACHE_CONF_PATH

a2enmod rewrite
a2enmod authz_groupfile
a2enmod cgid
a2enmod jk

cp -p $INST_CONF_PATH/.htpasswd $APACHE_CONF_PATH
cp -p $INST_CONF_PATH/010-a2site-massbank.conf $APACHE_CONF_PATH/sites-available
a2ensite 010-a2site-massbank

#cp -ip $INST_MODULE_PATH/mod_jk.so $APACHE_MODULE_PATH
cp -rp $INST_TOMCAT_PATH $DEST_TOMCAT_PATH
cp -rp $INST_TOMCAT_PATH/webapps/* $DEST_TOMCAT_PATH/webapps/

echo 
echo "Compile Search.cgi"
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean ; make ) 
cp -p ./Apache/cgi-bin/Search.cgi/Search.cgi $APACHE_HTDOCS_PATH/MassBank/cgi-bin/


#cp -ip $INST_ROOT_PATH/StartupScript/tomcat /etc/init.d
cp -p $INST_ROOT_PATH/StartupScript/xvfb /etc/init.d
#mv $APACHE_ERROR_PATH/noindex.html $APACHE_ERROR_PATH/noindex.html.bak
cp -fp $INST_ERROR_PATH/403.html $APACHE_ERROR_PATH/noindex.html

echo
echo ">> change file mode"
chmod a+x $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*.cgi
chmod a+x $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*/*.pl
chmod 755 $APACHE_HTDOCS_PATH/MassBank/script/*.pl
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct.dat
chmod 755 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct_server


mkdir -p $APACHE_HTDOCS_PATH/MassBank/StructureSearch/temp
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/temp
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch
#chmod u+x $APACHE_MODULE_PATH/mod_jk.so
#chmod u+x $DEST_TOMCAT_PATH/tomcat/bin/*.sh
mkdir -p $DEST_TOMCAT_PATH/temp
chmod a+w $DEST_TOMCAT_PATH/temp

chown -R tomcat7.tomcat7 $DEST_TOMCAT_PATH/webapps/MassBank/temp/

#chmod u+x /etc/init.d/tomcat
chmod u+x /etc/init.d/xvfb

## Configure Tomcat if not already done
if ! grep '^<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />$' $DEST_TOMCAT_PATH/conf/server.xml ; then 
sed -i -e 's#<!-- Define an AJP 1.3 Connector on port 8009 -->#<!-- Define an AJP 1.3 Connector on port 8009 -->\n<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />#' $DEST_TOMCAT_PATH/conf/server.xml    
fi 


echo
echo ">> service start"
/etc/init.d/xvfb start
service mysql start 
service tomcat7 start 
service apache2 start

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

#echo
#echo ">> chkconfig xvfb on"
#echo ">> chkconfig mysqld on"
#echo ">> chkconfig httpd on"
#echo ">> chkconfig tomcat on"
#chkconfig xvfb on
#chkconfig mysqld on
#chkconfig httpd on
#chkconfig tomcat on

echo
echo
echo
echo "Done."
echo
echo "Please edit \"FrontServer URL\" of \""$APACHE_HTDOCS_PATH"/MassBank/massbank.conf\" appropriately."
echo
echo
echo
