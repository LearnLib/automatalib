/* Copyright (C) 2013-2018 TU Dortmund
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

import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalPCDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public final class IncrementalPCDFAExample {

    private static final Alphabet<Character> ALPHABET = Alphabets.characters('a', 'c');

    private IncrementalPCDFAExample() {
    }

    public static void main(String[] args) {
        System.out.println("Incremental construction using a tree");
        IncrementalDFABuilder<Character> incPcDfaTree = new IncrementalPCDFATreeBuilder<>(ALPHABET);
        build(incPcDfaTree);

        System.out.println();

        System.out.println("Incremental construction using a DAG");
        IncrementalDFABuilder<Character> incPcDfaDag = new IncrementalPCDFADAGBuilder<>(ALPHABET);
        build(incPcDfaDag);
    }

    public static void build(IncrementalDFABuilder<Character> incPcDfa) {
        Word<Character> w1 = Word.fromString("abc");
        Word<Character> w2 = Word.fromString("acb");
        Word<Character> w3 = Word.fromString("ac");

        System.out.println("  Inserting " + w1 + " as accepted");
        incPcDfa.insert(w1, true);
        Visualization.visualize(incPcDfa.asGraph());

        System.out.println("  Inserting " + w2 + " as rejected");
        incPcDfa.insert(w2, false);
        Visualization.visualize(incPcDfa.asGraph());

        System.out.println("  Inserting " + w3 + " as accepted");
        incPcDfa.insert(w3, true);
        Visualization.visualize(incPcDfa.asGraph());
    }
}
