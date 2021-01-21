/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.util.automata.random;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.minimizer.OneSEVPAMinimizer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.index.qual.NonNegative;

public class RandomAutomata {

    private final Random random;

    public RandomAutomata() {
        this(new Random());
    }

    public RandomAutomata(Random random) {
        this.random = random;
    }

    public static RandomAutomata getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Randomly generates an initially connected DFA (ICDFA), i.e., a DFA where all states are reachable from the
     * initial state.
     *
     * @param rand
     *         the randomness source
     * @param numStates
     *         the number of states of the generated automaton
     * @param inputs
     *         the input alphabet
     * @param minimize
     *         determines whether or not the DFA will be minimized before being returned. Note that if {@code true} is
     *         passed for this parameter, the resulting automaton might have a {@link Automaton#size() size} less than
     *         {@code numStates}
     *
     * @return a randomly generated ICDFA
     */
    @SuppressWarnings("nullness") // false positive?
    public static <I> CompactDFA<I> randomICDFA(Random rand,
                                                @NonNegative int numStates,
                                                Alphabet<I> inputs,
                                                boolean minimize) {
        final CompactDFA<I> dfa =
                new RandomICAutomatonGenerator<Boolean, Void>().withStateProperties(Random::nextBoolean)
                                                               .generateICDeterministicAutomaton(numStates,
                                                                                                 inputs,
                                                                                                 new CompactDFA.Creator<>(),
                                                                                                 rand);
        return minimize ? DFAs.minimize(dfa) : dfa;
    }

    public static <I> DefaultOneSEVPA<I> randomOneSEVPA(final Random r,
                                                        final int locCount,
                                                        final VPDAlphabet<I> alphabet,
                                                        final double acceptanceProb,
                                                        final double initialRetTransProb,
                                                        final boolean minimize) {
        final DefaultOneSEVPA<I> result = new DefaultOneSEVPA<>(alphabet, locCount);

        result.addInitialLocation(r.nextDouble() < initialRetTransProb);

        for (int i = 0; i < locCount - 1; i++) {
            if (alphabet.getNumInternals() == 0 || r.nextDouble() < initialRetTransProb) {
                I retSym;
                Location srcLoc;
                int stackSym;

                do {
                    retSym = alphabet.getReturnSymbol(r.nextInt(alphabet.getNumReturns()));
                    srcLoc = result.getLocation(r.nextInt(result.size()));

                    I callSym = alphabet.getCallSymbol(r.nextInt(alphabet.getNumCalls()));
                    final Location stackLoc = result.getLocation(r.nextInt(result.size()));
                    stackSym = result.encodeStackSym(stackLoc, callSym);
                } while (result.getReturnSuccessor(srcLoc, retSym, stackSym) != null);

                final Location newLoc = result.addLocation(r.nextDouble() < acceptanceProb);
                result.setReturnSuccessor(srcLoc, retSym, stackSym, newLoc);
            } else {
                I intSym;
                Location srcLoc;

                do {
                    intSym = alphabet.getInternalSymbol(r.nextInt(alphabet.getNumInternals()));
                    srcLoc = result.getLocation(r.nextInt(result.size()));
                } while (result.getInternalSuccessor(srcLoc, intSym) != null);

                final Location newLoc = result.addLocation(r.nextDouble() < acceptanceProb);
                result.setInternalSuccessor(srcLoc, intSym, newLoc);
            }
        }

        for (Location loc : result.getLocations()) {
            for (I intSym : alphabet.getInternalAlphabet()) {
                if (result.getInternalSuccessor(loc, intSym) == null) {
                    final Location tgtLoc = result.getLocation(r.nextInt(result.size()));
                    result.setInternalSuccessor(loc, intSym, tgtLoc);
                }
            }

            for (I callSym : alphabet.getCallAlphabet()) {
                for (Location stackLoc : result.getLocations()) {
                    int stackSym = result.encodeStackSym(stackLoc, callSym);
                    for (I retSym : alphabet.getReturnAlphabet()) {
                        if (result.getReturnSuccessor(loc, retSym, stackSym) == null) {
                            final Location tgtLoc = result.getLocation(r.nextInt(result.size()));
                            result.setReturnSuccessor(loc, retSym, stackSym, tgtLoc);
                        }
                    }
                }
            }
        }

        if (minimize) {
            return OneSEVPAMinimizer.minimize(result, alphabet);
        }

        return result;
    }

    public <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A randomDeterministic(@NonNegative int numStates,
                                                                                                    Collection<? extends I> inputs,
                                                                                                    Collection<? extends SP> stateProps,
                                                                                                    Collection<? extends TP> transProps,
                                                                                                    A out) {
        return randomDeterministic(this.random, numStates, inputs, stateProps, transProps, out);
    }

    public static <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A randomDeterministic(Random rand,
                                                                                                           @NonNegative int numStates,
                                                                                                           Collection<? extends I> inputs,
                                                                                                           Collection<? extends SP> stateProps,
                                                                                                           Collection<? extends TP> transProps,
                                                                                                           A out) {
        return randomDeterministic(rand, numStates, inputs, stateProps, transProps, out, true);
    }

    public static <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A randomDeterministic(Random rand,
                                                                                                           @NonNegative int numStates,
                                                                                                           Collection<? extends I> inputs,
                                                                                                           Collection<? extends SP> stateProps,
                                                                                                           Collection<? extends TP> transProps,
                                                                                                           A out,
                                                                                                           boolean minimize) {

        RandomDeterministicAutomatonGenerator<S, I, T, SP, TP, A> gen =
                new RandomDeterministicAutomatonGenerator<>(rand, inputs, stateProps, transProps, out);

        gen.addStates(numStates);
        gen.addTransitions();
        gen.chooseInitial();

        if (minimize) {
            Automata.invasiveMinimize(out, inputs);
        }

        return out;
    }

    public <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A randomDeterministic(@NonNegative int numStates,
                                                                                                    Collection<? extends I> inputs,
                                                                                                    Collection<? extends SP> stateProps,
                                                                                                    Collection<? extends TP> transProps,
                                                                                                    A out,
                                                                                                    boolean minimize) {
        return randomDeterministic(this.random, numStates, inputs, stateProps, transProps, out, minimize);
    }

    public <I> CompactDFA<I> randomDFA(@NonNegative int numStates, Alphabet<I> inputs, boolean minimize) {
        return randomDFA(this.random, numStates, inputs, minimize);
    }

    public static <I> CompactDFA<I> randomDFA(Random rand,
                                              @NonNegative int numStates,
                                              Alphabet<I> inputs,
                                              boolean minimize) {
        return randomDeterministic(rand,
                                   numStates,
                                   inputs,
                                   DFA.STATE_PROPERTIES,
                                   DFA.TRANSITION_PROPERTIES,
                                   new CompactDFA<>(inputs),
                                   minimize);
    }

    public <I> CompactDFA<I> randomDFA(@NonNegative int numStates, Alphabet<I> inputs) {
        return randomDFA(this.random, numStates, inputs);
    }

    public static <I> CompactDFA<I> randomDFA(Random rand, @NonNegative int numStates, Alphabet<I> inputs) {
        return randomDFA(rand, numStates, inputs, true);
    }

    public <I, O> CompactMealy<I, O> randomMealy(@NonNegative int numStates,
                                                 Alphabet<I> inputs,
                                                 Collection<? extends O> outputs,
                                                 boolean minimize) {
        return randomMealy(this.random, numStates, inputs, outputs, minimize);
    }

    public static <I, O> CompactMealy<I, O> randomMealy(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs,
                                                        boolean minimize) {
        return randomDeterministic(rand,
                                   numStates,
                                   inputs,
                                   Collections.singleton(null),
                                   outputs,
                                   new CompactMealy<>(inputs),
                                   minimize);
    }

    public <I, O> CompactMealy<I, O> randomMealy(@NonNegative int numStates,
                                                 Alphabet<I> inputs,
                                                 Collection<? extends O> outputs) {
        return randomMealy(this.random, numStates, inputs, outputs);
    }

    public static <I, O> CompactMealy<I, O> randomMealy(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs) {
        return randomMealy(rand, numStates, inputs, outputs, true);
    }

    private static final class InstanceHolder {

        public static final RandomAutomata INSTANCE = new RandomAutomata();
    }

}
