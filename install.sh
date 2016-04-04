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

echo "Installation of MassBank modules."
echo -n "Are you sure(yes/no)? "
read ANSWER
if [ $ANSWER != "yes" ] && [ $ANSWER != "Yes" ] && [ $ANSWER != "YES" ] && [ $ANSWER != "y" ] && [ $ANSWER != "Y" ];
then
    echo
    echo "Install cancel."
    echo
    exit 0
fi

echo
echo "Starting install..."
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
APACHE_CONF_PATH=/etc/httpd/conf
APACHE_MODULE_PATH=/usr/lib/httpd/modules

# Tomcat Path
DEST_TOMCAT_PATH=/usr/local

echo
echo ">> service stop"
/etc/init.d/xvfb stop 2> /dev/null
/etc/init.d/tomcat stop 2> /dev/null
/etc/init.d/httpd stop

echo
echo ">> file copy"
cp -rp $INST_HTDOCS_PATH/* $APACHE_HTDOCS_PATH
cp -ip $INST_ROOT_PATH/massbank.conf $APACHE_HTDOCS_PATH/MassBank
cp -p $INST_ERROR_PATH/40?.html $APACHE_ERROR_PATH
cp -p $INST_ERROR_PATH/50?.html $APACHE_ERROR_PATH
cp -rip $INST_CONF_PATH/* $APACHE_CONF_PATH
cp -ip $INST_CONF_PATH/.htpasswd $APACHE_CONF_PATH
cp -ip $INST_MODULE_PATH/mod_jk.so $APACHE_MODULE_PATH
cp -rp $INST_TOMCAT_PATH $DEST_TOMCAT_PATH
cp -ip $INST_ROOT_PATH/StartupScript/tomcat /etc/init.d
cp -ip $INST_ROOT_PATH/StartupScript/xvfb /etc/init.d
mv $APACHE_ERROR_PATH/noindex.html $APACHE_ERROR_PATH/noindex.html.bak
cp -p $INST_ERROR_PATH/403.html $APACHE_ERROR_PATH/noindex.html

echo
echo ">> change file mode"
chmod a+x $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*.cgi
chmod a+x $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*/*.pl
chmod 755 $APACHE_HTDOCS_PATH/MassBank/script/*.pl
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct.dat
chmod 755 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct_server
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/temp
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch
chmod u+x $APACHE_MODULE_PATH/mod_jk.so
chmod u+x $DEST_TOMCAT_PATH/tomcat/bin/*.sh
chmod a+w $DEST_TOMCAT_PATH/tomcat/temp
chmod u+x /etc/init.d/tomcat
chmod u+x /etc/init.d/xvfb

echo
echo ">> service start"
/etc/init.d/xvfb start
/etc/init.d/mysqld start
/etc/init.d/tomcat start
/etc/init.d/httpd start

echo
echo ">> create database (root authority)"
mysql --user=root -p < $INST_SQL_PATH

echo
echo ">> chkconfig xvfb on"
echo ">> chkconfig mysqld on"
echo ">> chkconfig httpd on"
echo ">> chkconfig tomcat on"
chkconfig xvfb on
chkconfig mysqld on
chkconfig httpd on
chkconfig tomcat on

echo
echo
echo
echo "Done."
echo
echo "Please edit \"FrontServer URL\" of \""$APACHE_HTDOCS_PATH"/MassBank/massbank.conf\" appropriately."
echo
echo
echo
