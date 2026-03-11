@ECHO OFF
set /p rka="Enter Release Key Alias: "
set /p rkp="Enter Release Key Path: "
keytool -list -v -keystore %rkp% -alias %rka% 
pause
