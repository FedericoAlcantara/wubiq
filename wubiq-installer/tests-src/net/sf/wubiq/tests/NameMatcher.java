/**
 * 
 */
package net.sf.wubiq.tests;

import java.awt.Component;

import org.uispec4j.finder.ComponentMatcher;

/**
 * Component matcher.
 * @author Federico Alcantara
 *
 */
public class NameMatcher implements ComponentMatcher {
	private String name;
	
	public NameMatcher(String name) {
		this.name = name;
	}
	
	/**
	 * @see org.uispec4j.finder.ComponentMatcher#matches(java.awt.Component)
	 */
	@Override
	public boolean matches(Component comp) {
		return name.equals(comp.getName());
	}

}
