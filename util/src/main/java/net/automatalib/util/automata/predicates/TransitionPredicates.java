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
package net.automatalib.util.automata.predicates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import net.automatalib.automata.concepts.TransitionOutput;
import net.automatalib.ts.TransitionPredicate;

public abstract class TransitionPredicates {

	private TransitionPredicates() {}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> safePred(TransitionPredicate<S, I, T> pred, boolean nullValue) {
		if(pred != null) {
			return pred;
		}
		return (s,i,t) -> nullValue;
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> compose(Predicate<? super S> sourcePred, Predicate<? super I> inputPred, Predicate<? super T> transPred) {
		final Predicate<? super S> sourcePred_ = (sourcePred == null) ? (s -> true) : sourcePred;
		final Predicate<? super I> inputPred_ = (inputPred == null) ? (i -> true) : inputPred;
		final Predicate<? super T> transPred_ = (transPred == null) ? (t -> true) : transPred;
		return (s,i,t) -> sourcePred_.test(s) && inputPred_.test(i) && transPred_.test(t);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> alwaysTrue() {
		return (s,i,t) -> true;
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> alwaysFalse() {
		return (s,i,t) -> false;
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> constantValue(boolean value) {
		return (s,i,t) -> value;
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> sourceSatisfying(Predicate<? super S> sourcePred) {
		return (s,i,t) -> sourcePred.test(s);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputSatisfying(Predicate<? super I> inputPred) {
		return (s,i,t) -> inputPred.test(i);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> transitionSatisfying(Predicate<? super T> transPred) {
		return (s,i,t) -> transPred.test(t);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIs(Object input) {
		return (s,i,t) -> Objects.equals(i, input);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIsNot(Object input) {
		return (s,i,t) -> !Objects.equals(i, input);
	}
	
	@SafeVarargs
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIn(Object ...inputs) {
		return inputIn(Arrays.asList(inputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIn(Collection<?> inputs) {
		return (s,i,t) -> inputs.contains(i);
	}
	
	@SafeVarargs
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputNotIn(Object... inputs) {
		return inputNotIn(Arrays.asList(inputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputNotIn(Collection<?> inputs) {
		return (s,i,t) -> !inputs.contains(i);
	}
	
	
	
	public static <S,I,T,O>
	TransitionPredicate<S,I,T> outputSatisfies(TransitionOutput<? super T, ? extends O> transOut,
			Predicate<? super O> outputPred) {
		return new OutputSatisfies<>(transOut, outputPred);
	}
	
	public static <S,I,T,O>
	TransitionPredicate<S,I,T> outputViolates(TransitionOutput<? super T, ? extends O> transOut,
			Predicate<? super O> outputPred) {
		return new OutputSatisfies<>(transOut, outputPred, true);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputIs(TransitionOutput<? super T, ?> transOut,
			Object output) {
		return outputSatisfies(transOut, o -> Objects.equals(o, output));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputIsNot(TransitionOutput<? super T,?> transOut,
			Object output) {
		return outputViolates(transOut, o -> Objects.equals(o, output));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputIn(TransitionOutput<? super T, ?> transOut,
			Object... outputs) {
		return outputIn(transOut, Arrays.asList(outputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputIn(TransitionOutput<? super T,?> transOut,
			Collection<?> outputs) {
		return outputSatisfies(transOut, o -> outputs.contains(o));
	}
	
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputNotIn(TransitionOutput<? super T, ?> transOut,
			Object... outputs) {
		return outputNotIn(transOut, Arrays.asList(outputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputNotIn(TransitionOutput<? super T,?> transOut,
			Collection<?> outputs) {
		return outputViolates(transOut, o -> outputs.contains(o));
	}
	
	
}

