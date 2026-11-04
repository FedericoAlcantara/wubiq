package net.sf.wubiq.dotmatrix;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

import net.sf.wubiq.dotmatrix.witness.ExpectedText;

/**
 * A print job used to exercise the dot-matrix path. It is both the {@link Pageable}
 * handed to the code under test and the source of truth for where every string belongs.
 * Pages are US Letter with the whole sheet imageable, so user space equals paper position.
 * @author Federico Alcantara
 *
 */
public class WitnessFixture implements Pageable, Printable {
	private static final BufferedImage SCRATCH = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	private final String name;
	private final int pages;
	private final List<DrawItem> items = new ArrayList<DrawItem>();
	private final List<GraphicItem> graphics = new ArrayList<GraphicItem>();
	private final List<Integer> graphicPages = new ArrayList<Integer>();
	private final PageFormat pageFormat;

	public WitnessFixture(String name, int pages) {
		this.name = name;
		this.pages = pages;
		Paper paper = new Paper();
		paper.setSize(612, 792);
		paper.setImageableArea(0, 0, 612, 792);
		pageFormat = new PageFormat();
		pageFormat.setPaper(paper);
	}

	public String getName() {
		return name;
	}

	public DrawItem add(int page, String text, float x, float y, Font font) {
		DrawItem item = new DrawItem(page, text, x, y, font);
		items.add(item);
		return item;
	}

	/**
	 * Adds a shape or an image.
	 * @param page Zero based page.
	 * @param item What to draw.
	 */
	public void addGraphic(int page, GraphicItem item) {
		graphics.add(item);
		graphicPages.add(page);
	}

	/**
	 * @return True if the fixture draws shapes or images.
	 */
	public boolean hasGraphics() {
		return !graphics.isEmpty();
	}

	/**
	 * Draws the shapes and images of a page, with no text.
	 */
	public void drawGraphics(Graphics2D graphics2D, int pageIndex) {
		for (int index = 0; index < graphics.size(); index++) {
			if (graphicPages.get(index) == pageIndex) {
				graphics.get(index).draw(graphics2D);
			}
		}
	}

	/**
	 * Adds a string whose right edge is at <code>right</code>, measured with this JVM's font metrics,
	 * as a report engine would do.
	 */
	public DrawItem addRight(int page, String text, float right, float y, Font font) {
		return add(page, text, right - width(text, font), y, font);
	}

	public static float width(String text, Font font) {
		Graphics2D graphics = SCRATCH.createGraphics();
		try {
			FontMetrics metrics = graphics.getFontMetrics(font);
			return (float) metrics.getStringBounds(text, graphics).getWidth();
		} finally {
			graphics.dispose();
		}
	}

	public List<ExpectedText> getExpected() {
		List<ExpectedText> returnValue = new ArrayList<ExpectedText>();
		for (DrawItem item : items) {
			returnValue.add(item.expected());
		}
		return returnValue;
	}

	public int print(Graphics graphics, PageFormat format, int pageIndex) throws PrinterException {
		if (pageIndex < 0 || pageIndex >= pages) {
			return NO_SUCH_PAGE;
		}
		Graphics2D graphics2D = (Graphics2D) graphics;
		for (DrawItem item : items) {
			if (item.getPage() == pageIndex) {
				item.draw(graphics2D);
			}
		}
		drawGraphics(graphics2D, pageIndex);
		return PAGE_EXISTS;
	}

	public int getNumberOfPages() {
		return pages;
	}

	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return pageFormat;
	}

	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return this;
	}

	@Override
	public String toString() {
		return name;
	}
}
