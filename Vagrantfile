Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"

  config.vm.provision "docker",
    images: ["mariadb:latest", "tomcat:latest" , "maven:latest"]
  
  config.vm.synced_folder '.', '/massbank/MassBank-web'
  config.vm.synced_folder '../MassBank-data', '/massbank/MassBank-data'

  config.vm.provision "shell", inline: <<-SHELL
    hostnamectl set-hostname massbank
    curl -s -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    cd /massbank/MassBank-web
    ./install.sh start 0
    apt install apache2 -y
    a2enmod ssl rewrite proxy_http
    cp massbank.conf /etc/apache2/sites-available/massbank.conf
    cp massbank-ssl.conf /etc/apache2/sites-available/massbank-ssl.conf
    a2dissite 000-default
    a2ensite massbank massbank-ssl
  SHELL
  
  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  config.vm.network "private_network", ip: "192.168.35.18"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  config.vm.provider "virtualbox" do |vb|
    vb.memory = 4096
    vb.cpus = 2
  end
end
