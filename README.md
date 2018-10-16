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
Install and configure [docker environment](https://docs.docker.com/install/linux/docker-ce/ubuntu/) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your system-user is in the group 'docker'. Create a mariadb data directory `sudo mkdir /mariadb` amd adjust your root password for the database in `mariadb-docker-compose.yml`.

Start the database with `docker-compose -f mariadb-docker-compose.yml up -d` and check with `docker ps` for **massbank_mariadb**. Check database connectivity with `mysql -u root -h 127.0.0.1 -p`.

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


