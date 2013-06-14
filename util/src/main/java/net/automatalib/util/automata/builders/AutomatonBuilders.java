/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.util.automata.builders;

import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;

/**
 * Fluent interface automaton builders.
 * 
 * @author Malte Isberner
 *
 */
public abstract class AutomatonBuilders {

	private AutomatonBuilders() {
	}
	
	
	public static <S,I,A extends MutableDFA<S,? super I>>
	DFABuilder<S, I, A> forDFA(A dfa) {
		return new DFABuilder<>(dfa);
	}
	
	public static <I>
	DFABuilder<Integer,I,CompactDFA<I>> newDFA(Alphabet<I> alphabet) {
		return forDFA(new CompactDFA<>(alphabet));
	}
	
	public static <S,I,T,O,A extends MutableMealyMachine<S,? super I,T,? super O>>
	MealyBuilder<S,I,T,O,A> forMealy(A mealy) {
		return new MealyBuilder<>(mealy);
	}
	
	public static <I,O>
	MealyBuilder<Integer,I,CompactMealyTransition<O>,O,CompactMealy<I,O>> newMealy(Alphabet<I> alphabet) {
		return forMealy(new CompactMealy<I,O>(alphabet));
	}
}
