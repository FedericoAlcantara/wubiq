## What is Wubiq?
Wubiq is a web / client combination that allows users to share its printing services (printers).
It consist of a web application companion (wubiq-server) and a client print manager (wubiq-client).

### How it is done?
On the client side the client print manager identifies itself to the server exposing its available print services. 
The server on its parts registers those print services as its own.
Printing requests can then be directed to the server along with the print service name 
that will receive the document. The clients polls the server for its pending print jobs, if any, 
takes care of them and notifies the server of its success.

### Documentation
Wiki pages are available at: **https://github.com/FedericoAlcantara/wubiq/wiki/Main_Page/**

### Installing wubiq as a server
1. Download wubiq.zip and extract its files.
2. Deploy wubiq-server.war on a servlet container (this has been tested on Tomcat 6 and Tomcat 7).
3. Copy wubiq-common.jar and hsqldb.jar (included in the distribution) to your web application lib folder.
   (Re)start your servlet container if needed.
4. You can test if wubiq is running by opening a web browser and write: http://host:port/wubiq-server.
   If everything is fine you should see a list of local services (from server).

### Using the wubiq manager
Wubiq has a manager from where you can check the status of connected clients and their services. 
Also provides a link for installing or downloading wubiq-client to be run on local computers or mobile devices.
To bring up the manager on any client open a browser and just type: http://host:port/wubiq-server.

## Base methods
### Installing and Testing on the client
There are two ways to get the client to work on the remote computer.
1. Connecting to the wubiq-manager using http://host:port/wubiq-server.

Or by downloading a client program and running it from the command line.
1. From the previous downnload extract wubiq-client.jar.
2. You can make a batch or script to run the client with the following:
   **java -jar wubiq-client.jar -c http://host:8080**. For example:
   - java -jar wubiq-client.jar -c http://localhost:8080
   See the wubiq-client help by running java -jar wubiq-client.jar -?
3. To test if your clients are working as expected connect to wubiq-manager with: http://host:port/wubiq-server.
   If everything is working you should see a list of servers' print services including remote print services from client computers.

**Note: replace *host* with the host address, *port* with the port (by default:8080).

### Installing on Android
Wubiq can now be installed on android powered devices. It will enable bluetooth printing through wubiq's interface.
To install it on Android you have two options:
1. Connecting to the wubiq-manager using http://host:port/wubiq-server, and download the android app (wubiq-android.apk)

Or by extracting the android application wubiq-android.apk from a previously downloaded wubiq.zip.

Follow the installation steps and then configure the server address and the printers to be managed by wubiq.

### Limitations
- A client program must be running on each client computer that wish to use remote print services.

