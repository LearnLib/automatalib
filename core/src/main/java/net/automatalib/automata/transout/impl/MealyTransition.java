/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.automata.transout.impl;

/**
 * A transition of a mealy machine, comprising a successor state
 * and an output symbol.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
 *
 * @param <S> state class.
 * @param <O> output symbol class.
 */
public class MealyTransition<S, O> {
	private final S successor;
	private O output;
	
	
	/**
	 * Constructor.
	 * @param successor successor state.
	 * @param output output symbol.
	 */
	public MealyTransition(S successor, O output) {
		this.successor = successor;
		this.output = output;
	}


	/**
	 * Retrieves the output symbol.
	 * @return the output symbol.
	 */
	public O getOutput() {
		return output;
	}


	/**
	 * Sets the output symbol.
	 * @param output the new output symbol.
	 */
	public void setOutput(O output) {
		this.output = output;
	}


	/**
	 * Retrieves the successor state.
	 * @return the successor state.
	 */
	public S getSuccessor() {
		return successor;
	}
}
