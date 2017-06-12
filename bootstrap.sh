#!/bin/bash

# install a MassBank Dev machine
export DEBIAN_FRONTEND=noninteractive

# enable support for i386 binaries
sudo dpkg --add-architecture i386

# set up docker repo
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"


# Freshen package index
apt-get update

# Set timezone
ln -sf /usr/share/zoneinfo/Europe/Berlin /etc/localtime
dpkg-reconfigure tzdata

# speed up deployment with one install command
apt-get install -y -q \
docker-ce curl \
libstdc++6:i386 libc6:i386 libgcc1:i386 \
apache2 unzip apache2-utils libcgi-pm-perl \
mariadb-client  \
default-jdk tomcat8 libapache2-mod-jk \
nano joe \
lynx \
build-essential libmysqlclient-dev \
mc xterm mysql-workbench \
r-base-core \
openbabel \
maven

# enable docker support for standard user
usermod -a -G docker ubuntu

#install docker compose
curl -s -L https://github.com/docker/compose/releases/download/1.13.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# prepare mariadb
mkdir /mariadb

# pull up mariadb docker
cd /vagrant
docker-compose up -d


#set up apache httpd
cat >> /etc/apache2/apache2.conf << EOF
ServerName localhost
EOF

cat >>/etc/libapache2-mod-jk/workers.properties <<EOF
# configure jk-status
worker.list=jk-status
worker.jk-status.type=status
worker.jk-status.read_only=true
# configure jk-manager
worker.list=jk-manager
worker.jk-manager.type=status
EOF


# Create unique passwords
#echo $(cat /dev/urandom |  tr -dc _A-Z-a-z-0-9 | head -c${1:-16}) 

#mv robots.txt /var/www/html/
#mv stats.css /var/www/html/
IFS='<';echo $(sed '$i0 0   * * *   www-data    bash /vagrant/sitemap.sh' /etc/crontab) > /etc/crontab
IFS='<';echo $(sed '$i0 0   * * *   www-data    Rscript /vagrant/Statistics.R' /etc/crontab) > /etc/crontab

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

# Apache Path
APACHE_HTDOCS_PATH=/var/www/html
APACHE_ERROR_PATH=/var/www/error

# Tomcat Path
DEST_TOMCAT_PATH=/var/lib/tomcat8


echo
echo ">> service stop"
service tomcat8 stop 
service apache2 stop

# apache file copy
cp -r $INST_HTDOCS_PATH/. $APACHE_HTDOCS_PATH
cp -r $INST_ERROR_PATH/. $APACHE_ERROR_PATH
cp $INST_ROOT_PATH/massbank.conf $APACHE_HTDOCS_PATH/MassBank
chown -R www-data:www-data /var/www/*
find /var/www/ -type d -exec chmod 755 {} \;
find /var/www/ -type f -exec chmod 644 {} \;

#enable required apache modules
a2enmod rewrite
a2enmod authz_groupfile
a2enmod cgid
a2enmod jk

## mbadmin password
htpasswd -b -c /etc/apache2/.htpasswd massbank bird2006

# enable MassBank site
install -m 644 -o root -g root $INST_CONF_PATH/010-a2site-massbank.conf /etc/apache2/sites-available
a2ensite 010-a2site-massbank

echo 
echo "Compile Search.cgi"
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean ; make ) 
cp -p ./Apache/cgi-bin/Search.cgi/Search.cgi $APACHE_HTDOCS_PATH/MassBank/cgi-bin/
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean)

# change file mode
chmod 755 $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*.cgi \
          $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*/*.pl \
          $APACHE_HTDOCS_PATH/MassBank/script/*.pl \
          $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct_server
chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct.dat
install -d -m 777 -o www-data -g www-data $APACHE_HTDOCS_PATH/MassBank/StructureSearch/temp


#exit
# tomcat install webapp
echo "Compile MassBank"
cd MassBank
mvn install
echo "Copy webapp to tomcat"
cp target/MassBank.war /var/lib/tomcat8/webapps/


#cp -rp $INST_TOMCAT_PATH $DEST_TOMCAT_PATH
#cp -rp $INST_TOMCAT_PATH/webapps/* $DEST_TOMCAT_PATH/webapps/

#install -d -m 777 -o tomcat8 -g tomcat8 $DEST_TOMCAT_PATH/temp
#chown -R tomcat8:tomcat8 $DEST_TOMCAT_PATH/webapps/MassBank/temp/
chown -R tomcat8:tomcat8 $APACHE_HTDOCS_PATH/MassBank/DB/
chown -R tomcat8:tomcat8 $APACHE_HTDOCS_PATH/MassBank/massbank.conf


# Configure Tomcat if not already done
if ! grep '^<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />$' $DEST_TOMCAT_PATH/conf/server.xml ; then 
sed -i -e 's#<!-- Define an AJP 1.3 Connector on port 8009 -->#<!-- Define an AJP 1.3 Connector on port 8009 -->\n<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />#' $DEST_TOMCAT_PATH/conf/server.xml    
fi 


echo
echo ">> service start"
service tomcat8 restart 
service apache2 restart

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
