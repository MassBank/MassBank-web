SET PATH=C:\Java\jdk5.0\bin;%PATH%
SET CLASSPATH=.;C:\Java\jre5.0\lib\plugin.jar;
SET TOMCAT_MASSBANK=C:\Tomcat\Tomcat5.5\webapps\ROOT\WEB-INF\src\massbank\
del *.class
del SearchApplet.jar
javac -d . -Xlint:none "%TOMCAT_MASSBANK%GetConfig.java"
javac -d . -Xlint:none "%TOMCAT_MASSBANK%ResultRecord.java"
javac -d . -Xlint:none "%TOMCAT_MASSBANK%ResultList.java"
javac -d . -Xlint:none "%TOMCAT_MASSBANK%MassBankCommon.java"
javac -d . -Xlint:none "%TOMCAT_MASSBANK%GetInstInfo.java"
javac -Xlint:none *.java
jar cfmv SearchApplet.jar MANIFEST.MF *.class massbank/*.class
pause

del *.class
rmdir /s /q massbank
