package net.sf.wubiq.dotmatrix;

import static org.junit.Assert.assertTrue;

import net.sf.wubiq.dotmatrix.witness.CheckResult;
import net.sf.wubiq.dotmatrix.witness.EscpInterpreter;
import net.sf.wubiq.dotmatrix.witness.EscpResult;
import net.sf.wubiq.dotmatrix.witness.LayoutCheck;
import net.sf.wubiq.dotmatrix.witness.PrinterProfile;

import org.junit.Test;

/**
 * Every fixture, encoded correctly, must pass the layout check. This keeps the
 * fixtures and the witness honest: failures in {@link DotMatrixWitnessTest} are
 * then Wubiq's, not the harness's.
 * @author Federico Alcantara
 *
 */
public class ReferenceEncoderTest {

	@Test
	public void correctlyEncodedFixturesPass() throws Exception {
		PrinterProfile profile = PrinterProfile.ninePin();
		StringBuilder failures = new StringBuilder();
		for (WitnessFixture fixture : Fixtures.all()) {
			byte[] data = new ReferenceEscpEncoder(profile).encode(fixture.getExpected(), fixture.getNumberOfPages());
			EscpResult result = new EscpInterpreter(profile).interpret(data);
			CheckResult check = new LayoutCheck(profile).strictPitch(true)
					.check(result, fixture.getExpected(), fixture.getNumberOfPages());
			if (!check.isPassed()) {
				failures.append(fixture.getName()).append(": ").append(check).append('\n');
			}
		}
		assertTrue(failures.toString(), failures.length() == 0);
	}
}
