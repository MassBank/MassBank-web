#!/bin/bash

# install a MassBank Dev machine
export DEBIAN_FRONTEND=noninteractive

# get universe apt-get repo
sudo add-apt-repository "deb http://archive.ubuntu.com/ubuntu $(lsb_release -sc) universe"

# support the precompiled struct_server
sudo dpkg --add-architecture i386

apt-get install -y libgcc1:i386 libstdc++6:i386 libc6-i386 lib32stdc++6

# Freshen package index
apt-get update

# Set timezone
echo "Europe/Berlin" | tee /etc/timezone
dpkg-reconfigure --frontend noninteractive tzdata

# set locale
export LANGUAGE=en_US.UTF-8
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

locale-gen en_US.UTF-8 de_DE.UTF-8
dpkg-reconfigure locales

# virtual X
apt-get install -y xvfb openjdk-7-jre

# install Apache
apt-get install -y apache2 unzip apache2-utils

a2enmod rewrite #enable mod-rewrite
cat > /etc/apache2/sites-available/000-default.conf << EOF
<VirtualHost *:80>
        ServerName localhost
        ServerAdmin webmaster@localhost
        DocumentRoot /var/www/html
        ErrorLog ${APACHE_LOG_DIR}/error.log
        CustomLog ${APACHE_LOG_DIR}/access.log combined
        <Directory "/var/www/html">
    		AllowOverride All
		</Directory>
</VirtualHost>
EOF

## Deploys the MB configuration for apache2
## Configure Deny / Allow before public deployment

cat > /etc/apache2/sites-available/010-a2site-massbank.conf << EOF

#***************************************************
# For MassBank
#***************************************************
<Directory "/var/www/html/MassBank">
        AllowOverride All
        Options -Indexes
		# Deny from all
		# Allow from 127.0.0.1
</Directory>
ScriptAlias /MassBank/cgi-bin/ "/var/www/html/MassBank/cgi-bin/"

<Directory "/var/www/html/MassBank/cgi-bin">
        Options +ExecCGI
        AddHandler cgi-script .cgi
</Directory>
<Location ~ "/MassBankEnv">
        Order deny,allow
        # Deny from all
        # Allow from 127.0.0.1
</Location>

<Location ~ "/mbadmin">
    AuthType Basic
    AuthName "MassBank Administration Tool"
    AuthUserFile /etc/apache2/.htpasswd
    AuthGroupFile /dev/null
    require valid-user
    # Deny from all
    # Allow from 127.0.0.1
</Location>

<Directory "/var/www/MassBank/SVN/OpenData">
	AllowOverride All
    Order allow,deny
    Allow from all
</Directory>
EOF	

#***************************************************
# Tomcat Connection
#***************************************************
JkWorkerProperty worker.list=tomcat
JkWorkerProperty worker.tomcat.type=ajp13
JkWorkerProperty worker.tomcat.host=localhost
#JkWorkerProperty worker.tomcat.host=massbank.eu
JkWorkerProperty worker.tomcat.port=8009

JkLogFile /var/log/apache2/mod_jk.log

JkMountCopy All

JkMount /MassBank/jsp tomcat
JkMount /MassBank/jsp/* tomcat
JkMount /MassBank/temp tomcat
JkMount /MassBank/temp/* tomcat
JkMount /MassBank/MultiDispatcher tomcat
JkMount /MassBank/MultiDispatcher/* tomcat
JkMount /MassBank/MassBankEnv tomcat
JkMount /MassBank/MassBankEnv/* tomcat
JkMount /MassBank/mbadmin tomcat
JkMount /MassBank/mbadmin/* tomcat
JkMount /api tomcat
JkMount /api/* tomcat

JkMount /MassBank/ServerMonitor tomcat
JkMount /MassBank/ServerMonitor/* tomcat
JkMount /MassBank/pserver tomcat
JkMount /MassBank/pserver/* tomcat
EOF

cat >> /etc/apache2/apache2.conf << EOF
ServerName localhost
EOF

# enable required apache modules
a2ensite 010-a2site-massbank.conf
a2enmod rewrite
a2enmod cgi


# install mysql and java and perl clients
# ATTENTION: CUSTOMISE THE MYSQL PASSWORD FOR YOUR OWN INSTALLATION !!!
debconf-set-selections <<< 'mysql-server mysql-server/root_password password bird2006'
debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password bird2006'

apt-get install -y mysql-server mysql-client libdbd-mysql-perl

# clean environment
#rm -rf /var/www
#rm -rf /vagrant/source-mx
#rm -rf /vagrant/source-mx-feeds
#ln -fs /vagrant /var/www

# install tomcat
apt-get install -y tomcat7 libapache2-mod-jk

cat >>/etc/libapache2-mod-jk/workers.properties <<EOF
# configure jk-status
worker.list=jk-status
worker.jk-status.type=status
worker.jk-status.read_only=true
# configure jk-manager
worker.list=jk-manager
worker.jk-manager.type=status
EOF

# install GIT
apt-get install -y git-core

# install editors
apt-get install -y nano joe

# install lynx
apt-get install -y lynx

# Compiler to install Search.cgi
apt-get install -y build-essential libmysqlclient-dev

# Install European MassBank specific tools
# apt-get install -y mc xterm mysql-workbench

# download latest version of MassBank
git clone https://github.com/tsufz/MassBank-web

# Compile and Copy MassBank components

# restart Apache
service apache2 restart

## Create unique passwords
echo $(cat /dev/urandom |  tr -dc _A-Z-a-z-0-9 | head -c${1:-16}) 

cd MassBank-web

## During development: change into temporary branch
#git checkout updateFromCVS

sudo apt-get install -y r-base-core
sudo apt-get install -y openbabel

bash ./install-ubuntu.sh
# bash ./installChemdoodle.sh
sudo cp -p robots.txt /var/www/html/
sudo cp -p stats.css /var/www/html/MassBank/css
sudo mkdir -p /mbscripts
sudo cp -p sitemap.sh /mbscripts/
sudo cp -p Statistics.R /mbscripts/
sudo chown -R www-data.www-data /mbscripts/

IFS='<';echo $(sed '$i0 0   * * *   www-data    bash /mbscripts/sitemap.sh' /etc/crontab) > /etc/crontab
IFS='<';echo $(sed '$i0 0   * * *   www-data   Rscript /mbscsripts/Statistics.R' /etc/crontab) > /etc/crontab
