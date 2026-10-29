package net.sf.wubiq.dotmatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.SimpleDoc;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;

import net.sf.wubiq.wrappers.PageableStreamWrapper;

/**
 * Prints through the JDK's own print pipeline (the PostScript stream service), with no
 * printer. This exercises the real printer graphics, its device transform and the
 * JDK's multi-pass page loop, which is where JDK versions differ. The PostScript
 * output is kept as <code>job.ps</code>: what a laser printer would print.
 * @author Federico Alcantara
 *
 */
public class DocPrintJobDriver implements PrintDriver {

	public String getName() {
		return "jdk-print";
	}

	public String run(WitnessFixture fixture, File escp, File directory) throws Exception {
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
		StreamPrintServiceFactory[] factories = StreamPrintServiceFactory
				.lookupStreamPrintServiceFactories(flavor, "application/postscript");
		if (factories.length == 0) {
			throw new IllegalStateException("This JVM has no PostScript stream print service");
		}
		File postScript = new File(directory, "job.ps");
		OutputStream stream = new FileOutputStream(postScript);
		try {
			StreamPrintService service = factories[0].getPrintService(stream);
			DocPrintJob job = service.createPrintJob();
			job.print(new SimpleDoc(new PageableStreamWrapper(fixture, escp.getAbsolutePath()), flavor, null),
					new HashPrintRequestAttributeSet());
		} finally {
			stream.close();
		}
		return "DocPrintJob via " + factories[0].getClass().getName() + ", PostScript " + postScript.length() + " bytes\n";
	}
}
