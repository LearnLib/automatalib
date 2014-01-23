/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.incremental.mealy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public abstract class AbstractIncrementalMealyBuilder<I, O> implements
		IncrementalMealyBuilder<I, O> {
	
	protected Alphabet<I> inputAlphabet;
	
	public AbstractIncrementalMealyBuilder(Alphabet<I> alphabet) {
		this.inputAlphabet = alphabet;
	}
	
	@Override
	public Alphabet<I> getInputAlphabet() {
		return inputAlphabet;
	}

	@Override
	public boolean hasDefinitiveInformation(Word<I> word) {
		List<O> unused = new ArrayList<>(word.length());
		return lookup(word, unused);
	}

	@Override
	public Word<O> lookup(Word<I> inputWord) {
		WordBuilder<O> wb = new WordBuilder<>(inputWord.size());
		lookup(inputWord, wb);
		return wb.toWord();
	}

}
