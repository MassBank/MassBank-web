SET PATH=C:\Java\jdk5.0\bin;%PATH%
SET TOMCAT_MASSBANK=C:\Tomcat\Tomcat5.5\webapps\ROOT\WEB-INF\src\massbank\
del *.class
del PackageView.jar
javac -d . -Xlint:none "%TOMCAT_MASSBANK%GetConfig.java"
javac -d . -Xlint:none "%TOMCAT_MASSBANK%ResultRecord.java"
javac -d . -Xlint:none "%TOMCAT_MASSBANK%ResultList.java"
javac -d . -Xlint:none "%TOMCAT_MASSBANK%MassBankCommon.java"
javac -Xlint:none *.java
jar cfmv PackageView.jar MANIFEST.MF *.class massbank/*.class
pause

del *.class
rmdir /s /q massbank
