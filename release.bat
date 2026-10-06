rem Commit-Date: 2026-10-06T21:45:00Z
rem Commit-Version: 0.1.0-20261006214500
@echo off
rem Autor: Antonio Duce
rem Version del programa: 0.1.0; la revision Git se incorpora al artefacto.
rem Automatiza el versionado del estado actual mediante commit y push.
setlocal

cd /d "%~dp0"
git config core.hooksPath .githooks
set "COMMIT_MESSAGE=%~1"
if "%COMMIT_MESSAGE%"=="" set "COMMIT_MESSAGE=Documentacion y versionado automatico"

where git >nul 2>&1
if errorlevel 1 (
    echo No se encontro Git en PATH.
    exit /b 1
)

echo Version calculada:
set "COMMIT_DATE=%DATE%T%TIME%Z"
set "COMMIT_VERSION=0.1.0-%DATE:~6,4%%DATE:~3,2%%DATE:~0,2%%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%"
if defined GRADLE_HOME (
    call "%GRADLE_HOME%\bin\gradle.bat" updateMetadata "-PcommitDate=%COMMIT_DATE%" "-PcommitVersion=%COMMIT_VERSION%" --no-daemon
) else (
    where gradle.bat >nul 2>&1
    if errorlevel 1 (
        echo No se encontro Gradle; no se pueden actualizar los metadatos.
        exit /b 1
    ) else (
        call gradle.bat updateMetadata "-PcommitDate=%COMMIT_DATE%" "-PcommitVersion=%COMMIT_VERSION%" --no-daemon
    )
)

git add -A
git status --short

git diff --cached --quiet
if not errorlevel 1 (
    echo No hay cambios para publicar.
    exit /b 0
)

git commit -m "%COMMIT_MESSAGE%"
if errorlevel 1 exit /b %errorlevel%
git push origin main
exit /b %errorlevel%
