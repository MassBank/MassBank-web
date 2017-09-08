SET PATH=%PATH%;C:\Program Files\Java\jdk1.5.0_07\bin;
SET CLASSPATH=.;C:\Program Files\Tomcat 5.5\common\lib\servlet-api.jar;C:\Program Files\Tomcat 5.5\webapps\ROOT\WEB-INF\lib\commons-lang-2.3.jar;
SET pkg=massbank/admin
SET tmp_dir=build_massbank.admin
SET opt=-d %tmp_dir%

del massbank-admin.jar

mkdir %tmp_dir%
javac %pkg%/AdminCommon.java %opt%
javac %pkg%/CmdExecute.java %opt%
javac %pkg%/CmdResult.java %opt%
javac %pkg%/DatabaseAccess.java %opt%
javac %pkg%/FileUtil.java %opt%
javac %pkg%/MassBankScheduler.java %opt%
javac %pkg%/OperationManager.java %opt%
javac %pkg%/SqlFileGenerator.java %opt%
javac %pkg%/Validator.java %opt%
javac %pkg%/VersionInfo.java %opt%
javac %pkg%/VersionManager.java %opt%
javac %pkg%/UpdateConfig.java %opt%

cd %tmp_dir%
jar cfmv ../massbank-admin.jar ../%pkg%/MANIFEST.MF %pkg%/*.class

cd ../
rmdir /S /Q %tmp_dir%
pause
