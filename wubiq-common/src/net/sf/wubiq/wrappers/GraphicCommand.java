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
public class GraphicCommand implements Serializable {
	private static final long serialVersionUID = 1L;
	private String methodName;
	private GraphicParameter[] parameters;
	
	public GraphicCommand(String methodName, GraphicParameter...parameters) {
		this.methodName = methodName;
		this.parameters = parameters;
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
	
}
