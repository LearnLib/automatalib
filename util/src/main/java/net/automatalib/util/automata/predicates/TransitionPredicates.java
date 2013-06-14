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

import javax.annotation.Nonnull;

import net.automatalib.automata.concepts.TransitionOutput;
import net.automatalib.ts.TransitionPredicate;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public abstract class TransitionPredicates {

	private TransitionPredicates() {}
	
	public static <S,I,T>
	Predicate<T> toUnaryPredicate(final TransitionPredicate<? super S,? super I,? super T> transPred,
			final S source, final I input) {
		return new Predicate<T>() {
			@Override
			public boolean apply(@Nonnull T trans) {
				return transPred.apply(source, input, trans);
			}
		};
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> safePred(TransitionPredicate<S, I, T> pred, boolean nullValue) {
		if(pred != null) {
			return pred;
		}
		return constantValue(nullValue);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> compose(Predicate<? super S> sourcePred, Predicate<? super I> inputPred, Predicate<? super T> transPred) {
		if(sourcePred == null) {
			sourcePred = Predicates.alwaysTrue();
		}
		if(inputPred == null) {
			inputPred = Predicates.alwaysTrue();
		}
		if(transPred == null) {
			transPred = Predicates.alwaysTrue();
		}
		return new CompositeTransitionPredicate<>(sourcePred, inputPred, transPred); 
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> constantValue(boolean value) {
		if(value) {
			return alwaysTrue();
		}
		return alwaysFalse();
	}
	
	@SuppressWarnings("unchecked")
	public static <S,I,T>
	TransitionPredicate<S,I,T> alwaysTrue() {
		return (TransitionPredicate<S,I,T>)ConstantTransitionPredicate.TRUE;
	}
	
	@SuppressWarnings("unchecked")
	public static <S,I,T>
	TransitionPredicate<S,I,T> alwaysFalse() {
		return (TransitionPredicate<S,I,T>)ConstantTransitionPredicate.FALSE;
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> sourceSatisfying(Predicate<? super S> sourcePred) {
		return compose(sourcePred, null, null);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputSatisfying(Predicate<? super I> inputPred) {
		return compose(null, inputPred, null);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> transitionSatisfying(Predicate<? super T> transPred) {
		return compose(null, null, transPred);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIs(Object input) {
		return inputSatisfying(Predicates.equalTo(input));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIsNot(Object input) {
		return inputSatisfying(Predicates.not(Predicates.equalTo(input)));
	}
	
	@SafeVarargs
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIn(Object ...inputs) {
		return inputIn(Arrays.asList(inputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputIn(Collection<?> inputs) {
		return inputSatisfying(Predicates.in(inputs));
	}
	
	@SafeVarargs
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputNotIn(Object... inputs) {
		return inputNotIn(Arrays.asList(inputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> inputNotIn(Collection<?> inputs) {
		return inputSatisfying(Predicates.not(Predicates.in(inputs)));
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
		return outputSatisfies(transOut, Predicates.equalTo(output));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputIsNot(TransitionOutput<? super T,?> transOut,
			Object output) {
		return outputViolates(transOut, Predicates.equalTo(output));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputIn(TransitionOutput<? super T, ?> transOut,
			Object... outputs) {
		return outputIn(transOut, Arrays.asList(outputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputIn(TransitionOutput<? super T,?> transOut,
			Collection<?> outputs) {
		return outputSatisfies(transOut, Predicates.in(outputs));
	}
	
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputNotIn(TransitionOutput<? super T, ?> transOut,
			Object... outputs) {
		return outputNotIn(transOut, Arrays.asList(outputs));
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> outputNotIn(TransitionOutput<? super T,?> transOut,
			Collection<?> outputs) {
		return outputViolates(transOut, Predicates.in(outputs));
	}
	
	
}
