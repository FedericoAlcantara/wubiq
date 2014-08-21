/**
 * 
 */
package net.sf.wubiq.clients;

import java.util.ArrayList;
import java.util.List;

import net.sf.wubiq.utils.InstallerUtils;
import net.sf.wubiq.utils.Is;

/**
 * @author Federico Alcantara
 *
 */
public class JavaRun {
	private String[] jvmParameters;
	private String jarFile;
	private String mainName;
	private String[] fixedParameters;
	private String[] parameters;
		
	/**
	 * Creates the comand to run the java program.
	 * @return Array of string representing the command.
	 */
	public String[] command() {
		List<String> returnValue = new ArrayList<String>();
		returnValue.add(InstallerUtils.INSTANCE.jrePath());
		boolean validJava = false;
		if (jvmParameters != null) {
			for (String jvmParameter : jvmParameters) {
				if (!Is.emptyString(jvmParameter)) {
					returnValue.add(jvmParameter.trim());
				}
			}
		}
		if (!Is.emptyString(jarFile)) {
			returnValue.add("-jar");
			returnValue.add(jarFile);
			validJava = true;
		} else if (!Is.emptyString(mainName)){
			returnValue.add(mainName.trim());
			validJava = true;
		}
		if (validJava) {
			if (fixedParameters != null) {
				for (String fixedParameter : fixedParameters) {
					if (!Is.emptyString(fixedParameter)) {
						returnValue.add(fixedParameter.trim());
					}
				}
			}
			if (parameters != null) {
				for (String parameter : parameters) {
					if (!Is.emptyString(parameter)) {
						returnValue.add(parameter.trim());
					}
				}
			}
		} else {
			throw new RuntimeException("Invalid java command");
		}
		return returnValue.toArray(new String[0]);
	}
	
	/**
	 * @return the jvmParameters
	 */
	public String[] getJvmParameters() {
		return jvmParameters;
	}
	/**
	 * @param jvmParameters the jvmParameters to set
	 */
	public void setJvmParameters(String... jvmParameters) {
		this.jvmParameters = jvmParameters;
	}
	/**
	 * @return the jarFile
	 */
	public String getJarFile() {
		return jarFile;
	}
	/**
	 * @param jarFile the jarFile to set
	 */
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}
	/**
	 * @return the mainName
	 */
	public String getMainName() {
		return mainName;
	}
	/**
	 * @param mainName the mainName to set
	 */
	public void setMainName(String mainName) {
		this.mainName = mainName;
	}
	public String[] getFixedParameters() {
		return fixedParameters;
	}

	public void setFixedParameters(String... fixedParameters) {
		this.fixedParameters = fixedParameters;
	}

	/**
	 * @return the parameters
	 */
	public String[] getParameters() {
		return parameters;
	}
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(String... parameters) {
		this.parameters = parameters;
	}
	
	
}
