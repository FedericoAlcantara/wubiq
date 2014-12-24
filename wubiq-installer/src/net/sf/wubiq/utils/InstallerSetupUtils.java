/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.wubiq.common.ConfigurationKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for generating setups
 * @author Federico Alcantara
 *
 */
public class InstallerSetupUtils {
	
	private static final Log LOG = LogFactory.getLog(InstallerSetupUtils.class);
	
	private boolean stopClient = false;
	
	public static void main(String[] args) throws Exception {
		String connection = "";
		String group = "";
		if (args.length > 0) {
			connection = args[0];
			if (args.length > 1) {
				group = args[1];
			}
		}
		File outputFile = new File("output.jar");
		LOG.info(outputFile.getAbsolutePath());
		generateSetup(connection, group, new FileOutputStream(outputFile));
	}
	
	/**
	 * Generates setup.
	 * @param connections Connections to connect to.
	 * @param group Associated group.
	 * @param output Where the output is going to be output.
	 * @throws IOException
	 */
	public static void generateSetup(String connections, String group, 
			OutputStream output) throws IOException {
		InstallerSetupUtils u = new InstallerSetupUtils();
		u.makeSetup(connections, group, output);
	}
	
	/**
	 * The current version of wubiq.
	 * @return The current version of wubiq.
	 */
	public static String getVersion() {
		return Labels.VERSION;
	}
	
	/**
	 * Generates a setup based on the connection and group.
	 * @param connection Connection to associate the setup with.
	 * @param group Associated group.
	 * @return Made setup.
	 * @throws IOException Thrown on any compilation error.
	 */
	@SuppressWarnings("unchecked")
	private void makeSetup(String connection, String group, OutputStream output) throws IOException {
		File makeFolder = File.createTempFile("w-make", ""); // make folder
		File tempFolder = null;
		try {
			String setupName = "wubiq-setup.jar";
			makeFolder.mkdirs();
			makeFolder.delete();
			makeFolder.mkdirs();
			
			LOG.info("Generating:" + makeFolder.getAbsolutePath());
						
			File currentFolder = null;
			URL folderUrl = this.getClass().getResource("/installation");
			if (folderUrl != null) {
				File folder = new File(folderUrl.getFile());
				folder = folder.getParentFile();
				if (folder.getName().endsWith("!")) { // it is a jar file let's unjar it
					tempFolder = File.createTempFile("w-unjarred", "");
					tempFolder.mkdirs();
					tempFolder.delete();
					tempFolder.mkdirs();
					String tempFolderDest = tempFolder.getAbsolutePath();
					String jarFileName = folder.getName().substring(0, folder.getName().lastIndexOf('!'));
					URL jarFileURL = new URL(folder.getParentFile().getPath() + File.separator + jarFileName);
					
					File jarFile = new File(jarFileURL.getFile());
					ZipFile jar = null;
					try {
						jar = new ZipFile(jarFile);
						Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) jar.entries();
						// Create folders
						if (entries != null) {
							while (entries.hasMoreElements()) {
								ZipEntry entry = entries.nextElement();
								if (entry.isDirectory()) {
									new File(tempFolderDest + File.separator + entry.getName()).mkdirs();
								}
							}
							entries =  (Enumeration<ZipEntry>) jar.entries();
							while (entries.hasMoreElements()) {
								ZipEntry entry = entries.nextElement();
								if (!entry.isDirectory()) {
									IOUtils.INSTANCE.copy(jar.getInputStream(entry), new FileOutputStream(tempFolderDest + File.separator + entry.getName()));
								}
							}
							currentFolder = new File(tempFolder.getAbsolutePath() + File.separator + "installation");
						}
					} finally {
						if (jar != null) {
							jar.close();
						}
					}
				}
			}
			if (currentFolder == null) {
				currentFolder = new File ("./installation");
			}
			copyFiles(currentFolder, makeFolder);
			
			createPropertyFile(makeFolder, connection, group);
			
			String izpackHome = makeFolder.getAbsolutePath() + File.separator + "IzPack";
			String[]command = new String[]{
					jrePath(), "-Xmx512m", "-classpath", izpackHome + File.separator + "lib" + File.separator + "standalone-compiler.jar", 
					"-Dtools.jar=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Classes/classes.jar", 
					"-Dizpack.home=" + izpackHome,
					"com.izforge.izpack.compiler.Compiler",
					"install.xml",
					"-b", ".",
					"-o", setupName,
					"-k", "standard"};
			makeSetup(makeFolder, command);
			IOUtils.INSTANCE.copy(new FileInputStream(
					makeFolder.getAbsolutePath() + File.separator + setupName), output);
			output.flush();
			output.close();
		} finally {
			if (makeFolder != null) {
				deleteDir(makeFolder);
			}
			if (tempFolder != null) {
				deleteDir(tempFolder);
			}
		}
	}
	
	/**
	 * Creates a property file wubiq-installer.properties.
	 * @param makeFolder Installation folder.
	 * @param connection Connection to associate with.
	 * @param group Group Associated group.
	 * @return True if the property file was created, false otherwise.
	 * @throws IOException
	 */
	private boolean createPropertyFile(File makeFolder, String connection, String group) 
				throws IOException {
		boolean returnValue = false;
		// Creates the property file
		if ((connection != null && !connection.equals("")) ||
				(group != null && !group.equals(""))) {
			File propertyFile = new File(makeFolder.getAbsolutePath() + File.separator + "wubiq-installer.properties");
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new FileWriter(propertyFile));
				writer.println("# " + new Date().toString());
				if (!Is.emptyString(group)) {
					writer.println("uuid=" + group + "-${ENV[COMPUTERNAME]}");
				} else {
					writer.println("uuid=${ENV[COMPUTERNAME]}");
				}
				if (connection != null && !connection.equals("")) {
					writer.println(ConfigurationKeys.PROPERTY_CONNECTIONS + "=" + connection);
				}
				if (group != null && !group.equals("")) {
					writer.println(ConfigurationKeys.PROPERTY_GROUPS + "=" + group);
				}
				writer.flush();
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
			returnValue = true;
		}
		return returnValue;
	}
	
	/**
	 * Copy files from source folder to destination folder.
	 * @param sourceFolder Source folder.
	 * @param destinationFolder Destination folder.
	 * @throws FileNotFoundException If file not found occurs.
	 * @throws IOException Exception If can't write or read.
	 */
	private void copyFiles(File sourceFolder, File destinationFolder) throws FileNotFoundException, IOException {
		for (File child : sourceFolder.listFiles()) {
			if (child.isDirectory()) {
				File newDestinationFolder = new File(destinationFolder.getPath() + File.separator + child.getName());
				newDestinationFolder.mkdirs();
				copyFiles(child, newDestinationFolder);
			} else {
				copyFile(child, destinationFolder);
			}
		}
	}

	/**
	 * Copies one file to another.
	 * @param sourceFile Source file.
	 * @param destinationFolder Destination folder.
	 * @throws FileNotFoundException If file is not found.
	 * @throws IOException If can't write to folder.
	 */
	private void copyFile(File sourceFile, File destinationFolder) throws FileNotFoundException, IOException {
		IOUtils.INSTANCE.copy(new FileInputStream(sourceFile), 
				new FileOutputStream(
						new File(destinationFolder.getPath() + File.separator + sourceFile.getName())));
	}
	
	/**
	 * Current java run time path.
	 * @return JRE path.
	 */
	private String jrePath() {
		if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
			return System.getProperty("java.home") + "/bin/java.exe";
		}
		return System.getProperty("java.home") + "/bin/java";
	}
	
	/**
	 * Wubiq client run.
	 * @param wubiqClientJar Wubiq client jar file.
	 * @param sentParameters Sent parameters.
	 * @return Integer value with the command output.
	 */
	private int makeSetup(File startDirectory, String... command) throws IOException {
		int returnValue = 0;
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(startDirectory);
		processBuilder.command(command);
		Process currentProcess = null;
		try {
			currentProcess = processBuilder.start();
			StreamHandler stdOutHandler = new StreamHandler(currentProcess.getInputStream());
			StreamHandler stdErrorHandler = new StreamHandler(currentProcess.getErrorStream());
			Thread stdOut = new Thread(stdOutHandler, "StdOut");
			Thread stdErr = new Thread(stdErrorHandler, "StdErr");
			stdOut.start();
			stdErr.start();
			returnValue = currentProcess.waitFor();
			if (returnValue != 0) {
				throw new IOException(stdErrorHandler.getMessages());
			}
			LOG.info(stdOutHandler.getMessages());
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			returnValue = 1;
		}
		return returnValue;
	}
	
	/**
	 * Handles the output and error stream of a running process.
	 */
	private class StreamHandler implements Runnable {
		private InputStream stream;
		private StringBuffer messages;
		
		private StreamHandler(InputStream stream) {
			this.stream = stream;
		}
		
		public void run() {
			BufferedReader reader = null;
			String line = null;
			messages = new StringBuffer("");
			try {
				reader = new BufferedReader(new InputStreamReader(stream));
				while ((line = reader.readLine()) != null) {
					if (messages.length() > 0) {
						messages.append('\n');
					}
					messages.append(line);
					if (stopClient) {
						break;
					}
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}

		}

		/**
		 * @return the messages
		 */
		public String getMessages() {
			return messages.toString();
		}

	}

	/**
	 * Deletes a folder completely
	 * @param folder Folder to be deleted
	 */
	private void deleteDir(File folder) {
		for (File child : folder.listFiles()) {
			if (child.isDirectory()) {
				deleteDir(child);
			} else {
				child.delete();
			}
		}
		folder.delete();
	}
}
