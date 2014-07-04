cp -f ${installables.dir}/wubiq-installer.properties ${INSTALL_PATH}/bin/
mkdir ${INSTALL_PATH}/logs
service wubiq stop
update-rc.d wubiq defaults
service wubiq start
