#!/bin/bash
echo "**PREPARE BASE SYSTEM**************************************************"
export DEBIAN_FRONTEND=noninteractive

echo "**set up docker repo***************************************************"
#curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
#add-apt-repository \
#   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
#   $(lsb_release -cs) \
#   stable"

echo "**set timezone*********************************************************"
ln -sf /usr/share/zoneinfo/Europe/Berlin /etc/localtime
dpkg-reconfigure tzdata

echo "**freshen package index************************************************"
apt-get update

echo "**install software*****************************************************"
# just a little hack for multiline command with comments
CMD=(
apt-get install -y --no-install-recommends
#docker-ce # docker environment
docker.io
tomcat8 # tomcat8
maven # maven
git
)

"${CMD[@]}"

echo "**enable docker support for standard user******************************"
usermod -a -G docker vagrant

echo "**install docker-compose***********************************************"
curl -sSL https://github.com/docker/compose/releases/download/1.23.1/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

echo "**INSTALL MASSBANK COMPONENTS******************************************"

echo "**pull up mariadb docker container****************************************"
mkdir /mariadb
cd /vagrant 
docker-compose -f compose/mariadb-docker-compose.yml up -d

echo "**fetch data repo******************************************************"
(cd /home/vagrant; \
git clone --depth 1 -q https://github.com/MassBank/MassBank-data.git; \
chown -R vagrant:vagrant MassBank-data) 

echo "**compile MassBank webapp**********************************************"
(cd MassBank-Project; mvn install)
cp MassBank-Project/MassBank/target/MassBank.war /var/lib/tomcat8/webapps/
cp MassBank-Project/api/target/api.war /var/lib/tomcat8/webapps/
cp conf/massbank.conf /etc

echo "**update database******************************************************"
MassBank-Project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1/bin/RefreshDatabase

echo "**RESTARTING ALL SERVER************************************************"

systemctl restart tomcat8



