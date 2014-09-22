net stop wubiq

${ENV[SystemDrive]}
cd ${INSTALL_PATH}\bin

"prunsrv.exe" delete wubiq
exit /b 0
