package net.sf.wubiq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.PrintServiceAttribute;

import net.sf.wubiq.common.DirectConnectKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.print.services.RemotePrintServiceLookup;

public class ATest {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ATest test = new ATest();
		test.serialization();
	}
	
	private void serialization() throws IOException, ClassNotFoundException {
		List<byte[]> serializeds = new ArrayList<byte[]>();
		System.out.println("ORIGINAL");
		RemotePrintServiceLookup lookup = new RemotePrintServiceLookup(false);
		PrintServiceLookup.registerServiceProvider(lookup);
		for (PrintService printService : PrintServiceUtils.getPrintServices()) {
			RemotePrintService remotePrintService = new RemotePrintService(printService);
			RemotePrintServiceLookup.registerRemoteService(remotePrintService);
			remotePrintService.setUuid("mac_client");
			
			byte[] serialized = serializePrintService(remotePrintService);
			serializeds.add(serialized);
			System.out.println(remotePrintService);
		}
		System.out.println("\nAFTER REGISTRATION");
		for (PrintService printService : PrintServiceUtils.getPrintServices()) {
			System.out.println(printService);
		}		
		System.out.println("\nSERIALIZED");
		for (byte[] serialized : serializeds) {
			RemotePrintService newRemote = deserialize(serialized);
			System.out.println(newRemote);
		}
	}

	/**
	 * Serializes a remote print service.
	 * @param printService Remote print service to serialize.
	 * @return Byte array representing the object.
	 */
	private byte[] serializePrintService(RemotePrintService printService) {
		byte[] returnValue = null;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(stream);
			output.writeObject(printService.getName());
			output.writeObject(PrintServiceUtils.serializeServiceCategories(printService, false));
			output.writeObject(printService.getUuid());
			output.writeObject(printService.getRemoteComputerName());
			output.writeObject(printService.getRemoteName());
			output.writeObject(printService.getSupportedDocFlavors());
			output.writeBoolean(printService.isMobile());
			output.writeBoolean(printService.getDirectCommunicationEnabled());
			output.close();
			stream.flush();
			stream.close();
			returnValue = stream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	private RemotePrintService deserialize(byte[] service) throws IOException, ClassNotFoundException {
		RemotePrintService remotePrintService = null;
		if (service != null) {
			ByteArrayInputStream stream = new ByteArrayInputStream(service);
			ObjectInputStream input = new ObjectInputStream(stream);
			String serviceName = (String) input.readObject();
			String categoriesString = (String)input.readObject();
			String uuid = (String)input.readObject();
			String remoteComputerName = (String)input.readObject();
			String remoteName = (String)input.readObject();
			DocFlavor[] docFlavors = (DocFlavor[])input.readObject();
			boolean mobile = input.readBoolean();
			boolean directCommunication = input.readBoolean();
			
			remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteComputerName(remoteComputerName);
			remotePrintService.setSupportedDocFlavors(docFlavors);
			remotePrintService.setRemoteName(remoteName);
			remotePrintService.setMobile(mobile);
			remotePrintService.setDirectCommunicationEnabled(directCommunication);
		}
		return remotePrintService;
	}
	
	
	/**
	 * Serializes a remote print service.
	 * @param printService Remote print service to serialize.
	 * @return Byte array representing the object.
	 */
	private byte[] serializePrintServicex(RemotePrintService printService) {
		byte[] returnValue = null;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(stream);

			String categories = PrintServiceUtils.serializeServiceCategories(printService, false);
			output.writeObject(categories);
			output.writeObject(printService.getUuid());
			output.writeObject(printService.getRemoteName());
			output.writeObject(printService.getRemoteComputerName());
			output.writeBoolean(printService.isMobile());
			output.writeObject(printService.getSupportedDocFlavors());
			output.writeBoolean(printService.getDirectCommunicationEnabled());
			output.writeObject(printService.getClientVersion());
			output.writeObject(printService.getGroups());
			output.close();
			stream.flush();
			stream.close();
			returnValue = stream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
}
