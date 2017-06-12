# Installation

There are two possibilities: Install MassBank in a virtual machine
without major impact on the host computer or install it as "Dynamic Web Module"
in eclipse for development(requires installation of local Apache httpd).

## Virtual Machine Installation
The requirement is the "vagrant" system and Virtualbox. I used the latest vagrant (1.9.5) with Virtualbox 5.1 on 
Ubuntu 16.04. Please follow the instructions on [https://www.vagrantup.com/docs/installation/](https://www.vagrantup.com/docs/installation/) and [https://www.virtualbox.org/wiki/Linux_Downloads](https://www.virtualbox.org/wiki/Linux_Downloads).

Check out the MassBank-web project(or fork on github first and clone from your own repository), 
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
Now the the virtual machineshould be available in the Oracle VM VirtualBox manager

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
Get your docker environment incl. docker-compose ready. Create a mariadb data directory `mkdir /mariadb` and issue `docker-compose up -d` in the root directory of this repo. Check with `docker ps -a` for **massbankweb_mariadb_1**. Check database with `mysql -u bird -h 127.0.0.1 -p`.

## Install Apache httpd content
Install apache httpd and make sure you have no old projects installed.
```
sudo cp -rp modules/apache/error /var/www/
sudo cp -rp modules/apache/html /var/www/
sudo chown -R www-data:www-data /var/www/*
```

## Import project into Eclipse
File-> Import -> Existing Maven Project
Select the `MassBank` folder from this repo for import.
