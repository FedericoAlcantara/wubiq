## wubiq-server

This module renders the web management console.

### Requirements

- [apache ant](https://ant.apache.org/)
- [Docker](https://www.docker.com/)
- [Java 8](https://www.java.com/en/download/manual.jsp)

### Running the server
You have to build the server to run it. wubiq-common is its only dependency.

The wubiq-server.war needs a tomcat server to run, there is one available under docker.

Open a terminal and follow these steps while in the wubiq-server directory:

```shell
% ant wubiq-server
% docker compose up -d
```

You can access the wubiq server in http://localhost:8080/wubiq-server
> [!NOTE] 
> You can open a chrome instance from the command line, useful for capturing screenshots
> MAC: /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --app=http://localhost:8080/wubiq-server --force-device-scale-factor=1 --user-data-dir="/tmp/chrome-temp-profile" --window-size=940,480
> WINDOWS: chrome.exe --app=http://localhost:8080/wubiq-server --force-device-scale-factor=1 --user-data-dir="/tmp/chrome-temp-profile" --window-size=940,480

> [!IMPORTANT]
> The user-data-dir should point to an empty dir for scale-factor to work as expected. Chrome may ask for initial configuration preferences.


If you want to login, user: test, password: 12345

Now that you have the server, you'll need some clients to connect to it. Install the native package built by `../wubiq-installer`, or download the 2.7.4 client jar from the webpage and run it with an installed Java:

```shell
% java -jar ./wubiq-client.jar -h http://localhost:8080 -u client-test
```

All the printers will be exposed in the server.

### Client packages

The server serves two kinds of clients:

| Client | Asks | Is answered |
|---|---|---|
| 2.7.4 and older, started by the IzPack installation's launcher | the version, without naming its platform | `2.7.4`, and `wubiq-client.jar` from the webpage: the 2.7.4 jar, kept in `WebContent` |
| packaged client (native package of `../wubiq-installer`) | the version, naming its platform (`windows-x64`, `macos-arm64`, `macos-x64`, `linux-x64`) and package type (`msi`, `pkg`, `deb`, `rpm`) | the highest version among the packages for that platform and type; the client downloads the package through the servlet when that version is higher than its own |

To offer packages, set the directory that holds them in `wubiq-server.properties`:

```
client.packages.directory=/var/lib/wubiq/packages
```

Copy the packages there with the names the installer build gives them, `wubiq-client-VERSION-PLATFORM.TYPE`:

```
wubiq-client-2.8.0-windows-x64.msi
wubiq-client-2.8.0-macos-arm64.pkg
wubiq-client-2.8.0-macos-x64.pkg
wubiq-client-2.8.0-linux-x64.deb
wubiq-client-2.8.0-linux-x64.rpm
```

The version is read from the file name; a file with another name is ignored. Without the property, or with an empty directory, packaged clients are offered nothing and keep running.

The console shows the connected clients by version (`Connected clients by version: 2.7.4 (3), 2.8.0 (5)`), so it is visible when no 2.7.x client connects any more.

### Distributing the application
The whole application building and distribution is handled by the build.xml (apache ant).

In the wubiq-server directory run the following command:

```shell
ant
```

That will create all the distribution files in **wubiq-server/dist**.

The client packages are built by `../wubiq-installer`, on each operating system; `ant` here does not build them.


