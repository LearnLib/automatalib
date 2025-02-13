/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.automaton.util;

import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.FastAlphabet;
import net.automatalib.alphabet.impl.Symbol;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.FiniteStateAcceptor;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.FastNFA;
import net.automatalib.automaton.fsa.impl.FastNFAState;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.MutableMooreMachine;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.ts.acceptor.AcceptorTS;
import net.automatalib.word.Word;
import org.testng.Assert;

public final class TestUtil {

    public static final Symbol<Character> IN_A = new Symbol<>('a');
    public static final Symbol<Character> IN_B = new Symbol<>('b');
    public static final Alphabet<Symbol<Character>> ALPHABET = new FastAlphabet<>();

    public static final String OUT_OK = "ok";
    public static final String OUT_ERROR = "error";

    static {
        ALPHABET.add(IN_A);
        ALPHABET.add(IN_B);
    }

    private TestUtil() {
        // prevent instantiation
    }

    public static FastMealy<Symbol<Character>, String> constructMealy() {
        return constructMealy(FastMealy::new);
    }

    public static <S, T, M extends MutableMealyMachine<S, Symbol<Character>, T, String>> M constructMealy(Function<Alphabet<Symbol<Character>>, M> constructor) {
        M fm = constructor.apply(ALPHABET);

        S s0 = fm.addInitialState(), s1 = fm.addState(), s2 = fm.addState();

        fm.addTransition(s0, IN_A, s1, OUT_OK);
        fm.addTransition(s0, IN_B, s0, OUT_ERROR);

        fm.addTransition(s1, IN_A, s2, OUT_OK);
        fm.addTransition(s1, IN_B, s0, OUT_OK);

        fm.addTransition(s2, IN_A, s2, OUT_ERROR);
        fm.addTransition(s2, IN_B, s1, OUT_OK);

        return fm;
    }

    public static <S, T, M extends MutableMooreMachine<S, Symbol<Character>, T, String>> M constructMoore(Function<Alphabet<Symbol<Character>>, M> constructor) {
        M fm = constructor.apply(ALPHABET);

        S s0 = fm.addInitialState(OUT_OK), s1 = fm.addState(OUT_ERROR), s2 = fm.addState(OUT_OK);

        fm.addTransition(s0, IN_A, s2, null);
        fm.addTransition(s0, IN_B, s1, null);

        fm.addTransition(s1, IN_A, s1, null);
        fm.addTransition(s1, IN_B, s0, null);

        fm.addTransition(s2, IN_A, s0, null);
        fm.addTransition(s2, IN_B, s1, null);

        return fm;
    }

    public static FastNFA<Symbol<Character>> constructNFA() {
        FastNFA<Symbol<Character>> fnfa = new FastNFA<>(ALPHABET);

        FastNFAState s0 = fnfa.addInitialState(), s1 = fnfa.addState(true), s2 = fnfa.addState();

        fnfa.addTransition(s0, IN_A, s1);
        fnfa.addTransition(s0, IN_B, s0);

        fnfa.addTransition(s1, IN_A, s2);
        fnfa.addTransition(s1, IN_B, s0);

        fnfa.addTransition(s2, IN_A, s2);
        fnfa.addTransition(s2, IN_B, s1);

        return fnfa;
    }

    public static <S, I> void checkOutput(DFA<S, I> dfa, Word<I> word, Boolean expected) {
        checkOutput((FiniteStateAcceptor<?, I>) dfa, word, expected);
        Assert.assertEquals(dfa.computeStateOutput(dfa.getInitialState(), word), expected);
    }

    public static <I> void checkOutput(NFA<?, I> nfa, Word<I> word, Boolean expected) {
        checkOutput((FiniteStateAcceptor<?, I>) nfa, word, expected);
        checkOutput(nfa.powersetView(), word, expected);
    }

    public static <I, A extends SuffixOutput<I, Boolean> & AcceptorTS<?, I>> void checkOutput(A dfa, Word<I> word, Boolean expected) {
        Assert.assertEquals(dfa.accepts(word), expected);
        Assert.assertEquals(dfa.computeOutput(word), expected);
        Assert.assertEquals(dfa.computeSuffixOutput(Word.epsilon(), word), expected);
        Assert.assertEquals(dfa.computeSuffixOutput(word, Word.epsilon()), expected);

        final int n = word.length();
        final int mid = n / 2;
        Assert.assertEquals(dfa.computeSuffixOutput(word.prefix(mid), word.suffix(n-mid)), expected);
    }
}
