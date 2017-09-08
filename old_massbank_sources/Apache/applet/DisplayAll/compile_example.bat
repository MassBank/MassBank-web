SET PATH=%PATH%;C:\Program Files\Java\jdk1.5.0_16\bin
SET TOMCAT_MASSBANK=C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\classes\massbank\
del *.class
javac -cp .;build "%TOMCAT_MASSBANK%GetConfig.java" -d ./build
javac -cp .;build "%TOMCAT_MASSBANK%ResultRecord.java" -d ./build
javac -cp .;build "%TOMCAT_MASSBANK%ResultList.java" -d ./build
javac -cp .;build "%TOMCAT_MASSBANK%MassBankCommon.java" -d ./build
javac -cp .;build canvas/DrawPane.java -d ./build
javac -cp .;build draw2d/MOLformat.java -d ./build
javac -cp .;build metabolic/MolFigure.java -d ./build
javac -cp .;build *.java -d ./build

cd ./build
jar cfmv ../DisplayAll2.jar ../MANIFEST.MF *.class massbank/*.class *
pause

del *.class
rmdir /s /q massbank
