/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.automata.transout.impl;

import net.automatalib.automata.base.fast.FastMutableDet;
import net.automatalib.automata.transout.MutableMooreMachine;
import net.automatalib.words.Alphabet;


/**
 * A fast implementation of a Moore automaton.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class.
 * @param <O> output symbol class.
 */
public final class FastMoore<I, O> extends FastMutableDet<FastMooreState<O>, I, FastMooreState<O>, O, Void>
		implements MutableMooreMachine<FastMooreState<O>, I, FastMooreState<O>, O> {

	public FastMoore(Alphabet<I> alphabet) {
		super(alphabet);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getSuccessor(java.lang.Object)
	 */
	@Override
	public FastMooreState<O> getSuccessor(FastMooreState<O> transition) {
		return transition;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.StateOutput#getStateOutput(java.lang.Object)
	 */
	@Override
	public O getStateOutput(FastMooreState<O> state) {
		return state.getOutput();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#createTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public FastMooreState<O> createTransition(FastMooreState<O> successor,
			Void properties) {
		return successor;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.MutableStateOutput#setStateOutput(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setStateOutput(FastMooreState<O> state, O output) {
		state.setOutput(output);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.base.fast.FastMutableDet#createState(java.lang.Object)
	 */
	@Override
	protected FastMooreState<O> createState(O property) {
		return new FastMooreState<O>(inputAlphabet.size(), property);
	}

}
