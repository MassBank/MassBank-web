#!/bin/bash

# install a MassBank Dev machine
export DEBIAN_FRONTEND=noninteractive

# Freshen package index
apt-get update

# Set timezone
ln -sf /usr/share/zoneinfo/Europe/Berlin /etc/localtime
dpkg-reconfigure tzdata

# install Apache
apt-get install -y apache2 unzip apache2-utils

cat >> /etc/apache2/apache2.conf << EOF
ServerName localhost
EOF

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

# enable required apache modules
a2enmod rewrite
a2enmod cgi


# install mysql and perl clients
apt-get install -y mariadb-server mariadb-client libdbd-mysql-perl

# ATTENTION: CUSTOMISE THE MYSQL PASSWORD FOR YOUR OWN INSTALLATION !!!

cat > mariadb_secure.sh << EOF
#!/bin/bash

apt-get install -y expect

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
apt-get -y purge --auto-remove expect
EOF

chmod +x mariadb_secure.sh && ./mariadb_secure.sh && rm mariadb_secure.sh

# clean environment
#rm -rf /var/www
#rm -rf /vagrant/source-mx
#rm -rf /vagrant/source-mx-feeds
#ln -fs /vagrant /var/www

# install tomcat
apt-get install -y default-jre tomcat7 libapache2-mod-jk

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
apt-get install -y mc xterm mysql-workbench

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

sudo apt-get install -y r-base-core
sudo apt-get install -y openbabel

bash ./install-ubuntu.sh
#sudo mv robots.txt /var/www/html/
#sudo mv stats.css /var/www/html/
IFS='<';echo $(sed '$i0 0   * * *   www-data    bash /vagrant/sitemap.sh' /etc/crontab) > /etc/crontab
IFS='<';echo $(sed '$i0 0   * * *   www-data    Rscript /vagrant/Statistics.R' /etc/crontab) > /etc/crontab
