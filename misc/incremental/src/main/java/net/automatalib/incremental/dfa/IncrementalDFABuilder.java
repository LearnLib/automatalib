/* Copyright (C) 2013 TU Dortmund
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
package net.automatalib.incremental.dfa;

import net.automatalib.incremental.ConflictException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Incrementally builds an (acyclic) DFA, from a set of positive and negative words.
 * Using {@link #insert(Word, boolean)}, either the set of words definitely in the target language
 * or definitely <i>not</i> in the target language is augmented. The {@link #lookup(Word)} method
 * then returns, for a given word, whether this word is in the set of definitely accepted
 * words ({@link Acceptance#TRUE}), definitely rejected words ({@link Acceptance#FALSE}), or
 * neither ({@link Acceptance#DONT_KNOW}).
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 */
public class IncrementalDFABuilder<I> extends AbstractIncrementalDFABuilder<I> {
	
	/**
	 * Constructor. Initializes the incremental builder.
	 * @param inputAlphabet the input alphabet to use
	 */
	public IncrementalDFABuilder(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
	}
	
	
	/**
	 * Checks the ternary acceptance status for a given word.
	 * @param word the word to check
	 * @return the acceptance status for the given word
	 */
	@Override
	public Acceptance lookup(Word<I> word) {
		State s = getState(word);
		if(s == null)
			return Acceptance.DONT_KNOW;
		return s.getAcceptance();
	}
	
	/**
	 * Inserts a word into either the set of accepted or rejected words.
	 * @param word the word to insert
	 * @param accepting whether to insert this word into the set of accepted or rejected words.
	 */
	@Override
	public void insert(Word<I> word, boolean accepting) {
		int len = word.length();
		Acceptance acc = Acceptance.fromBoolean(accepting);
		
		State curr = init;
		State conf = null;
		
		int confIndex = -1;
		
		int prefixLen = 0;
		for(I sym : word) {
			if(conf == null && curr.isConfluence()) {
				conf = curr;
				confIndex = prefixLen;
			}
			
			int idx = inputAlphabet.getSymbolIndex(sym);
			State succ = curr.getSuccessor(idx);
			if(succ == null)
				break;
			curr = succ;
			prefixLen++;
		}
		
		if(prefixLen == len) {
			Acceptance currAcc = curr.getAcceptance();
			if(currAcc == acc)
				return;
			else if(conf == null) {
				if(currAcc == Acceptance.DONT_KNOW) {
					if(curr == init) {
						updateInitSignature(acc);
						return;
					}
					State upd = updateSignature(curr, acc);
					if(upd == curr)
						return;
					curr = upd;
				}
				else
					throw new ConflictException("Incompatible acceptances: " + currAcc + " vs. " + acc);
			}
		}
		
		
		Word<I> suffix = word.subWord(prefixLen); 
		
		State last;
		
		State suffixState = null;
		State endpoint = null;
		int suffTransIdx = -1;
		if(!suffix.isEmpty()) {
			if(conf != null)
				suffixState = createSuffix(suffix.subWord(1), acc);
			else {
				SuffixInfo suffixRes = createSuffix2(suffix.subWord(1), acc);
				suffixState = suffixRes.last;
				endpoint = suffixRes.end;
			}
			I sym = suffix.getSymbol(0);
			suffTransIdx = inputAlphabet.getSymbolIndex(sym);
		}
		
		int currentIndex;
		if(conf != null) {
			if(suffTransIdx == -1)
				last = clone(curr, acc);
			else
				last = clone(curr, suffTransIdx, suffixState);
			
			for(int i = prefixLen - 1; i >= confIndex; i--) {
				State s = getState(word.prefix(i));
				I sym = word.getSymbol(i);
				int idx = inputAlphabet.getSymbolIndex(sym);
				last = clone(s, idx, last);
			}
			
			currentIndex = confIndex;
		}
		else {
			if(suffTransIdx == -1)
				last = curr;
			else if(endpoint == curr)
				last = clone(curr, suffTransIdx, suffixState);
			else if(curr == init) {
				updateInitSignature(suffTransIdx, suffixState);
				return;
			}
			else
				last = updateSignature(curr, suffTransIdx, suffixState);
			currentIndex = prefixLen;
		}
		
		while(--currentIndex > 0) {
			State state = getState(word.prefix(currentIndex));
			I sym = word.getSymbol(currentIndex);
			int idx = inputAlphabet.getSymbolIndex(sym);
			last = updateSignature(state, idx, last);
			if(state == last)
				return;
		}
		
		I sym = word.getSymbol(0);
		int idx = inputAlphabet.getSymbolIndex(sym);
		updateInitSignature(idx, last);
	}
	
	
	
	/**
	 * Retrieves the state reached by a given word.
	 * @param word the word
	 * @return the state reached by the given word, or <tt>null</tt> if no state is reachable
	 * by that word
	 */
	@Override
	protected State getState(Word<I> word) {
		State s = init;
		
		for(I sym : word) {
			int idx = inputAlphabet.getSymbolIndex(sym);
			s = s.getSuccessor(idx);
			if(s == null)
				return null;
		}
		return s;
	}

	
	
	
	/**
	 * Creates a suffix state sequence, i.e., a linear sequence of states connected by transitions
	 * labeled by the letters of the given suffix word.
	 * @param suffix the suffix word
	 * @param acc the acceptance status of the final state
	 * @return the first state in the sequence
	 */
	private State createSuffix(Word<I> suffix, Acceptance acc) {
		StateSignature sig = new StateSignature(alphabetSize, acc);
		sig.updateHashCode();
		State last = replaceOrRegister(sig);
		
		int len = suffix.length();
		for(int i = len - 1; i >= 0; i--) {
			sig = new StateSignature(alphabetSize, Acceptance.DONT_KNOW);
			I sym = suffix.getSymbol(i);
			int idx = inputAlphabet.getSymbolIndex(sym);
			sig.successors[idx] = last;
			sig.updateHashCode();
			last = replaceOrRegister(sig);
		}
		
		return last;
	}
	
	private static final class SuffixInfo {
		private final State last;
		private final State end;
		
		public SuffixInfo(State last, State end) {
			this.last = last;
			this.end = end;
		}
	}
	
	private SuffixInfo createSuffix2(Word<I> suffix, Acceptance acc) {
		StateSignature sig = new StateSignature(alphabetSize, acc);
		sig.updateHashCode();
		State last = replaceOrRegister(sig);
		State end = last;
		
		int len = suffix.length();
		for(int i = len - 1; i >= 0; i--) {
			sig = new StateSignature(alphabetSize, Acceptance.DONT_KNOW);
			I sym = suffix.getSymbol(i);
			int idx = inputAlphabet.getSymbolIndex(sym);
			sig.successors[idx] = last;
			sig.updateHashCode();
			last = replaceOrRegister(sig);
		}
		
		return new SuffixInfo(last, end);
	}
	

}
