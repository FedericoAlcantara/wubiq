@echo off
rem Registers or removes the Wubiq client service. The MSI runs it as SYSTEM.
setlocal enabledelayedexpansion
set "APP=%~dp0"
for %%I in ("%APP%..") do set "ROOT=%%~fI"
set "DATA=%ProgramData%\Wubiq"
set "PRUNSRV=%APP%prunsrv-amd64.exe"

if /i "%~1"=="install" goto install
if /i "%~1"=="remove" goto remove
echo Usage: wubiq-service.bat install^|remove
exit /b 2

:install
if not exist "%DATA%\logs" mkdir "%DATA%\logs"
icacls "%DATA%" /inheritance:r /grant:r "*S-1-5-18:(OI)(CI)F" "*S-1-5-32-544:(OI)(CI)F" "*S-1-5-32-545:(OI)(CI)RX" >nul
set "CP="
for %%J in ("%APP%*.jar") do set "CP=!CP!%%~fJ;"
net stop wubiq-client >nul 2>&1
"%PRUNSRV%" delete wubiq-client >nul 2>&1
"%PRUNSRV%" install wubiq-client --DisplayName "Wubiq" --Description "Wubiq print client" --Startup auto --Jvm "%ROOT%\runtime\bin\server\jvm.dll" --StartMode jvm --StopMode jvm --Classpath "!CP!" --StartPath "%DATA%" --StartClass net.sf.wubiq.clients.WubiqLauncher --StartParams start --StopClass net.sf.wubiq.clients.WubiqLauncher --StopParams stop --LogPath "%DATA%\logs" --LogPrefix wubiq --StdOutput auto --StdError auto ++JvmOptions "-Dwubiq.package.type=msi" ++JvmOptions "--add-opens=java.base/java.lang=ALL-UNNAMED" ++JvmOptions "--add-opens=java.desktop/javax.print=ALL-UNNAMED" ++JvmOptions "--add-opens=java.desktop/sun.print=ALL-UNNAMED"
if errorlevel 1 exit /b 1
net start wubiq-client
exit /b %errorlevel%

:remove
net stop wubiq-client >nul 2>&1
"%PRUNSRV%" delete wubiq-client >nul 2>&1
exit /b 0
