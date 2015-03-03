package net.sf.wubiq.wrappers;

import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.wubiq.utils.DirectConnectUtils;

public class CompressedGraphicsPage implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<String>methodNamesList;
	@SuppressWarnings("rawtypes")
	private List<Class>parametersTypeList;
	private List<Object>parametersValueList;
	private List<Font>fontsList;
	private byte[] compressedData;
	private transient int index;
	private transient Set<GraphicCommand> graphicCommands;
	
	@SuppressWarnings("rawtypes")
	public CompressedGraphicsPage(GraphicsChunkRecorder graphicsRecorder,
			Set<GraphicCommand> graphicCommands) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		methodNamesList = new ArrayList<String>(graphicsRecorder.getMethodNamesList());
		parametersTypeList = new ArrayList<Class>(graphicsRecorder.getParametersTypeList());
		parametersValueList = new ArrayList<Object>(graphicsRecorder.getParametersValueList());
		fontsList = new ArrayList<Font>(graphicsRecorder.getFontsList());
		for (GraphicCommand command : graphicCommands) {
			output.write(DirectConnectUtils.INSTANCE.intToByteArray4(command.getExecutionOrder()), 0, 4);
			output.write(graphicsRecorder.getMethodNamesList().indexOf(command.getMethodName()));
			output.write(command.getParameters().length);
			for (GraphicParameter parameter : command.getParameters()) {
				output.write(graphicsRecorder.getParametersTypeList().indexOf(parameter.getParameterType()));
				output.write(DirectConnectUtils.INSTANCE.intToByteArray4(graphicsRecorder.getParametersValueList().indexOf(parameter.getParameterValue())), 0, 4);
			}
		}
		try {
			output.flush();
			output.close();
			compressedData = output.toByteArray();
		} catch (IOException e) {
			
		}
	}
	
	public Set<GraphicCommand> getGraphicCommands() {
		if (graphicCommands == null) {
			graphicCommands = new TreeSet<GraphicCommand>();
			index = 0;
			while (index < compressedData.length) {
				addCommand();
			}
		}
		return graphicCommands;
	}
	
	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the methodNamesList
	 */
	public List<String> getMethodNamesList() {
		return methodNamesList;
	}

	/**
	 * @return the parametersTypeList
	 */
	@SuppressWarnings("rawtypes")
	public List<Class> getParametersTypeList() {
		return parametersTypeList;
	}

	/**
	 * @return the parametersValueList
	 */
	public List<Object> getParametersValueList() {
		return parametersValueList;
	}

	/**
	 * Gets the fontsList list.
	 * @return Fonts list.
	 */
	public List<Font> getFontsList() {
		return fontsList;
	}
	
	/**
	 * Clears all the lists.
	 */
	public void clearLists() {
		if (methodNamesList != null) {
			methodNamesList.clear();
		}
		if (parametersTypeList != null) {
			parametersTypeList.clear();
		}
		if (parametersValueList != null) {
			parametersValueList.clear();
		}
		if (fontsList != null) {
			fontsList.clear();
		}
		if (graphicCommands != null) {
			graphicCommands.clear();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void addCommand() {
		int executionOrder = DirectConnectUtils.INSTANCE.byteArrayToInt4(Arrays.copyOfRange(compressedData, index, index + 4));
		index += 4;
		String methodName = methodNamesList.get(compressedData[index++]);
		int parameterCount = compressedData[index++];
		int parameterIndex = 0;
		GraphicParameter[] graphicParameters = new GraphicParameter[parameterCount];
		while (parameterIndex < parameterCount) {
			Class parameterType = parametersTypeList.get(compressedData[index++]);
			Object parameterValue = parametersValueList.get(DirectConnectUtils.INSTANCE.byteArrayToInt4(Arrays.copyOfRange(compressedData, index, index + 4)));
			index += 4;
			graphicParameters[parameterIndex++] = new GraphicParameter(parameterType, parameterValue);
		}
		
		GraphicCommand command = new GraphicCommand(executionOrder, methodName, graphicParameters);
		graphicCommands.add(command);
	}
	
}
