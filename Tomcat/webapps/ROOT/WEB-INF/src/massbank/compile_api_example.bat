SET PATH=%PATH%C:\Java\jdk5.0\bin;
SET CLASSPATH=.;C:\Program Files\Tomcat 5.5\common\lib\servlet-api.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\axis.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\wsdl4j-1.5.1.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-lang-2.3.jar;C:\Program Files\Tomcat 5.5\webapps\api\WEB-INF\lib\axis2-kernel-1.5.jar;C:\Program Files\Tomcat 5.5\webapps\api\WEB-INF\lib\axis2-transport-http-1.5.jar;C:\Program Files\Tomcat 5.5\webapps\api\WEB-INF\lib\axiom-api-1.2.8.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\src\massbank.jar;
SET pkg=massbank/api
SET tmp_dir=build_massbank.api
SET opt=-d %tmp_dir% -g

del massbank_api.aar

mkdir %tmp_dir%
xcopy .\massbank\api\META-INF %tmp_dir%\META-INF\ /e /c /h /y
javac %pkg%/MassBankAPI.java %opt%

cd %tmp_dir%
jar cf ../massbank_api.aar %pkg%/*.class META-INF

cd ../
rmdir /S /Q %tmp_dir%
pause
