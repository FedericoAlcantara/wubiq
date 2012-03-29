/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.HashMap;
import java.util.Map;

import net.sf.wubiq.enums.TextAlignmentType;

/**
 * Print occurs using simple text drawing.
 * @author Federico Alcantara
 *
 */
public class TextPrintable implements Printable {
	private TextPageable textPageable;
	private Font lastFontUsed;
	
	public TextPrintable(TextPageable textPageable) {
		this.textPageable = textPageable;
	}
	/**
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	public int print(Graphics graph, PageFormat format, int pageIndex)
			throws PrinterException {
		boolean oneFound = false;
		lastFontUsed = null;
		Graphics2D graphics2D = (Graphics2D)graph;
		graphics2D.setBackground(Color.WHITE);
		graphics2D.setColor(Color.BLACK);
		float xOffset = (float) (18f + format.getPaper().getImageableX());
		float yOffset = (float) (30f + format.getPaper().getImageableY());
		if (xOffset >= 36) {
			xOffset -= 18f;
		}
		if (yOffset >= 60) {
			yOffset -= 30;
		}
		graphics2D.translate(xOffset, yOffset);
		for (TextField textField : textPageable.getTextLines()) {
			if (textField.getPage() == pageIndex) {
				oneFound = true;
				printField(graphics2D, textField);
			} else if (textField.getPage() > pageIndex) {
				oneFound = true;
			}
		}
		if (oneFound) {
			return Printable.PAGE_EXISTS;
		}
		return Printable.NO_SUCH_PAGE;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void printField(Graphics2D graph, TextField textField) {
		Map attributes = new HashMap();
		float fontSize = textField.getFontSize() - 1f;
		attributes.put(TextAttribute.FAMILY, Font.SANS_SERIF);
		attributes.put(TextAttribute.SIZE, fontSize);
		attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRA_LIGHT);
		attributes.put(TextAttribute.WIDTH, TextAttribute.WIDTH_REGULAR);
		if (textField.isBold()) {
			attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		}
		if (textField.isItalic()) {
			attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		}
		if (textField.isUnderline()) {
			attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		}
		Font font = Font.getFont(attributes);
		if (lastFontUsed == null || !lastFontUsed.equals(font)) {
			graph.setFont(font);
			lastFontUsed = font;
		}
		float x = textField.getX() * 72 / 1440;
		float y = textField.getY() * 72 / 1440;
		if (textField.getTextAlignment().equals(TextAlignmentType.RIGHT)) {
			float width = textField.getWidth() * 72 / 1440;
			Rectangle2D bounds = font.getStringBounds(textField.getText(), graph.getFontRenderContext());
			float offset = (float) (width - bounds.getWidth());
			x += offset;
		} else if (textField.getTextAlignment().equals(TextAlignmentType.CENTER)) {
			float width = textField.getWidth() * 72 / 1440;
			Rectangle2D bounds = font.getStringBounds(textField.getText(), graph.getFontRenderContext());
			float offset = (float) ((width - bounds.getWidth()) / 2);
			x += offset;
		}
		graph.drawString(textField.getText(), x, y);
	}
}
