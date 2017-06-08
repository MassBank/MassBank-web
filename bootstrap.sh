#!/bin/bash

# install a MassBank Dev machine
export DEBIAN_FRONTEND=noninteractive

# Freshen package index
apt-get update

# Set timezone
ln -sf /usr/share/zoneinfo/Europe/Berlin /etc/localtime
dpkg-reconfigure tzdata

# speed up deployment with one install command
apt-get install -y -q \
apache2 unzip apache2-utils \
mariadb-server mariadb-client libdbd-mysql-perl \
default-jre tomcat7 libapache2-mod-jk \
git-core \
nano joe \
lynx \
build-essential libmysqlclient-dev \
mc xterm mysql-workbench \
r-base-core \
openbabel

cat >> /etc/apache2/apache2.conf << EOF
ServerName localhost
EOF

# enable required apache modules
a2enmod rewrite
a2enmod cgi

# ATTENTION: CUSTOMISE THE MYSQL PASSWORD FOR YOUR OWN INSTALLATION !!!

cat > mariadb_secure.sh << EOF
#!/bin/bash

apt-get install -y -q expect

MYSQL_ROOT_PASSWORD=bird2006

SECURE_MYSQL=\$(expect -c "
set timeout 10
spawn mysql_secure_installation

expect \"Enter current password for root (enter for none):\"
send \"\r\"

expect \"Set root password?\"
send \"y\r\"

expect \"New password:\"
send \"\MYSQL_ROOT_PASSWORD\r\"

expect \"Re-enter new password:\"
send \"\MYSQL_ROOT_PASSWORD\r\"

expect \"Remove anonymous users?\"
send \"y\r\"

expect \"Disallow root login remotely?\"
send \"y\r\"

expect \"Remove test database and access to it?\"
send \"y\r\"

expect \"Reload privilege tables now?\"
send \"y\r\"

expect eof
")

echo "\$SECURE_MYSQL"
apt-get -y -q purge --auto-remove expect
EOF

chmod +x mariadb_secure.sh && ./mariadb_secure.sh && rm mariadb_secure.sh

# clean environment
#rm -rf /var/www
#rm -rf /vagrant/source-mx
#rm -rf /vagrant/source-mx-feeds
#ln -fs /vagrant /var/www

cat >>/etc/libapache2-mod-jk/workers.properties <<EOF
# configure jk-status
worker.list=jk-status
worker.jk-status.type=status
worker.jk-status.read_only=true
# configure jk-manager
worker.list=jk-manager
worker.jk-manager.type=status
EOF

# download latest version of MassBank
git clone https://github.com/MassBank/MassBank-web

# Compile and Copy MassBank components

# restart Apache
service apache2 restart

## Create unique passwords
echo $(cat /dev/urandom |  tr -dc _A-Z-a-z-0-9 | head -c${1:-16}) 


cd MassBank-web

## During development: change into temporary branch
#git checkout updateFromCVS

bash ./install-ubuntu.sh
#sudo mv robots.txt /var/www/html/
#sudo mv stats.css /var/www/html/
IFS='<';echo $(sed '$i0 0   * * *   www-data    bash /vagrant/sitemap.sh' /etc/crontab) > /etc/crontab
IFS='<';echo $(sed '$i0 0   * * *   www-data    Rscript /vagrant/Statistics.R' /etc/crontab) > /etc/crontab
