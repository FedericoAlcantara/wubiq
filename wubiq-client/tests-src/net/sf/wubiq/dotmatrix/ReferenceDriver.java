package net.sf.wubiq.dotmatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.sf.wubiq.dotmatrix.witness.PrinterProfile;

/**
 * Not Wubiq: writes what a correct encoder produces, so each report shows a passing
 * sheet next to Wubiq's.
 * @author Federico Alcantara
 *
 */
public class ReferenceDriver implements PrintDriver {

	public String getName() {
		return "reference";
	}

	public String run(WitnessFixture fixture, File escp, File directory) throws Exception {
		byte[] data = new ReferenceEscpEncoder(PrinterProfile.ninePin())
				.encode(fixture.getExpected(), fixture.getNumberOfPages());
		OutputStream stream = new FileOutputStream(escp);
		try {
			stream.write(data);
		} finally {
			stream.close();
		}
		return "ReferenceEscpEncoder (not Wubiq code)\n";
	}
}
