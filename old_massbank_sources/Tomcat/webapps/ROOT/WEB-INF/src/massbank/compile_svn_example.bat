SET PATH=%PATH%;C:\Program Files\Java\jdk1.5.0_16\bin
SET CLASSPATH=.;C:\Program Files\Tomcat 5.5\common\lib\servlet-api.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\svnkit-1.7.5-v1.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-io-1.4.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-lang-2.3.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\mail.jar;

SET pkg=massbank/svn
SET tmp_dir=build_svn
SET opt=-d %tmp_dir%

mkdir %tmp_dir%
del /Q %tmp_dir%\massbank\svn\*
javac %pkg%/*.java %opt%
xcopy "./%pkg%\svn.info" %tmp_dir%

cd %tmp_dir%
jar cfmv ../../lib/massbank-svn.jar ../%pkg%/MANIFEST.MF %pkg%/*.class svn.info
pause
