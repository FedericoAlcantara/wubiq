/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.image.ColorModel;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;

/**
 * @author Federico Alcantara
 *
 */
public class ColorModelRemote extends ColorModel implements IProxyClient {

	public ColorModelRemote() {
		super(0);
		initialize();
	}
	
	public static final String[] FILTERED_METHODS = new String[]{
	};
	
	@Override
	public int getAlpha(int pixel) {
		return 0;
	}

	@Override
	public int getBlue(int pixel) {
		return 0;
	}

	@Override
	public int getGreen(int pixel) {
		return 0;
	}

	@Override
	public int getRed(int pixel) {
		return 0;
	}


	/* ***************************
	 * Proxied methods
	 * ***************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyClient#manager()
	 */
	public DirectPrintManager manager() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	public void initialize(){
	}

	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	public UUID objectUUID() {
		return null;
	}

}
