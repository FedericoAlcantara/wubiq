package net.sf.wubiq.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Utility for handling IOs.
 * @author Federico Alcantara
 *
 */
public enum IOUtils {
	INSTANCE;
	
	/**
	 * Copy from input stream to output stream. Doesn't close any of the streams.
	 * @param input Input stream, must be opened and in the starting position.
	 * @param output Output stream, must be opened.
	 * @throws IOException If input is not readable or input is not writable.
	 */
	public void copy(InputStream input, OutputStream output) throws IOException {
		int readData = -1;
		byte[] readBuffer = new byte[65535];
		while ((readData = input.read(readBuffer)) > -1) {
			output.write(readBuffer, 0, readData);
		}
	}

	/**
	 * Copy from input stream to output stream. Doesn't close any of the streams.
	 * @param input Input stream, must be opened and in the starting position.
	 * @param output Output stream, must be opened.
	 * @throws IOException If input is not readable or input is not writable.
	 */
	public void copy(Reader input, OutputStream output) throws IOException {
		int readData = -1;
		char[] readBuffer = new char[65535];
		Writer writer = new OutputStreamWriter(output);
		while ((readData = input.read(readBuffer)) > -1) {
			writer.write(readBuffer, 0, readData);
		}
	}
}
