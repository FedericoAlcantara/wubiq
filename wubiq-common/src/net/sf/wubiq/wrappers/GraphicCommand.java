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
	
}
