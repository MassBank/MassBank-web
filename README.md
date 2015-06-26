Installation
============

The requirement is the "vagrant" system, which you install on ubuntu via
sudo apt-get install vagrant (I used version 1:1.7.2)

Then you check out the MassBank-web project, 
(or fork on github first and clone from your own repository)

which contains 1) The virtual machine initialization files 
Vagrantfile and bootstrap.sh and 2) the MassBank software 
to be installed inside the virtual machine. 

After cloning the repo, the create the virtual machine 
and fire it up in virtualbox. Once everything finished, 
you  should have a running MassBank, so point your browser 
to http://192.168.35.18/MassBank/ You can also ssh into 
the virtual machine without password:

````
git clone https://github.com/MassBank/MassBank-web
cd MassBank-web 
vagrant up
vagrant ssh
````








