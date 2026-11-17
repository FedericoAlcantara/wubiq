## wubiq-installer

Builds the native packages of the Wubiq print client and holds the two programs they install: the service launcher (`WubiqLauncher`) and the configurator (`WubiqConfigurator`). A package carries wubiq-client, wubiq-common and its own Java 25 runtime; nothing in installing, configuring or running the client needs an installed Java.

| Operating system | Package | Installs into |
|---|---|---|
| Windows 10 or later, 64-bit | `wubiq-client-VERSION-windows-x64.msi` | `C:\Program Files\Wubiq` |
| macOS, Apple silicon | `wubiq-client-VERSION-macos-arm64.pkg` | `/Applications/Wubiq.app` |
| macOS, Intel | `wubiq-client-VERSION-macos-x64.pkg` | `/Applications/Wubiq.app` |
| Linux x64, Debian family | `wubiq-client-VERSION-linux-x64.deb` | `/opt/wubiq` |
| Linux x64, Red Hat family | `wubiq-client-VERSION-linux-x64.rpm` | `/opt/wubiq` |

### Building

A package is built on the operating system it is for. It needs:
- a JDK 25 as `JAVA_HOME` (its `jdeps`, `jlink` and `jpackage` are used);
- Ant;
- on Windows, the WiX Toolset 3 on the `PATH`;
- on Linux, `dpkg-deb` and `fakeroot` for the DEB and `rpmbuild` for the RPM.

```
ant package
```

compiles wubiq-common, wubiq-client and the installer, builds the runtime with `jlink` from the modules `jdeps` lists, and writes the package of the current operating system (both of them on Linux) into `dist`, named as in the table above. The version is the client's version.

The packages are not signed.

### Installing

| Operating system | Steps |
|---|---|
| Windows | Open the MSI. SmartScreen warns that the publisher is unknown: choose **More info**, then **Run anyway**. |
| macOS | Open the PKG. Gatekeeper refuses it: open **System Settings › Privacy & Security** and choose **Open Anyway**. |
| Debian, Ubuntu | `sudo dpkg -i wubiq-client-VERSION-linux-x64.deb` |
| Fedora, RHEL | `sudo rpm -U wubiq-client-VERSION-linux-x64.rpm` |

The package registers the client as a service that starts at boot, and starts it:

| Operating system | Service | Stop and start |
|---|---|---|
| Windows | `wubiq-client` (shown as Wubiq) | `net stop wubiq-client`, `net start wubiq-client` |
| macOS | launchd daemon `net.sf.wubiq.client-Wubiq` | `sudo launchctl unload /Library/LaunchDaemons/net.sf.wubiq.client-Wubiq.plist`, `sudo launchctl load /Library/LaunchDaemons/net.sf.wubiq.client-Wubiq.plist` |
| Linux | systemd unit `wubiq-Wubiq` | `sudo systemctl stop wubiq-Wubiq`, `sudo systemctl start wubiq-Wubiq` |

### Configuring

The service reads `wubiq-installer.properties` from the configuration directory. Only an administrator can write there: the file holds the options of a program that runs as the system.

| Operating system | Configuration directory | Log |
|---|---|---|
| Windows | `C:\ProgramData\Wubiq` | `C:\ProgramData\Wubiq\logs` |
| macOS | `/Library/Application Support/Wubiq` | `/var/log/wubiq.log` |
| Linux | `/etc/wubiq` | `journalctl -u wubiq-Wubiq` |

Start the configurator as an administrator, set the server addresses, the unique id and the groups, and save. Saving restarts the client.

| Operating system | Configurator |
|---|---|
| Windows | Start menu › **WubiqConfigurator**, with right click › **Run as administrator** |
| macOS | `sudo /Applications/Wubiq.app/Contents/MacOS/WubiqConfigurator` |
| Linux | `sudo /opt/wubiq/bin/WubiqConfigurator` |

Until a server address is saved, the service runs and waits.

### Updating

The service asks its server for the client version on offer for its platform and package type. When that version is higher than its own, it downloads the package from the server and runs the operating system's installer unattended (`msiexec /i FILE /qn /norestart`, `installer -pkg FILE -target /`, `dpkg -i FILE`, `rpm -U FILE`). The installer stops the service, replaces the files and starts the service again; the configuration directory is kept. An equal or lower version is never installed. The configurator's title shows when a higher version is on offer.

The output of the last update is in `wubiq-client-update.log`, in the temporary directory of the system account (`C:\Windows\Temp`, `/tmp`). When an update fails, the service keeps running its version and tries again after its next start.

The server offers the packages of its `client.packages.directory` (see `../wubiq-server/README.md`).

### Upgrading from an IzPack installation

Install the package over the old installation. At its first start the service:
1. finds the old installation (the Windows service `wubiq`, or `/etc/init.d/wubiq`);
2. copies its `wubiq-installer.properties` into the configuration directory, unless one is there already;
3. stops the old client and removes its service.

The old installation's folder is not deleted; remove it once the new client prints.

### Uninstalling

| Operating system | Steps |
|---|---|
| Windows | **Settings › Apps**, Wubiq, **Uninstall** |
| macOS | `sudo launchctl unload /Library/LaunchDaemons/net.sf.wubiq.client-Wubiq.plist`, then delete that file and `/Applications/Wubiq.app` |
| Debian, Ubuntu | `sudo dpkg -r wubiq` |
| Fedora, RHEL | `sudo rpm -e wubiq` |

The configuration directory is left in place.
