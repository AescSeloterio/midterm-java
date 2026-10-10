@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>&1
if errorlevel 1 (
 echo ERROR: JDK 17 or newer is required. javac was not found on PATH.
 pause
 exit /b 1
)
if not exist "lib\mysql-connector-j-9.6.0.jar" (
 echo ERROR: MySQL Connector/J JAR is missing from lib.
 pause
 exit /b 1
)
if not exist "build\classes" mkdir "build\classes"
echo Compiling Academia One...
javac -encoding UTF-8 -d "build\classes" "src\main\java\DatabaseStore.java" "src\main\java\LoginApp.java" "src\main\java\ProgramCatalog.java"
if errorlevel 1 (
 echo ERROR: Compilation failed.
 pause
 exit /b 1
)
echo Starting Academia One with MySQL Connector/J 9.6.0...
java -cp "build\classes;src\main\resources;src;lib\mysql-connector-j-9.6.0.jar" LoginApp
if errorlevel 1 (
 echo Application exited with an error.
 pause
 exit /b 1
)
endlocal
