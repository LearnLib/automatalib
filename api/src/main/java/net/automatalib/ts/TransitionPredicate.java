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
package net.automatalib.ts;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Predicate;


@ParametersAreNonnullByDefault
@FunctionalInterface
public interface TransitionPredicate<S,I,T> {
	public boolean apply(S source, @Nullable I input, T transition);
	
	default public Predicate<? super T> toUnaryPredicate(final S source, final I input) {
		return trans -> apply(source, input, trans);
	}
	
	public static <S,I,T>
	TransitionPredicate<S,I,T> safePred(TransitionPredicate<S, I, T> pred, final boolean nullValue) {
		if(pred != null) {
			return pred;
		}
		return (s,i,t) -> nullValue;
	}
}
