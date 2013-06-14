package net.automatalib.words.abstractimpl;

import java.util.AbstractList;

import net.automatalib.words.Alphabet;

public abstract class AbstractAlphabet<I> extends AbstractList<I> implements Alphabet<I> {


	@Override
	public int compare(I o1, I o2) {
		return getSymbolIndex(o1) - getSymbolIndex(o2);
	}

	@Override
	public I get(int index) {
		return getSymbol(index);
	}
	
	
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
		for(int i = offset, j = tgtOfs, k = 0; k < num; i++, j++, k++) {
			array[j] = getSymbol(i);
		}
	}
}
