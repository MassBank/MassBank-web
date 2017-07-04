@echo off
setlocal
Set /p MBUSERNAME= Please set an username for the MassBank administration tool: 


:pw
Set /p PASSWORD= Please set a password for the MassBank administration tool: 
Set /p PASSWORD1= Please repeat the password for the MassBank administration tool: 
If %PASSWORD%==%PASSWORD1% ( goto start) else ( goto pw_repeat)

:pw_repeat
echo The passwords did not match. Repeat please.
Set /p PASSWORD= Please set a password for the MassBank administration tool: 
Set /p PASSWORD1= Please repeat the password for the MassBank administration tool: 
If %PASSWORD%==%PASSWORD1% ( goto start) else ( goto pw_repeat)

:start

vagrant up

:clean up
echo Delete the MBUSERNAME and PASSWORD variables from memory:
setx MBUSERNAME ""
setx PASSWORD ""
setx PASSWORD1 ""

endlocal
