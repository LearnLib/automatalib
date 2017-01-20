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
package net.automatalib.automata.ads.impl;

import net.automatalib.automata.ads.ADSNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a symbol node
 *
 * @param <S> (hypothesis) state type
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 * @author frohme
 */
public class ADSSymbolNode<S, I, O> implements ADSNode<S, I, O> {

	private ADSNode<S, I, O> parent;

	private I symbol;

	private Map<O, ADSNode<S, I, O>> successors;

	public ADSSymbolNode(ADSNode<S, I, O> parent, I symbol) {
		this.successors = new HashMap<>();
		this.parent = parent;
		this.symbol = symbol;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public S getHypothesisState() {
		return null;
	}

	@Override
	public void setHypothesisState(S state) {
		throw new UnsupportedOperationException("Cannot set hypothesis state on a symbol node");
	}

	@Override
	public I getSymbol() {
		return this.symbol;
	}

	@Override
	public void setSymbol(I symbol) {
		this.symbol = symbol;
	}

	@Override
	public ADSNode<S, I, O> getParent() {
		return this.parent;
	}

	@Override
	public void setParent(ADSNode<S, I, O> parent) {
		this.parent = parent;
	}

	@Override
	public Map<O, ADSNode<S, I, O>> getChildren() {
		return this.successors;
	}

	@Override
	public String toString() {
		return this.symbol.toString();
	}

}
