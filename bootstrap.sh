#!/bin/bash
echo "**PREPARE BASE SYSTEM**************************************************"
export DEBIAN_FRONTEND=noninteractive

echo "**set up docker repo***************************************************"
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"

echo "**set timezone*********************************************************"
ln -sf /usr/share/zoneinfo/Europe/Berlin /etc/localtime
dpkg-reconfigure tzdata

echo "**freshen package index************************************************"
apt-get update

echo "**install software*****************************************************"
# just a little hack for multiline command with comments
CMD=(
apt-get install -y
docker-ce # docker environment
apache2 apache2-utils # apache
default-jdk tomcat8 libapache2-mod-jk # tomcat8
maven # maven
git
)

"${CMD[@]}"

echo "**enable docker support for standard user******************************"
usermod -a -G docker vagrant

echo "**install docker-compose***********************************************"
curl -sSL https://github.com/docker/compose/releases/download/1.20.1/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

echo "**INSTALL MASSBANK COMPONENTS******************************************"

echo "**pull mariadb docker container****************************************"
mkdir /mariadb
cd /vagrant 
docker-compose up -d

echo "**copy apache files****************************************************"
rm -r /var/www/*
cp -r  modules/apache/html /var/www/html
cp -r  modules/apache/error /var/www/error
chown -R www-data:www-data /var/www/*
install -m 644 -o root -g root modules/apache/conf/010-a2site-massbank.conf /etc/apache2/sites-available
a2ensite 010-a2site-massbank

echo "**fetch data repo******************************************************"
(cd /home/vagrant; \
git clone https://github.com/MassBank/MassBank-data.git; \
chown -R vagrant:vagrant MassBank-data; \
ln -s $PWD/MassBank-data /var/www/html/MassBank/DB )

echo "**enable required apache modules***************************************"
a2enmod rewrite
#a2enmod cgid
a2enmod jk

echo "**compile MassBank webapp**********************************************"
(cd MassBank-Project; mvn install)
cp MassBank-Project/MassBank/target/MassBank.war /var/lib/tomcat8/webapps/
cp MassBank-Project/api/target/api.war /var/lib/tomcat8/webapps/
cp massbank.conf /etc

echo "**update database******************************************************"
MassBank-Project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1/bin/RefreshDatabase

echo "**configure Tomcat if not already done*********************************"
if ! grep '^<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />$' /var/lib/tomcat8/conf/server.xml ; then
sed -i -e 's#<!-- Define an AJP 1.3 Connector on port 8009 -->#<!-- Define an AJP 1.3 Connector on port 8009 -->\n<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />#' /var/lib/tomcat8/conf/server.xml
fi

echo "**RESTARTING ALL SERVER************************************************"

systemctl restart apache2
systemctl restart tomcat8



