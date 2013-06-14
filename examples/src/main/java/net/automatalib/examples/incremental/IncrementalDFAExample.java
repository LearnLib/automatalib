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
package net.automatalib.examples.incremental;

import java.io.IOException;
import java.io.Writer;

import net.automatalib.commons.dotutil.DOT;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class IncrementalDFAExample {
	private static final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
	
	public static void build(IncrementalDFABuilder<Character> incDfa) throws IOException {
		Word<Character> w1 = Word.fromString("abc");
		Word<Character> w2 = Word.fromString("ac");
		Word<Character> w3 = Word.fromString("acb");
		Word<Character> w4 = Word.epsilon();
		
		System.out.println("  Inserting " + w1 + " as accepted");
		incDfa.insert(w1, true);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w2 + " as rejected");
		incDfa.insert(w2, false);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w3 + " as accepted");
		incDfa.insert(w3, true);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w4 + " as accepted");
		incDfa.insert(w4, true);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Incremental construction using a Tree");
		IncrementalDFABuilder<Character> incDfaTree
			= new IncrementalDFATreeBuilder<>(alphabet);
		build(incDfaTree);
		
		System.out.println();
		
		System.out.println("Incremental construction using a DAG");
		IncrementalDFABuilder<Character> incDfaDag
			= new IncrementalDFADAGBuilder<>(alphabet);
		build(incDfaDag);
	}
}
