mkdir ${INSTALL_PATH}/logs
service wubiq stop
update-rc.d wubiq defaults
service wubiq start
