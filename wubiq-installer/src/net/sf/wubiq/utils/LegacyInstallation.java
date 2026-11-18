/**
 *
 */
package net.sf.wubiq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Finds an installation made by the IzPack setup and takes it over: its configuration and its service.
 * @author Federico Alcantara
 *
 */
public enum LegacyInstallation {
	INSTANCE;

	private static final Log LOG = LogFactory.getLog(LegacyInstallation.class);

	private static final String SERVICE_NAME = "wubiq";
	private static final String INIT_SCRIPT = "/etc/init.d/wubiq";
	private static final String UPDATE_RC = "/usr/sbin/update-rc.d";
	private static final String PROPERTIES_FILE = "wubiq-installer.properties";

	/**
	 * Finds the bin directory of an IzPack installation.
	 * @return The old installation's bin directory, or null when there is none.
	 */
	public File find() {
		try {
			File directory = null;
			if (System.getProperty("os.name").startsWith("Windows")) {
				ProcessBuilder processBuilder = new ProcessBuilder("sc", "qc", SERVICE_NAME);
				processBuilder.redirectErrorStream(true);
				Process process = processBuilder.start();
				String binaryLine = null;
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						if (binaryLine == null && line.contains("BINARY_PATH_NAME")) {
							binaryLine = line;
						}
					}
				} finally {
					reader.close();
				}
				int exitCode = process.waitFor();
				if (exitCode != 0 || binaryLine == null) {
					return null;
				}
				String text = binaryLine.substring(binaryLine.indexOf(':') + 1).trim();
				String path;
				if (text.startsWith("\"")) {
					int end = text.indexOf('"', 1);
					path = end < 0 ? text.substring(1) : text.substring(1, end);
				} else {
					int end = text.indexOf(' ');
					path = end < 0 ? text : text.substring(0, end);
				}
				directory = new File(path).getParentFile();
			} else {
				File script = new File(INIT_SCRIPT);
				if (!script.isFile()) {
					return null;
				}
				BufferedReader reader = new BufferedReader(new FileReader(script));
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.trim().startsWith("cd ")) {
							directory = new File(line.trim().substring(3).trim());
							break;
						}
					}
				} finally {
					reader.close();
				}
			}
			if (directory != null) {
				LOG.info("IzPack installation found in " + directory);
			}
			return directory;
		} catch (IOException | InterruptedException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Copies the IzPack installation's properties file into the configuration directory, unless one is already there.
	 * @param legacyBin The old installation's bin directory.
	 */
	public void importConfiguration(File legacyBin) {
		File old = new File(legacyBin, PROPERTIES_FILE);
		File current = new File(InstallerUtils.INSTANCE.configDirectory(), PROPERTIES_FILE);
		try {
			if (old.isFile() && !current.exists()) {
				Files.copy(old.toPath(), current.toPath());
				InstallerProperties.INSTANCE().resetProperties();
				LOG.info("Configuration imported from " + old);
			} else if (!old.isFile()) {
				LOG.info("Configuration not imported: " + old + " is not a file");
			} else {
				LOG.info("Configuration not imported: " + current + " already exists");
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Removes the IzPack installation's service.
	 */
	public void removeService() {
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				LOG.info("sc stop " + SERVICE_NAME + " ended with " + run("sc", "stop", SERVICE_NAME));
				LOG.info("sc delete " + SERVICE_NAME + " ended with " + run("sc", "delete", SERVICE_NAME));
			} else {
				if (new File(UPDATE_RC).canExecute()) {
					LOG.info("update-rc.d -f " + SERVICE_NAME + " remove ended with " + run(UPDATE_RC, "-f", SERVICE_NAME, "remove"));
				}
				LOG.info("Deleting " + INIT_SCRIPT + ": " + new File(INIT_SCRIPT).delete());
			}
		} catch (IOException | InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Runs a command, discards its output and waits for it.
	 * @param command Command and its arguments.
	 * @return Exit code.
	 * @throws IOException When the command cannot be started or read.
	 * @throws InterruptedException When the wait is interrupted.
	 */
	private int run(String... command) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try {
			while (reader.readLine() != null) {
				// discarded
			}
		} finally {
			reader.close();
		}
		return process.waitFor();
	}
}
