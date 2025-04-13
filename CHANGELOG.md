## Changelog
| version | date | description |
| ------- | ---- | ----------- |
| Wubiq 2.6.1 | 2025-04-13 | -Wubiq-android-gradle: Compatible from android 4 (api 14) to android 15 (api 35).
| Wubiq 2.5.3 | 2024-09-22 | -Wubiq-android-gradle: Compatible from android 4 (api 14) to android 14 (api 34). Improved Bluetooth handling.
| Wubiq 2.5.1 | 2020-10-25 | -Wubiq-android-gradle: Compatible from android(5.1) version 22 to android(14.0) api version 34 
| Wubiq 2.4.3e | 2020-10-25 | -Wubiq-server: Fixed data source errors when NOT using persistence.
| Wubiq 2.4.3d | 2019-03-13 | -Wubiq-android-gradle: Fix to handle notification errors with version less than android 8.
| Wubiq 2.4.3b | 2019-02-21 | -Wubiq-android-gradle: Compatible from android(4.0) version 14 up to android (9) version 28.
| Wubiq 2.4.2  | 2018-12-13 | -Wubiq-android-gradle: Compatible up to android (9) version 28.
| Wubiq 2.4.1 | 2018-12-06 | - Wubiq-android-gradle: Fixed incompatibility with android version 23 - 24.
| Wubiq 2.4b | 2018-12-02 | -Wubiq-android-gradle: Fixed displayed items for spanish locale.
| Wubiq 2.4 | 2018-12-02 | -Wubiq-android-gradle: All changes to android development are produced here.<br/> -Wubiq-android-gradle: Virtual device enabled for testing purposes<br/> -Wubiq-android-gradle: Compatible from android version 23 and up.<br/> -Wubiq-android-gradle: Removed suppress notification checkbox, notification suppression relies on android os settings.<br/> -Wubiq-common: Code compatible with virtual devices. 
| Wubiq 2.3.4 | 2018-11-30| -Wubiq-android: Frozen build compatible up to version 22 (5.1 lollipop). New development will be on wubiq-android-gradle.<br/> -Wubiq-client: Build script modified to produce a jar without wubiq-common bits.<br/> -Wubiq-common: Remote print job handles fixed wrong behavior with test pdf sent from wubiq-server page.
| Wubiq 2.3.3.3 | 2018-08-10 | -Wubiq-server: Allow the server to continue start even on table creation errors.
| Wubiq 2.3.3.2 | 2018-08-10 | -Wubiq-server: Ip/host discovery improved.
| Wubiq 2.3.3 | 018-08-08 | -Wubiq-server: Java 8 / Tomcat 8 compatible.<br/> -Wubiq-server: Can handle multiple ports for communication.<br/> -Wubiq-server: Can be in more than one tomcat instance in the same server.<br/> -Wubiq-android: Support removed for compatible option of single host / port combination.<br/> -Wubiq-android: For Android 4.0 Ice Cream Sandwich (Api level 14).<br/> -Packaging: Relies on provided ant task for packaging wubiq components.
| Wubiq 2.3.2.5 | 2018-03-22 | -Improved log information for PrintableWrapper.

#OLD VERSIONS

Wubiq 2.3.2.4 (2017-10-08)
-Fixed hangs when trying to run corrupted wubiq-cient.jar on services (wubiq-installer)
 
Wubiq 2.3.2.3 (2017-08-04)
-Fixed missing groups information when received from mobile devices
-Shows group for remote services

Wubiq 2.3.2.2 (2017-07-02)
-Fixed missing groups information when creating print job from persistence.

Wubiq 2.3.2.1 (2017-04-08)
-Copyrighted images eliminated from android app.

Wubiq 2.3.2 (2017-02-18)
-PdfBox / FontBox libraries downgraded to 1.8.12

Wubiq 2.3.1 (2016-05-17)
-PdfBox / FontBox libraries upgraded to 2.0.1.
-Mobile devices can print pageable / printable printouts directly.
-Fixed unnecessary errors when printing on mobile devices.
-Fixed failure to remove printed print jobs.
-Fixed bug when using multiple connection addresses.

Wubiq 2.2.2 (2016-05-10)
-Fixed dropped prints on mobile devices when successive prints were sent.

Wubiq 2.2.1 (2016-04-20)
-Support of data sources for cluster and servers escalation capabilities.

Wubiq 2.1.6 (2015-12-16)
-When parsing attributes of print services with semicolon in it, throws errors.

Wubiq 2.1.5 (2015-11-17)
-Fixed errors while closing print jobs prevented further printing.

Wubiq 2.1.4 (2015-11-16)
-Fixed print service hold after a couple of prints, preventing more printing.

Wubiq 2.1.3 (2015-11-14)
-Compiled with java 6.

Wubiq 2.1.2 (2015-10-28)
-Fix report printing order when outputting several print jobs to same client + printer.

Wubiq 2.1.1 (2015-03-05)
-New communications engine for reducing bandwidth by more than 90%
-Parallel printing to different print services within the same client.
-Fixed memory fault when printing large pageable documents.
-Printing status on print services.
-Pending jobs to print no longer requires login to be seen.

Wubiq 2.0.18 (2015-02-26)
-Improved resilience against communication instability.

Wubiq 2.0.17 (2015-01-08)
-Fixed missed parameters handling in wubiq-installer.

Wubiq 2.0.16 (2015-01-07)
-Fixed null pointer exception on server.
-Stopping clients is forbidden for none logged users.
-Remote client filter is enabled in wubiq-server console.
-Localization enabled in wubiq-server console.

Wubiq 2.0.15 (2014-12-28)
- Merry Christmas and Happy New Year!
- Adds group as part of UUID on installer configuration.

Wubiq 2.0.14 (2014-12-03)
- Ensures compatibility with old servers.
- Added image for android app store.

Wubiq 2.0.13 (2014-11-29)
- Bug #1. Print jobs with images might stall wubiq clients.
- Bug #2. Remove all print jobs not working.

Wubiq 2.0.12 (2014-10-05)
-Wubiq can now create and register custom media size, including wider than taller ones!
-Wubiq android updated to be compatible with versions 2.0 and up.

Wubiq 2.0.11 (2014-09-26)
-Fixed incompatibilities with java 1.6.

Wubiq 2.0.10 (2014-09-23)
-Added group management. Allows to group clients.
-Added jobs web management. Allows to pause clients, remove print jobs.
-PdfBox / FontBox libraries upgraded to 1.8.6.
-Android application notifications bugs fixed.
-Android application improved connection reliability.
-Implemented service installer for windows, macos-x and linux. 
-Android application support for 2.1 dropped. Use version 1.7.6 and below.
-Star micronics library for android upgraded to support newer androids.
-Improved printing quality for remote clients.
-Universal installer for Windows, MacOSX and linux.
-Fix to orientation issues when printing on non standard papers.

Wubiq 1.9.0 (2014-06-20)
-New remote printing methodology.
-Large remote printing now works without client out of memory.

Wubiq 1.8.1 (2014-05-25)
-Improved font handling for dot matrix printing

Wubiq 1.8.0 (2014-02-01)
-Improved media handling when clients and server are running different jvm implementations.
-Added option: connections that allows several connections for different clients.
-Wubiq android: Added notifications of the status of the connection.

Wubiq 1.7.6 (2013-11-17)
-Fixed pages printed in a wrong format when receiving multiple transformation commands. 

Wubiq 1.7.5 (2013-11-15)
-Fixed blank pages after first page.

Wubiq 1.7.4 (2013-09-04)
-Fixed error when printing images on Mac OSX.
-Multiple pages Printable implemented for PrintRequestAttribute with PageRanges of more than one page.
-GlyphVectorWrapper modified to be produced by Font classes.
-Fixed NPE when printing on mobile devices.
-Deprecated class ServerPrintDirectUtils removed.

Wubiq 1.7.3 (2013-05-15)
-Fixed black strip while printing on mobile printers.
-Scalr library upgraded from 4.0 to 4.2. 

Wubiq 1.7.2 (2013-05-14)
-Fixed printing issues with mobile printing for zebra printers.
-Improved printing compatibility for dot matrix printers connected to MacOS.

Wubiq 1.7.1 (2013-05-04)
-Improved printing compatibility for dot matrix printers.
-Fixed different scales for same document on local and remote printers.
-Copies now handled by javax.printing api.
-Remote jobs name are correcly printed.

Wubiq 1.6.2 (2013-03-05)
-Prevents calls to db before it is completely initialized.

Wubiq 1.6.1 (2012-10-11)
-Fixed multiple document printing to android devices.

Wubiq 1.6.0 (2012-10-10)
-Android better communication support for bluetooth devices.

Wubiq 1.5.1 (2012-09-28)
-Android Manifest updated to reflect supported API.
-Fixed print test page mis-behavior when testing from an external web-browser.

Wubiq 1.5 (2012-09-22)
-Android added support for bluetooth printers: Zebra MZ series, Datamax - O'Neil Apex/Andes Series.
-Published in google play.

Wubiq 1.4 (2012-08-08)
-Improved printing quality for mobile printer from Crystal Reports.

Wubiq 1.3 (2012-08-06)
-Android handles advanced settings now.
-Fixed print test button
-Fixed kill client button

Wubiq 1.1 (2012-07-19)
-Added interval and wait command line parameters.

Wubiq 1.0.236 (2012-05-19)
-Fixed error in Android application.
-Added support for new devices client development.
-Implemented report bottom trimming for mobile devices.

Wubiq 1.0.206 (2012-04-05)
-Improved performance in data transfer.
-Added selectable font to TextPageable.

Wubiq 1.0.203 (2012-03-30)
-Text printing adjusted for taking into consideration printers' imageable area

Wubiq 1.0.201 (2012-03-28)
-TextPageable, to generate text reports that can be printed to anywhere easily
-Version display.

Wubiq 1.0.188 (2012-03-21)
-Replace client printing engine with Doc instead of PrinterJob

Wubiq 1.0.186 (2012-03-16)
-Fixed attributes to remotes.

Wubiq 1.0.183 (2012-03-14)
-Fixed issues with RenderingHints.
-Fixed stroke issues.
-Implemented setComposite graphic command.
-Fixed aspect ratio of remote pages.

Wubiq 1.0.175 (2012-03-10)
-Pageable and Printable printing completed.
-Fixed scaling issues after serialization.
-Fixed image rendering on client.

Wubiq 1.0.170 (2012-03-06)
-Serialization of printable completed for most graphic commands.

Wubiq 1.0.168 (2012-02-28)
-Implemented Pageable and Printable direct printing.
-Solved compatibility with print service users which rely on PrinterName attribute.
-Included wubiq-printerjob to handle application that uses PrinterJob pageable or printable.

Wubiq 0.9.127 (2012-02-14)
- Changed output to print services from pdf or png to pageable. Now is portable across operating systems.

Wubiq 0.9.121 (2012-02-12)
- Fixed client print service refresh failure when disconnected from the host.

Wubiq 0.9.119 (2012-02-11)
- Printing from windows old printer drivers fixed.
- Multiple page documents for old windows drivers enabled.
- Smaller footprint for wubiq-server.

Wubiq 0.8 (2012-02-02)
- Improved printer lookup.

Wubiq 0.7 (2011-11-30)
- Added Android application for bluetooth devices.
- Bluetooth devices supported: Star Micronics SM models, Woosim Porti-S models.

Wubiq 0.6 (2011-11-11)
- Refresh of server services improved.

Wubiq 0.5 (2011-07-08)
- Wubiq Manager. Implementation of a web manager
- Auto refresh of print services when the server fails.
- Remote killing of the client.

Wubiq 0.4 (2011-07-05)
wubiq-client
------------
- Automatically recovers from server failures.
- Verbose option for tracing its behavior.

wubiq-server
------------
- Converted to web application.
- Implements hsqldb based print management, making it more scalable.
- Support for external server configuration.

wubiq-common
------------
- Comprises all elements for accessing the remote print functions from web application or clients.

Wubiq 0.3 (2011-07-01)
- wubiq-client rewritten to remove use of HtmlUnit. Size of jar changed from 12mb to 184kb.

Wubiq 0.2 (2011-06-28)
New Features
------------
- Printing can be performed from other formats besides PDF.

Fixes
-----
- wubiq-client.jar manifest was not according to Jar_specifications_.
- Wrong computer id gathered upon initialization in wubiq-server.

.. _Jar_specifications: http://download.oracle.com/javase/1.4.2/docs/guide/jar/jar.html

Wubiq 0.1 (2011-06-27)
- First version.
