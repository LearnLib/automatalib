/* Copyright (C) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.examples.incremental;

import java.io.IOException;
import java.io.Writer;

import net.automatalib.commons.dotutil.DOT;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;


public class IncrementalMealyExample {
	
	private static final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
	private static final Word<Character> w1 = Word.fromString("abc");
	private static final Word<Character> w1o = Word.fromString("xyz");
	private static final Word<Character> w2 = Word.fromString("ac");
	private static final Word<Character> w2o = Word.fromString("xw");
	private static final Word<Character> w3 = Word.fromString("acb");
	private static final Word<Character> w3o = Word.fromString("xwu");
	
	public static void build(IncrementalMealyBuilder<Character,Character> incMealy) throws IOException {
		System.out.println("  Inserting " + w1 + " / " + w1o);
		incMealy.insert(w1, w1o);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incMealy.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w2 + " / " + w2o);
		incMealy.insert(w2, w2o);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incMealy.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w3 + " / " + w3o);
		incMealy.insert(w3, w3o);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incMealy.asGraph(), w);
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Incremental construction using a tree");
		IncrementalMealyBuilder<Character, Character>
			incMealyTree = new IncrementalMealyTreeBuilder<>(alphabet);
		build(incMealyTree);
		
		System.out.println();
		
		System.out.println("Incremental construction using a DAG");
		IncrementalMealyBuilder<Character, Character>
			incMealyDag = new IncrementalMealyDAGBuilder<>(alphabet);
		build(incMealyDag);
	}
}
