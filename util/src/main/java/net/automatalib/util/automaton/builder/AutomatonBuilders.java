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
package net.automatalib.util.automaton.builder;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.MutableMooreMachine;
import net.automatalib.automaton.transducer.MutableSubsequentialTransducer;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.automaton.transducer.impl.CompactSST;
import net.automatalib.word.Word;

/**
 * Fluent builders for automata.
 */
public final class AutomatonBuilders {

    private AutomatonBuilders() {}

    public static <S, I, T, SP, TP, A extends MutableAutomaton<S, ? super I, T, ? super SP, ? super TP>> AutomatonBuilder<S, I, T, SP, TP, A> forAutomaton(
            A automaton) {
        return new AutomatonBuilder<>(automaton);
    }

    public static <I> DFABuilder<Integer, I, CompactDFA<I>> newDFA(Alphabet<I> alphabet) {
        return forDFA(new CompactDFA<>(alphabet));
    }

    public static <S, I, A extends MutableDFA<S, ? super I>> DFABuilder<S, I, A> forDFA(A dfa) {
        return new DFABuilder<>(dfa);
    }

    public static <I> FSABuilder<Integer, I, CompactNFA<I>> newNFA(Alphabet<I> alphabet) {
        return new FSABuilder<>(new CompactNFA<>(alphabet));
    }

    public static <S, I, A extends MutableNFA<S, ? super I>> FSABuilder<S, I, A> forNFA(A nfa) {
        return new FSABuilder<>(nfa);
    }

    public static <I, O> MealyBuilder<Integer, I, CompactTransition<O>, O, CompactMealy<I, O>> newMealy(Alphabet<I> alphabet) {
        return forMealy(new CompactMealy<>(alphabet));
    }

    public static <S, I, T, O, A extends MutableMealyMachine<S, ? super I, T, ? super O>> MealyBuilder<S, I, T, O, A> forMealy(
            A mealy) {
        return new MealyBuilder<>(mealy);
    }

    public static <I, O> MooreBuilder<Integer, I, Integer, O, CompactMoore<I, O>> newMoore(Alphabet<I> alphabet) {
        return forMoore(new CompactMoore<>(alphabet));
    }

    public static <S, I, T, O, A extends MutableMooreMachine<S, ? super I, T, ? super O>> MooreBuilder<S, I, T, O, A> forMoore(
            A moore) {
        return new MooreBuilder<>(moore);
    }

    public static <I, O> AutomatonBuilder<Integer, I, CompactTransition<Word<O>>, Word<O>, Word<O>, CompactSST<I, O>> newSST(
            Alphabet<I> alphabet) {
        return forSST(new CompactSST<>(alphabet));
    }

    public static <S, I, T, O, A extends MutableSubsequentialTransducer<S, ? super I, T, ? super O>> AutomatonBuilder<S, I, T, Word<O>, Word<O>, A> forSST(
            A sst) {
        return new AutomatonBuilder<>(sst);
    }
}
