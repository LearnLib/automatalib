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
package net.automatalib.util.automata.random;

import java.util.Collection;
import java.util.Random;

import net.automatalib.automata.MutableDeterministic;

public class RandomDeterministicAutomatonGenerator<S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> extends
		AbstractRandomAutomatonGenerator<S, I, T, SP, TP, A> {

	public RandomDeterministicAutomatonGenerator(Random random,
			Collection<? extends I> inputs,
			Collection<? extends SP> stateProps,
			Collection<? extends TP> transProps, A automaton) {
		super(random, inputs, stateProps, transProps, automaton);
	}
	
	public void addTransitions() {
		for(S s : states) {
			for(I in : inputs) {
				S succ = randomState();
				TP prop = randomTransProperty();
				automaton.addTransition(s, in, succ, prop);
			}
		}
	}

}
