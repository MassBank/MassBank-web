# Installation

There are two possibilities: Install MassBank in a virtual machine
without major impact on the host computer or install it as "Dynamic Web Module"
in eclipse for development(requires installation of local Apache httpd).

## Virtual Machine Installation
The requirement is the "vagrant" system and Virtualbox. I used the latest vagrant (1.9.5) with Virtualbox 5.1 on 
Ubuntu 16.04. Please follow the instructions on [https://www.vagrantup.com/docs/installation/](https://www.vagrantup.com/docs/installation/) and [https://www.virtualbox.org/wiki/Linux_Downloads](https://www.virtualbox.org/wiki/Linux_Downloads).
Note: 16.04 default vagrant(1.8.1) refuses to work correctly with 16.04 client boxes.

Install [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) on your system if not present. Check out the MassBank-web project(or fork on github first and clone from your own repository), 
which contains 1) The virtual machine initialization files 
`Vagrantfile` and `bootstrap.sh` and 2) the MassBank software 
to be installed inside the virtual machine. 

After cloning the repo, create the virtual machine 
and fire it up in virtualbox. Once everything finished, 
you  should have a running MassBank, so point your browser 
to [http://192.168.35.18/MassBank/](http://192.168.35.18/MassBank/). You can also ssh into 
the virtual machine without password:

```
git clone https://github.com/MassBank/MassBank-web
cd MassBank-web 
vagrant up
vagrant ssh
```

## !Not tested with latest MassBank!
### Fedora 22 Workstation

Install vagrant

```
sudo dnf install vagrant
```
According to [this](https://unix.stackexchange.com/questions/194691/use-virtualbox-provider-by-default-on-fedora-21) StackExchange post, Fedora is not using VirtualBox as a default provider.
When geting the follwoing error:
```
The provider 'libvirt' could not be found, but was requested to
back the machine 'default'. Please use a provider that exists.
```
Execute the these two steps:
```
$ echo "export VAGRANT_DEFAULT_PROVIDER=virtualbox" >> ~/.zshr
$ source ~/.zshrc
```
The vagrant version from the repository (Version 1.7.2) does NOT support Virtualbox Version 5


### Windows 7 64 bit
Install Virtualbox for Windows
https://www.virtualbox.org/wiki/Downloads

Install Vagrant for Windows
https://www.vagrantup.com/downloads.html

Install the Tortoise Git client
https://tortoisegit.org/download/

Clone the repo by right mouse click and click to "Git clone" to your favorite folder 
Source: https://github.com/MassBank/MassBank-web

Start a commandline window and change to the folder with the repo:
```
cd MassBank-web
vagrant up
```

Now the the virtual machine should be available in the Oracle VM VirtualBox manager

Type to connect to the VM
```
vagrant ssh
```
You may also use your favorite terminal software such as Putty. You will find the private key to connect to the VM under
MassBank-web\.vagrant\machines\default\virtualbox

### Transfer to VMWare ESX cluster or VMWare workstation
Start the Oracle VM VirtualBox manager and export the appliance to the Open Virtualisation Format (ova file).
Use the expert mode for individual settings (e.g. the name of the machine) and select the tickbox to create a manifest file.
If the tickbox is checked, the integrity of the appliance will be checked during the import.

Import the ova file to your VMWare environment and change the settings if necessary.

# Install in eclipse for development

There are three parts needed to have a working development environment:
1. MySQL database - we use a MariaDB docker container
2. Apache httpd
3. Local Apache tomcat in eclipse

## Install MariaDB
Install and configure [docker environment](https://store.docker.com/editions/community/docker-ce-server-ubuntu) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your system-user is in the group 'docker'. Create a mariadb data directory `sudo mkdir /mariadb` and issue `docker-compose up -d` in the root directory of this repo. Check with `docker ps` for **massbank_mariadb**. Check database with `mysql -u bird -h 127.0.0.1 -p`.

## Install Apache httpd content
Install dependencies: apache2 libcgi-pm-perl build-essential libmysqlclient-dev libapache2-mod-jk libdbd-mysql-perl mariadb-client.
Install apache httpd and make sure you have no old projects installed.
```
# copy Massbank-web content into folder '/var/www/'
sudo cp -rp modules/apache/error /var/www/
sudo cp -rp modules/apache/html /var/www/

# built Search.cgi and copy to '/var/www/html/MassBank/cgi-bin/'
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean ; make ) 
sudo cp -p ./Apache/cgi-bin/Search.cgi/Search.cgi /var/www/html/MassBank/cgi-bin/
(cd ./Apache/cgi-bin/Search.cgi/ ; make clean)

# configure file system rights
sudo chown -R www-data:www-data /var/www/*
sudo chown -R $USER /var/www/html/MassBank/DB
sudo chown -R $USER /var/www/html/MassBank/massbank.conf
```
Configure apache httpd.
```
# write server-name to apache2.conf
sudo bash -c 'cat >> /etc/apache2/apache2.conf << EOF
ServerName localhost
EOF'

# enable apache sites and modules
sudo bash -c 'install -m 644 -o root -g root modules/apache/conf/010-a2site-massbank.conf /etc/apache2/sites-available
a2ensite 010-a2site-massbank
a2enmod rewrite
a2enmod authz_groupfile
a2enmod cgid
a2enmod jk'

# configure htpasswd for mbadmin
sudo htpasswd -b -c /etc/apache2/.htpasswd massbank bird2006
```
Restart server: `sudo systemctl restart apache2`.

## Import project into Eclipse
Download the [Eclipse IDE for Java EE Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/marsr) with "Maven Integration for Eclipse" and "Maven Integration for WTP" and extract it to your preferred folder. Start eclipse and import the clones github-project 'Massbank-web': File -> Import -> Existing Maven Project -> Select this repo for import. 
Download [Apache Tomcat](http://tomcat.apache.org/) and extract it to your preferred folder.
Then create a Tomcat server in eclipse. Please follow the instructions [here](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftomcat.html) and [here](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftwtomprf.html). Edit '/var/www/html/MassBank/massbank.conf' and set `<MiddleServer URL="http://localhost/MassBank/"/>`. Run the MassBank-project on the Tomcat server and access MassBank at [http://localhost/MassBank/](http://localhost/MassBank/).

## PIWIK log analyser (https://piwik.org/)
The default MassBank server installation includes the PIWIK log analyser. Consider that user tracking has privacy issues.
The default preset follows very strict rules according to http://piwik.org/docs/privacy/ and only the following usage data:

* Site from which the file was requested
* Name of the file
* Date and time of the request
* Volume of data transferred
* Access status (file transferred, file not found)
* Description of the type of web browser used
* IP address of the requesting computer shortened by the last six digits.

The creation of personal user profiles is therefore precluded.

## customised piwik tracking code

The tracking code for piwik is specific for each site and configuration in order to fullfil your local legal and personal requirements!
The tracking code for http://your_server_url/MassBank is called from /var/www/html/MassBank/script/Piwik.js.
Make sure to replace the default code with your customised tracking code. Exclude the script tags.
Make also sure that you customise your superuser if using the default bootstrap.sh!


