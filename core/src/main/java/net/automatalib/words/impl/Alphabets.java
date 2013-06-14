package net.automatalib.words.impl;

import java.util.List;

import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;

public abstract class Alphabets {

	public static <T> Alphabet<T> fromList(List<? extends T> list) {
		return new ListAlphabet<>(list);
	}
	
	public static Alphabet<Integer> integers(int startInclusive, int endInclusive) {
		List<Integer> lst = CollectionsUtil.intRange(startInclusive, endInclusive + 1);
		return fromList(lst);
	}
	
	public static Alphabet<Character> characters(char startInclusive, char endInclusive) {
		List<Character> lst = CollectionsUtil.charRange(startInclusive, (char)(endInclusive + 1));
		return fromList(lst);
	}
	
	
	
	private Alphabets() {
		// prevent inheritance
	}

}
