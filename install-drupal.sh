#!/bin/sh
#------------------------------------------------
# Piwik and drupal
#------------------------------------------------
APACHE_HTDOCS_PATH=/var/www/html
#Download and install recent piwik
cd /vagrant
sudo wget http://builds.piwik.org/piwik.zip && unzip piwik.zip
sudo cp -p piwik $APACHE_HTDOCS_PATH/
sudo chown -R www-data:www-data $APACHE_HTDOCS_PATH/piwik
sudo chmod a+w $APACHE_HTDOCS_PATH/piwik

#install drupal 7
wget http://ftp.drupal.org/files/projects/drupal-7.9.tar.gz
tar -xvzf drupal-7.9.tar.gz
sudo mkdir $APACHE_HTDOCS_PATH/drupal
sudo mv drupal-7.9/* drupal-7.9/.htaccess drupal-7.9/.gitignore $APACHE_HTDOCS_PATH/drupal

sudo mkdir $APACHE_HTDOCS_PATH/drupal/sites/default/files
sudo chown www-data:www-data $APACHE_HTDOCS_PATH/drupal/sites/default/files
sudo cp $APACHE_HTDOCS_PATH/drupal/sites/default/default.settings.php /var/www/drupal/sites/default/settings.php
sudo chown www-data:www-data $APACHE_HTDOCS_PATH/drupal/sites/default/settings.php
mysqladmin -u root -p mass_2016?bank create drupal
mysql -u root -p mass_2016?bank
mysql> GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES ON drupal.* TO 'drupaluser'@'localhost' IDENTIFIED BY 'drupalpass';
mysql> FLUSH PRIVILEGES;
mysql> \q
sudo chmod a+w $APACHE_HTDOCS_PATH/drupal
sudo service apache2 restart
