@echo off
rem Autor: Antonio Duce
rem Version del programa: 0.1.0; la revision Git se incorpora al artefacto.
rem Automatiza el versionado del estado actual mediante commit y push.
setlocal

cd /d "%~dp0"
set "COMMIT_MESSAGE=%~1"
if "%COMMIT_MESSAGE%"=="" set "COMMIT_MESSAGE=Documentacion y versionado automatico"

where git >nul 2>&1
if errorlevel 1 (
    echo No se encontro Git en PATH.
    exit /b 1
)

echo Version calculada:
if defined GRADLE_HOME (
    call "%GRADLE_HOME%\bin\gradle.bat" gitVersion --no-daemon 2>nul
) else (
    where gradle.bat >nul 2>&1
    if errorlevel 1 (
        echo No se encontro Gradle; se continua con la revision Git.
    ) else (
        call gradle.bat gitVersion --no-daemon 2>nul
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
