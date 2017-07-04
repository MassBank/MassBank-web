@ECHO OFF
Set /p MBUSERNAME= Please set an username for the MassBank administration tool: 

:PW
REM Powershell script based on https://stackoverflow.com/questions/19950620/how-to-hide-password-in-command-line-with-and-get-the-value-into-bat-file (License CC BY-CA 3.0)
set "psCommand=powershell -Command "$pword = read-host 'Please set a password for the MassBank administration tool' -AsSecureString ; ^
		$BSTR=[System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($pword); ^
		[System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)""
for /f "usebackq delims=" %%p in (`%psCommand%`) do set PASSWORD=%%p

set "psCommand=powershell -Command "$pword = read-host 'Please repeat the password' -AsSecureString ; ^
		$BSTR=[System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($pword); ^
		[System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)""
for /f "usebackq delims=" %%p in (`%psCommand%`) do set PASSWORD1=%%p

If %PASSWORD%==%PASSWORD1% ( goto start) else ( goto pw_repeat)

:pw_repeat
echo The passwords did not match. Repeat please.
goto PW

:start

vagrant up

:clean up
echo Delete the MBUSERNAME and PASSWORD variables from memory:
setx MBUSERNAME ""
setx PASSWORD ""
setx PASSWORD1 ""

endlocal
