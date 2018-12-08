/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import net.sf.wubiq.listeners.ContextListener;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public enum ServerWebUtils {
	INSTANCE;
	
	private final Log LOG = LogFactory.getLog(ServerWebUtils.class);
	private final String LOCALHOST = "127.0.0.1";
	
	/**
	 * Creates a back response after performing actions that produce a user information.
	 * @param request Originating request.
	 * @param body Contents to be shown in the html body. 
	 * @return A Html string to be used.
	 * @throws IOException
	 */
	public String backResponse(HttpServletRequest request, String body) throws IOException {
		StringBuffer returnValue = new StringBuffer("");
		String context = request.getContextPath().substring(1);
		
		String url =  "/" + context;
		returnValue.append("<html>")
			.append("<header>")
			.append("<meta http-equiv=\"refresh\" content=\"3, " + url + "\"/>")
			.append("</header>")
			.append("<body>")
			.append(body)
			.append("</body>")
			.append("</html>");

		return returnValue.toString();
	}
	
	/**
	 * Gets all server ips. Only adds IPv4 addresses.
	 * @return A list of servers ips. 
	 */
	public List<String> serverIps() {
		List<Integer> serverPorts = httpPorts();
		List<String> returnValue = new ArrayList<String>();
		Set<String> ips = new HashSet<String>();
		File ipConfFile = ipConfFile();
		if (ipConfFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(ipConfFile));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (!Is.emptyString(line.trim())) {
						String processLine = (line.trim().split("#")[0]).trim();
						if (!Is.emptyString(processLine) &&
								!LOCALHOST.equals(processLine)) {
							if (serverPorts.size() > 0) {
								for (int serverPort : serverPorts) {
									ips.add(processLine + ":" + serverPort);
								}
							} else {
								ips.add(processLine);
							}
						}
					}
				}
				reader.close();
				returnValue.addAll(ips);
			} catch (FileNotFoundException e) {
				LOG.debug(ExceptionUtils.getMessage(e));
			} catch (IOException e) {
				LOG.debug(ExceptionUtils.getMessage(e));
			}
		}
		// Tries to use the ips of the server.
		if (ips.isEmpty()) {
			String serverIp = serverIp();
			if (!Is.emptyString(serverIp)) {
				if (serverPorts.size() > 0) {
					for (int serverPort : serverPorts) {
						ips.add(serverIp + ":" + serverPort);
					}
				} else {
					ips.add(serverIp);
				}

			}
		}
		if (ips.isEmpty()) {
			try {
				Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
				while (networkInterfaces.hasMoreElements()) {
					NetworkInterface networkInterface = networkInterfaces.nextElement();
					Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress address = addresses.nextElement();
						if (address.getAddress().length == 4 &&
								!LOCALHOST.equals(address.getHostAddress().trim())) { // IPv4
							if (serverPorts.size() > 0) {
								for (int serverPort : serverPorts) {
									ips.add(address.getHostAddress().trim() + ":" + serverPort);
								}
							} else {
								ips.add(address.getHostAddress().trim());
							}
						}
					}
				}
			} catch (SocketException e) {
				LOG.debug(ExceptionUtils.getMessage(e));
			}
		}
		returnValue.addAll(ips);
		return returnValue;
	}

	/**
	 * External file location for ip information. 
	 * @return Ip address.
	 */
	private File ipConfFile() {
		File returnValue = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			String appData = System.getenv("ALLUSERSAPPDATA");
			if (!Is.emptyString(appData)) {
				returnValue = new File(appData + File.separator + "/wubiq-server-ip.conf");
			}
		} else {
			returnValue = new File("/etc/wubiq-server-ip.conf");
		}
		return returnValue;
	}
	
	/**
	 * Checks if the server is enumerated in its ip. By default address 127.0.0.1 is IGNORED.
	 * @param ownIps List of current servers ips.
	 * @param otherIps List of other ips.
	 * @return True if the ip is present in other ips.
	 */
	public boolean hasAddress(List<String> ownIps, List<String> otherIps) {
		boolean returnValue = false;
		for (String ownIp : ownIps) {
			if (LOCALHOST.equals(ownIp)) {
				continue;
			}
			if (otherIps.contains(ownIp)) {
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}
	
	/**
	 * Checks if an IP is reachable.
	 * @param ip Ip to test.
	 * @return True if ip is reachable, false otherwise.
	 */
	public boolean canConnect(String ip) {
		try {
			return InetAddress.getByName(ip.split(":")[0]).isReachable(200);
		} catch (UnknownHostException e) {
			LOG.debug(ExceptionUtils.getMessage(e));
		} catch (IOException e) {
			LOG.debug(ExceptionUtils.getMessage(e));
		} catch (Exception e) {
			LOG.debug(ExceptionUtils.getMessage(e));
		}
		return false;
	}
	
	/**
	 * Finds the computer name.
	 * @return Computer name found or null.
	 */
	public String computerName() {
		String returnValue = null;
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			if (localHost != null) {
				returnValue = localHost.getHostName();		
			}
		} catch (UnknownHostException e) {
			LOG.error(ExceptionUtils.getMessage(e));
		}
		
		return returnValue;
	}
	
	/**
	 * Minimum date.
	 * @return Minimum default date.
	 */
	public Date minimumDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(1900, 00, 01);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 1);
		return calendar.getTime();
	}
		
	/**
	 * Determines if the server ip contains the given ip.
	 * Takes into account if the server is an old version.
	 * @param ip Current ip.
	 * @return True if the ip is within the list of server.
	 */
	public boolean containsIp(String ip) {
		boolean removeSemicolon = ip.contains(":");
		for (String ipRead : ContextListener.serverIps()) {
			String cleanedIp = ipRead;
			if (removeSemicolon && ipRead.contains(":")) {
				cleanedIp = ipRead.split(":")[0];
			}
			if (cleanedIp.equals(ip)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the http (or https) ports.
	 * @return Http/s ports.
	 */
	private List<Integer> httpPorts() {
		List<Integer> returnValue = new ArrayList<Integer>();
		MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
		ObjectName name;
		try {
			name = new ObjectName("Catalina:type=Connector,*");
			for (ObjectName connector : mBeanServer.queryNames(name, null)) {
				MBeanInfo info = mBeanServer.getMBeanInfo(connector);
				System.out.println(connector);
				for (MBeanAttributeInfo atInf : info.getAttributes()) {
					System.out.println("     " + atInf.getName() + "=" + mBeanServer.getAttribute(connector, atInf.getName()));
				}
				String scheme = (String) mBeanServer.getAttribute(connector, "scheme");
				String protocol = (String) mBeanServer.getAttribute(connector, "protocol");

				if ("http".equalsIgnoreCase(scheme)
						&& protocolIsHttp(protocol)) {
					int port = (Integer)mBeanServer.getAttribute(connector, "port");
					if (port > 0) {
						returnValue.add(port);
					}
				}
			}
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getMessage(e));
		}
		return returnValue;
	}
	
	/**
	 * Tries to get the server Ip from the engine or the host.
	 * @return
	 */
	private String serverIp() {
		String returnValue = "";
		MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
		ObjectName name;
		try {
			name = new ObjectName("Catalina:type=Engine");
			String defaultHost = (String) mBeanServer.getAttribute(name, "defaultHost");
			if (!LOCALHOST.equalsIgnoreCase(defaultHost)
					&& defaultHost.contains(".")) {
				returnValue = defaultHost;
			} else {
				name = new ObjectName("Catalina:type=Host");
				defaultHost = (String) mBeanServer.getAttribute(name, "name");
				if (!LOCALHOST.equalsIgnoreCase(defaultHost)
						&& defaultHost.contains(".")) {
					returnValue = defaultHost;
				}
			}
			
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getRootCauseMessage(e));
		}
		return returnValue;
	}
	
	/**
	 * Determines if the protocol is HTTP or HTTPS.
	 * @param protocol Protocol to check
	 * @return True if protocol is http/s.
	 */
	private boolean protocolIsHttp(String protocol) {
		return protocol.toLowerCase().indexOf("http") > -1;
	}
}
