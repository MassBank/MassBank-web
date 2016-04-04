SET PATH=%PATH%C:\Java\jdk5.0\bin;
SET CLASSPATH=.;C:\Program Files\Tomcat 5.5\common\lib\servlet-api.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-httpclient-2.0-final.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-fileupload-1.0.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\mail.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\activation.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-lang-2.3.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-io-1.4.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\axis.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-discovery-0.2.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\jaxrpc.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\keggapi.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\wsdl4j-1.5.1.jar;
SET pkg=massbank
SET tmp_dir=build_%pkg%
SET opt=-d %tmp_dir%

del massbank.jar

mkdir %tmp_dir%
javac %pkg%/BatchSearchWorker.java %opt%
javac %pkg%/BatchService.java %opt%
javac %pkg%/CallCgi.java %opt%
javac %pkg%/FileUpload.java %opt%
javac %pkg%/GetConfig.java %opt%
javac %pkg%/GetInstInfo.java %opt%
javac %pkg%/JobInfo.java %opt%
javac %pkg%/JobManager.java %opt%
javac %pkg%/MassBankCommon.java %opt%
javac %pkg%/MassBankLog.java %opt%
javac %pkg%/MultiDispatcher.java %opt%
javac %pkg%/ResultList.java %opt%
javac %pkg%/ResultRecord.java %opt%
javac %pkg%/Sanitizer.java %opt%
javac %pkg%/SendMail.java %opt%
javac %pkg%/SendMailInfo.java %opt%
javac %pkg%/ServerStatus.java %opt%
javac %pkg%/ServerStatusInfo.java %opt%
javac %pkg%/StartupExecModule.java %opt%
javac %pkg%/ColorPathway.java %opt%
javac %pkg%/QueryFileUtil.java %opt%
javac %pkg%/MassBankEnv.java %opt%

cd %tmp_dir%
jar cfmv ../massbank.jar ../%pkg%/MANIFEST.MF %pkg%/*.class

cd ../
rmdir /S /Q %tmp_dir%
pause
