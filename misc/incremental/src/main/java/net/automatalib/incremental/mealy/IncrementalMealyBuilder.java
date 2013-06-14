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
package net.automatalib.incremental.mealy;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.ts.transout.MealyTransitionSystem;
import net.automatalib.words.Word;

public interface IncrementalMealyBuilder<I, O> extends IncrementalConstruction<MealyMachine<?,I,?,O>, I> {

	public static interface GraphView<I, O, N, E> extends DOTPlottableGraph<N, E> {
		@Nullable
		public I getInputSymbol(@Nonnull E edge);
		@Nullable
		public O getOutputSymbol(@Nonnull E edge);
		@Nonnull
		public N getInitialNode();
	}
	
	public static interface TransitionSystemView<I, O, S, T> extends MealyTransitionSystem<S, I, T, O> {
	}
	
	public Word<O> lookup(Word<? extends I> inputWord);
	public boolean lookup(Word<? extends I> inputWord, List<? super O> output);
	public void insert(Word<? extends I> inputWord, Word<? extends O> outputWord) throws ConflictException;
	
	@Override
	public TransitionSystemView<I,O,?,?> asTransitionSystem();
	
	@Override
	public GraphView<I,O,?,?> asGraph();
}
