/**
 * 
 */
package net.sf.wubiq.adapters;

import java.io.Serializable;
import java.util.Arrays;

import net.sf.wubiq.enums.RemoteCommandType;
import net.sf.wubiq.wrappers.GraphicParameter;


/**
 * Represents a graphic command to be executed by a printable.
 * @author Federico Alcantara
 *
 */
public class RemoteCommand implements Serializable {
	private static final long serialVersionUID = 1L;
	private RemoteCommandType remoteCommandType;
	private String methodName;
	private GraphicParameter[] parameters;
		
	public RemoteCommand(RemoteCommandType remoteCommandType, 
			String methodName, GraphicParameter...parameters) {
		this.remoteCommandType = remoteCommandType;
		this.methodName = methodName;
		this.parameters = parameters;
	}	

	/**
	 * @return the remoteCommandType
	 */
	public RemoteCommandType getRemoteCommandType() {
		return remoteCommandType;
	}

	/**
	 * @param remoteCommandType the remoteCommandType to set
	 */
	public void setRemoteCommandType(RemoteCommandType remoteCommandType) {
		this.remoteCommandType = remoteCommandType;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the parameters
	 */
	public GraphicParameter[] getParameters() {
		return parameters;
	}

	/**
	 * @param methodName the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(GraphicParameter[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RemoteCommand)) {
			return false;
		}
		RemoteCommand other = (RemoteCommand) obj;
		if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (parameters.length != other.parameters.length) {
			return false;
		}
		for (int index = 0; index < parameters.length; index++) {
			if (!parameters[index].getParameterType().equals(other.parameters[index].getParameterType())) {
				return false;
			}
			if (parameters[index].getParameterValue() == null && other.parameters[index].getParameterValue() != null) {
				return false;
			}
			if (parameters[index].getParameterValue() != null && other.parameters[index].getParameterValue() == null) {
				return false;
			}
			if (parameters[index].getParameterValue() != null && other.parameters[index].getParameterValue() != null &&
					!parameters[index].getParameterValue().equals(other.parameters[index].getParameterValue())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "RemoteCommand [remoteCommandType=" + remoteCommandType
				+ ", methodName=" + methodName + ", parameters="
				+ Arrays.toString(parameters) + "]";
	}
	
}
