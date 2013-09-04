package net.automatalib.words.impl;

import java.util.Objects;

import net.automatalib.words.abstractimpl.AbstractAlphabet;

public class ArrayAlphabet<I> extends AbstractAlphabet<I> {

	protected final I[] symbols;
	
	@SafeVarargs
	public ArrayAlphabet(I ...symbols) {
		this.symbols = symbols;
	}

	@Override
	public I getSymbol(int index) throws IllegalArgumentException {
		return symbols[index];
	}

	@Override
	public int getSymbolIndex(I symbol) throws IllegalArgumentException {
		for(int i = 0; i < symbols.length; i++) {
			if(Objects.equals(symbols[i], symbol))
				return i;
		}
		return -1;
	}

	@Override
	public int size() {
		return symbols.length;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.words.abstractimpl.AbstractAlphabet#writeToArray(int, java.lang.Object[], int, int)
	 */
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
		System.arraycopy(symbols, offset, array, tgtOfs, num);
	}
	
	

}
