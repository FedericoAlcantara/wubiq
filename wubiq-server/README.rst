What is Wubiq?
-------------
Wubiq is a web / client combination that allows users to share its printing services (printers).
It consist of a web application companion (wubiq-server) and a client print manager (wubiq-client).

How it is done?
---------------
On the client side the client print manager identifies itself to the server exposing its available print services. 
The server on its parts registers those print services as its own.
Printing requests can then be directed to the server along with the print service name 
that will receive the document. The clients polls the server for its pending print jobs, if any, 
takes care of them and notifies the server of its success.

Documentation
-------------
Wiki pages are available at: **http://sourceforge.net/apps/mediawiki/wubiq/index.php?title=Main_Page**

Installing wubiq as a server
----------------------------
1. Download wubiq.zip and extract its files.
2. Deploy wubiq-server.war on a servlet container (this has been tested on Tomcat 6).
3. Copy wubiq-common.jar to your web application lib folder. (Re)start your servlet container if needed.
4. You can test if wubiq is running by opening a web browser and write: http://server:port/yourApplication/wubiq.do?command=printTestPage.
   If everything is fine you should see a test page on your browser or pdf viewer.

Using the wubiq manager
-----------------------
Wubiq has a manager from where you can check the status of connected clients and their services. 
Also provides a link for running wubiq-client on a local computer to expose connected devices.
Use its methods for installing and testing remote clients. 
To bring up the manager on any client open a browser and just type: http://your-host:port/wubiq-server.

Base methods
============

Installing on the client
------------------------
1. From the previous downnload extract wubiq-client.jar.
2. You can make a batch or script to run the client with the following:
   **java -jar wubiq-client.jar --host your-web-application-url**. For example:
   - java -jar wubiq-client.jar --host http://sicflex.com
   See the wubiq-client help by running java -jar wubiq-client.jar -?
3. To test if your clients are connecting on any browser write: http://server:port/yourApplication/wubiq.do?command=showPrintServices.
   If everything is working you should see a list of servers' print services including remote print services (R).

**Note: replace *server* with the server, *port* with the port (by default:8080), *yourApplication* with your web application content name.**

Testing remote printing
-----------------------
You can test remote printing by opening a web browser and run these tests urls. (Remember to have your clients running)

- To view a test page on the browser. **http://localhost:8080/wubiq-test/wubiq.do?command=printTestPage**
- To show available print services. **http://localhost:8080/wubiq-test/wubiq.do?command=showPrintServices**.
  Take note of the print service name and the uuid (should be 1234 for these tests).  
- To print a test page to a remote print service (assuming the remote printer is shown in print services as  *HP840C (R) computername*).
  **http://localhost:8080/wubiq-test/wubiq.do?command=printTestPage&printServiceName=HP840C&&uuid=1234**

Limitations
-----------
- Administration services are not yet implemented for pending printing jobs.
- Client program must be running on each client computer that wish to use remote print services.


