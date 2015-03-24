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
package net.automatalib.util.automata.transout;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.words.Alphabet;

/**
 * Various utility methods to filter Mealy machines.
 * 
 * @author Malte Isberner 
 *
 */
public abstract class MealyFilter {
	
	/**
	 * Returns a Mealy machine with all transitions removed that have one of the specified output values. The resulting
	 * Mealy machine will not contain any unreachable states.
	 * <p>
	 * This is a convenience varargs overload of {@link #pruneTransitionsWithOutput(MealyMachine, Alphabet, Collection)}.
	 * 
	 * @param in the input Mealy machine
	 * @param inputs the input alphabet
	 * @param outputs the outputs to remove
	 * @return a Mealy machine with all transitions removed that have one of the specified outputs.
	 */
	@SafeVarargs
	public static <I,O>
	CompactMealy<I,O> pruneTransitionsWithOutput(
			MealyMachine<?,I,?,O> in,
			Alphabet<I> inputs,
			O... outputs) {
		return pruneTransitionsWithOutput(in, inputs, Arrays.asList(outputs));
	}
	
	/**
	 * Returns a Mealy machine with all transitions removed that have one of the specified output values. The resulting
	 * Mealy machine will not contain any unreachable states.
	 * 
	 * @param in the input Mealy machine
	 * @param inputs the input alphabet
	 * @param outputs the outputs to remove
	 * @return a Mealy machine with all transitions removed that have one of the specified outputs.
	 */
	public static <I,O>
	CompactMealy<I,O> pruneTransitionsWithOutput(
			MealyMachine<?,I,?,O> in,
			Alphabet<I> inputs,
			Collection<? super O> outputs) {
		return filterByOutput(in, inputs, o -> !outputs.contains(o));
	}
	
	/**
	 * Returns a Mealy machine with all transitions removed that have an output not among the specified values. The resulting
	 * Mealy machine will not contain any unreachable states.
	 * <p>
	 * This is a convenience varargs overload of {@link #retainTransitionsWithOutput(MealyMachine, Alphabet, Collection)}.
	 * 
	 * @param in the input Mealy machine
	 * @param inputs the input alphabet
	 * @param outputs the outputs to retain
	 * @return a Mealy machine with all transitions retained that have one of the specified outputs.
	 */
	@SafeVarargs
	public static <I,O>
	CompactMealy<I,O> retainTransitionsWithOutput(
			MealyMachine<?,I,?,O> in,
			Alphabet<I> inputs,
			O... outputs) {
		return retainTransitionsWithOutput(in, inputs, Arrays.asList(outputs));
	}
	
	/**
	 * Returns a Mealy machine with all transitions removed that have an output not among the specified values. The resulting
	 * Mealy machine will not contain any unreachable states.
	 * 
	 * @param in the input Mealy machine
	 * @param inputs the input alphabet
	 * @param outputs the outputs to retain
	 * @return a Mealy machine with all transitions retained that have one of the specified outputs.
	 */
	public static <I,O>
	CompactMealy<I,O> retainTransitionsWithOutput(
			MealyMachine<?,I,?,O> in,
			Alphabet<I> inputs,
			Collection<? super O> outputs) {
		return filterByOutput(in, inputs, o -> outputs.contains(o));
	}
	
	public static <I,O>
	CompactMealy<I,O> filterByOutput(
			MealyMachine<?,I,?,O> in,
			Alphabet<I> inputs,
			Predicate<? super O> outputPred) {
		CompactMealy<I,O> out = new CompactMealy<>(inputs);
		filterByOutput(in, inputs, out, outputPred);
		return out;
	}
	
	public static <S1,T1,S2,I,O>
	Mapping<S1,S2> filterByOutput(MealyMachine<S1,I,T1,O> in,
			Collection<? extends I> inputs,
			MutableMealyMachine<S2, I, ?, O> out,
			Predicate<? super O> outputPred) {
		TransitionPredicate<S1,I,T1> transPred = TransitionPredicates.outputSatisfies(in, outputPred);
		
		return AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, in, inputs, out, (Predicate<S1>)(s -> true), transPred);
	}

	private MealyFilter() {
		throw new IllegalStateException("Constructor should never be invoked");
	}
}
