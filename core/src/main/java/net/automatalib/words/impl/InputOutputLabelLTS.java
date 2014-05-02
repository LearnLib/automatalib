/**
 * 
 */
package net.automatalib.words.impl;

import net.automatalib.words.InputOutputLabel;

/**
 * @author mvolpato
 *
 */
public class InputOutputLabelLTS implements InputOutputLabel {
	
	private final Object label;
	private final boolean input;

	/**
	 * 
	 */
	public InputOutputLabelLTS(Object label, boolean isInput) {
		this.label = label;
		this.input = isInput;
	}
	
	@Override
	public boolean isInput() {
		return input;
	}

	@Override
	public boolean isOutput() {
		return !isInput();
	}

	@Override
	public Object getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return label.toString();
	}


}
