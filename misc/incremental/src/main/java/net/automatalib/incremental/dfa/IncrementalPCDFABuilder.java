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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.incremental.dfa;

import net.automatalib.incremental.ConflictException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class IncrementalPCDFABuilder<I> extends AbstractIncrementalDFABuilder<I> {
	
	public IncrementalPCDFABuilder(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder#getState(net.automatalib.words.Word)
	 */
	@Override
	protected State getState(Word<I> word) {
		State s = init;
		
		for(I sym : word) {
			int idx = inputAlphabet.getSymbolIndex(sym);
			s = s.getSuccessor(idx);
			if(s == null || s == sink)
				return s;
		}
		return s;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder#lookup(net.automatalib.words.Word)
	 */
	@Override
	public Acceptance lookup(Word<I> word) {
		State s = getState(word);
		if(s == null)
			return Acceptance.DONT_KNOW;
		return (s != sink) ? s.getAcceptance() : Acceptance.FALSE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder#insert(net.automatalib.words.Word, boolean)
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
			if(curr == sink)
				break;
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
		
		if(curr == sink) {
			if(accepting)
				throw new RuntimeException("Conflict");
			return;
		}
		
		if(prefixLen == len) {
			Acceptance currAcc = curr.getAcceptance();
			if(currAcc == acc)
				return;
			else if(conf == null) {
				if(currAcc == Acceptance.DONT_KNOW) {
					if(accepting) {
						State upd = updateSignature(curr, Acceptance.TRUE);
						if(upd == curr)
							return;
						curr = upd;
					}
					else {
						purge(curr);
						curr = sink;
					}
				}
				else
					throw new ConflictException("Incompatible acceptances: " + currAcc + " vs " + acc);
			}
		}
		
		
		Word<I> suffix = word.subWord(prefixLen); 
		
		State last;
		
		State suffixState = null;
		int suffTransIdx = -1;
		if(!suffix.isEmpty()) {
			suffixState = createSuffix(suffix.subWord(1), accepting);
			I sym = suffix.getSymbol(0);
			suffTransIdx = inputAlphabet.getSymbolIndex(sym);
		}
		
		int currentIndex;
		if(conf != null) {
			if(suffTransIdx == -1)
				last = clone(curr, acc);
			else /* if(accepting) */
				last = clone(curr, Acceptance.TRUE, suffTransIdx, suffixState);
			
			for(int i = prefixLen - 1; i >= confIndex; i--) {
				State s = getState(word.prefix(i));
				I sym = word.getSymbol(i);
				int idx = inputAlphabet.getSymbolIndex(sym);
				if(accepting)
					last = clone(s, Acceptance.TRUE, idx, last);
				else
					last = clone(s, idx, last);
			}
			
			currentIndex = confIndex;
		}
		else {
			if(suffTransIdx == -1)
				last = updateSignature(curr, acc);
			else /* if(accepting) */
				last = updateSignature(curr, Acceptance.TRUE, suffTransIdx, suffixState);
			currentIndex = prefixLen;
		}
		
		while(--currentIndex >= 0) {
			State state = getState(word.prefix(currentIndex));
			I sym = word.getSymbol(currentIndex);
			int idx = inputAlphabet.getSymbolIndex(sym);
			if(accepting) {
				Acceptance lastAcc = last.getAcceptance();
				last = updateSignature(state, Acceptance.TRUE, idx, last);
				if(state == last && lastAcc == Acceptance.TRUE)
					break;
			}
			else {
				last = updateSignature(state, idx, last);
				if(state == last)
					break;
			}
		}
	}
		
	/**
	 * Removes a state and all of its successors from the register.
	 * @param state the state to purge
	 */
	private void purge(State state) {
		StateSignature sig = state.getSignature();
		if(register.remove(sig) == null)
			return;
		for(int i = 0; i < alphabetSize; i++) {
			State succ = sig.successors[i];
			if(succ != null)
				purge(succ);
			sig.successors[i] = null;
		}
	}
	
	/**
	 * Creates a suffix state sequence, i.e., a linear sequence of states connected by transitions
	 * labeled by the letters of the given suffix word.
	 * @param suffix the suffix word
	 * @param acc whether or not the final state should be accepting
	 * @return the first state in the sequence
	 */
	private State createSuffix(Word<I> suffix, boolean accepting) {
		State last;
		Acceptance intermediate;
		if(!accepting) {
			if(sink == null)
				sink = new State(null);
			last = sink;
			intermediate = Acceptance.DONT_KNOW;
		}
		else {
			StateSignature sig = new StateSignature(alphabetSize, Acceptance.TRUE);
			last = replaceOrRegister(sig);
			intermediate = Acceptance.TRUE;
		}
		
		int len = suffix.length();
		for(int i = len - 1; i >= 0; i--) {
			StateSignature sig = new StateSignature(alphabetSize, intermediate);
			I sym = suffix.getSymbol(i);
			int idx = inputAlphabet.getSymbolIndex(sym);
			sig.successors[idx] = last;
			last = replaceOrRegister(sig);
		}
		
		return last;
	}

}
