package net.sf.wubiq.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rules of the packaged client: versions, package file names, platforms and
 * unattended install commands.
 * @author Federico Alcantara
 *
 */
public final class ClientPackages {
	public static final String FILE_PREFIX = "wubiq-client-";

	private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
			"^wubiq-client-(\\d+(?:\\.\\d+)*)-(windows-x64|macos-arm64|macos-x64|linux-x64)\\.(msi|pkg|deb|rpm)$");
	private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)*$");

	private static final String[] PLATFORMS = {"windows-x64", "macos-arm64", "macos-x64", "linux-x64"};

	private ClientPackages() {
	}

	/**
	 * Compares two versions part by part as numbers. A missing part counts as 0.
	 * @param left First version.
	 * @param right Second version.
	 * @return Negative, 0 or positive as left is lower, equal or higher than right.
	 * @throws IllegalArgumentException when either version is null or malformed.
	 */
	public static int compareVersions(String left, String right) {
		checkVersion(left);
		checkVersion(right);
		String[] leftParts = left.split("\\.");
		String[] rightParts = right.split("\\.");
		int length = Math.max(leftParts.length, rightParts.length);
		for (int index = 0; index < length; index++) {
			long leftValue = index < leftParts.length ? Long.parseLong(leftParts[index]) : 0L;
			long rightValue = index < rightParts.length ? Long.parseLong(rightParts[index]) : 0L;
			if (leftValue != rightValue) {
				return leftValue > rightValue ? 1 : -1;
			}
		}
		return 0;
	}

	/**
	 * Tells whether an offered version is newer than the own version.
	 * @param offered Offered version.
	 * @param own Own version.
	 * @return True only when both are well formed and offered is higher than own.
	 */
	public static boolean isNewer(String offered, String own) {
		if (!isVersion(offered) || !isVersion(own)) {
			return false;
		}
		return compareVersions(offered, own) > 0;
	}

	/**
	 * Builds the file name of a client package.
	 * @param version Client version.
	 * @param platform Platform of the package.
	 * @param type Package type.
	 * @return The package file name.
	 */
	public static String fileName(String version, String platform, String type) {
		return FILE_PREFIX + version + "-" + platform + "." + type;
	}

	/**
	 * Reads a client package file name.
	 * @param fileName File name to read.
	 * @return Version, platform and type, or null when the name is not a client package name.
	 */
	public static String[] parse(String fileName) {
		if (fileName == null) {
			return null;
		}
		Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
		if (!matcher.matches()) {
			return null;
		}
		return new String[] {matcher.group(1), matcher.group(2), matcher.group(3)};
	}

	/**
	 * Finds the package with the highest version for a platform and type.
	 * @param directory Directory holding the packages.
	 * @param platform Platform wanted.
	 * @param type Package type wanted.
	 * @return The package file, or null when none is offered.
	 */
	public static File latest(File directory, String platform, String type) {
		if (directory == null || !directory.isDirectory()) {
			return null;
		}
		File[] files = directory.listFiles();
		if (files == null) {
			return null;
		}
		File latestFile = null;
		String latestVersion = null;
		for (File file : files) {
			if (!file.isFile()) {
				continue;
			}
			String[] parts = parse(file.getName());
			if (parts == null || !parts[1].equals(platform) || !parts[2].equals(type)) {
				continue;
			}
			if (latestVersion == null || compareVersions(parts[0], latestVersion) > 0) {
				latestFile = file;
				latestVersion = parts[0];
			}
		}
		return latestFile;
	}

	/**
	 * Maps an operating system name and architecture to a package platform.
	 * @param osName Value of os.name.
	 * @param osArch Value of os.arch.
	 * @return The platform, or blank when no package exists for it.
	 */
	public static String platform(String osName, String osArch) {
		if (osName == null || osArch == null) {
			return "";
		}
		String os = null;
		if (osName.startsWith("Windows")) {
			os = "windows";
		} else if (osName.startsWith("Mac")) {
			os = "macos";
		} else if (osName.startsWith("Linux")) {
			os = "linux";
		}
		String arch = null;
		if ("amd64".equals(osArch) || "x86_64".equals(osArch)) {
			arch = "x64";
		} else if ("aarch64".equals(osArch) || "arm64".equals(osArch)) {
			arch = "arm64";
		}
		String result = os + "-" + arch;
		for (String platform : PLATFORMS) {
			if (platform.equals(result)) {
				return result;
			}
		}
		return "";
	}

	/**
	 * Platform of the running Java virtual machine.
	 * @return The platform, or blank when no package exists for it.
	 */
	public static String currentPlatform() {
		return platform(System.getProperty("os.name"), System.getProperty("os.arch"));
	}

	/**
	 * Builds the unattended install command of a package.
	 * @param type Package type.
	 * @param path Absolute path of the package.
	 * @return The command, or null for an unknown type.
	 */
	public static String[] installCommand(String type, String path) {
		if ("msi".equals(type)) {
			return new String[] {"msiexec", "/i", path, "/qn", "/norestart"};
		} else if ("pkg".equals(type)) {
			return new String[] {"/usr/sbin/installer", "-pkg", path, "-target", "/"};
		} else if ("deb".equals(type)) {
			return new String[] {"dpkg", "-i", path};
		} else if ("rpm".equals(type)) {
			return new String[] {"rpm", "-U", path};
		}
		return null;
	}

	/**
	 * Prints the package file name of this version for the current platform.
	 * @param args The package type.
	 */
	public static void main(String[] args) {
		String platform = currentPlatform();
		if (args.length == 0 || "".equals(platform)) {
			System.err.println("Usage: ClientPackages msi|pkg|deb|rpm (on Windows, macOS or Linux, x64 or arm64)");
			System.exit(2);
			return;
		}
		System.out.println(fileName(Labels.VERSION, platform, args[0]));
	}

	private static boolean isVersion(String version) {
		return version != null && VERSION_PATTERN.matcher(version).matches();
	}

	private static void checkVersion(String version) {
		if (!isVersion(version)) {
			throw new IllegalArgumentException("Not a version: " + version);
		}
	}
}
