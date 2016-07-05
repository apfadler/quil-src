@echo off
Setlocal EnableDelayedExpansion

if "%OS%" == "Windows_NT"  setlocal

:: Check JAVA_HOME.
if defined JAVA_HOME  goto :checkquil
    echo %0, ERROR:
    echo JAVA_HOME environment variable is not found.
    echo Please point JAVA_HOME variable to location of JDK 1.8.
    echo You can also download latest JDK at http://java.com/download.
goto :eof

:checkquil
if defined QUIL_HOME  goto buildcp
	echo Defaulting QUIL_HOME to .
	set QUIL_HOME=.

:buildcp

SET CP=
for /R %%F in (%QUIL_HOME%\libs\*) do call :concat %%F

echo %CP%

set QUIL_WARPATH=%QUIL_HOME%\libs\webapp-1.0-SNAPSHOT.war
set QUIL_WORKER=true

%JAVA_HOME%\bin\java -Dlog4j.configuration=file:config/java.util.logging.properties -cp "%CP%" org.quil.server.QuilServer

:concat
set CP=%CP%;%1
goto :eof