/* Copyright (C) 2014 AutomataLib
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
package net.automatalib.automata.lts.impl;

import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.lts.MutableLTS;
import net.automatalib.automata.lts.abstractimpl.AbstractMutableLTS;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.InputOutputLabel;

/**
 * A nondeterministic input output labelled transition system.
 * 
 * @author Michele Volpato
 *
 * @param <L> input symbol class
 */
public class InputOutputLTS<I,O> extends AbstractMutableLTS<InputOutputLabel> implements MutableLTS<InputOutputLabel>,
		DOTPlottableAutomaton<Integer, InputOutputLabel, Integer> {

	public InputOutputLTS(Alphabet<InputOutputLabel> alphabet) {
		super(alphabet);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Void getStateProperty(Integer state) {
		// no property
		return null;
	}

	@Override
	public Void getTransitionProperty(Integer transition) {
		// no property
		return null;
	}

	@Override
	public void setStateProperty(Integer state, Void property) {
		// no property
		
	}

	@Override
	public void setTransitionProperty(Integer transition, Void property) {
		// no property
		
	}

	@Override
	public GraphDOTHelper<Integer, TransitionEdge<InputOutputLabel, Integer>> getDOTHelper() {
		// TODO Auto-generated method stub
		return null;
	}



	

}
