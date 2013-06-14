/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.words;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Function;

import net.automatalib.AutomataLibSettings;
import net.automatalib.commons.util.array.AWUtil;
import net.automatalib.commons.util.array.ArrayWritable;
import net.automatalib.commons.util.strings.AbstractPrintable;

/**
 * A word is an ordered sequence of symbols. {@link Word}s are generally immutable,
 * i.e., a single {@link Word} object will never change (unless symbol objects are modified,
 * which is however highly discouraged).
 * <p>
 * This class provides the following static methods for creating words in the most common scenarios:
 * <ul>
 * <li> {@link #epsilon()} returns the empty word of length 0
 * <li> {@link #fromLetter(Object)} turns a single letter into a word of length 1
 * <li> {@link #fromSymbols(Object...)} creates a word from an array of symbols
 * <li> {@link #fromArray(Object[], int, int)} creates a word from a subrange of a symbols array
 * <li> {@link #fromList(List)} creates a word from a {@link List} of symbols
 * </ul>
 * <p>
 * Modification operations like {@link #append(Object)} or {@link #concat(Word...)} create
 * new objects, subsequently invoking these operations on the respective objects returned is
 * therefore highly inefficient. If words need to be dynamically created, a {@link WordBuilder}
 * should be used.
 * <p>
 * This is an abstract base class for word representations. Implementing classes only need to implement
 * <ul>
 * <li> {@link #getSymbol(int)}
 * <li> {@link #length()}
 * </ul>
 * <p>
 * However, for the sake of efficiency it is highly encouraged to overwrite the other methods
 * as well, providing specialized realizations.
 *
 * @param <I> symbol class
 * 
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public abstract class Word<I> extends AbstractPrintable implements ArrayWritable<I>, Iterable<I> {


	private static String emptyWordRep = "ε";
	private static String wordDelimLeft = "";
	private static String wordDelimRight = "";
	private static String wordSymbolSeparator = " ";
	private static String wordSymbolDelimLeft = "";
	private static String wordSymbolDelimRight = "";

	static {
		AutomataLibSettings settings = AutomataLibSettings.getInstance();
		emptyWordRep = settings.getProperty("word.empty", "ε");
		wordDelimLeft = settings.getProperty("word.delim.left", "");
		wordDelimRight = settings.getProperty("word.delim.right", "");
		wordSymbolSeparator = settings.getProperty("word.symbol.separator", " ");
		wordSymbolDelimLeft = settings.getProperty("word.symbol.delim.left", "");
		wordSymbolDelimRight = settings.getProperty("word.symbol.delim.right", "");
	}

	
	public static <I> Comparator<Word<? extends I>> canonicalComparator(Comparator<? super I> symComparator) {
		return new CanonicalWordComparator<>(symComparator);
	}

	
	/**
	 * Retrieves the empty word.
	 * @return the empty word.
	 * @see Collections#emptyList()
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public static <I> Word<I> epsilon() {
		return (Word<I>)(Word<?>)EmptyWord.INSTANCE;
	}
	
	/**
	 * Constructs a word from a single letter.
	 * @param letter the letter
	 * @return a word consisting of only this letter
	 */
	@Nonnull
	public static <I> Word<I> fromLetter(@Nullable I letter) {
		return new LetterWord<>(letter);
	}
	
	/**
	 * Creates a word from an array of symbols.
	 * @param symbols the symbol array
	 * @return a word containing the symbols in the specified array
	 */
	@SafeVarargs
	@Nonnull
	public static <I> Word<I> fromSymbols(I ...symbols) {
		if(symbols.length == 0)
			return epsilon();
		if(symbols.length == 1)
			return fromLetter(symbols[0]);
		Object[] array = new Object[symbols.length];
		System.arraycopy(symbols, 0, array, 0, symbols.length);
		return new SharedWord<>(symbols);
	}
	
	/**
	 * Creates a word from a subrange of an array of symbols. Note that to
	 * ensure immutability, internally a copy of the array is made.
	 * @param symbols the symbols array
	 * @param offset the starting index in the array
	 * @param length the length of the resulting word (from the starting index on)
	 * @return the word consisting of the symbols in the range
	 */
	@Nonnull
	public static <I> Word<I> fromArray(I[] symbols, int offset, int length) {
		if(length == 0)
			return epsilon();
		if(length == 1)
			return fromLetter(symbols[offset]);
		Object[] array = new Object[length];
		System.arraycopy(symbols, offset, array, 0, length);
		return new SharedWord<>(symbols);
	}
	
	/**
	 * Creates a word from a list of symbols
	 * @param symbolList the list of symbols
	 * @return the resulting word
	 */
	@Nonnull
	public static <I> Word<I> fromList(List<? extends I> symbolList) {
		int siz = symbolList.size();
		if(siz == 0)
			return epsilon();
		if(siz == 1)
			return Word.<I>fromLetter(symbolList.get(0));
		return new SharedWord<>(symbolList);
	}
	
	@Nonnull
	public static Word<Character> fromString(String str) {
		int len = str.length();
		Character[] chars = new Character[str.length()];
		for(int i = 0; i < len; i++)
			chars[i] = str.charAt(i);
		return new SharedWord<>(chars);
	}
	
	@SafeVarargs
	public static <I> Word<I> fromWords(Word<? extends I>... words) {
		int totalLength = 0;
		for(Word<?> w : words) {
			totalLength += w.length();
		}
		
		if(totalLength == 0) {
			return epsilon();
		}
		
		Object[] array = new Object[totalLength];
		
		int currOfs = 0;
		for(Word<? extends I> w : words) {
			AWUtil.safeWrite(w, array, currOfs);
			currOfs += w.length();
		}
		
		return new SharedWord<>(array);
	}
	
	/*
	 * General word iterator
	 */
	private class Iterator implements java.util.Iterator<I> {

		private int index = 0;
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return (index < Word.this.length());
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		@Nullable
		public I next() {
			if(index >= Word.this.length()) {
				throw new NoSuchElementException();
			}
			return Word.this.getSymbol(index++);
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/*
	 * Representing a word as a list.
	 */
	private class AsList extends AbstractList<I> {
		
		/*
		 * (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		@Nullable
		public I get(int index) {
			return Word.this.getSymbol(index);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return Word.this.length();
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.AbstractList#iterator()
		 */
		@Override
		@Nonnull
		public java.util.Iterator<I> iterator() {
			return Word.this.iterator();
		}
	}

    /**
     * Return symbol that is at the specified position
     * @param index the position
     * @return symbol at position i, <tt>null</tt> if no such symbol exists
     */
	@Nullable
    public abstract I getSymbol(int index);
    
    /**
     * Retrieves the length of this word.
     * @return the length of this word.
     */
    public abstract int length();
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
    	int hash = 5;
    	for(I sym : this) {
    		hash *= 89;
    		hash += (sym != null) ? sym.hashCode() : 0;
    	}
        return hash;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
    	if(this == other)
    		return true;
    	if(other == null)
    		return false;
    	if(!(other instanceof Word))
    		return false;
    	Word<?> otherWord = (Word<?>)other;
    	int len = otherWord.length();
    	if(len != length())
    		return false;
    	java.util.Iterator<I> thisIt = iterator();
    	java.util.Iterator<?> otherIt = otherWord.iterator();
    	while(thisIt.hasNext()) {
    		I thisSym = thisIt.next();
    		Object otherSym = otherIt.next();
    		if(!Objects.equals(thisSym, otherSym))
    			return false;
    	}
    	return true;
    }
    
    /*
     * (non-Javadoc)
     * @see net.automatalib.commons.util.strings.Printable#print(java.lang.Appendable)
     */
    @Override
	public void print(Appendable a) throws IOException {
    	if(isEmpty()) {
		    a.append(emptyWordRep);
    	}
    	else {
		    a.append(wordDelimLeft);
		    java.util.Iterator<? extends I> symIt = iterator();
		    assert symIt.hasNext();
		    appendSymbol(a, symIt.next());
		    while(symIt.hasNext()) {
			    a.append(wordSymbolSeparator);
			    appendSymbol(a, symIt.next());
		    }
		    a.append(wordDelimRight);
    	}
	}

	private static void appendSymbol(Appendable a, Object symbol) throws IOException {
		a.append(wordSymbolDelimLeft);
		a.append(String.valueOf(symbol));
		a.append(wordSymbolDelimRight);
	}

	/*
     * (non-Javadoc)
     * @see net.automatalib.commons.util.array.ArrayWritable#size()
     */
    @Override
    public final int size() {
    	return length();
    }
    
    
    
    /**
     * Retrieves a word representing the specified subrange of this word.
     * As words are immutable, this function usually can be realized quite efficient
     * (implementing classes should take care of this).
     * 
     * @param fromIndex the first index, inclusive.
     * @param toIndex the last index, exclusive.
     * @return the word representing the specified subrange.
     */
    @Nonnull
    public final Word<I> subWord(int fromIndex, int toIndex) {
    	if(fromIndex < 0 || toIndex < fromIndex || toIndex > length())
    		throw new IndexOutOfBoundsException("Invalid subword range [" + fromIndex + ", " + toIndex + ")");
    	
    	return _subWord(fromIndex, toIndex);
    }
   
    /**
     * Retrieves the subword of this word starting at the given index and extending
     * until the end of this word. Calling this method is equivalent to calling
     * <pre>w.subWord(fromIndex, w.length())</pre>
     * @param fromIndex the first index, inclusive
     * @return the word representing the specified subrange
     */
    @Nonnull
    public final Word<I> subWord(int fromIndex) {
    	if(fromIndex <= 0) {
    		if(fromIndex == 0)
    			return this;
    		throw new IndexOutOfBoundsException("Invalid subword range [" + fromIndex + ",)");
    	}
    	return _subWord(fromIndex, length());
    }
    
    /**
     * Internal subword operation implementation. In contrast to {@link #subWord(int, int)},
     * no range checks need to be performed. As this method is flagged as <tt>protected</tt>,
     * implementations may rely on the specified indices being valid.
     * @param fromIndex the first index, inclusive (guaranteed to be valid)
     * @param toIndex the last index, exclusive (guaranteed to be valid)
     * @return the word representing the specified subrange
     */
    @Nonnull
    protected Word<I> _subWord(int fromIndex, int toIndex) {
    	int len = toIndex - fromIndex;
    	Object[] array = new Object[len];
    	writeToArray(fromIndex, array, 0, len);
    	return new SharedWord<>(array);
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    @Nonnull
	public java.util.Iterator<I> iterator() {
		return new Iterator();
	}

    /*
     * (non-Javadoc)
     * @see net.automatalib.commons.util.array.ArrayWritable#writeToArray(int, java.lang.Object[], int, int)
     */
    @Override
	public void writeToArray(int offset, Object[] array, int tgtOffset, int length) {
    	int idx = offset, arrayIdx = tgtOffset;
    	
    	while(length-- > 0)
    		array[arrayIdx++] = getSymbol(idx++);
    }
    
    
    /**
     * Retrieves a {@link List} view on the contents of this word.
     * @return an unmodifiable list of the contained symbols.
     */
    @Nonnull
    public List<I> asList() {
    	return new AsList();
    }
    
    /**
	 * Retrieves a prefix of the given length. If <code>length</code>
	 * is negative, then a prefix consisting of all but the last
	 * <code>-length</code> symbols is returned.
	 * 
	 * @param prefixLen the length of the prefix (may be negative, see above).
	 * @return the prefix of the given length.
	 */
    @Nonnull
	public final Word<I> prefix(int prefixLen) {
		if(prefixLen < 0)
			prefixLen = length() + prefixLen;
		
		return subWord(0, prefixLen);
	}
	
	/**
	 * Retrieves a suffix of the given length. If <code>length</code> is
	 * negative, then a suffix consisting of all but the first
	 * <code>-length</code> symbols is returned.
	 * 
	 * @param suffixLen the length of the suffix (may be negative, see above).
	 * @return the suffix of the given length.
	 */
    @Nonnull
	public final Word<I> suffix(int suffixLen) {
		int wordLen = length();
		int startIdx = (suffixLen < 0) ? -suffixLen : (wordLen - suffixLen);
		
		return subWord(startIdx, wordLen);
	}
	
	/**
	 * Retrieves the list of all prefixes of this word. In the default implementation,
	 * the prefixes are lazily instantiated upon the respective calls of {@link List#get(int)}
	 * or {@link Iterator#next()}.
	 * @param longestFirst whether to start with the longest prefix (otherwise, the first prefix
	 * in the list will be the shortest).
	 * @return a (non-materialized) list containing all prefixes 
	 */
    @Nonnull
	public List<? extends Word<I>> prefixes(boolean longestFirst) {
		return new SubwordList<>(this, true, longestFirst);
	}
	
	/**
	 * Retrieves the list of all suffixes of this word. In the default implementation,
	 * the suffixes are lazily instantiated upon the respective calls of {@link List#get(int)}
	 * or {@link Iterator#next()}.
	 * @param longestFirst whether to start with the longest suffix (otherwise, the first suffix
	 * in the list will be the shortest).
	 * @return a (non-materialized) list containing all suffix 
	 */
    @Nonnull
	public List<? extends Word<I>> suffixes(boolean longestFirst) {
		return new SubwordList<>(this, false, longestFirst);
	}
	
	
	/**
	 * Retrieves the next word after this in canonical order. Figuratively
	 * speaking, if there are <tt>k</tt> alphabet symbols, one can think of
	 * a word of length <tt>n</tt> as an <tt>n</tt>-digit radix-<tt>k</tt>
	 * representation of the number. The next word in canonical order
	 * is the representation for the number represented by this word plus one.
	 * @param sigma the alphabet
	 * @return the next word in canonical order
	 */
    @Nonnull
	public Word<I> canonicalNext(Alphabet<I> sigma) {
		int len = length();
		Object[] symbols = new Object[len];
		writeToArray(0, symbols, 0, len);
		
		int alphabetSize = sigma.size();
		
		int i = 0;
		boolean overflow = true;
		for(I sym : this) {
			int nextIdx = (sigma.getSymbolIndex(sym) + 1) % alphabetSize; 
			symbols[i++] = sigma.getSymbol(nextIdx);
			if(nextIdx != 0) {
				overflow = false;
				break;
			}
		}
		
		while(i < len) {
			symbols[i] = getSymbol(i);
			i++;
		}
		
		if(overflow) {
			Object[] newSymbols = new Object[len+1];
			newSymbols[0] = sigma.getSymbol(0);
			System.arraycopy(symbols, 0, newSymbols, 1, len);
			symbols = newSymbols;
		}
		
		return new SharedWord<>(symbols);
	}
	
	/**
	 * Retrieves the last symbol of this word.
	 * @return the last symbol of this word.
	 */
    @Nullable
	public I lastSymbol() {
		return getSymbol(length() - 1);
	}
	
	/**
	 * Retrieves the first symbol of this word.
	 * @return the first symbol of this word
	 */
    @Nullable
	public I firstSymbol() {
		return getSymbol(0);
	}
	
	/**
	 * Appends a symbol to this word and returns the result as a new word.
	 * @param symbol the symbol to append
	 * @return the word plus the given symbol
	 */
    @Nonnull
	public Word<I> append(@Nullable I symbol) {
		int len = length();
		Object[] array = new Object[len + 1];
		writeToArray(0, array, 0, len);
		array[len] = symbol;
		return new SharedWord<>(array);
	}
	
	/**
	 * Prepends a symbol to this word and returns the result as a new word.
	 * @param symbol the symbol to prepend
	 * @return the given symbol plus to word.
	 */
    @Nonnull
	public Word<I> prepend(@Nullable I symbol) {
		int len = length();
		Object[] array = new Object[len+1];
		array[0] = symbol;
		writeToArray(0, array, 1, len);
		
		return new SharedWord<>(array);
	}
	
	/**
	 * Concatenates this word with several other words and returns the result
	 * as a new word.
	 * 
	 * Note that this method cannot be overridden. Implementing classes need to
	 * override the {@link #_concat(Word...)} method instead.
	 *  
	 * @param words the words to concatenate with this word
	 * @return the result of the concatenation
	 * @see #_concat(Word...)
	 */
	@SafeVarargs
	@Nonnull
	public final Word<I> concat(Word<? extends I> ...words) {
		return _concat(words);
	}
	
	/**
	 * Realizes the concatenation of this word with several other words.
	 * @param words the words to concatenate
	 * @return the results of the concatenation
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	protected Word<I> _concat(Word<? extends I> ...words) {
		if(words.length == 0)
			return this;
		
		int len = length();
		
		int totalSize = len;
		for(int i = 0; i < words.length; i++)
			totalSize += words[i].length();
		
		Object[] array = new Object[totalSize];
		writeToArray(0, array, 0, len);
		int currOfs = len;
		for(int i = 0; i < words.length; i++) {
			Word<? extends I> w = words[i];
			int wLen = w.length();
			w.writeToArray(0, array, currOfs, wLen);
			currOfs += wLen;
		}

		return new SharedWord<>(array);
	}
	
	/**
	 * Checks if this word is a prefix of another word.
	 * @param other the other word
	 * @return <tt>true</tt> if this word is a prefix of the other word, <tt>false</tt>
	 * otherwise.
	 */
	public boolean isPrefixOf(Word<?> other) {
		int len = length(), otherLen = other.length();
		if(otherLen < len)
			return false;
		
		for(int i = 0; i < len; i++) {
			I sym1 = getSymbol(i);
			Object sym2 = other.getSymbol(i);
			
			if(!Objects.equals(sym1,  sym2))
				return false;
		}
		return true;
	}
	
	/**
	 * Determines the longest common prefix of this word and another
	 * word.
	 * @param other the other word
	 * @return the longest common prefix of this word and the other word
	 */
	@Nonnull
	public Word<I> longestCommonPrefix(Word<?> other) {
		int len = length(), otherLen = other.length();
		int maxIdx = (len < otherLen) ? len : otherLen;
		
		int i = 0;
		while(i < maxIdx) {
			I sym1 = getSymbol(i);
			Object sym2 = other.getSymbol(i);
			
			if(!Objects.equals(sym1, sym2))
				break;
			i++;
		}
		
		return prefix(i);
	}
	
	/**
	 * Checks if this word is a suffix of another word
	 * @param other the other word
	 * @return <tt>true</tt> if this word is a suffix of the other word, <tt>false</tt>
	 * otherwise. 
	 */
	public boolean isSuffixOf(Word<?> other) {
		int len = length(), otherLen = other.length();
		if(otherLen < len)
			return false;
		
		int ofs = otherLen - len;
		for(int i = 0; i < len; i++) {
			I sym1 = getSymbol(i);
			Object sym2 = other.getSymbol(ofs + i);
			if(!Objects.equals(sym1, sym2))
				return false;
		}
		return true;
	}
	
	/**
	 * Determines the longest common suffix of this word and another word.
	 * @param other the other word
	 * @return the longest common suffix
	 */
	@Nonnull
	public Word<I> longestCommonSuffix(Word<?> other) {
		int len = length(), otherLen = other.length();
		int minLen = (len < otherLen) ? len : otherLen;
		
		int idx1 = len, idx2 = otherLen;
		int i = 0;
		while(i < minLen) {
			I sym1 = getSymbol(--idx1);
			Object sym2 = other.getSymbol(--idx2);
			
			if(!Objects.equals(sym1, sym2))
				break;
			
			i++;
		}
		
		return suffix(i);
	}
	
	/**
	 * Retrieves a "flattened" version of this word, i.e., without any hierarchical structure
	 * attached.
	 * This can be helpful if {@link Word} is subclassed to allow representing, e.g., a concatenation
	 * dynamically, but due to performance concerns not too many levels of indirection
	 * should be introduced.
	 * @return a flattened version of this word.
	 */
	@Nonnull
	public Word<I> flatten() {
		int len = length();
		Object[] array = new Object[len];
		writeToArray(0, array, 0, len);
		return new SharedWord<>(array);
	}
	
	@Nonnull
	public Word<I> trimmed() {
		int len = length();
		Object[] array = new Object[len];
		writeToArray(0, array, 0, len);
		return new SharedWord<>(array);
	}

	/**
	 * Checks if this word is empty, i.e., contains no symbols.
	 * @return <tt>true</tt> if this word is empty, <tt>false</tt> otherwise.
	 */
	public boolean isEmpty() {
		return (length() == 0);
	}

	@Nonnull
	public <T>
	Word<T> transform(Function<? super I,? extends T> transformer) {
		int len = length();
		Object[] array = new Object[len];
		int i = 0;
		for(I symbol : this) {
			array[i++] = transformer.apply(symbol);
		}
		return new SharedWord<>(array);
	}
	
	@SuppressWarnings("unchecked")
	public static <I> Word<I> upcast(Word<? extends I> word) {
		return (Word<I>)word;
	}
}
