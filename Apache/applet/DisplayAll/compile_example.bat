SET PATH=C:\Program Files\Java\jdk1.5.0_07\bin;%PATH%
SET TOMCAT_MASSBANK=C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\classes\massbank\
del *.class
javac -d . -Xlint:unchecked "%TOMCAT_MASSBANK%GetConfig.java"
javac -d . -Xlint:unchecked "%TOMCAT_MASSBANK%ResultRecord.java"
javac -d . -Xlint:unchecked "%TOMCAT_MASSBANK%ResultList.java"
javac -d . -Xlint:unchecked "%TOMCAT_MASSBANK%MassBankCommon.java"
javac *.java
jar cfmv DisplayAll2.jar MANIFEST.MF *.class massbank/*.class
pause

del *.class
rmdir /s /q massbank
