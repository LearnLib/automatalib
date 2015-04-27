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
package net.automatalib.automata.transout;

import java.util.Collection;

import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.ts.transout.DeterministicTransitionOutputTS;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;


public interface TransitionOutputAutomaton<S, I, T, O>
		extends DetSuffixOutputAutomaton<S, I, T, Word<O>>, DeterministicTransitionOutputTS<S,I,T,O> {
	@Override
	default public Word<O> computeStateOutput(S state, Iterable<? extends I> input) {
		WordBuilder<O> result;
		if (input instanceof Collection) {
			result = new WordBuilder<>(((Collection<?>) input).size());
		}
		else {
			result = new WordBuilder<>();
		}
		trace(state, input, result);
		return result.toWord();
	}
}

