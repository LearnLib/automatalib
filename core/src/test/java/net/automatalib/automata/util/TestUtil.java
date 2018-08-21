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
package net.automatalib.automata.util;

import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

public final class TestUtil {

    public static final Symbol IN_A = new Symbol("a");
    public static final Symbol IN_B = new Symbol("b");

    public static final String OUT_OK = "ok";
    public static final String OUT_ERROR = "error";

    private TestUtil() {
        // prevent instantiation
    }

    public static FastMealy<Symbol, String> constructMealy() {
        Alphabet<Symbol> alpha = new FastAlphabet<>();
        alpha.add(IN_A);
        alpha.add(IN_B);

        FastMealy<Symbol, String> fm = new FastMealy<>(alpha);

        FastMealyState<String> s0 = fm.addInitialState(), s1 = fm.addState(), s2 = fm.addState();

        fm.addTransition(s0, IN_A, s1, OUT_OK);
        fm.addTransition(s0, IN_B, s0, OUT_ERROR);

        fm.addTransition(s1, IN_A, s2, OUT_OK);
        fm.addTransition(s1, IN_B, s0, OUT_OK);

        fm.addTransition(s2, IN_A, s2, OUT_ERROR);
        fm.addTransition(s2, IN_B, s1, OUT_OK);

        return fm;
    }

    public static FastNFA<Symbol> constructNFA() {
        Alphabet<Symbol> alpha = new FastAlphabet<>();
        alpha.add(IN_A);
        alpha.add(IN_B);

        FastNFA<Symbol> fnfa = new FastNFA<>(alpha);

        FastNFAState s0 = fnfa.addInitialState(), s1 = fnfa.addState(true), s2 = fnfa.addState();

        fnfa.addTransition(s0, IN_A, s1);
        fnfa.addTransition(s0, IN_B, s0);

        fnfa.addTransition(s1, IN_A, s2);
        fnfa.addTransition(s1, IN_B, s0);

        fnfa.addTransition(s2, IN_A, s2);
        fnfa.addTransition(s2, IN_B, s1);

        return fnfa;
    }
}
