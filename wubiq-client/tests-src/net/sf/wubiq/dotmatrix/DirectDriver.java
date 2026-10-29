package net.sf.wubiq.dotmatrix;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;

import net.sf.wubiq.wrappers.PageableStreamWrapper;

/**
 * Calls the stream wrappers directly on an image graphics, twice per page like the
 * JDK print loop does. With a dpi above 72 the graphics carries a device scale, as
 * printer graphics do.
 * @author Federico Alcantara
 *
 */
public class DirectDriver implements PrintDriver {
	private final String name;
	private final int dpi;

	public DirectDriver(String name, int dpi) {
		this.name = name;
		this.dpi = dpi;
	}

	public String getName() {
		return name;
	}

	public String run(WitnessFixture fixture, File escp, File directory) throws Exception {
		PageableStreamWrapper pageable = new PageableStreamWrapper(fixture, escp.getAbsolutePath());
		StringBuilder notes = new StringBuilder();
		for (int page = 0; page < pageable.getNumberOfPages(); page++) {
			PageFormat format = pageable.getPageFormat(page);
			Printable printable = pageable.getPrintable(page);
			for (int pass = 1; pass <= 2; pass++) {
				int width = (int) Math.ceil(format.getWidth() * dpi / 72d);
				int height = (int) Math.ceil(format.getHeight() * dpi / 72d);
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
				Graphics2D graphics = image.createGraphics();
				int result;
				try {
					graphics.scale(dpi / 72d, dpi / 72d);
					result = printable.print(graphics, format, page);
				} finally {
					graphics.dispose();
				}
				notes.append("print(page ").append(page).append(", pass ").append(pass).append(") -> ")
						.append(result == Printable.PAGE_EXISTS ? "PAGE_EXISTS" : "NO_SUCH_PAGE").append('\n');
			}
		}
		return notes.toString();
	}
}
