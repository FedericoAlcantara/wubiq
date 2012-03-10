/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.io.Serializable;
import java.util.Arrays;


/**
 * @author Federico Alcantara
 *
 */
public class GraphicCommand implements Serializable, Comparable<GraphicCommand> {
	private static final long serialVersionUID = 1L;
	private int executionOrder;
	private String methodName;
	private GraphicParameter[] parameters;
	
	public GraphicCommand(int executionOrder, String methodName, GraphicParameter...parameters) {
		this.executionOrder = executionOrder;
		this.methodName = methodName;
		this.parameters = parameters;
	}

	/**
	 * @return the executionOrder
	 */
	public int getExecutionOrder() {
		return executionOrder;
	}

	/**
	 * @param executionOrder the executionOrder to set
	 */
	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GraphicCommand [methodName=" + methodName + ", parameters="
				+ Arrays.toString(parameters) + "]";
	}

	@Override
	public int compareTo(GraphicCommand other) {
		int returnValue = 0;
		if (this.getExecutionOrder() < other.executionOrder) {
			returnValue = -1;
		}
		if (this.getExecutionOrder() > other.executionOrder) {
			returnValue = 1;
		}
		return returnValue;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + executionOrder;
		return result;
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
		if (!(obj instanceof GraphicCommand)) {
			return false;
		}
		GraphicCommand other = (GraphicCommand) obj;
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
	
}
