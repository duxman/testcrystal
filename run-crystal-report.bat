@echo off
setlocal

cd /d "%~dp0"

if "%~1"=="" (
    set "REPORT_NAME=BeforeTV.rpt"
) else (
    set "REPORT_NAME=%~1"
)

set "REPORT=Crystal-Reports-master\%REPORT_NAME%"
if "%~2"=="" (
    set "PDF=build\output\%~n1.pdf"
    if "%~1"=="" set "PDF=build\output\BeforeTV.pdf"
) else (
    set "PDF=%~2"
)

set "PARAMETERS=%~3"
call "%~dp0run-report.bat" "%REPORT%" "%PDF%" "%PARAMETERS%"
exit /b %ERRORLEVEL%
