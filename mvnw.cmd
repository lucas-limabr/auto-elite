@echo off
set "PROJECT_DIR=%~dp0"
call "%PROJECT_DIR%backend\mvnw.cmd" -f "%PROJECT_DIR%backend\pom.xml" %*

