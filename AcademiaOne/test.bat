@echo off
setlocal
cd /d "%~dp0"
if not exist "build\test-classes" mkdir "build\test-classes"
javac -encoding UTF-8 -d "build\test-classes" "src\main\java\DatabaseStore.java" "src\main\java\LoginApp.java" "src\main\java\ProgramCatalog.java" "tests\PasswordSmokeTest.java" "tests\UiSmokeTest.java"
if errorlevel 1 exit /b 1
java -cp "build\test-classes;lib\mysql-connector-j-9.6.0.jar" PasswordSmokeTest
if errorlevel 1 exit /b 1
java -cp "build\test-classes;lib\mysql-connector-j-9.6.0.jar" UiSmokeTest
if errorlevel 1 exit /b 1
echo All local smoke tests passed.
endlocal
