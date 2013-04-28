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
package net.automatalib.automata.dot;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.dot.GraphDOTHelper;

public interface DOTPlottableAutomaton<S, I, T> extends Automaton<S, I, T>, InputAlphabetHolder<I> {
	public GraphDOTHelper<S,Pair<I,T>> getDOTHelper();
}
