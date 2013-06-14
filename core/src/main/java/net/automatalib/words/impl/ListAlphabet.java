package net.automatalib.words.impl;

import java.util.List;

import net.automatalib.words.abstractimpl.AbstractAlphabet;

public class ListAlphabet<I> extends AbstractAlphabet<I> {
	
	private final List<? extends I> list;

	public ListAlphabet(List<? extends I> list) {
		this.list = list;
	}

	@Override
	public I getSymbol(int index) throws IllegalArgumentException {
		return list.get(index);
	}

	@Override
	public int getSymbolIndex(I symbol) throws IllegalArgumentException {
		int idx = list.indexOf(symbol);
		if(idx == -1)
			throw new IllegalArgumentException("Symbol " + symbol + " is not contained in the alphabet");
		return idx;
	}

	@Override
	public I get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

}
