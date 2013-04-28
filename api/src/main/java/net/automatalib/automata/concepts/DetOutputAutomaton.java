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
package net.automatalib.automata.concepts;

import net.automatalib.automata.DeterministicAutomaton;

/**
 * An automaton which deterministically produces an output for an input word. Here,
 * output refers to the <i>complete</i> output that is made when an input word is read,
 * not a single symbol.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 * @param <O> output class
 */
public interface DetOutputAutomaton<S, I, T, O> extends
		OutputAutomaton<S, I, T, O>, DeterministicAutomaton<S, I, T> {
}
