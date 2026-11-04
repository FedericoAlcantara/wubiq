package net.sf.wubiq.dotmatrix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import net.sf.wubiq.dotmatrix.witness.BitImage;
import net.sf.wubiq.dotmatrix.witness.CheckResult;
import net.sf.wubiq.dotmatrix.witness.EscpResult;
import net.sf.wubiq.dotmatrix.witness.PrinterProfile;

/**
 * Checks that the dots of a printed job are where the fixture's shapes and images are: every dot
 * lies near something the fixture drew, and every pixel the fixture drew that is at least a fifth covered
 * (a hairline that falls between two pixels is) has a dot near it. The fixture is drawn antialiased, as
 * a laser printer would print it.
 * @author Federico Alcantara
 *
 */
public class GraphicsCheck {
	private static final int DPI = 100;
	/** Distance, in pixels of {@link #DPI}, a dot may be from what it prints (0.02 in). */
	private static final int NEAR = 2;
	private static final double MINIMUM_SHARE = 0.97;
	/** Pixels darker than this have been drawn on. */
	private static final int COVERED = 200;
	private final PrinterProfile profile;

	public GraphicsCheck(PrinterProfile profile) {
		this.profile = profile;
	}

	public void check(EscpResult result, WitnessFixture fixture, CheckResult check) {
		for (int page = 0; page < fixture.getNumberOfPages(); page++) {
			BufferedImage drawn = draw(fixture, page);
			boolean[][] dots = dots(result.getBitImages(page), drawn.getWidth(), drawn.getHeight());
			int printed = 0;
			int printedNearInk = 0;
			int solid = 0;
			int solidNearDot = 0;
			for (int x = 0; x < drawn.getWidth(); x++) {
				for (int y = 0; y < drawn.getHeight(); y++) {
					int brightness = brightness(drawn.getRGB(x, y));
					if (dots[x][y]) {
						printed++;
						if (near(drawn, x, y)) {
							printedNearInk++;
						}
					}
					if (brightness < COVERED) {
						solid++;
						if (nearDot(dots, x, y)) {
							solidNearDot++;
						}
					}
				}
			}
			String subject = "graphics page " + (page + 1);
			if (printed == 0 && solid > 0) {
				check.fail(subject, "no dots printed for " + solid + " pixels the fixture draws");
				continue;
			}
			double precision = printed == 0 ? 1 : (double) printedNearInk / printed;
			double recall = solid == 0 ? 1 : (double) solidNearDot / solid;
			String message = String.format("%.1f%% of the printed pixels lie on drawn graphics, %.1f%% of the drawn pixels have a dot",
					precision * 100, recall * 100);
			if (precision >= MINIMUM_SHARE && recall >= MINIMUM_SHARE) {
				check.pass(subject, message);
			} else {
				check.fail(subject, message);
			}
		}
	}

	private BufferedImage draw(WitnessFixture fixture, int page) {
		BufferedImage image = new BufferedImage((int) Math.ceil(profile.getPaperWidth() * DPI),
				(int) Math.ceil(profile.getPaperHeight() * DPI), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.scale(DPI / 72d, DPI / 72d);
			graphics.setColor(Color.BLACK);
			fixture.drawGraphics(graphics, page);
		} finally {
			graphics.dispose();
		}
		return image;
	}

	private static boolean[][] dots(List<BitImage> bitImages, int width, int height) {
		boolean[][] returnValue = new boolean[width][height];
		for (BitImage bitImage : bitImages) {
			for (int column = 0; column < bitImage.getColumns().length; column++) {
				for (int dot = 0; dot < bitImage.getDots(); dot++) {
					if (bitImage.isSet(column, dot)) {
						int x = (int) Math.floor((bitImage.getX() + (column + 0.5) * bitImage.getColumnWidth()) * DPI);
						int y = (int) Math.floor((bitImage.getY() + (dot + 0.5) * bitImage.getDotPitch()) * DPI);
						if (x >= 0 && x < width && y >= 0 && y < height) {
							returnValue[x][y] = true;
						}
					}
				}
			}
		}
		return returnValue;
	}

	private static boolean near(BufferedImage image, int x, int y) {
		for (int dx = -NEAR; dx <= NEAR; dx++) {
			for (int dy = -NEAR; dy <= NEAR; dy++) {
				int px = x + dx;
				int py = y + dy;
				if (px >= 0 && py >= 0 && px < image.getWidth() && py < image.getHeight()
						&& brightness(image.getRGB(px, py)) < 250) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean nearDot(boolean[][] dots, int x, int y) {
		for (int dx = -NEAR; dx <= NEAR; dx++) {
			for (int dy = -NEAR; dy <= NEAR; dy++) {
				int px = x + dx;
				int py = y + dy;
				if (px >= 0 && py >= 0 && px < dots.length && py < dots[0].length && dots[px][py]) {
					return true;
				}
			}
		}
		return false;
	}

	private static int brightness(int rgb) {
		return (((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114) / 1000;
	}
}
