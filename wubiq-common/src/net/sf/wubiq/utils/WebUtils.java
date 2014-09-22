/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities for web handling.
 * @author Federico Alcantara
 *
 */
public enum WebUtils {
	INSTANCE;
	private Log LOG = LogFactory.getLog(WebUtils.class);
	
	/**
	 * Properly encodes the given value.
	 * @param input Input to be encoded.
	 * @return Encoded string.
	 */
	public String encode(String input) {
		try {
			return URLEncoder.encode(input, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
		}
		return input;
	}
	
	/**
	 * Properly decode the given value.
	 * @param input Decode value.
	 * @return Decoded string.
	 */
	public String decode(String input) {
		try {
			return URLDecoder.decode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
		}
		return input;
	}
	
	/**
	 * Decodes a html with % codes.
	 * @param input Html with % codes.
	 * @return Converted string.
	 */
	public String decodeHtml(String input) {
		StringBuffer buffer = new StringBuffer("");
		if (input != null) {
			for (int index = 0; index < input.length(); index++) {
				char characterAt = input.charAt(index);
				if (characterAt == '%') {
					if ((index + 2) < input.length()) {
						String hex = new String(new char[]{input.charAt(index + 1),
								input.charAt(index + 2)});
						buffer.append((char)Integer.parseInt(hex, 16));
						index += 2;
					} else {
						buffer.append(characterAt);
					}
				} else {
					buffer.append(characterAt);
				}
			}
		}
		return buffer.toString();
	}
}
