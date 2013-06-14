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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.abstractimpl.AbstractDTS;
import net.automatalib.ts.abstractimpl.AbstractDeterministicTransOutTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public abstract class AbstractIncrementalMealyBuilder<I, O> implements
		IncrementalMealyBuilder<I, O> {
	
	public abstract static class AbstractGraphView<I, O, N, E>
			extends AbstractGraph<N, E> implements GraphView<I, O, N, E> {
		@Override
		public GraphDOTHelper<N, E> getGraphDOTHelper() {
			return new DefaultDOTHelper<N,E>() {
				@Override
				public Collection<? extends N> initialNodes() {
					return Collections.singleton(getInitialNode());
				}
				@Override
				public boolean getEdgeProperties(N src, E edge, N tgt,
						Map<String, String> properties) {
					if(!super.getEdgeProperties(src, edge, tgt, properties)) {
						return false;
					}
					I input = getInputSymbol(edge);
					O output = getOutputSymbol(edge);
					properties.put(EdgeAttrs.LABEL, input + " / " + output);
					return true;
				}
				
			};
		}
	}

	public static abstract class AbstractTransitionSystemView<I,O,S,T> extends
			AbstractDTS<S, I, T> implements TransitionSystemView<I, O, S, T> {
		@Override
		public O getOutput(S state, I input) {
			return AbstractDeterministicTransOutTS.getOutput(this, state, input);
		}
		@Override
		public boolean trace(Iterable<? extends I> input, List<? super O> output) {
			return AbstractDeterministicTransOutTS.trace(this, input, output);
		}
		@Override
		public boolean trace(S state, Iterable<? extends I> input, List<? super O> output) {
			return AbstractDeterministicTransOutTS.trace(this, state, input,
					output);
		}
		@Override
		public Void getStateProperty(S state) {
			return null;
		}
		@Override
		public O getTransitionProperty(T transition) {
			return getTransitionOutput(transition);
		}
	}
	
	protected Alphabet<I> inputAlphabet;
	
	public AbstractIncrementalMealyBuilder(Alphabet<I> alphabet) {
		this.inputAlphabet = alphabet;
	}
	
	@Override
	public Alphabet<I> getInputAlphabet() {
		return inputAlphabet;
	}

	@Override
	public boolean hasDefinitiveInformation(Word<? extends I> word) {
		List<O> unused = new ArrayList<>(word.length());
		return lookup(word, unused);
	}

	@Override
	public Word<O> lookup(Word<? extends I> inputWord) {
		WordBuilder<O> wb = new WordBuilder<>(inputWord.size());
		lookup(inputWord, wb);
		return wb.toWord();
	}

}
