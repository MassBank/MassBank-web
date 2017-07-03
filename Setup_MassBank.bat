@echo off
SetLocal
If [%1]=="" echo Please set the environment variables USER and PASSWORD for your site before running this script. else ( goto pw)
If [%1]=="" echo You can use a command like:
If [%1]=="" echo Setup_MassBank.bat USER PASSWORD

:pw
If [%2]=="" echo Please set the environment variables USER and PASSWORD for your site before running this script. else ( goto start)
If [%2]=="" echo You can use a command like:
If [%2]=="" echo Setup_MassBank.bat USER PASSWORD

:start
set USER=%1
set PASSWORD=%2
vagrant up
setx USER ""
setx PASS ""
EndLocal
