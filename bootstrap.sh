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
default-jre tomcat7 libapache2-mod-jk \
nano joe \
lynx \
build-essential libmysqlclient-dev \
mc xterm mysql-workbench \
r-base-core \
openbabel

# enable docker support for standard user
usermod -a -G docker ubuntu

#install docker compose
curl -L https://github.com/docker/compose/releases/download/1.13.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
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

# enable required apache modules
a2enmod rewrite
a2enmod cgi

cat >>/etc/libapache2-mod-jk/workers.properties <<EOF
# configure jk-status
worker.list=jk-status
worker.jk-status.type=status
worker.jk-status.read_only=true
# configure jk-manager
worker.list=jk-manager
worker.jk-manager.type=status
EOF


# Compile and Copy MassBank components

# restart Apache
service apache2 restart

## Create unique passwords
echo $(cat /dev/urandom |  tr -dc _A-Z-a-z-0-9 | head -c${1:-16}) 


#cd MassBank-web

## During development: change into temporary branch
#git checkout updateFromCVS

bash ./install-ubuntu.sh
#sudo mv robots.txt /var/www/html/
#sudo mv stats.css /var/www/html/
IFS='<';echo $(sed '$i0 0   * * *   www-data    bash /vagrant/sitemap.sh' /etc/crontab) > /etc/crontab
IFS='<';echo $(sed '$i0 0   * * *   www-data    Rscript /vagrant/Statistics.R' /etc/crontab) > /etc/crontab
