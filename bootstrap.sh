#!/bin/bash

# check for user and password environmental variables
if [ -z "$MBUSERNAME" ]; then
 echo "Please set the environment variables MBUSERNAME and PASSWORD for your site before running"
 echo "this script. On linux use a command like this:"
 echo "MBUSERNAME=massbankuser PASSWORD=massbankpassword vagrant up"
 echo "On windows use a command like this:"
 echo "set MBUSERNAME=massbankuser & PASSWORD=massbankpassword & vagrant up"
 exit 1
fi  

if [ -z "$PASSWORD" ]; then
 echo "Please set the environment variables MBUSERNAME and PASSWORD for your site before running"
 echo "this script. On linux use a command like this:"
 echo "MBUSERNAME=massbankuser PASSWORD=massbankpassword vagrant up"
 echo "On windows use a command like this:"
 echo "set MBUSERNAME=massbankuser & PASSWORD=massbankpassword & vagrant up"
 exit 1
fi

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

#cat >>/etc/libapache2-mod-jk/workers.properties <<EOF
## configure jk-status
#worker.list=jk-status
#worker.jk-status.type=status
#worker.jk-status.read_only=true
## configure jk-manager
#worker.list=jk-manager
#worker.jk-manager.type=status
#EOF


echo
echo
echo "*********************************************"
echo "* MassBank System Installer                 *"
echo "*  version 1.8.1                            *"
echo "*********************************************"
echo

# Install Files Path
INST_ROOT_PATH=./modules
INST_HTDOCS_PATH=$INST_ROOT_PATH/apache/html
INST_ERROR_PATH=$INST_ROOT_PATH/apache/error
INST_CONF_PATH=$INST_ROOT_PATH/apache/conf
INST_TOMCAT_PATH=$INST_ROOT_PATH/tomcat
INST_SQL_PATH=$INST_ROOT_PATH/sql

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
chown -R www-data:www-data /var/www/*

#enable required apache modules
a2enmod rewrite
a2enmod authz_groupfile
a2enmod cgid
a2enmod jk

## mbadmin password
htpasswd -b -c /etc/apache2/.htpasswd $MBUSERNAME $PASSWORD



# enable MassBank site
install -m 644 -o root -g root $INST_CONF_PATH/010-a2site-massbank.conf /etc/apache2/sites-available
a2ensite 010-a2site-massbank

echo 
echo "Compile Search.cgi"
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean ; make ) 
cp -p ./Apache/cgi-bin/Search.cgi/Search.cgi $APACHE_HTDOCS_PATH/MassBank/cgi-bin/
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean)

# change file mode
#chmod 755 $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*.cgi \
#          $APACHE_HTDOCS_PATH/MassBank/cgi-bin/*/*.pl \
#          $APACHE_HTDOCS_PATH/MassBank/script/*.pl \
#          $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct_server
#chmod 777 $APACHE_HTDOCS_PATH/MassBank/StructureSearch/struct.dat
#install -d -m 777 -o www-data -g www-data $APACHE_HTDOCS_PATH/MassBank/StructureSearch/temp


# tomcat install webapp
echo "Compile MassBank"
cd MassBank
mvn -q install
echo "Copy webapp to tomcat"
cp target/MassBank.war /var/lib/tomcat8/webapps/
cd ..

# add tomcat folders
TOMCAT_SHARE_PATH=/usr/share/tomcat8
TOMCAT_CACHE_PATH=/var/cache/tomcat8
mkdir $TOMCAT_SHARE_PATH/common
mkdir $TOMCAT_SHARE_PATH/common/classes
mkdir $TOMCAT_SHARE_PATH/server
mkdir $TOMCAT_SHARE_PATH/server/classes
mkdir $TOMCAT_SHARE_PATH/shared
mkdir $TOMCAT_SHARE_PATH/shared/classes
mkdir $TOMCAT_CACHE_PATH/temp

# Deploy permissions to tomcat
chown -R tomcat8:tomcat8 $TOMCAT_SHARE_PATH
find $TOMCAT_SHARE_PATH -type d -exec chmod 755 {} \;
find $TOMCAT_SHARE_PATH -type f -exec chmod 644 {} \;
find $TOMCAT_SHARE_PATH/bin -name "*.sh" -type f -exec chmod 755 {} \;

chown -R tomcat8:tomcat8 $APACHE_HTDOCS_PATH/MassBank/DB/
chown -R tomcat8:tomcat8 $APACHE_HTDOCS_PATH/MassBank/massbank.conf

chown -R tomcat8:tomcat8 $TOMCAT_CACHE_PATH/temp
find $TOMCAT_CACHE_PATH -type d -exec chmod 755 {} \;

# Configure Tomcat if not already done
if ! grep '^<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />$' $DEST_TOMCAT_PATH/conf/server.xml ; then 
sed -i -e 's#<!-- Define an AJP 1.3 Connector on port 8009 -->#<!-- Define an AJP 1.3 Connector on port 8009 -->\n<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />#' $DEST_TOMCAT_PATH/conf/server.xml    
fi 


echo
echo ">> service start"
service tomcat8 restart 
service apache2 restart

apt-get -y install \
php php-curl php-gd \
php-mbstring php-mysql \
libapache2-mod-php php-mcrypt \
php-zip php-json php-opcache php-xml \
mcrypt

wget -qO- https://builds.piwik.org/latest.tar.gz | tar xz -C $APACHE_HTDOCS_PATH
cp -f $INST_CONF_PATH/config.ini.php $APACHE_HTDOCS_PATH/piwik/config/
cp -f $INST_CONF_PATH/global.ini.php $APACHE_HTDOCS_PATH/piwik/config/
chown -R www-data:www-data $APACHE_HTDOCS_PATH/piwik

rm $APACHE_HTDOCS_PATH/How\ to\ install\ Piwik.html
rm -Rf $APACHE_HTDOCS_PATH/piwik/plugins/Morpheus/icons/submodules







# docker exec -i vagrant_mariadb_1 sh -c 'mysql -u bird -pbird2006' < $INST_SQL_PATH/piwik_create_db.sql

# echo
# echo "Please run \"192.168.35.18/piwik\" to configure Piwik."

# Deploy permissions
find $APACHE_HTDOCS_PATH -type d -exec chmod 755 {} \;
find $APACHE_HTDOCS_PATH -type f -exec chmod 644 {} \;
find $APACHE_HTDOCS_PATH/MassBank -name "*.cgi" -type f -exec chmod 755 {} \;

service apache2 restart

# Append curation scripts to crontab
IFS='<';echo $(sed '$i0 0   * * *   www-data    bash /var/www/html/MassBank/script/Sitemap.sh' /etc/crontab) > /etc/crontab
IFS='<';echo $(sed '$i0 0   * * *   www-data    Rscript /var/www/html/MassBank/script/Statistics.R' /etc/crontab) > /etc/crontab 


echo
echo
echo
echo "Done."
echo
echo "Please edit \"FrontServer URL\" of \""$APACHE_HTDOCS_PATH"/MassBank/massbank.conf\" appropriately."
echo
echo
echo
