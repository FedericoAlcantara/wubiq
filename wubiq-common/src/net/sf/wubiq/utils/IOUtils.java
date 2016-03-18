package net.sf.wubiq.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for handling IOs.
 * @author Federico Alcantara
 *
 */
public enum IOUtils {
	INSTANCE;
	private final Log LOG = LogFactory.getLog(IOUtils.class);
	
	/**
	 * Copy from input stream to output stream. Closes both streams.
	 * @param input Input stream, must be opened and in the starting position.
	 * @param output Output stream, must be opened.
	 * @throws IOException If input is not readable or input is not writable.
	 */
	public void copy(InputStream input, OutputStream output) throws IOException {
		try {
			if (input instanceof ByteArrayInputStream) {
				input.reset();
			}
			int readData = -1;
			byte[] readBuffer = new byte[65535];
			while ((readData = input.read(readBuffer)) > -1) {
				output.write(readBuffer, 0, readData);
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (output != null) {
				try {
					output.flush();
					output.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Copy from input stream to output stream. Closes both streams.
	 * @param input Input stream, must be opened and in the starting position.
	 * @param output Output stream, must be opened.
	 * @throws IOException If input is not readable or input is not writable.
	 */
	public void copy(Reader input, OutputStream output) throws IOException {
		try {
			int readData = -1;
			char[] readBuffer = new char[65535];
			Writer writer = new OutputStreamWriter(output);
			while ((readData = input.read(readBuffer)) > -1) {
				writer.write(readBuffer, 0, readData);
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (output != null) {
				try {
					output.flush();
					output.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
}
