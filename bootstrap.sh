#!/bin/bash

# install a MassBank Dev machine
export DEBIAN_FRONTEND=noninteractive

# get universe apt-get repo
sudo add-apt-repository "deb http://archive.ubuntu.com/ubuntu $(lsb_release -sc) universe"

# Freshen package index
apt-get update
apt-get upgrade

# support the precompiled struct_server
sudo dpkg --add-architecture i386


apt-get install -y libgcc1:i386 libstdc++6:i386 libc6-i386 lib32stdc++6

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

cat >> /etc/apache2/apache2.conf << EOF
ServerName localhost
EOF

# enable required apache modules
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
apt-get install -y mc xterm mysql-admin mysql-workbench

# download latest version of MassBank
git clone https://github.com/ermueller/MassBank-web

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
sudo mv robots.txt /var/www/html/
sudo mv stats.css /var/www/html/
IFS='<';echo $(sed '$i0 0   * * *   root    bash /vagrant/sitemap.sh' /etc/crontab) > /etc/crontab
IFS='<';echo $(sed '$i0 0   * * *   root    Rscript /vagrant/Statistics.R' /etc/crontab) > /etc/crontab
