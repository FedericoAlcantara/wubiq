package net.sf.wubiq.dotmatrix;

import java.io.File;

/**
 * Pushes a fixture through Wubiq's dot-matrix stream path, the way some caller would.
 * @author Federico Alcantara
 *
 */
public interface PrintDriver {
	String getName();

	/**
	 * @param fixture Job to print.
	 * @param escp File given to the stream wrappers as the printer url.
	 * @param directory Report directory, for side outputs.
	 * @return Notes about the run (calls made, page results).
	 */
	String run(WitnessFixture fixture, File escp, File directory) throws Exception;
}
