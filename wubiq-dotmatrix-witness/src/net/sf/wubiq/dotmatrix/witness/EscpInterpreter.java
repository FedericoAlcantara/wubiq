package net.sf.wubiq.dotmatrix.witness;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Virtual Epson ESC/P (9-pin) and ESC/P 2 (24-pin) text-mode printer.
 * <p>
 * It follows the print head through a byte stream the way a real printer would,
 * and records every character cell it lays down (a {@link Glyph}), a decoded
 * command listing, and warnings for anything a real printer would do differently
 * from what the stream seems to intend (wrapping past the right margin, short
 * page lengths, UTF-8 bytes sent to a single-byte character table, unknown commands).
 * </p>
 * <p>
 * Positions are tracked in inches from the left edge of the paper and from the
 * top of form of the first sheet. Bit-image graphics are skipped (their width
 * still moves the head). Proportional spacing is not modelled.
 * </p>
 * @author Federico Alcantara
 *
 */
public class EscpInterpreter {
	private static final int ESC = 0x1B;
	private static final double EPSILON = 0.0001;
	private static final String PC437 = "IBM437";
	private static final String ITALIC = "italic";
	private static final Map<String, String> REGISTERED_TABLES = new HashMap<String, String>();
	private static final Map<String, String> TABLE_NAMES = new HashMap<String, String>();
	private static final String LATIN_AMERICA_CODES = "#$@[\\]^`{|}~";
	private static final String LATIN_AMERICA_CHARS = "#$á¡Ñ¿éüíñóú";

	static {
		// ESC ( t d2/d3 registered table identifiers (Epson ESC/P 2 reference).
		REGISTERED_TABLES.put("0/0", ITALIC);
		REGISTERED_TABLES.put("1/0", PC437);
		REGISTERED_TABLES.put("3/0", "IBM850");
		REGISTERED_TABLES.put("7/0", "IBM860");
		REGISTERED_TABLES.put("8/0", "IBM863");
		REGISTERED_TABLES.put("9/0", "IBM865");
		TABLE_NAMES.put(ITALIC, "Italic");
		TABLE_NAMES.put(PC437, "PC437");
		TABLE_NAMES.put("IBM850", "PC850");
		TABLE_NAMES.put("IBM860", "PC860");
		TABLE_NAMES.put("IBM863", "PC863");
		TABLE_NAMES.put("IBM865", "PC865");
	}

	private final PrinterProfile profile;

	private EscpResult result;
	private byte[] data;
	private int pos;

	private double x;
	private double formY;
	private double logicalTop;
	private double pageLength;
	private double lineSpacing;
	private double leftMargin;
	private double rightMargin;
	private double basePitch;
	private double hmi;
	private double extraSpace;
	private double definedUnit;
	private boolean condensed;
	private boolean doubleWidth;
	private boolean doubleWidthLine;
	private boolean bold;
	private boolean italic;
	private boolean underline;
	private boolean letterQuality;
	private String[] tables;
	private int currentTable;
	private int international;
	private double[] tabStops;
	private boolean warnedShortPage;
	private boolean warnedProportional;
	private int formFeeds;
	private int lastSheet;

	private StringBuilder rawText;
	private StringBuilder decodedText;
	private int textStart;

	public EscpInterpreter(PrinterProfile profile) {
		this.profile = profile;
	}

	public EscpInterpreter() {
		this(PrinterProfile.ninePin());
	}

	/**
	 * Runs the byte stream through the virtual printer.
	 * @param bytes Raw ESC/P stream, exactly as it would reach the printer.
	 * @return Laid down glyphs, decoded listing and warnings.
	 */
	public EscpResult interpret(byte[] bytes) {
		result = new EscpResult();
		data = bytes;
		pos = 0;
		formY = 0;
		formFeeds = 0;
		lastSheet = 0;
		warnedShortPage = false;
		warnedProportional = false;
		rawText = new StringBuilder();
		decodedText = new StringBuilder();
		reset();
		if (data.length > 0 && !(data[0] == ESC && data.length > 1 && data[1] == '@')) {
			warn(0, "stream does not start with ESC @ (initialize printer); leftover settings from a previous job apply");
		}
		while (pos < data.length) {
			int start = pos;
			int value = data[pos++] & 0xFF;
			if (value == ESC) {
				flushText();
				command(start);
			} else if (value < 0x20 || value == 0x7F) {
				flushText();
				control(value, start);
			} else {
				text(value, start);
			}
		}
		flushText();
		if (data.length > 0 && !endsWithFormFeed()) {
			warn(data.length, "stream does not end with a form feed; the next job starts on the same sheet");
		}
		result.setFormFeeds(formFeeds);
		result.setPageCount(lastSheet + 1);
		return result;
	}

	private boolean endsWithFormFeed() {
		for (int index = data.length - 1; index >= 0; index--) {
			int value = data[index] & 0xFF;
			if (value == 0x0C) {
				return true;
			}
			if (value != 0x0D && value != 0x00) {
				return false;
			}
		}
		return false;
	}

	private void reset() {
		logicalTop = formY;
		pageLength = profile.getPaperHeight();
		lineSpacing = 1d / 6d;
		leftMargin = 0;
		rightMargin = profile.getCarriageWidth();
		x = leftMargin;
		basePitch = 10;
		hmi = 0;
		extraSpace = 0;
		definedUnit = 1d / 360d;
		condensed = false;
		doubleWidth = false;
		doubleWidthLine = false;
		bold = false;
		italic = false;
		underline = false;
		letterQuality = false;
		tables = new String[] {ITALIC, PC437, PC437, PC437};
		currentTable = 1;
		international = 0;
		tabStops = null;
	}

	// ------------------------------------------------------------------ text

	private void text(int value, int start) {
		if (rawText.length() == 0) {
			textStart = start;
		}
		checkUtf8(value, start);
		String table = tables[currentTable];
		boolean italicTable = ITALIC.equals(table);
		char character;
		if (value < 0x80) {
			character = international((char) value);
			rawText.append((char) value);
		} else {
			rawText.append(String.format(Locale.US, "\\x%02X", value));
			if (italicTable) {
				character = international((char) (value - 0x80));
			} else {
				character = decode(value, table, start);
			}
		}
		decodedText.append(character);
		printCell(character, italicTable && value >= 0x80, start);
	}

	private void printCell(char character, boolean italicTable, int offset) {
		double width = cellWidth();
		if (x + width > rightMargin + EPSILON) {
			warn(offset, String.format(Locale.US, "text passes the right margin (%.3f in); the printer wraps to the next line", rightMargin));
			lineFeed();
		}
		int sheet = sheet();
		lastSheet = Math.max(lastSheet, sheet);
		double y = formY - sheet * profile.getPaperHeight();
		result.getGlyphs().add(new Glyph(sheet, x, y, width, 1d / width, character,
				bold, italic || italicTable, underline, doubleWidth || doubleWidthLine));
		x += width;
	}

	private char international(char character) {
		if (international == 12) {
			int index = LATIN_AMERICA_CODES.indexOf(character);
			if (index >= 0) {
				return LATIN_AMERICA_CHARS.charAt(index);
			}
		}
		return character;
	}

	private char decode(int value, String table, int offset) {
		try {
			return new String(new byte[] {(byte) value}, table).charAt(0);
		} catch (UnsupportedEncodingException e) {
			warn(offset, "this JVM has no charset " + table + "; byte shown as U+FFFD");
			return '�';
		}
	}

	private void checkUtf8(int value, int start) {
		if (value < 0xC2 || value > 0xF4) {
			return;
		}
		int continuation = value >= 0xF0 ? 3 : value >= 0xE0 ? 2 : 1;
		if (start + continuation >= data.length) {
			return;
		}
		for (int index = 1; index <= continuation; index++) {
			int next = data[start + index] & 0xFF;
			if (next < 0x80 || next > 0xBF) {
				return;
			}
		}
		byte[] sequence = new byte[continuation + 1];
		System.arraycopy(data, start, sequence, 0, sequence.length);
		try {
			String intended = new String(sequence, "UTF-8");
			StringBuilder printed = new StringBuilder();
			StringBuilder hex = new StringBuilder();
			for (byte item : sequence) {
				int unsigned = item & 0xFF;
				hex.append(String.format(Locale.US, "%02X ", unsigned));
				String table = tables[currentTable];
				printed.append(ITALIC.equals(table) ? (char) (unsigned - 0x80) : decode(unsigned, table, start));
			}
			warn(start, "bytes " + hex.toString().trim() + " are UTF-8 for '" + intended
					+ "'; the printer prints " + sequence.length + " characters '" + printed
					+ "' from table " + tableName(tables[currentTable]));
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is always available
		}
	}

	private void flushText() {
		if (rawText.length() > 0) {
			String raw = rawText.toString();
			String decoded = decodedText.toString();
			String line = String.format(Locale.US, "%06X  TEXT \"%s\"", textStart, raw);
			if (!raw.equals(decoded)) {
				line += "  = \"" + decoded + "\"";
			}
			result.getDump().add(line);
			rawText.setLength(0);
			decodedText.setLength(0);
		}
	}

	/**
	 * @return Current character cell width in inches.
	 */
	private double cellWidth() {
		double width;
		if (hmi > 0) {
			width = hmi;
		} else if (condensed && basePitch == 10) {
			width = 7d / 120d;
		} else if (condensed && basePitch == 12) {
			width = 6d / 120d;
		} else {
			width = 1d / basePitch;
		}
		if (doubleWidth || doubleWidthLine) {
			width *= 2;
		}
		return width + extraSpace;
	}

	private int sheet() {
		return (int) Math.floor((formY + EPSILON) / profile.getPaperHeight());
	}

	// ------------------------------------------------------------- controls

	private void control(int value, int start) {
		switch (value) {
		case 0x0D:
			x = leftMargin;
			cmd(start, "CR", "carriage return");
			break;
		case 0x0A:
			lineFeed();
			cmd(start, "LF", String.format(Locale.US, "line feed %.4f in -> y %.4f", lineSpacing, formY));
			break;
		case 0x0B:
			lineFeed();
			cmd(start, "VT", "vertical tab (no channels set: line feed)");
			break;
		case 0x0C:
			formFeed(start);
			cmd(start, "FF", String.format(Locale.US, "form feed -> y %.4f (sheet %d)", formY, sheet() + 1));
			break;
		case 0x09:
			tab();
			cmd(start, "HT", String.format(Locale.US, "tab -> x %.4f", x));
			break;
		case 0x08:
			x = Math.max(leftMargin, x - cellWidth());
			cmd(start, "BS", "backspace");
			break;
		case 0x0E:
			doubleWidthLine = true;
			cmd(start, "SO", "double width (one line)");
			break;
		case 0x14:
			doubleWidthLine = false;
			cmd(start, "DC4", "cancel one-line double width");
			break;
		case 0x0F:
			condensed = true;
			cmd(start, "SI", "condensed on");
			break;
		case 0x12:
			condensed = false;
			cmd(start, "DC2", "condensed off");
			break;
		case 0x00:
		case 0x07:
		case 0x11:
		case 0x13:
			cmd(start, String.format(Locale.US, "0x%02X", value), "ignored control");
			break;
		case 0x18:
		case 0x7F:
			cmd(start, value == 0x18 ? "CAN" : "DEL", "cancel line / delete character");
			warn(start, "CAN/DEL edits the line buffer; not modelled");
			break;
		default:
			cmd(start, String.format(Locale.US, "0x%02X", value), "unknown control");
			warn(start, String.format(Locale.US, "unknown control code 0x%02X", value));
		}
	}

	private void lineFeed() {
		formY += lineSpacing;
		x = leftMargin;
		doubleWidthLine = false;
	}

	private void formFeed(int offset) {
		formFeeds++;
		lastSheet = Math.max(lastSheet, sheet());
		if (pageLength < profile.getPaperHeight() - EPSILON && !warnedShortPage) {
			warnedShortPage = true;
			warn(offset, String.format(Locale.US, "form feed with page length %.4f in on %.2f in paper: it only advances to the next %.4f in boundary",
					pageLength, profile.getPaperHeight(), pageLength));
		}
		double pagesDown = Math.floor((formY - logicalTop) / pageLength + EPSILON) + 1;
		logicalTop += pagesDown * pageLength;
		formY = logicalTop;
		x = leftMargin;
		doubleWidthLine = false;
	}

	private void tab() {
		if (tabStops != null) {
			for (double stop : tabStops) {
				if (stop > x + EPSILON) {
					x = stop;
					return;
				}
			}
			return;
		}
		double step = 8 * cellWidth();
		double relative = x - leftMargin;
		x = leftMargin + (Math.floor(relative / step + EPSILON) + 1) * step;
	}

	// ------------------------------------------------------------- commands

	private void command(int start) {
		int command = next(start);
		if (command < 0) {
			return;
		}
		int n;
		switch (command) {
		case '@':
			reset();
			cmd(start, "ESC @", "initialize printer (top of form here)");
			break;
		case 'x':
			n = next(start);
			letterQuality = flag(n);
			cmd(start, "ESC x " + n, letterQuality ? "letter quality" : "draft");
			break;
		case 'k':
			n = next(start);
			cmd(start, "ESC k " + n, "typeface");
			break;
		case 'p':
			n = next(start);
			cmd(start, "ESC p " + n, flag(n) ? "proportional on" : "proportional off");
			if (flag(n)) {
				warnProportional(start);
			}
			break;
		case 'P':
			basePitch = 10;
			hmi = 0;
			cmd(start, "ESC P", "10 cpi");
			break;
		case 'M':
			basePitch = 12;
			hmi = 0;
			cmd(start, "ESC M", "12 cpi");
			break;
		case 'g':
			basePitch = 15;
			hmi = 0;
			cmd(start, "ESC g", "15 cpi");
			break;
		case 0x0F:
			condensed = true;
			cmd(start, "ESC SI", "condensed on");
			break;
		case 0x0E:
			doubleWidthLine = true;
			cmd(start, "ESC SO", "double width (one line)");
			break;
		case 'W':
			n = next(start);
			doubleWidth = flag(n);
			cmd(start, "ESC W " + n, doubleWidth ? "double width on" : "double width off");
			break;
		case 'w':
			n = next(start);
			cmd(start, "ESC w " + n, "double height (not modelled)");
			break;
		case 'E':
			bold = true;
			cmd(start, "ESC E", "bold on");
			break;
		case 'F':
			bold = false;
			cmd(start, "ESC F", "bold off");
			break;
		case 'G':
		case 'H':
			cmd(start, "ESC " + (char) command, command == 'G' ? "double strike on" : "double strike off");
			break;
		case '4':
			italic = true;
			cmd(start, "ESC 4", "italic on");
			break;
		case '5':
			italic = false;
			cmd(start, "ESC 5", "italic off");
			break;
		case '-':
			n = next(start);
			underline = flag(n);
			cmd(start, "ESC - " + n, underline ? "underline on" : "underline off");
			break;
		case '!':
			n = next(start);
			masterSelect(n, start);
			cmd(start, "ESC ! " + n, "master select");
			break;
		case '0':
			lineSpacing = 1d / 8d;
			cmd(start, "ESC 0", "line spacing 1/8 in");
			break;
		case '1':
			lineSpacing = 7d / 72d;
			cmd(start, "ESC 1", "line spacing 7/72 in");
			break;
		case '2':
			lineSpacing = 1d / 6d;
			cmd(start, "ESC 2", "line spacing 1/6 in");
			break;
		case '3':
			n = next(start);
			lineSpacing = n * profile.getFineLineUnit();
			cmd(start, "ESC 3 " + n, String.format(Locale.US, "line spacing %.4f in", lineSpacing));
			break;
		case 'A':
			n = next(start);
			lineSpacing = n / (profile.isPin24() ? 60d : 72d);
			cmd(start, "ESC A " + n, String.format(Locale.US, "line spacing %.4f in", lineSpacing));
			break;
		case '+':
			n = next(start);
			lineSpacing = n / 360d;
			cmd(start, "ESC + " + n, String.format(Locale.US, "line spacing %.4f in", lineSpacing));
			break;
		case 'J':
			n = next(start);
			formY += n * profile.getFineLineUnit();
			cmd(start, "ESC J " + n, String.format(Locale.US, "advance paper -> y %.4f", formY));
			break;
		case 'j':
			n = next(start);
			formY -= n * profile.getFineLineUnit();
			cmd(start, "ESC j " + n, String.format(Locale.US, "reverse feed -> y %.4f", formY));
			break;
		case 'C':
			pageLength(start);
			break;
		case 'N':
		case 'U':
		case 's':
		case 'r':
		case 'q':
		case 'I':
		case 'i':
		case 'm':
		case '%':
		case 0x19:
		case 'S':
			n = next(start);
			cmd(start, "ESC " + printable(command) + " " + n, "setting (not modelled)");
			break;
		case 'a':
			n = next(start);
			cmd(start, "ESC a " + n, "justification");
			if (n != 0 && n != '0') {
				warn(start, "ESC a justification is not modelled");
			}
			break;
		case 'O':
		case 'T':
		case '6':
		case '7':
		case '8':
		case '9':
		case '<':
			cmd(start, "ESC " + (char) command, "setting (not modelled)");
			break;
		case 'l':
			n = next(start);
			leftMargin = n / basePitch;
			x = Math.max(x, leftMargin);
			cmd(start, "ESC l " + n, String.format(Locale.US, "left margin %.4f in", leftMargin));
			break;
		case 'Q':
			n = next(start);
			rightMargin = Math.min(n / basePitch, profile.getCarriageWidth());
			cmd(start, "ESC Q " + n, String.format(Locale.US, "right margin %.4f in", rightMargin));
			break;
		case '$':
			n = word(start);
			x = leftMargin + n / 60d;
			cmd(start, "ESC $ " + n, String.format(Locale.US, "absolute position x %.4f", x));
			break;
		case '\\':
			n = (short) word(start);
			x += n * (profile.isPin24() && letterQuality ? 1d / 180d : 1d / 120d);
			cmd(start, "ESC \\ " + n, String.format(Locale.US, "relative position x %.4f", x));
			break;
		case ' ':
			n = next(start);
			extraSpace = n * (profile.isPin24() && letterQuality ? 1d / 180d : 1d / 120d);
			cmd(start, "ESC SP " + n, "inter-character space");
			break;
		case 'c':
			n = word(start);
			hmi = n / 360d;
			cmd(start, "ESC c " + n, String.format(Locale.US, "horizontal motion index %.4f in", hmi));
			break;
		case 'X':
			next(start);
			word(start);
			cmd(start, "ESC X", "select font by pitch and point (not modelled)");
			break;
		case 'e':
		case 'f':
		case '?':
			next(start);
			next(start);
			cmd(start, "ESC " + (char) command, "setting (not modelled)");
			break;
		case ':':
			next(start);
			next(start);
			next(start);
			cmd(start, "ESC :", "copy ROM to RAM (not modelled)");
			break;
		case 'D':
			horizontalTabs(start);
			break;
		case 'B':
			while (pos < data.length && (data[pos++] & 0xFF) != 0) {
				// vertical tab channels are not modelled
			}
			cmd(start, "ESC B", "vertical tabs (not modelled)");
			break;
		case '/':
			next(start);
			cmd(start, "ESC /", "vertical tab channel (not modelled)");
			break;
		case 't':
			n = next(start);
			currentTable = (n >= '0' ? n - '0' : n) & 3;
			cmd(start, "ESC t " + n, "select character table " + currentTable + " = " + tableName(tables[currentTable]));
			break;
		case 'R':
			n = next(start);
			international = n;
			cmd(start, "ESC R " + n, "international character set " + n);
			if (n != 0 && n != 12) {
				warn(start, "international character set " + n + " is not modelled (treated as USA)");
			}
			break;
		case '(':
			extended(start);
			break;
		case '*':
			n = next(start);
			bitImage(start, "ESC * " + n, bitImageDpi(n), n >= 32 ? (n >= 64 ? 6 : 3) : 1);
			break;
		case 'K':
			bitImage(start, "ESC K", 60, 1);
			break;
		case 'L':
		case 'Y':
			bitImage(start, "ESC " + (char) command, 120, 1);
			break;
		case 'Z':
			bitImage(start, "ESC Z", 240, 1);
			break;
		case '^':
			next(start);
			bitImage(start, "ESC ^", 60, 2);
			break;
		default:
			cmd(start, String.format(Locale.US, "ESC 0x%02X", command), "unknown command");
			warn(start, String.format(Locale.US, "unknown command ESC 0x%02X ('%s'); its parameters will be read as text",
					command, printable(command)));
		}
	}

	private void masterSelect(int n, int start) {
		basePitch = (n & 1) != 0 ? 12 : 10;
		hmi = 0;
		condensed = (n & 4) != 0;
		bold = (n & 8) != 0;
		doubleWidth = (n & 32) != 0;
		italic = (n & 64) != 0;
		underline = (n & 128) != 0;
		if ((n & 2) != 0) {
			warnProportional(start);
		}
	}

	private void pageLength(int start) {
		int n = next(start);
		if (n == 0) {
			int inches = next(start);
			pageLength = inches;
			cmd(start, "ESC C 0 " + inches, "page length " + inches + " in");
		} else {
			pageLength = n * lineSpacing;
			cmd(start, "ESC C " + n, String.format(Locale.US, "page length %d lines = %.4f in", n, pageLength));
		}
		logicalTop = formY;
		if (pageLength < profile.getPaperHeight() - EPSILON) {
			warn(start, String.format(Locale.US, "page length %.4f in is shorter than the %.2f in paper", pageLength, profile.getPaperHeight()));
		}
	}

	private void horizontalTabs(int start) {
		double[] stops = new double[32];
		int count = 0;
		int previous = 0;
		while (pos < data.length) {
			int column = data[pos++] & 0xFF;
			if (column == 0 || column <= previous || count == stops.length) {
				break;
			}
			stops[count++] = leftMargin + column * cellWidth();
			previous = column;
		}
		tabStops = new double[count];
		System.arraycopy(stops, 0, tabStops, 0, count);
		cmd(start, "ESC D", count + " horizontal tab stops");
	}

	private void extended(int start) {
		int sub = next(start);
		int length = word(start);
		if (sub < 0 || length < 0) {
			return;
		}
		int[] args = new int[length];
		for (int index = 0; index < length; index++) {
			args[index] = next(start);
		}
		String name = "ESC ( " + printable(sub);
		switch (sub) {
		case 't':
			if (length >= 3) {
				String key = args[1] + "/" + args[2];
				String table = REGISTERED_TABLES.get(key);
				if (table == null) {
					table = PC437;
					warn(start, "registered character table " + key + " is not known to the witness; decoding as PC437");
				}
				tables[args[0] & 3] = table;
				cmd(start, name, "assign table " + key + " (" + tableName(table) + ") to slot " + (args[0] & 3));
			} else {
				cmd(start, name, "assign character table (short)");
			}
			break;
		case 'U':
			if (length >= 1 && args[0] > 0) {
				definedUnit = args[0] / 3600d;
			}
			cmd(start, name, String.format(Locale.US, "unit %.5f in", definedUnit));
			break;
		case 'V':
			if (length >= 2) {
				formY = logicalTop + (args[0] + 256 * args[1]) * definedUnit;
			}
			cmd(start, name, String.format(Locale.US, "absolute vertical -> y %.4f", formY));
			break;
		case 'v':
			if (length >= 2) {
				formY += ((short) (args[0] + 256 * args[1])) * definedUnit;
			}
			cmd(start, name, String.format(Locale.US, "relative vertical -> y %.4f", formY));
			break;
		case 'C':
			if (length >= 2) {
				pageLength = (args[0] + 256 * args[1]) * definedUnit;
				logicalTop = formY;
			}
			cmd(start, name, String.format(Locale.US, "page length %.4f in", pageLength));
			break;
		case '-':
			if (length >= 3) {
				underline = args[2] != 0;
			}
			cmd(start, name, underline ? "score line on" : "score line off");
			break;
		default:
			cmd(start, name, "extended setting (not modelled)");
		}
	}

	private void bitImage(int start, String name, int dpi, int bytesPerColumn) {
		int columns = word(start);
		if (columns < 0) {
			return;
		}
		int skip = Math.min(columns * bytesPerColumn, data.length - pos);
		pos += skip;
		x += columns / (double) dpi;
		cmd(start, name, columns + " graphics columns at " + dpi + " dpi (skipped)");
		warn(start, "bit image graphics are not rendered by the witness");
	}

	private int bitImageDpi(int mode) {
		switch (mode) {
		case 1: case 2: case 33: case 72:
			return 120;
		case 3: case 39:
			return 180;
		case 4:
			return 80;
		case 6: case 38:
			return 90;
		case 40: case 73:
			return 360;
		default:
			return 60;
		}
	}

	private void warnProportional(int start) {
		if (!warnedProportional) {
			warnedProportional = true;
			warn(start, "proportional spacing is not modelled; fixed pitch assumed");
		}
	}

	// -------------------------------------------------------------- helpers

	private int next(int start) {
		if (pos >= data.length) {
			warn(start, "command truncated at end of stream");
			return -1;
		}
		return data[pos++] & 0xFF;
	}

	private int word(int start) {
		int low = next(start);
		int high = next(start);
		if (low < 0 || high < 0) {
			return -1;
		}
		return low + 256 * high;
	}

	private boolean flag(int n) {
		return n == 1 || n == '1';
	}

	private String printable(int value) {
		return value >= 0x21 && value < 0x7F ? String.valueOf((char) value) : String.format(Locale.US, "0x%02X", value);
	}

	private String tableName(String table) {
		String name = TABLE_NAMES.get(table);
		return name == null ? table : name;
	}

	private void cmd(int start, String mnemonic, String description) {
		StringBuilder hex = new StringBuilder();
		int end = Math.min(pos, start + 8);
		for (int index = start; index < end; index++) {
			hex.append(String.format(Locale.US, "%02X ", data[index] & 0xFF));
		}
		if (pos - start > 8) {
			hex.append("..");
		}
		result.getDump().add(String.format(Locale.US, "%06X  %-27s %-16s %s", start, hex.toString().trim(), mnemonic, description));
	}

	private void warn(int offset, String message) {
		result.getWarnings().add(String.format(Locale.US, "@%06X %s", offset, message));
	}
}
