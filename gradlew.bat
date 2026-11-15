@echo off
setlocal EnableDelayedExpansion

set JAVA_HOME=C:\Program Files\Java\jdk-17
set ANDROID_HOME=%~dp0android-sdk
set PATH=%JAVA_HOME%\bin;%PATH%

echo Starting Gradle build for irtax...
echo.

cd /d "%~dp0"

if exist "bin\gradle.bat" (
    call bin\gradle.bat %*
) else (
    echo Error: Gradle not found in bin folder
    echo Please ensure Gradle is properly installed
    exit /b 1
)
