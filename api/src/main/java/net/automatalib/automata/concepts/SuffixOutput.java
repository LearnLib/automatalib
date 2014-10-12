/* Copyright (C) 2013-2014 TU Dortmund
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;

/**
 * Feature for automata that compute a <i>suffix-observable</i> output function, i.e.,
 * they compute an output containing a part that can be attributed to a suffix of
 * the input.
 * <p>
 * Note that this is a special case of the {@link Output} feature, as
 * <code>computeOutput(input) = computeSuffixOutput(epsilon, input)</code>.
 *  
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@ParametersAreNonnullByDefault
public interface SuffixOutput<I, D> extends Output<I,D> {
	@Nullable
	public D computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix);
	
	@Override
	@Nullable
	default public D computeOutput(Iterable<? extends I> input) {
		return computeSuffixOutput(Word.epsilon(), input);
	}
}
