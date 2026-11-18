/**
 *
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Updates the packaged client by downloading and installing the package the server offers.
 * @author Federico Alcantara
 *
 */
public enum PackageUpdater {
	INSTANCE;

	private static final Log LOG = LogFactory.getLog(PackageUpdater.class);

	private String failedVersion;

	/**
	 * Downloads and installs the offered version when it is newer than this client.
	 * @param offered Version the server offers.
	 * @param servletUrl Servlet URL to download the package from.
	 * @param uuid Client unique identifier.
	 * @return True when the installer ran and ended with exit code 0.
	 */
	public boolean updateIfNewer(String offered, URL servletUrl, String uuid) {
		if (!ClientPackages.isNewer(offered, Labels.VERSION)) {
			return false;
		}
		if (servletUrl == null || offered.equals(failedVersion)) {
			return false;
		}
		String platform = ClientPackages.currentPlatform();
		String type = InstallerUtils.INSTANCE.packageType();
		if (Is.emptyString(platform) || Is.emptyString(type)) {
			return false;
		}
		File target = new File(System.getProperty("java.io.tmpdir"), ClientPackages.fileName(offered, platform, type));
		try {
			download(servletUrl, uuid, platform, type, target);
			String[] command = ClientPackages.installCommand(type, target.getAbsolutePath());
			LOG.info("Updating from " + Labels.VERSION + " to " + offered + ": " + Arrays.toString(command));
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);
			processBuilder.redirectOutput(new File(System.getProperty("java.io.tmpdir"), "wubiq-client-update.log"));
			int exitCode = processBuilder.start().waitFor();
			if (exitCode == 0) {
				return true;
			}
			failedVersion = offered;
			LOG.error("The update to " + offered + " failed: the installer ended with " + exitCode + "; see wubiq-client-update.log");
			return false;
		} catch (IOException | InterruptedException e) {
			failedVersion = offered;
			LOG.error("The update to " + offered + " failed: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Streams the package from the server to the target file and checks its size.
	 * @param servletUrl Servlet URL.
	 * @param uuid Client unique identifier.
	 * @param platform Package platform.
	 * @param type Package type.
	 * @param target File to write the package to.
	 * @throws IOException When the server does not answer 200 or the package is incomplete.
	 */
	private void download(URL servletUrl, String uuid, String platform, String type, File target) throws IOException {
		String body = ParameterKeys.UUID + "=" + WebUtils.INSTANCE.encode(uuid)
				+ "&" + ParameterKeys.COMMAND + "=" + CommandKeys.READ_PACKAGE
				+ "&" + ParameterKeys.CLIENT_PLATFORM + "=" + platform
				+ "&" + ParameterKeys.CLIENT_PACKAGE_TYPE + "=" + type;
		HttpURLConnection connection = (HttpURLConnection) servletUrl.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(60000);
		connection.setUseCaches(false);
		OutputStream output = connection.getOutputStream();
		output.write(body.getBytes(StandardCharsets.UTF_8));
		output.close();
		int code = connection.getResponseCode();
		if (code != 200) {
			throw new IOException("the server answered " + code);
		}
		long expected = connection.getContentLengthLong();
		try {
			IOUtils.INSTANCE.copy(connection.getInputStream(), new FileOutputStream(target));
		} catch (IOException e) {
			target.delete();
			throw e;
		}
		if (expected <= 0 || target.length() != expected) {
			long actual = target.length();
			target.delete();
			throw new IOException("incomplete package: " + actual + " of " + expected + " bytes");
		}
	}
}
