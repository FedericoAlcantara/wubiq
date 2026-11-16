package net.sf.wubiq.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * Tests the version, file name, platform and install command rules of the client packages.
 */
public class ClientPackagesTest {

	private static final String[] PACKAGE_DIRECTORY_FILES = {
		"wubiq-client-2.8.0-linux-x64.deb",
		"wubiq-client-2.10.0-linux-x64.deb",
		"wubiq-client-2.9.0-linux-x64.rpm",
		"wubiq-client-3.0.0-windows-x64.msi",
		"notes.txt"
	};

	@Test
	public void versionsCompareAsNumbers() {
		assertTrue(ClientPackages.compareVersions("2.10.0", "2.9.9") > 0);
		assertEquals(0, ClientPackages.compareVersions("2.8", "2.8.0"));
	}

	@Test
	public void onlyANewerOfferIsAccepted() {
		assertTrue(ClientPackages.isNewer("2.8.1", "2.8.0"));
		assertFalse(ClientPackages.isNewer("2.8.0", "2.8.0"));
		assertFalse(ClientPackages.isNewer("2.7.4", "2.8.0"));
		assertFalse(ClientPackages.isNewer("", "2.8.0"));
		assertFalse(ClientPackages.isNewer(null, "2.8.0"));
		assertFalse(ClientPackages.isNewer("< 2.0", "2.8.0"));
	}

	@Test
	public void fileNameRoundTrip() {
		assertEquals("wubiq-client-2.8.0-linux-x64.deb", ClientPackages.fileName("2.8.0", "linux-x64", "deb"));
		assertArrayEquals(new String[] {"2.8.0", "linux-x64", "deb"}, ClientPackages.parse("wubiq-client-2.8.0-linux-x64.deb"));
	}

	@Test
	public void misnamedFilesAreIgnored() {
		assertNull(ClientPackages.parse("wubiq-client-2.8.0-linux-x64.zip"));
		assertNull(ClientPackages.parse("Wubiq-2.8.0.pkg"));
		assertNull(ClientPackages.parse("wubiq-client-2.8.0-solaris-x64.deb"));
	}

	@Test
	public void latestPicksTheHighestVersionOfThePlatformAndType() throws IOException {
		File dir = createPackageDirectory();
		try {
			assertEquals("wubiq-client-2.10.0-linux-x64.deb", ClientPackages.latest(dir, "linux-x64", "deb").getName());
		} finally {
			deletePackageDirectory(dir);
		}
	}

	@Test
	public void latestReturnsNullWhenNothingIsOffered() throws IOException {
		File dir = createPackageDirectory();
		try {
			assertNull(ClientPackages.latest(new File(dir, "missing"), "linux-x64", "deb"));
			assertNull(ClientPackages.latest(null, "linux-x64", "deb"));
			assertNull(ClientPackages.latest(dir, "macos-arm64", "pkg"));
		} finally {
			deletePackageDirectory(dir);
		}
	}

	@Test
	public void platformOfOsNameAndArchitecture() {
		assertEquals("windows-x64", ClientPackages.platform("Windows 11", "amd64"));
		assertEquals("macos-arm64", ClientPackages.platform("Mac OS X", "aarch64"));
		assertEquals("macos-x64", ClientPackages.platform("Mac OS X", "x86_64"));
		assertEquals("linux-x64", ClientPackages.platform("Linux", "amd64"));
		assertEquals("", ClientPackages.platform("Linux", "arm"));
		assertEquals("", ClientPackages.platform("SunOS", "amd64"));
	}

	@Test
	public void installCommands() {
		assertArrayEquals(new String[] {"msiexec", "/i", "C:\\t\\p.msi", "/qn", "/norestart"}, ClientPackages.installCommand("msi", "C:\\t\\p.msi"));
		assertArrayEquals(new String[] {"/usr/sbin/installer", "-pkg", "/t/p.pkg", "-target", "/"}, ClientPackages.installCommand("pkg", "/t/p.pkg"));
		assertArrayEquals(new String[] {"dpkg", "-i", "/t/p.deb"}, ClientPackages.installCommand("deb", "/t/p.deb"));
		assertArrayEquals(new String[] {"rpm", "-U", "/t/p.rpm"}, ClientPackages.installCommand("rpm", "/t/p.rpm"));
		assertNull(ClientPackages.installCommand("zip", "/t/p.zip"));
	}

	private File createPackageDirectory() throws IOException {
		File dir = File.createTempFile("packages", "");
		dir.delete();
		dir.mkdir();
		for (String name : PACKAGE_DIRECTORY_FILES) {
			new File(dir, name).createNewFile();
		}
		return dir;
	}

	private void deletePackageDirectory(File dir) {
		for (String name : PACKAGE_DIRECTORY_FILES) {
			new File(dir, name).delete();
		}
		dir.delete();
	}
}
