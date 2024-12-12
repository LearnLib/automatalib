/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.util.automaton.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.ProceduralOutputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.impl.StackSBA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.automaton.procedural.impl.StackSPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.automaton.vpa.impl.DefaultOneSEVPA;
import net.automatalib.automaton.vpa.impl.Location;
import net.automatalib.common.util.HashUtil;
import net.automatalib.util.automaton.fsa.DFAs;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.util.automaton.minimizer.OneSEVPAMinimizer;
import net.automatalib.util.automaton.procedural.SPAs;
import org.checkerframework.checker.index.qual.NonNegative;

public final class RandomAutomata {

    private RandomAutomata() {
        // prevent instantiation
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
     *         determines whether the DFA will be minimized before being returned. Note that if {@code true} is passed
     *         for this parameter, the resulting automaton might have a {@link Automaton#size() size} less than
     *         {@code numStates}
     * @param <I>
     *         input symbol type
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

    public static <I> DefaultOneSEVPA<I> randomOneSEVPA(Random r,
                                                        int locCount,
                                                        VPAlphabet<I> alphabet,
                                                        double acceptanceProb,
                                                        double initialRetTransProb,
                                                        boolean minimize) {
        return randomOneSEVPA(r,
                              locCount,
                              alphabet,
                              acceptanceProb,
                              initialRetTransProb,
                              minimize,
                              new DefaultOneSEVPA<>(alphabet, locCount));
    }

    public static <I> DefaultOneSEVPA<I> randomOneSEVPA(Random r,
                                                        int locCount,
                                                        VPAlphabet<I> alphabet,
                                                        double acceptanceProb,
                                                        double initialRetTransProb,
                                                        boolean minimize,
                                                        DefaultOneSEVPA<I> result) {
        result.addInitialLocation(r.nextDouble() < acceptanceProb);

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

    public static <I> SPA<?, I> randomSPA(Random random, ProceduralInputAlphabet<I> alphabet, int procedureSize) {
        return randomSPA(random, alphabet, procedureSize, true);
    }

    public static <I> SPA<?, I> randomSPA(Random random,
                                          ProceduralInputAlphabet<I> alphabet,
                                          int procedureSize,
                                          boolean minimize) {

        SPA<?, I> result;

        do {
            final Map<I, DFA<?, I>> dfas = new HashMap<>(HashUtil.capacity(alphabet.getNumCalls()));
            final Alphabet<I> proceduralAlphabet = alphabet.getProceduralAlphabet();

            for (I procedure : alphabet.getCallAlphabet()) {
                final DFA<?, I> dfa = RandomAutomata.randomDFA(random, procedureSize, proceduralAlphabet, minimize);
                dfas.put(procedure, dfa);
            }

            result = new StackSPA<>(alphabet, alphabet.getCallSymbol(random.nextInt(alphabet.getNumCalls())), dfas);
        } while (minimize && !SPAs.isMinimal(result));

        return result;
    }

    public static <I> SBA<?, I> randomSBA(Random random, ProceduralInputAlphabet<I> alphabet, int procedureSize) {
        return randomSBA(random, alphabet, procedureSize, true);
    }

    public static <I> SBA<?, I> randomSBA(Random random,
                                          ProceduralInputAlphabet<I> alphabet,
                                          int procedureSize,
                                          boolean minimize) {

        assert procedureSize > 1;

        final List<I> inputs = new ArrayList<>(alphabet.size());
        final List<I> nonTerminatingProcedures = new ArrayList<>(alphabet.getNumCalls());

        inputs.add(alphabet.getReturnSymbol());
        inputs.addAll(alphabet.getInternalAlphabet());

        for (I i : alphabet.getCallAlphabet()) {
            if (random.nextBoolean()) {
                inputs.add(i);
            } else {
                nonTerminatingProcedures.add(i);
            }
        }

        final Map<I, DFA<?, I>> dfas = new HashMap<>(HashUtil.capacity(alphabet.getNumCalls()));

        for (I procedure : alphabet.getCallAlphabet()) {
            final CompactDFA<I> dfa = new CompactDFA<>(alphabet);
            RandomAutomata.randomDeterministic(random,
                                               procedureSize - 2,
                                               inputs,
                                               Collections.singletonList(Boolean.TRUE),
                                               DFA.TRANSITION_PROPERTIES,
                                               dfa,
                                               false);

            final List<Integer> originalStates = new ArrayList<>(dfa.getStates());
            final Integer successSink = dfa.addState(true);
            final Integer sink = dfa.addState(false);

            for (I i : alphabet) {
                dfa.setTransition(successSink, i, sink);
                dfa.setTransition(sink, i, sink);
            }

            for (Integer s : originalStates) {
                for (I i : nonTerminatingProcedures) {
                    dfa.setTransition(s, i, successSink);
                }
                if (nonTerminatingProcedures.contains(procedure)) {
                    dfa.setTransition(s, alphabet.getReturnSymbol(), sink);
                } else if (random.nextBoolean()) {
                    dfa.setTransition(s, alphabet.getReturnSymbol(), successSink);
                } else {
                    dfa.setTransition(s, alphabet.getReturnSymbol(), sink);
                }
            }

            if (minimize) {
                HopcroftMinimizer.minimizeDFAInvasive(dfa, alphabet);
            }

            assert DFAs.isPrefixClosed(dfa, alphabet);
            dfas.put(procedure, dfa);
        }

        return new StackSBA<>(alphabet, alphabet.getCallSymbol(random.nextInt(alphabet.getNumCalls())), dfas);
    }

    public static <I, O> SPMM<?, I, ?, O> randomSPMM(Random random,
                                                     ProceduralInputAlphabet<I> inputAlphabet,
                                                     ProceduralOutputAlphabet<O> outputAlphabet,
                                                     int procedureSize) {
        return randomSPMM(random, inputAlphabet, outputAlphabet, procedureSize, true);
    }

    public static <I, O> SPMM<?, I, ?, O> randomSPMM(Random random,
                                                     ProceduralInputAlphabet<I> inputAlphabet,
                                                     ProceduralOutputAlphabet<O> outputAlphabet,
                                                     int procedureSize,
                                                     boolean minimize) {

        assert procedureSize > 0;

        final Set<I> nonContinuableSymbols = new HashSet<>();

        nonContinuableSymbols.add(inputAlphabet.getReturnSymbol());

        for (I i : inputAlphabet.getCallAlphabet()) {
            if (random.nextBoolean()) {
                nonContinuableSymbols.add(i);
            }
        }

        final Map<I, MealyMachine<?, I, ?, O>> mealies = new HashMap<>(HashUtil.capacity(inputAlphabet.getNumCalls()));

        for (I procedure : inputAlphabet.getCallAlphabet()) {
            final CompactMealy<I, O> mealy = new CompactMealy<>(inputAlphabet);
            RandomAutomata.randomDeterministic(random,
                                               procedureSize - 1,
                                               inputAlphabet,
                                               Collections.emptyList(),
                                               outputAlphabet,
                                               mealy,
                                               false);

            final List<Integer> originalStates = new ArrayList<>(mealy.getStates());
            final Integer sink = mealy.addState();

            for (I i : inputAlphabet) {
                mealy.setTransition(sink, i, sink, outputAlphabet.getErrorSymbol());
                boolean isNonContinuable = nonContinuableSymbols.contains(i);

                for (Integer s : originalStates) {
                    final O output = mealy.getTransitionProperty(s, i);

                    if (isNonContinuable || outputAlphabet.isErrorSymbol(output)) {
                        mealy.setTransition(s, i, sink, output);
                    }
                }
            }

            if (minimize) {
                HopcroftMinimizer.minimizeMealyInvasive(mealy, inputAlphabet);
            }

            mealies.put(procedure, mealy);
        }

        final Alphabet<O> internalOutputs = outputAlphabet.getRegularAlphabet();
        return new StackSPMM<>(inputAlphabet,
                               inputAlphabet.getCallSymbol(random.nextInt(inputAlphabet.getNumCalls())),
                               internalOutputs.getSymbol(random.nextInt(internalOutputs.size())),
                               outputAlphabet.getErrorSymbol(),
                               mealies);
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
            HopcroftMinimizer.minimizeUniversalInvasive(out, inputs);
        }

        return out;
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

    public static <I> CompactDFA<I> randomDFA(Random rand, @NonNegative int numStates, Alphabet<I> inputs) {
        return randomDFA(rand, numStates, inputs, true);
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

    public static <I, O> CompactMealy<I, O> randomMealy(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs) {
        return randomMealy(rand, numStates, inputs, outputs, true);
    }

    public static <I, O> CompactMoore<I, O> randomMoore(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs,
                                                        boolean minimize) {
        return randomDeterministic(rand,
                                   numStates,
                                   inputs,
                                   outputs,
                                   Collections.singleton(null),
                                   new CompactMoore<>(inputs),
                                   minimize);
    }

    public static <I, O> CompactMoore<I, O> randomMoore(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs) {
        return randomMoore(rand, numStates, inputs, outputs, true);
    }

}
