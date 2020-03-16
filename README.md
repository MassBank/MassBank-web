[![Build
Status](https://travis-ci.org/MassBank/MassBank-web.svg?branch=master)](https://travis-ci.org/MassBank/MassBank-web)

# Installation

MassBank needs a MariaDB database running somewhere, a tomcat server for the webapp and the maven build system for installation. This can be provided directly by the hosting system or in docker containers. Although in principle it might be possible to run this webapp on a windows server, we haven't tested it and it will not run out of the box.

Besides running MassBank on a server system it is also possible to run it in the integrated tomcat server within eclipse for easy development and debugging. Our development platform is Ubuntu 16.04/18.04.

## Install in eclipse for development

Needed for a working development environment:
1. MySQL database - we use a MariaDB docker container
2. Eclipse Java EE IDE for Web Developers.
3. Local Apache tomcat for eclipse

### Install MariaDB docker
Install and configure [docker environment](https://docs.docker.com/install/linux/docker-ce/ubuntu/) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your user is in the group 'docker'. Create a mariadb data directory `sudo mkdir /mariadb` and adjust your root password for the database in `compose/mariadb-docker-compose.yml`.

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
A docker compose file named `compose/full-service.yaml` is provided to install all required container to run MassBank as a server system. The only requirement is the  [docker environment](https://docs.docker.com/install/linux/docker-ce/ubuntu/) including [docker-compose](https://docs.docker.com/compose/install/). Make sure that your user is in the group 'docker'. A setup script called `install.sh` is provided to make deployment easy.

### Install one instance with `docker-compose`
To set up the MassBank webapp the source of the webapp and the data repository are needed. Get them with:
```
git clone git@github.com:MassBank/MassBank-web.git
git clone git@github.com:MassBank/MassBank-data.git
```
If you want to use the `install.sh`-script from MassBank-web both repositories need to be in the same directory. Usage is:
```
./install.sh 
Usage: install.sh <operation> <instance>
         <operation> ... start, stop, deploy or refresh
         <instance>  ... 0 to 9
```
`install.sh` supports four operations:
- start a MassBank service (incl. the required database)
- stop and remove a MassBank service
- refresh the content of the MassBank
- deploy the code to the tomcat container
- instance is used to separate the networks of different MassBank servers and determines the port of the service, 0 serves at 8080, 1 serves at 8081 ...

Several networks with independent instances of MassBank are possible with this deployment method. To deploy to standard port 8080 use `./install.sh start 0`. After 30 min you can find a MassBank instance at http://\<your-ip\>:8080/MassBank. To upload new content update the data repo (needs to be in `../MassBank-data`) and issue `./install.sh refresh 0`. To remove this MassBank use `./install.sh stop 0`.

### Install multiple instance with `docker-compose`
The deployment uses the codebase in MassBank-web and the data in MassBank-data in the same parent directory for one deployment. Because the data in `MassBank-data` is not only used for deployment, but also for serving it is recommended to have a separate data repo for each instance of MassBank, e.g.:
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
With this layout its easy to have several instances with different codebase / data on the same server. To start MassBank1 go to MassBank1/MassBank-web and use `./install.sh start 1`. Your server will be at http://\<your-ip\>:8081/MassBank. To start MassBank2 go to MassBank2/MassBank-web and use `./install.sh start 2`. Your server will be at http://\<your-ip\>:8082/MassBank. The second parameter of the install script is used to separate the different instances by using different subnets and ports on the local machine. 

## Install as server system with Vagrant
A `Vagrantfile` is provided for easy installation of a MassBank-server. This config creates a Ubuntu VM with IP `192.168.35.18`. Inside this VM the `docker-compose` mechanism as described above is used to create a MassBank-server on port 8080. Additionally a Apache2 http server is installed as reverse proxy. The config can be found in `conf/apache2`. Please modify if needed. The final MassBank site will be available at [https://192.168.35.18/MassBank](https://192.168.35.18/MassBank/). The installation uses the MassBank-data repository from `../MassBank-data`. You can modify the location in the Vagrantfile. The installation can be started with `vagrant up`.
 
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

# Release strategy

## Main branches
We use two main branches, `master` and `dev`. All development should happen in `dev` and we define every commit to `master` to be a release. When the source code in the `dev` branch reaches a stable point and is ready to be released, all of the changes should be merged back into `master` somehow and then tagged with a release number. How this is done in detail will be discussed further on. To use all of the command lines below the [github/hub](https://docs.docker.com/install/linux/docker-ce/ubuntu/) tool is required.


## Supporting branches
The different types of branches we may use are:
* Feature branches
* Release branches
* Hotfix branches

### Feature branches
Branch off from: `dev`

Must merge back into: `dev`

Branch naming: anything except `master`, `dev`, `release-*` or `hotfix-*`

Feature branches are used to develop new features.

#### Creating a feature branch
```
$ git checkout -b myfeature dev
Switched to a new branch "myfeature"
```
#### Incorporating a finished feature on dev
```
$ git checkout dev
Switched to branch 'develop'
$ git merge --no-ff myfeature
(Summary of changes)
$ git branch -d myfeature
Deleted branch myfeature
$ git push origin dev
```

### Release branches
Branch off from: `dev`

Must merge back into: `dev` and `master`

Branch naming: `release-*`

Release branches support preparation of a new production release. They allow for minor bug fixes and preparing the version number for a release. It is exactly at the start of a release branch that the upcoming release gets assigned a version number.

#### Creating a release branch
```
$ git checkout -b release-2.1 dev
Switched to a new branch "release-2.1"
$ ./bump-version.sh 2.1
Files modified successfully, version bumped to 2.1.
git commit -a -m "Bumped version number to 2.1"
[release-2.1 74d9424] Bumped version number to 2.1
$ git push --set-upstream origin release-2.1
```
#### Finishing a release branch
When the state of the release branch is ready to become a real release, the release branch is merged into `master` with a pull request and tagged for easy future reference.

```
$ hub pull-request -m 'Release version 2.1'
```
Wait for all checks to finish. Now the release can be merged to `master`. 
```
$ git checkout master
$ git merge --no-ff release-2.1
```
There might be conflicts. Resolve and commit them.
```
$ git push origin master
$ git tag -a 2.1 -m 'Release version 2.1'
$ git push origin 2.1
```
If there were any changes in the release branch we need to merge them back to `dev`.

```
$ git checkout dev
Switched to branch 'dev'
$ git merge --no-ff release-2.1
Merge made by recursive.
(Summary of changes)
```
This may well lead to merge conflicts, which needs to be fixed. If so, fix it and commit.

Now we are done and the release branch may be removed.
```
$ git branch -d release-2.1
Deleted branch release-2.1 (was ff452fe).
```

### Hotfix branches
Branch off from: `master`

Must merge back into: `dev` and `master`

Branch naming: `hotfix-*`

Hotfix branches are very much like release branches in that they are also meant to prepare for a new production release. They arise from the necessity to act immediately upon an undesired state of a live production version.

#### Creating a hotfix branch
```
$ git checkout -b hotfix-2.1.1 master
Switched to a new branch "hotfix-2.1.1"
$ ./bump-version.sh 2.1.1
Files modified successfully, version bumped to 2.1.1.
git commit -a -m "Bumped version number to 2.1.1"
[hotfix-2.1.1 74d9424] Bumped version number to 2.1.1

```
Then, fix the bug and commit the fix in one or more separate commits.

#### Finishing a hotfix branch
When finished, the bugfix needs to be merged back into `master`, but also needs to be merged back into `dev`.
First, update `master` and tag the release.
```
$ hub pull-request -m 'Release version 2.1.1'
```
Wait for all checks to finish. Now the release can be merged to `master`. 
```
$ git checkout master
$ git merge --no-ff hotfix-2.1.1
$ git push origin master
$ git tag -a 2.1.1 -m 'Release version 2.1.1'
$ git push origin 2.1.1
```
Next, include the bugfix in `dev`, too:

```
$ git checkout dev
Switched to branch 'dev'
$ git merge --no-ff hotfix-2.1.1
Merge made by recursive.
(Summary of changes)

```
The one exception to the rule here is that, when a release branch currently exists, the hotfix changes need to be merged into that release branch as well.
Finally, remove the temporary branch:

```
$ git branch -d hotfix-2.1.1
Deleted branch hotfix-2.1.1 (was abbe5d6).
```
