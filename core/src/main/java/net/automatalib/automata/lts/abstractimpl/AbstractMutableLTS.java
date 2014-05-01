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
package net.automatalib.automata.lts.abstractimpl;

import net.automatalib.automata.abstractimpl.AbstractMutableAutomaton;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.lts.MutableLTS;

/**
 * Abstract base class for automata nondeterministic mutable labelled transition system.
 * 
 * @author Michele Volpato
 *
 * @param <S> state class
 * @param <I> input symbol class
 */
public abstract class AbstractMutableLTS<S,I> extends AbstractMutableAutomaton<S, I, S, Void, Void> 
	implements MutableLTS<S, I>, DOTPlottableAutomaton<S, I, S> {

	/**
	 * 
	 */
	public AbstractMutableLTS() {
		// TODO Auto-generated constructor stub
	}

}
