net stop wubiq

${ENV[SystemDrive]}
cd ${INSTALL_PATH}\bin

"prunsrv.exe" delete wubiq
"prunsrv.exe" install wubiq --Description "Wubiq Service" --DisplayName "Wubiq" --JavaHome "${JAVA_HOME}" --Startup auto --StartMode jvm --StartPath "${INSTALL_PATH}\bin" --StartClass net.sf.wubiq.clients.WubiqLauncher --StartParams start --StopMode jvm --StopClass net.sf.wubiq.clients.WubiqLauncher --StopParams stop --LogPath "${INSTALL_PATH}\logs" --LogPrefix wubiq --StdOutput auto --StdError auto

exit /b 0