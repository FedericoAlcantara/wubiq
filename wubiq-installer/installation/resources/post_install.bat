${ENV[SystemDrive]}
cd ${INSTALL_PATH}\bin
javaw -cp . -splash:splash.png net.sf.wubiq.clients.WubiqConfigurator

net start wubiq
exit /b 0
