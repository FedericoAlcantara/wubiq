package net.sf.wubiq.dotmatrix;

import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.wubiq.dotmatrix.witness.CheckResult;
import net.sf.wubiq.dotmatrix.witness.EscpInterpreter;
import net.sf.wubiq.dotmatrix.witness.EscpResult;
import net.sf.wubiq.dotmatrix.witness.LayoutCheck;
import net.sf.wubiq.dotmatrix.witness.PrinterProfile;
import net.sf.wubiq.dotmatrix.witness.WitnessReport;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Prints every fixture through every driver into Wubiq's dot-matrix stream path
 * (PageableStreamWrapper &rarr; PrintableStreamWrapper &rarr; ESCPrinter), feeds the bytes
 * to the virtual dot-matrix printer and checks the result against what the fixture drew.
 * <p>System properties:</p>
 * <ul>
 * <li><code>wubiq.dotmatrix.report</code> report directory (default build-tests/dotmatrix-report/java-VERSION)</li>
 * <li><code>wubiq.dotmatrix.strict</code> true to fail the test on layout or golden mismatches (default false: report only)</li>
 * <li><code>wubiq.dotmatrix.golden</code> golden files directory (default tests-src/net/sf/wubiq/dotmatrix/golden)</li>
 * <li><code>wubiq.dotmatrix.updateGolden</code> true to rewrite golden files from the "direct" driver</li>
 * <li><code>wubiq.dotmatrix.images</code> false to skip png rendering</li>
 * <li><code>wubiq.dotmatrix.only</code> comma separated case names (fixture-driver) to run</li>
 * </ul>
 * @author Federico Alcantara
 *
 */
@RunWith(Parameterized.class)
public class DotMatrixWitnessTest {
	private static final int DPI = 100;
	private static final PrinterProfile PROFILE = PrinterProfile.ninePin();

	private final WitnessFixture fixture;
	private final PrintDriver driver;

	@Parameters(name = "{0}-{1}")
	public static Collection<Object[]> parameters() {
		String only = System.getProperty("wubiq.dotmatrix.only");
		List<Object[]> returnValue = new ArrayList<Object[]>();
		for (Object[] parameters : allCases()) {
			if (only == null || ("," + only + ",").contains("," + caseName(parameters) + ",")) {
				returnValue.add(parameters);
			}
		}
		return returnValue;
	}

	private static List<Object[]> allCases() {
		List<PrintDriver> drivers = new ArrayList<PrintDriver>();
		drivers.add(new DirectDriver("direct", 72));
		drivers.add(new DirectDriver("direct-300dpi", 300));
		drivers.add(new DocPrintJobDriver());
		drivers.add(new ReferenceDriver());
		List<Object[]> returnValue = new ArrayList<Object[]>();
		for (WitnessFixture fixture : Fixtures.all()) {
			for (PrintDriver driver : drivers) {
				returnValue.add(new Object[] {fixture, driver.getName(), driver});
			}
		}
		return returnValue;
	}

	private static String caseName(Object[] parameters) {
		return ((WitnessFixture) parameters[0]).getName() + "-" + parameters[1];
	}

	/**
	 * <code>list</code> prints the case names (for <code>wubiq.dotmatrix.only</code>);
	 * <code>index</code> rebuilds index.html and summary.tsv from the case directories,
	 * for runs split over several JVMs.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 0 && "list".equals(args[0])) {
			for (Object[] parameters : allCases()) {
				System.out.println(caseName(parameters));
			}
		} else {
			writeIndex();
		}
	}

	public DotMatrixWitnessTest(WitnessFixture fixture, String driverName, PrintDriver driver) {
		this.fixture = fixture;
		this.driver = driver;
	}

	@Test
	public void printsWhereTheApplicationDrew() throws Exception {
		String name = fixture.getName() + "-" + driver.getName();
		File directory = new File(reportRoot(), name);
		directory.mkdirs();
		File escp = new File(directory, "wubiq-output.escp");
		escp.delete();
		String notes;
		try {
			notes = driver.run(fixture, escp, directory);
		} catch (Throwable e) {
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			notes = "Driver failed: " + trace;
		}
		byte[] data = escp.exists() ? read(escp) : new byte[0];
		escp.delete();
		EscpResult result = new EscpInterpreter(PROFILE).interpret(data);
		CheckResult check = new LayoutCheck(PROFILE).check(result, fixture.getExpected(), fixture.getNumberOfPages());
		if (data.length == 0) {
			check.fail("job", "no bytes reached the printer");
		}
		if (notes.startsWith("Driver failed")) {
			check.fail("driver", notes.split("\n")[0]);
		}
		String golden = compareGolden(result, check);
		boolean images = !"false".equals(System.getProperty("wubiq.dotmatrix.images"));
		new WitnessReport(PROFILE, DPI).write(directory, name + " on Java " + System.getProperty("java.version"),
				data, result, images ? references() : null, check);
		write(new File(directory, "driver.txt"), notes);
		write(new File(directory, "result.tsv"), fixture.getName() + "\t" + driver.getName() + "\t"
				+ (check.isPassed() ? "PASS" : "FAIL") + "\t" + check.count(CheckResult.Status.PASS) + "\t"
				+ check.count(CheckResult.Status.FAIL) + "\t" + data.length + "\t" + sha1(data) + "\t" + golden + "\n"
				+ check.getSummary() + "\t" + golden + ", " + data.length + " bytes, " + result.getPageCount()
				+ " sheet(s), " + firstFailure(check).replace('\t', ' ').replace('\n', ' ') + "\n");
		if (Boolean.getBoolean("wubiq.dotmatrix.strict") || driver instanceof ReferenceDriver) {
			assertTrue(name + ": " + check, check.isPassed());
		}
	}

	@AfterClass
	public static void writeIndex() throws IOException {
		File root = reportRoot();
		List<String[]> rows = new ArrayList<String[]>();
		StringBuilder summary = new StringBuilder("fixture\tdriver\tstatus\tok\tfailed\tbytes\tsha1\tgolden\n");
		for (Object[] parameters : allCases()) {
			String name = caseName(parameters);
			File result = new File(new File(root, name), "result.tsv");
			if (result.exists()) {
				String[] lines = new String(read(result), "UTF-8").split("\n");
				summary.append(lines[0]).append('\n');
				String[] row = lines[1].split("\t", 2);
				rows.add(new String[] {name, name + "/index.html", row[0], row.length > 1 ? row[1] : ""});
			}
		}
		WitnessReport.writeIndex(new File(root, "index.html"),
				"Dot-matrix witness, Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")", rows);
		write(new File(root, "summary.tsv"), summary.toString());
		System.out.println("Dot-matrix witness report: " + new File(root, "index.html").getAbsolutePath());
	}

	/**
	 * What a laser printer would print, used as the red layer of the sheet images.
	 */
	private List<BufferedImage> references() throws Exception {
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (int page = 0; page < fixture.getNumberOfPages(); page++) {
			BufferedImage image = new BufferedImage((int) Math.ceil(PROFILE.getPaperWidth() * DPI),
					(int) Math.ceil(PROFILE.getPaperHeight() * DPI), BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			try {
				graphics.scale(DPI / 72d, DPI / 72d);
				graphics.setColor(java.awt.Color.BLACK);
				fixture.print(graphics, fixture.getPageFormat(page), page);
			} finally {
				graphics.dispose();
			}
			images.add(image);
		}
		return images;
	}

	private String compareGolden(EscpResult result, CheckResult check) throws IOException {
		File goldenDirectory = new File(System.getProperty("wubiq.dotmatrix.golden", "tests-src/net/sf/wubiq/dotmatrix/golden"));
		File golden = new File(goldenDirectory, fixture.getName() + ".dump.txt");
		String dump = result.getDumpText();
		if (driver instanceof ReferenceDriver) {
			return "reference";
		}
		if (Boolean.getBoolean("wubiq.dotmatrix.updateGolden") && "direct".equals(driver.getName())) {
			goldenDirectory.mkdirs();
			write(golden, dump);
			return "golden updated";
		}
		if (!golden.exists()) {
			return "no golden";
		}
		if (new String(read(golden), "UTF-8").equals(dump)) {
			return "golden match";
		}
		check.fail("golden", "output differs from " + golden.getPath());
		return "golden DIFFERS";
	}

	private static String firstFailure(CheckResult check) {
		for (CheckResult.Item item : check.getItems()) {
			if (item.getStatus() == CheckResult.Status.FAIL) {
				String text = item.getSubject() + ": " + item.getMessage();
				return text.length() > 140 ? text.substring(0, 140) + "..." : text;
			}
		}
		return "";
	}

	private static File reportRoot() {
		return new File(System.getProperty("wubiq.dotmatrix.report",
				"build-tests/dotmatrix-report/java-" + System.getProperty("java.version")));
	}

	private static String sha1(byte[] data) {
		try {
			StringBuilder builder = new StringBuilder();
			for (byte value : MessageDigest.getInstance("SHA-1").digest(data)) {
				builder.append(String.format("%02x", value & 0xFF));
			}
			return builder.toString();
		} catch (Exception e) {
			return "?";
		}
	}

	private static byte[] read(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[8192];
			int count;
			while ((count = stream.read(chunk)) > 0) {
				buffer.write(chunk, 0, count);
			}
			return buffer.toByteArray();
		} finally {
			stream.close();
		}
	}

	private static void write(File file, String text) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		try {
			writer.write(text);
		} finally {
			writer.close();
		}
	}
}
