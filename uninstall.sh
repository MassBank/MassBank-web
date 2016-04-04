#!/bin/sh
#------------------------------------------------
# MassBank System Uninstaller
#------------------------------------------------

echo
echo
echo "*********************************************"
echo "* MassBank System Uninstaller               *"
echo "*  version 1.8.1                            *"
echo "*********************************************"
echo

echo "All data registered in MassBank and MassBank modules are deleted."
echo -n "Are you sure(yes/no)? "
read ANSWER
if [ $ANSWER != "yes" ] && [ $ANSWER != "Yes" ] && [ $ANSWER != "YES" ] && [ $ANSWER != "y" ] && [ $ANSWER != "Y" ];
then
    echo
    echo "Uninstall cancel."
    echo
    exit 0
fi

echo
echo "Starting uninstall..."
echo

# Uninstall Files Path
UNINST_SQL_PATH=./modules/sql/drop.sql

# Apache Path
APACHE_HTDOCS_PATH=/var/www/html
APACHE_ERROR_PATH=/var/www/error
APACHE_CONF_PATH=/etc/httpd/conf
APACHE_MODULE_PATH=/usr/lib/httpd/modules

# Tomcat Path
DEST_TOMCAT_PATH=/usr/local

echo
echo ">> chkconfig tomcat off"
echo ">> chkconfig httpd off"
echo ">> chkconfig mysqld off"
echo ">> chkconfig xvfb off"
chkconfig tomcat off
chkconfig httpd off
chkconfig mysqld off
chkconfig xvfb off

echo
echo ">> drop database (root authority)"
mysql --user=root -p < $UNINST_SQL_PATH

echo
echo ">> service stop"
/etc/init.d/httpd stop
/etc/init.d/tomcat stop 2> /dev/null
/etc/init.d/mysqld stop
/etc/init.d/xvfb stop 2> /dev/null

echo
echo ">> file remove"
rm -f /etc/init.d/tomcat
rm -rf $APACHE_HTDOCS_PATH/*
rm -f $APACHE_ERROR_PATH/40?.html
rm -f $APACHE_ERROR_PATH/50?.html
rm -f $APACHE_MODULE_PATH/mod_jk.so
rm -rf $DEST_TOMCAT_PATH/*
rm -f /etc/init.d/tomcat
rm -f /etc/init.d/xvfb
mv $APACHE_ERROR_PATH/noindex.html.bak $APACHE_ERROR_PATH/noindex.html 2> /dev/null

echo
echo
echo
echo "Done."
echo
echo
echo
