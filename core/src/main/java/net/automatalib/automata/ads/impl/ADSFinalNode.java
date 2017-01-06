package net.automatalib.automata.ads.impl;

import net.automatalib.automata.ads.ADSNode;

import java.util.Collections;
import java.util.Map;

public class ADSFinalNode<S, I, O> implements ADSNode<S, I, O> {

	private ADSNode<S, I, O> parent;
	private S hypothesisState;

	public ADSFinalNode() {}

	public ADSFinalNode(ADSNode<S, I, O> parent, S hypothesisState) {
		this.parent = parent;
		this.hypothesisState = hypothesisState;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public S getHypothesisState() {
		return this.hypothesisState;
	}

	@Override
	public void setHypothesisState(S state) {
		this.hypothesisState = state;
	}

	@Override
	public I getSymbol() {
		return null;
	}

	@Override
	public void setSymbol(I symbol) {
		throw new UnsupportedOperationException("Cannot set symbol state on a leaf node");}

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
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		return this.getHypothesisState() == null ? "<null>" : this.getHypothesisState().toString();
	}
}
