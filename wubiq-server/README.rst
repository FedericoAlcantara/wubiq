======================
Wubiq 0.1 (2011-06-27)
======================
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

Installing wubiq
----------------
1.Download wubiq-server.jar, wubiq-common.jar and wubiq-client.jar.
2.Copy wubiq-server.jar and wubiq-common.jar to your web application lib folder. (Re)start your web application.
3.You can test if wubiq is running by opening a web browser and write: http://server:port/yourApplication/wubiq.do?command=printTestPage.
If everything is fine you should see a test page on your browser or pdf viewer.
4.In each computer make a batch or script to run the client with the following:
java -jar wubiq-client.jar --host server --port port --app yourApplication --uuid=anyUniqueCombination.
5.To test if your clients are connecting on any browser write: http://server:port/yourApplication/wubiq.do?command=showPrintServices.
If everything is working you should see a list of servers' print services including remote print services (R).

***Note: replace *server* with the server, *port* with the port (by default:8080), *yourApplication* with your web application content name.**

Testing remote printing
-----------------------
You can test remote printing by downloading wubiq-test.war and deploying it on a tomcat 6 container.
Assuming it is running on localhost:8080 you can perform the following on a browser.
After starting up the server, run client(s) with: **java -jar wubiq-client.jar --app wubiq-test --uuid 1234**.

Open a web browser and run these tests urls.

- To view a test page on the browser. **http://localhost:8080/wubiq-test/wubiq.do?command=printTestPage**
- To show available print services. **http://localhost:8080/wubiq-test/wubiq.do?command=showPrintServices**.
  Take note of the print service name and the uuid (should be 1234 for these tests).  
- To print a test page to a remote print service (assuming the remote printer is shown in print services as  *HP840C (R) computername*).
 **http://localhost:8080/wubiq-test/wubiq.do?command=printTestPage&printServiceName=HP840C&&uuid=1234**


Limitations
-----------
- In its first delivery wubiq only allows printing of pdf stream/files.
  Later installments should handle other type of documents.
- Administration services are not yet implemented for pending printing jobs.

