rem Commit-Date: 2026-10-06T21:45:00Z
rem Commit-Version: 0.1.0-20261006214500
@echo off
rem Autor: Antonio Duce
rem Version del programa: 0.1.0; la revision Git se incorpora al JAR.
rem Ejecuta un RPT y conserva el PDF; la impresora es el cuarto argumento opcional.
setlocal

cd /d "%~dp0"
set "JAVA_HOME=C:\dev\tools\java\jdk22"
set "GRADLE_HOME=C:\dev\tools\gradle"
if "%CRYSTAL_CONFIG_DIR%"=="" set "CRYSTAL_CONFIG_DIR=%ProgramData%\CrystalReportService\config"
set "CONFIG_FILE=%CRYSTAL_CONFIG_DIR%\application.properties"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo No se encontro Java 22 en %JAVA_HOME%.
    exit /b 1
)

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    echo No se encontro Gradle en %GRADLE_HOME%.
    exit /b 1
)

if not exist "%CONFIG_FILE%" (
    echo No se encontro la configuracion externa: %CONFIG_FILE%
    echo Copia config-example\application.properties y protegela en esa ruta.
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
set "PRINTER=%~4"

echo Reporte: %REPORT%
echo PDF: %PDF%
echo Parametros: %PARAMETERS%
echo Impresora: %PRINTER%
call "%GRADLE_HOME%\bin\gradle.bat" runReport --no-daemon "-PconfigDir=%CRYSTAL_CONFIG_DIR%" "-Prpt=%REPORT%" "-Ppdf=%PDF%" "-Pparameters=%PARAMETERS%" "-Pprinter=%PRINTER%"
exit /b %ERRORLEVEL%
