@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@echo off
set ERROR_CODE=0

@REM set %~dp0 is the directory of this script
set MAVEN_PROJECT_BASEDIR=%~dp0

if exist "%MAVEN_PROJECT_BASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    java -jar "%MAVEN_PROJECT_BASEDIR%\.mvn\wrapper\maven-wrapper.jar" %*
) else (
    mvn %*
)
if ERRORLEVEL 1 set ERROR_CODE=1

exit /B %ERROR_CODE%
