SET PATH=%PATH%;C:\Program Files\Java\jdk1.5.0_07\bin;
SET CLASSPATH=.;C:\Program Files\Tomcat 5.5\common\lib\servlet-api.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-httpclient-2.0-final.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\activation.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\mail.jar;
SET pkg=massbank/pserver
SET tmp_dir=build_massbank.pserver
SET opt=-d %tmp_dir%

del massbank-pserver.jar

mkdir %tmp_dir%
javac %pkg%/SendMail.java %opt%
javac %pkg%/ServerMonitor.java %opt%

cd %tmp_dir%
jar cfmv ../massbank-pserver.jar ../%pkg%/MANIFEST.MF %pkg%/*.class

cd ../
rmdir /S /Q %tmp_dir%
pause
