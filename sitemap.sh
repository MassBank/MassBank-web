#!/bin/bash

# Create sitemap index file with information that stays constant
TDATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
CONFFILE='/var/www/MassBank/massbank.conf'
BASESITEURL=$(grep "FrontServer *URL" $CONFFILE | awk -F\" '{print $(NF-1)}')
#BASESITEURL='http://massbank.eu/MassBank/'
cd /tmp
sudo echo '<?xml version="1.0" encoding="UTF-8"?>' > sitemap-DB$TDATE.xml
sudo echo '' >> sitemap-DB$TDATE.xml
sudo echo '<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">' >> sitemap-DB$TDATE.xml
sudo echo '' >> sitemap-DB$TDATE.xml

sudo echo '<sitemap>' >> sitemap-DB$TDATE.xml
sudo echo '' >> sitemap-DB$TDATE.xml
	sudo echo '<loc>http://massbank.eu/MassBank/sitemap.xml</loc>' >> sitemap-DB$TDATE.xml
	sudo echo '' >> sitemap-DB$TDATE.xml
	sudo echo '<lastmod>'"$TDATE"'</lastmod>' >> sitemap-DB$TDATE.xml
	sudo echo '' >> sitemap-DB$TDATE.xml
sudo echo '</sitemap>' >> sitemap-DB$TDATE.xml
sudo echo '' >> sitemap-DB$TDATE.xml

DBLIST=(/var/www/MassBank/DB/annotation/*)
for DB in "${DBLIST[@]}"
do
    DBBASENAME=$(basename $DB)
    echo "Processing DB $DBBASENAME"
    SITEMAPFILENAME="$BASESITEURL$DBBASENAME.xml"
    FILENAME="$DBBASENAME"'.xml'
    # Create sitemap entry in index
    sudo echo -e '\t<sitemap>' >> sitemap-DB$TDATE.xml
    sudo echo '' >> sitemap-DB$TDATE.xml
    sudo echo -e '\t\t<loc>'"$SITEMAPFILENAME"'</loc>' >> sitemap-DB$TDATE.xml
    sudo echo '' >> sitemap-DB$TDATE.xml
    sudo echo -e '\t\t<lastmod>'"$TDATE"'</lastmod>' >> sitemap-DB$TDATE.xml
    sudo echo '' >> sitemap-DB$TDATE.xml
    sudo echo -e '\t</sitemap>' >> sitemap-DB$TDATE.xml
    sudo echo '' >> sitemap-DB$TDATE.xml
    
    # Create sitemap-file
    sudo echo '<?xml version="1.0" encoding="UTF-8"?>' > $FILENAME
    sudo echo '' >> $FILENAME
    sudo echo '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">' >> $FILENAME
    RECORDS=($DB/*)
    for RECORDPATH in "${RECORDS[@]}"
    do
        RECORDNAME=$(basename $RECORDPATH)
        RECORD="${RECORDNAME%.*}"
        # Create an entry for every record
        sudo echo '' >> $FILENAME
        sudo echo -e '\t<url>' >> $FILENAME
        sudo echo '' >> $FILENAME
        sudo echo -e '\t\t<loc>'"$BASESITEURL"'jsp/FwdRecord.jsp?id='"$RECORD"'</loc>' >> $FILENAME
        sudo echo '' >> $FILENAME
        sudo echo -e '\t\t<changefreq>always</changefreq>' >> $FILENAME
        sudo echo '' >> $FILENAME
        sudo echo -e '\t</url>' >> $FILENAME
        sudo echo '' >> $FILENAME
    done
    sudo echo '</urlset>' >> $FILENAME
done

sudo echo '</sitemapindex>' >> sitemap-DB$TDATE.xml

# Now move all the things to where they're supposed to be
sudo mv sitemap-DB$TDATE.xml /var/www/MassBank/sitemapindex.xml
sudo chown tomcat.tomcat *.xml
for DB in "${DBLIST[@]}"
do
    DBBASENAME=$(basename $DB)
    FILENAME="$DBBASENAME"'.xml'
    sudo mv $FILENAME /var/www/MassBank/$FILENAME
done
