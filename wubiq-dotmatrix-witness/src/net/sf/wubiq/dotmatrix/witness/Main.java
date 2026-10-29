package net.sf.wubiq.dotmatrix.witness;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Command line entry point of the virtual dot-matrix printer.
 * <pre>
 *   render [options] FILE|-   interpret one ESC/P stream and write a report
 *   listen [options]          act as a raw (port 9100) network printer; one report per job
 * </pre>
 * @author Federico Alcantara
 *
 */
public class Main {
	private PrinterProfile profile = PrinterProfile.ninePin();
	private File out;
	private int port = 9100;
	private String bind = "127.0.0.1";
	private int dpi = 100;
	private boolean strict;
	private String input;
	private final List<String[]> jobs = new ArrayList<String[]>();
	private int jobCount;

	public static void main(String[] args) {
		Main main = new Main();
		try {
			System.exit(main.run(args));
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage();
			System.exit(2);
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(2);
		}
	}

	private int run(String[] args) throws IOException {
		if (args.length == 0) {
			throw new IllegalArgumentException("Missing command.");
		}
		String command = args[0];
		for (int index = 1; index < args.length; index++) {
			String arg = args[index];
			if ("--pins".equals(arg)) {
				profile.setPin24("24".equals(value(args, ++index, arg)));
			} else if ("--paper".equals(arg)) {
				String[] size = value(args, ++index, arg).toLowerCase().split("x");
				profile.setPaperWidth(Double.parseDouble(size[0])).setPaperHeight(Double.parseDouble(size[1]));
			} else if ("--carriage".equals(arg)) {
				profile.setCarriageWidth(Double.parseDouble(value(args, ++index, arg)));
			} else if ("--out".equals(arg)) {
				out = new File(value(args, ++index, arg));
			} else if ("--port".equals(arg)) {
				port = Integer.parseInt(value(args, ++index, arg));
			} else if ("--bind".equals(arg)) {
				bind = value(args, ++index, arg);
			} else if ("--dpi".equals(arg)) {
				dpi = Integer.parseInt(value(args, ++index, arg));
			} else if ("--strict".equals(arg)) {
				strict = true;
			} else if (arg.startsWith("--")) {
				throw new IllegalArgumentException("Unknown option " + arg);
			} else {
				input = arg;
			}
		}
		if ("render".equals(command)) {
			return render();
		} else if ("listen".equals(command)) {
			listen();
			return 0;
		}
		throw new IllegalArgumentException("Unknown command " + command);
	}

	private int render() throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("render needs a file, or - for stdin.");
		}
		byte[] data = "-".equals(input) ? read(System.in) : read(new File(input));
		File directory = out != null ? out : new File("witness-report");
		EscpResult result = report(directory, "-".equals(input) ? "stdin" : new File(input).getName(), data);
		return strict && !result.getWarnings().isEmpty() ? 1 : 0;
	}

	private void listen() throws IOException {
		final File root = out != null ? out : new File("witness-jobs");
		ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(bind));
		System.out.println("Virtual dot-matrix printer (" + profile + ") listening on " + bind + ":" + port);
		System.out.println("Reports: " + new File(root, "index.html").getAbsolutePath());
		while (true) {
			final Socket socket = server.accept();
			final File directory = new File(root, "job-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())
					+ "-" + nextJob());
			new Thread(new Runnable() {
				public void run() {
					handle(socket, directory, root);
				}
			}, "witness-job").start();
		}
	}

	private synchronized int nextJob() {
		return ++jobCount;
	}

	private void handle(Socket socket, File directory, File root) {
		try {
			socket.setSoTimeout(30000);
			byte[] data;
			try {
				data = read(socket.getInputStream());
			} catch (SocketTimeoutException e) {
				System.out.println("Job from " + socket.getInetAddress() + " idle for 30 s; closing it.");
				data = new byte[0];
			}
			socket.close();
			if (data.length == 0) {
				return;
			}
			EscpResult result = report(directory, directory.getName() + " from " + socket.getInetAddress().getHostAddress(), data);
			synchronized (jobs) {
				jobs.add(0, new String[] {directory.getName(), directory.getName() + "/index.html",
						result.getWarnings().isEmpty() ? "PASS" : "WARN",
						data.length + " bytes, " + result.getPageCount() + " sheet(s), " + result.getWarnings().size() + " warning(s)"});
				WitnessReport.writeIndex(new File(root, "index.html"), "Virtual dot-matrix printer jobs", jobs);
			}
		} catch (IOException e) {
			System.err.println("Job failed: " + e.getMessage());
		}
	}

	private EscpResult report(File directory, String title, byte[] data) throws IOException {
		EscpResult result = new EscpInterpreter(profile).interpret(data);
		new WitnessReport(profile, dpi).write(directory, title, data, result, null, null);
		System.out.println(title + ": " + data.length + " bytes, " + result.getPageCount() + " sheet(s), "
				+ result.getFormFeeds() + " form feed(s), " + result.getWarnings().size() + " warning(s)");
		for (String warning : result.getWarnings()) {
			System.out.println("  " + warning);
		}
		System.out.println("  " + new File(directory, "index.html").getAbsolutePath());
		return result;
	}

	private static String value(String[] args, int index, String option) {
		if (index >= args.length) {
			throw new IllegalArgumentException(option + " needs a value.");
		}
		return args[index];
	}

	private static byte[] read(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		try {
			return read(stream);
		} finally {
			stream.close();
		}
	}

	private static byte[] read(InputStream stream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int count;
		while ((count = stream.read(chunk)) > 0) {
			buffer.write(chunk, 0, count);
		}
		return buffer.toByteArray();
	}

	private static void usage() {
		System.err.println("Usage:\n"
				+ "  java -jar wubiq-dotmatrix-witness.jar render [options] FILE|-\n"
				+ "  java -jar wubiq-dotmatrix-witness.jar listen [options]\n"
				+ "Options:\n"
				+ "  --pins 9|24       printer type (default 9)\n"
				+ "  --paper WxH       sheet size in inches (default 8.5x11)\n"
				+ "  --carriage IN     printable line width in inches (default 8.0, 13.6 for 136 columns)\n"
				+ "  --dpi N           png resolution (default 100)\n"
				+ "  --out DIR         report directory (render: witness-report, listen: witness-jobs)\n"
				+ "  --port N          listen port (default 9100)\n"
				+ "  --bind ADDRESS    listen address (default 127.0.0.1, 0.0.0.0 for the LAN)\n"
				+ "  --strict          render: exit 1 when the printer reports warnings");
	}
}
