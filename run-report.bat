@echo off
setlocal

cd /d "%~dp0"
set "JAVA_HOME=C:\dev\tools\java\jdk22"
set "GRADLE_HOME=C:\dev\tools\gradle"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo No se encontro Java 22 en %JAVA_HOME%.
    exit /b 1
)

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    echo No se encontro Gradle en %GRADLE_HOME%.
    exit /b 1
)

if "%~1"=="" (
    set "REPORT=..\assets\CR4EMIP32_0-80004572\eclipse\plugins\com.businessobjects.crystalreports.samples_12.2.233.r5802\SampleReports\Statement of Account.rpt"
    set "PDF=build\output\reporte.pdf"
) else (
    set "REPORT=%~1"
    if "%~2"=="" (
        set "PDF=build\output\%~n1.pdf"
    ) else (
        set "PDF=%~2"
    )
)

set "PARAMETERS=%~3"

echo Reporte: %REPORT%
echo PDF: %PDF%
echo Parametros: %PARAMETERS%
call "%GRADLE_HOME%\bin\gradle.bat" runReport --no-daemon "-Prpt=%REPORT%" "-Ppdf=%PDF%" "-Pparameters=%PARAMETERS%"
exit /b %ERRORLEVEL%