package net.sf.wubiq.dotmatrix;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.wubiq.dotmatrix.witness.ExpectedText;
import net.sf.wubiq.dotmatrix.witness.PrinterProfile;

/**
 * A deliberately simple, correct ESC/P encoder for the fixtures' expectations.
 * It proves the fixtures and the witness tolerances can be met: absolute horizontal
 * moves (ESC $), paper advance in 1/216 inch (ESC J), an 11 inch page and PC850 text
 * (PC437 lacks the uppercase accented vowels other than E acute).
 * @author Federico Alcantara
 *
 */
public class ReferenceEscpEncoder {
	private static final int ESC = 0x1B;
	private final PrinterProfile profile;

	public ReferenceEscpEncoder(PrinterProfile profile) {
		this.profile = profile;
	}

	public byte[] encode(List<ExpectedText> expectations, int pages) throws UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(out, ESC, '@', ESC, 'x', 0, ESC, 'p', 0);
		write(out, ESC, '(', 't', 3, 0, 1, 3, 0, ESC, 't', 1);
		write(out, ESC, 'C', 0, (int) Math.round(profile.getPaperHeight()));
		for (int page = 0; page < pages; page++) {
			List<ExpectedText> items = new ArrayList<ExpectedText>();
			for (ExpectedText item : expectations) {
				if (item.getPage() == page) {
					items.add(item);
				}
			}
			Collections.sort(items, new Comparator<ExpectedText>() {
				public int compare(ExpectedText first, ExpectedText second) {
					int value = Double.compare(first.getBaseline(), second.getBaseline());
					return value != 0 ? value : Double.compare(first.getX(), second.getX());
				}
			});
			int line = 0;
			for (ExpectedText item : items) {
				int target = (int) Math.round((item.getBaseline() - profile.getBaselineOffset()) / profile.getFineLineUnit());
				while (line < target) {
					int step = Math.min(255, target - line);
					write(out, ESC, 'J', step);
					line += step;
				}
				write(out, 0x0D);
				int position = (int) Math.round(item.getX() * 60);
				write(out, ESC, '$', position % 256, position / 256);
				pitch(out, item.getCpi());
				write(out, ESC, item.isBold() ? 'E' : 'F', ESC, item.isItalic() ? '4' : '5', ESC, '-', item.isUnderline() ? 1 : 0);
				byte[] text = item.getText().getBytes("IBM850");
				out.write(text, 0, text.length);
				write(out, ESC, '-', 0);
			}
			write(out, 0x0D, 0x0C);
		}
		return out.toByteArray();
	}

	private void pitch(ByteArrayOutputStream out, double cpi) {
		write(out, 0x12, ESC, 'W', 0);
		if (cpi >= 20) {
			write(out, ESC, 'M', 0x0F);
		} else if (cpi >= 17) {
			write(out, ESC, 'P', 0x0F);
		} else if (cpi >= 15) {
			write(out, ESC, 'g');
		} else if (cpi >= 12) {
			write(out, ESC, 'M');
		} else if (cpi >= 10) {
			write(out, ESC, 'P');
		} else if (cpi >= 6) {
			write(out, ESC, 'M', ESC, 'W', 1);
		} else {
			write(out, ESC, 'P', ESC, 'W', 1);
		}
	}

	private static void write(ByteArrayOutputStream out, int... values) {
		for (int value : values) {
			out.write(value);
		}
	}
}
