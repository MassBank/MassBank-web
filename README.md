[![Build
Status](https://travis-ci.org/MassBank/MassBank-web.svg?branch=master)](https://travis-ci.org/MassBank/MassBank-web)

# Installation

MassBank needs a MariaDB database running somewhere, a tomcat server for the webapp and the maven build system for installation. This can be provided directly by the hosting system or in docker containers. Although in principle it might be possible to run this webapp on a windows server, we havent tested it and it will not run out of the box.

Besides running MassBank on a server system it is also possible to run it in the integrated tomcat server within eclipse for easy development and debuging. Our development platform is Ubuntu 16.04/18.04.

## Install in eclipse for development

Needed for a working development environment:
1. MySQL database - we use a MariaDB docker container
2. Eclipse Java EE IDE for Web Developers.
3. Local Apache tomcat for eclipse

### Install MariaDB docker
Install and configure [docker environment](https://docs.docker.com/install/linux/docker-ce/ubuntu/) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your user is in the group 'docker'. Create a mariadb data directory `sudo mkdir /mariadb` amd adjust your root password for the database in `compose/mariadb-docker-compose.yml`.

Start the database with `docker-compose -f compose/mariadb-docker-compose.yml up -d` and check with `docker ps` for **massbank_mariadb**. Check database connectivity with `mysql -u root -h 127.0.0.1 -p`.

### Install massbank.conf
Adjust settings in massbank.conf and copy it to /etc. 

### Import project into Eclipse
Download and install the [Eclipse IDE for Java EE Developers](https://www.eclipse.org/downloads). Start eclipse and import the folder 'MassBank-Project' from this repo: File -> Import -> Existing Maven Project -> Select this repo for import. 
Download [Apache Tomcat](http://tomcat.apache.org/) and extract it to your preferred folder.
Then create a Tomcat server in eclipse. Please follow the instructions [here](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftomcat.html) and [here](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftwtomprf.html). 

### Populate database
Clone the data from [MassBank-data](https://github.com/MassBank/MassBank-data) to a local directory and make sure the 'DataRootPath' in 'massbank.conf' is correctly set to your data repo. Run '/MassBank-lib/src/main/java/massbank/RefreshDatabase.java' from your data repo to populate your database.

### Run MassBank webapp
Run the MassBank-project on the Tomcat server and access MassBank at [http://localhost:8080/MassBank/](http://localhost:8080/MassBank/).


## Install as server system with docker/multiple instances on one server possible
A docker compose file named 'compose/full-service.yaml' is provided to install all required container to run MassBank as a server system. The only requirement are the  [docker environment](https://docs.docker.com/install/linux/docker-ce/ubuntu/) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your user is in the group 'docker'. A setup script called `install.sh` is provided to make deployment easy.

### Install one instance with `docker-compose`
To set up the MassBank webapp the source of the webapp and the data repository are needed. Get them with:
```
git clone git@github.com:MassBank/MassBank-web.git
git clone git@github.com:MassBank/MassBank-data.git
```
If you want to use the `install.sh`-script MassBank-web and MassBank-data need to be in the same directory. Usage is:
```
./install.sh 
Usage: install.sh <operation> <instance>
         <operation> ... start, stop or refresh
         <instance>  ... 0 to 9
```
`install.sh` supports three operations:
- start a MassBank service (incl. the required database)
- stop and remove a MassBank service
- refresh the content of the MassBank
- instance is used to seperate the networks of different MassBank servers and determines the port of the service, 0 serves at 8080, 1 serves at 8081 ...

Several networks with independent instances of MassBank are possible with this deployment method. To deploy to standard port 80 use `./install.sh start 0`. After 30 min you can find a MassBank instance at http://\<your-ip\>:8080/MassBank. To upload new content update the data repo (needs to be in `../MassBank-data`) and issue `./install.sh refresh 0`. To remove this MassBank use `./install.sh stop 0`.

### Install multiple instance with `docker-compose`
The deployment uses the codebase in MassBank-web and the data in MassBank-data in the same parent directory for one deployment. Because the data in `MassBank-data` is not only used for deployment, but also for serving it is recomended to have a separate data repo for each instance of MassBank, e.g.:
```
|
|-MassBank1
|  |
|  |-MassBank-web
|  |-MassBank-data
|
|-MassBank2
   |
   |-MassBank-web 
   |-MassBank-data
```
With this layout its easy to have several instances with differnet codebase / data on the same server. To start MassBank1 go to MassBank1/MassBank-web and use `./install.sh start 1`. Your server will be at http://\<your-ip\>:8081/MassBank. To start MassBank2 go to MassBank2/MassBank-web and use `./install.sh start 2`. Your server will be at http://\<your-ip\>:8082/MassBank. The second parameter of the install script is used to seperate the different instances by using different subnets and ports on the local machine. 
 

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


