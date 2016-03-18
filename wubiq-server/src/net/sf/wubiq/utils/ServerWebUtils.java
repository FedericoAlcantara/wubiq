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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
			.append("<meta http-equiv=\"refresh\" content=\"3," + url + "\"/>")
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
							ips.add(processLine);
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
							ips.add(address.getHostAddress().trim());
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
			return InetAddress.getByName(ip).isReachable(200);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnValue;
	}
}
