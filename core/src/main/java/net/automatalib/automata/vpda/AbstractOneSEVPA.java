/* Copyright (C) 2017 TU Dortmund
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
package net.automatalib.automata.vpda;

import net.automatalib.words.VPDAlphabet;

/**
 * Abstract class for 1-SEVPAs that implements functionality shared across different subtypes.
 *
 * @param <L> location type
 * @param <I> input alphabet type
 *
 * @author Malte Isberner
 */
public abstract class AbstractOneSEVPA<L, I> implements OneSEVPA<L, I> {

	protected final VPDAlphabet<I> alphabet;

	public AbstractOneSEVPA(final VPDAlphabet<I> alphabet) {
		this.alphabet = alphabet;
	}

	public VPDAlphabet<I> getAlphabet() {
		return alphabet;
	}

	@Override
	public int encodeStackSym(final L srcLoc, final I callSym) {
		return encodeStackSym(srcLoc, alphabet.getCallSymbolIndex(callSym));
	}

	public int encodeStackSym(final L srcLoc, final int callSymIdx) {
		return alphabet.getNumCalls() * getLocationId(srcLoc) + callSymIdx;
	}

	@Override
	public State<L> getTransition(final State<L> state, final I input) {
		if (state.isSink()) {
			return State.getSink();
		}

		switch (alphabet.getSymbolType(input)) {
			case CALL:
				final int newStackElem = encodeStackSym(state.getLocation(), input);
				return new State<>(getInitialLocation(), StackContents.push(newStackElem, state.getStackContents()));
			case RETURN: {
				if (state.getStackContents() == null) {
					return State.getSink();
				}
				final int stackElem = state.getStackContents().peek();
				final L succ = getReturnSuccessor(state.getLocation(), input, stackElem);
				if (succ == null) {
					return State.getSink();
				}
				return new State<>(succ, state.getStackContents().pop());
			}
			case INTERNAL: {
				final L succ = getInternalSuccessor(state.getLocation(), input);
				if (succ == null) {
					return State.getSink();
				}
				return new State<>(succ, state.getStackContents());
			}
		}

		throw new AssertionError();
	}

	public L getStackLoc(final int stackSym) {
		return getLocation(stackSym / alphabet.getNumCalls());
	}

	public I getCallSym(final int stackSym) {
		return alphabet.getCallSymbol(stackSym % alphabet.getNumCalls());
	}

	@Override
	public int getNumStackSymbols() {
		return size() * alphabet.getNumCalls();
	}

}
