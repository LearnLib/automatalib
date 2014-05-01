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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.lts.MutableLTS;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.words.Alphabet;

/**
 * A nondeterministic labelled transition system.
 * 
 * @author Michele Volpato
 *
 * @param <I> input symbol class
 */
public class InputOutputLTS<I> implements MutableLTS<I>,
		DOTPlottableAutomaton<Integer, I, Integer> {

	/**
	 * @param inputAlphabet 
	 * 
	 */
	public InputOutputLTS(Alphabet<Character> inputAlphabet) {
		// TODO Auto-generated constructor stub
	}


}