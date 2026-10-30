@echo off
echo ========================================================
echo   Compiling Smart Campus Placement Management System
echo ========================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib\*;src" -d bin src\com\campus\placement\models\*.java src\com\campus\placement\dao\*.java src\com\campus\placement\*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation complete. Classes compiled into 'bin' folder.
) else (
    echo [ERROR] Compilation failed. Please check errors above.
)
pause
