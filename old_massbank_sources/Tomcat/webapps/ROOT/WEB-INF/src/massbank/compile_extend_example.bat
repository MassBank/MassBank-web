SET PATH=%PATH%;C:\Program Files\Java\jdk1.5.0_16\bin
SET CLASSPATH=.;C:\Program Files\Tomcat 5.5\common\lib\servlet-api.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-lang-2.3.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\massbank.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\crimson.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\mail.jar;
SET pkg=massbank/extend
SET tmp_dir=build_extend
SET opt=-d %tmp_dir%

mkdir %tmp_dir%
del /Q %tmp_dir%\massbank\extend\*
javac %pkg%/ChemicalFormulaUtils.java %opt%
javac %pkg%/RelationInfo.java %opt%
javac %pkg%/RelationSearch.java %opt%
javac %pkg%/RelationSearchResult.java %opt%

cd %tmp_dir%
jar cfmv ../../lib/massbank-extend.jar ../%pkg%/MANIFEST.MF %pkg%/*.class
pause
