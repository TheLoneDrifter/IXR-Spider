@echo off
REM ---------------------------------------------------------------------
REM Gradle start up script for Windows
REM ---------------------------------------------------------------------

setlocal

if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java"
)

set "PRG=%~dp0gradle\wrapper\gradle-wrapper.jar"

"%JAVA_EXE%" -classpath "%PRG%" org.gradle.wrapper.GradleWrapperMain %*
