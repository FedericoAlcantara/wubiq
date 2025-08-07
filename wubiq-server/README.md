## wubiq-server

This module renders the web management console.

### Requirements

- [apache ant](https://ant.apache.org/)
- [Docker](https://www.docker.com/)
- [Java 8](https://www.java.com/en/download/manual.jsp)

### Running the server
You have to build the server to run it. The wubiq-common and the wubiq-client are dependencies.

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

Now that you have the server, you'll need some clients to connect to it.
Download the wubiq-client jar (from the webpage) and run (requires java 8):

```shell
% java -cp ./wubiq-client.jar net.sf.wubiq.clients.LocalPrintManager -h http://localhost:8080 -u client-test
```

All the printers will be exposed in the server.

### Distributing the application
The whole application building and distribution is handled by the build.xml (apache ant).

In the wubiq-server directory run the following command:

```shell
ant
```

That will create all the distribution files in **wubiq-server/dist**.


