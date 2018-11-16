[![Build
Status](https://travis-ci.org/MassBank/MassBank-web.svg?branch=master)](https://travis-ci.org/MassBank/MassBank-web)

# Installation

MassBank needs a few dependencies to run. This needs to be provided by the server system. It needs a MariaDB database running somewhere, a tomcat server for the webapp and the maven build system for installation. This can be provided directly by the hosting system or in docker containers. Although in principle it might be possible to run this webapp directly on a windows server, we havent tested it and it will not run out of the box. With the help of docker it can easily be run on Window.

Besides running MassBank on a server system it is also possible to run it in the integrated tomcat server within eclipse for easy development and debuging. Our development platform is Ubuntu 16.04/18.04.

## Install in eclipse for development

Needed for a working development environment:
1. MySQL database - we use a MariaDB docker container
2. Eclipse Java EE IDE for Web Developers.
3. Local Apache tomcat for eclipse

### Install MariaDB
Install and configure [docker environment](https://docs.docker.com/install/linux/docker-ce/ubuntu/) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your user is in the group 'docker'. Create a mariadb data directory `sudo mkdir /mariadb` amd adjust your root password for the database in `mariadb-docker-compose.yml`.

Start the database with `docker-compose -f compose/mariadb-docker-compose.yml up -d` and check with `docker ps` for **massbank_mariadb**. Check database connectivity with `mysql -u root -h 127.0.0.1 -p`.

### Install massbank.conf
Adjust settings in massbank.conf and copy it to /etc. 

### Import project into Eclipse
Download and install the [Eclipse IDE for Java EE Developers](https://www.eclipse.org/downloads). Start eclipse and import the folder 'MassBank-Project' from this repo: File -> Import -> Existing Maven Project -> Select this repo for import. 
Download [Apache Tomcat](http://tomcat.apache.org/) and extract it to your preferred folder.
Then create a Tomcat server in eclipse. Please follow the instructions [here](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftomcat.html) and [here](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftwtomprf.html). 

### Populate database
Clone the data from [MassBank-data](https://github.com/MassBank/MassBank-data) to a local directory and make sure the 'DataRootPath' in 'massbank.conf' is correctly set to your data repo. Run '/MassBank-lib/src/main/java/massbank/RefreshDatabase.java' to populate your database.

### Run MassBank webapp

Run the MassBank-project on the Tomcat server and access MassBank at [http://localhost:8080/MassBank/](http://localhost:8080/MassBank/).


## Install as server system
A docker compose file named 'full-service.yaml' is provided to install all required container to run MassBank as a server system. The only requirement are the  [docker environment](https://docs.docker.com/install/linux/docker-ce/ubuntu/) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your user is in the group 'docker' and create a mariadb data directory `sudo mkdir /mariadb`.

### Prepare database and tomcat server
To set up the MassBank webapp the source of the webapp and the data repository are needed. Get them with:
```
git clone git@github.com:MassBank/MassBank-web.git
git clone git@github.com:MassBank/MassBank-data.git
```
Change to the MassBank-web folder and compile the source with `docker-compose -f compose/full-service.yml run maven mvn clean package -f /project`. Please note: Due to the way docker works the compiled files within `MassBank-Project` folder are owned by `root`. They can be removed with `docker-compose -f compose/full-service.yml run maven mvn clean -f /project`. Other option for compilation would be to install Maven and a JDK and run `mvn package`inside the `MassBank-Project` folder.

With the compiled war-files the service container can be pulled up. 
Run: 
```
docker-compose -f compose/full-service.yml up -d mariadb
docker-compose -f compose/full-service.yml up -d tomcat
```
Now the webapp should be visible at `http://hostname/MassBank`

### Update database content
After creation of the database server the content is missing. The database can only be accessed from a host within the service network inside docker. The `RefreshDatabase` application needs to run from a docker container.
```
docker-compose -f compose/full-service.yml run maven /project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1/bin/RefreshDatabase
```


## Install as server system within vagrant
We provide a vagrant setup to automate the installation of a complete virtual server. Required are Vagrant and Virtualbox. Please follow the instructions on [https://www.vagrantup.com/docs/installation/](https://www.vagrantup.com/docs/installation/) and [https://www.virtualbox.org/wiki/Linux_Downloads](https://www.virtualbox.org/wiki/Linux_Downloads).

Check out the MassBank-web project and the MassBank-data project(or fork on github first and clone from your own repository).


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

### Fedora 22 Workstation (!Not tested with latest MassBank!)

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

### Windows 10 64 bit Professional

With regards to [this](https://stackoverflow.com/questions/37955942/vagrant-up-vboxmanage-exe-error-vt-x-is-not-available-verr-vmx-no-vmx-code] stackoverflow post, Virtual Box has troubles with enabled Microsoft Hyper-V environment. Thus disable the Hyper-V accordingly.

Then follow the instructions for Windows 7 64 bit below and consider also the general instructions above.

### Windows 7 64 bit (!Not tested for other Windows versions!)
Install Virtualbox for Windows
https://www.virtualbox.org/wiki/Downloads

Install Vagrant for Windows
https://www.vagrantup.com/downloads.html

Install Git for Windows
https://git-scm.com/download/win

Clone the repo to your favorite folder

```
cmd
cd your_favorite_folder
git clone https://github.com/MassBank/MassBank-web
```

Install the Tortoise Git client if you don't feel comfortable with the command line tool
https://tortoisegit.org/download/

Clone the repo by right mouse click and click to "Git clone" to your favorite folder 
Source: https://github.com/MassBank/MassBank-web

Start a commandline window and change to the folder with the repo:
```
cd your_favorite_folder\MassBank-web
vagrant up
```
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

Import the ova file to your VMWare environment and change the settings if necessary using the console



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


