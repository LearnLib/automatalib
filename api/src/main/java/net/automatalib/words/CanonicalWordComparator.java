package net.automatalib.words;

import java.util.Comparator;

import net.automatalib.commons.util.comparison.CmpUtil;

class CanonicalWordComparator<I> implements Comparator<Word<? extends I>> {
	
	private final Comparator<? super I> symComparator;
	
	public CanonicalWordComparator(Comparator<? super I> symComparator) {
		this.symComparator = symComparator;
	}

	@Override
	public int compare(Word<? extends I> o1, Word<? extends I> o2) {
		int ldiff = o1.length() - o2.length();
		if(ldiff != 0) {
			return ldiff;
		}
		return CmpUtil.lexCompare(o1, o2, symComparator);
	}

}
