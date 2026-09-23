/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.incremental.dfa.dag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder;
import net.automatalib.incremental.dfa.AbstractVisualizationHelper;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.incremental.util.DFSepWordFinder;
import net.automatalib.incremental.util.DFSepWordFinder.Visitor;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractIncrementalDFADAGBuilder<I> extends AbstractIncrementalDFABuilder<I> {

    final Map<StateSignature, State> register;
    final State init;
    State sink;

    AbstractIncrementalDFADAGBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        this.register = new LinkedHashMap<>();
        StateSignature sig = new StateSignature(alphabetSize, Acceptance.DONT_KNOW);
        this.init = new State(sig);
        register.put(sig, init);
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        final int oldSize = alphabetSize;
        super.addAlphabetSymbol(symbol);
        final int newSize = alphabetSize;

        if (oldSize < newSize) {
            register.values().forEach(n -> n.ensureInputCapacity(newSize));
        }
    }

    @Override
    public @Nullable Word<I> findSeparatingWord(DFA<?, I> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    <S> @Nullable Word<I> doFindSeparatingWord(DFA<S, I> target,
                                               Collection<? extends I> inputs,
                                               boolean omitUndefined) {

        final Visitor<State, S, State, S> visitor = new Visitor<>() {

            @Override
            public boolean initConflict(State incInit, S targetInit) {
                return incInit.getAcceptance().conflicts(target.isAccepting(targetInit));
            }

            @Override
            public boolean succConflict(State incTrans, @Nullable S targetTrans) {
                return incTrans.getAcceptance().conflicts(targetTrans != null && target.isAccepting(targetTrans));
            }

            @Override
            public boolean continueExploration(State incSucc, @Nullable S targetSucc) {
                // in the prefix-closed case, we break the DAG contract by making the sink reflexive
                // thus we need to explicitly terminate exploration
                return incSucc != sink || targetSucc != null;
            }
        };

        return DFSepWordFinder.findSeparatingWord(new AutomatonView(), target, inputs, omitUndefined, visitor);
    }

    abstract @Nullable State getState(Word<? extends I> word);

    void updateInitSignature(Acceptance acc) {
        StateSignature sig = init.getSignature();
        sig.acceptance = acc;
    }

    void updateInitSignature(int idx, State succ) {
        StateSignature sig = init.getSignature();
        State oldSucc = sig.successors.get(idx);
        if (oldSucc == succ) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors.set(idx, succ);
        succ.increaseIncoming();
    }

    void updateInitSignature(Acceptance acc, int idx, State succ) {
        StateSignature sig = init.getSignature();
        State oldSucc = sig.successors.get(idx);
        Acceptance oldAcc = sig.acceptance;
        if (oldSucc == succ && oldAcc == acc) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors.set(idx, succ);
        succ.increaseIncoming();
        sig.acceptance = acc;
    }

    /**
     * Updates the signature for a given state.
     *
     * @param state
     *         the state
     * @param acc
     *         the new acceptance value
     *
     * @return the canonical state for the updated signature
     */
    State updateSignature(State state, Acceptance acc) {
        assert state != init;
        StateSignature sig = state.getSignature();
        if (sig.acceptance == acc) {
            return state;
        }
        register.remove(sig);
        sig.acceptance = acc;
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    /**
     * Updates the signature for a given state.
     *
     * @param state
     *         the state
     * @param idx
     *         the index of the transition to change
     * @param succ
     *         the new successor for the above index
     *
     * @return the canonical state for the updated signature
     */
    State updateSignature(State state, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        if (sig.successors.get(idx) == succ) {
            return state;
        }
        register.remove(sig);
        if (sig.successors.get(idx) != null) {
            sig.successors.get(idx).decreaseIncoming();
        }

        sig.successors.set(idx, succ);
        succ.increaseIncoming();
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    State updateSignature(State state, Acceptance acc, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        if (sig.successors.get(idx) == succ && sig.acceptance == acc) {
            return state;
        }
        register.remove(sig);
        sig.successors.set(idx, succ);
        sig.acceptance = acc;
        succ.increaseIncoming();
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    /**
     * Returns the canonical state for the given state's signature, or registers the state as canonical if no state with
     * that signature exists.
     *
     * @param state
     *         the state
     *
     * @return the canonical state for the given state's signature
     */
    State replaceOrRegister(State state) {
        StateSignature sig = state.getSignature();
        State other = register.get(sig);
        if (other != null) {
            if (state != other) {
                for (State succ : sig.successors) {
                    if (succ != null) {
                        succ.decreaseIncoming();
                    }
                }
            }
            return other;
        }

        register.put(sig, state);
        return state;
    }

    /**
     * Returns (and possibly creates) the canonical state for the given signature.
     *
     * @param sig
     *         the signature
     *
     * @return the canonical state for the given signature
     */
    State replaceOrRegister(StateSignature sig) {
        State state = register.get(sig);
        if (state != null) {
            return state;
        }

        state = new State(sig);
        register.put(sig, state);
        for (State succ : sig.successors) {
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return state;
    }

    State hiddenClone(State other) {
        assert other != init;

        StateSignature sig = other.getSignature().duplicate();
        for (State succ : sig.successors) {
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return new State(sig);
    }

    void hide(State state) {
        assert state != init;

        StateSignature sig = state.getSignature();
        register.remove(sig);
    }

    State unhide(State state, Acceptance acc, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        sig.acceptance = acc;
        State prevSucc = sig.successors.get(idx);
        if (prevSucc != null) {
            prevSucc.decreaseIncoming();
        }
        sig.successors.set(idx, succ);
        if (succ != null) {
            succ.increaseIncoming();
        }
        sig.updateHashCode();

        return replaceOrRegister(state);
    }

    State unhide(State state, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        State prevSucc = sig.successors.get(idx);
        if (prevSucc != null) {
            prevSucc.decreaseIncoming();
        }
        sig.successors.set(idx, succ);
        if (succ != null) {
            succ.increaseIncoming();
        }
        sig.updateHashCode();

        return replaceOrRegister(state);
    }

    /**
     * Clones a state, changing the signature.
     *
     * @param other
     *         the state to clone
     * @param acc
     *         the new acceptance value
     *
     * @return the canonical state for the derived signature
     */
    State clone(State other, Acceptance acc) {
        assert other != init;

        StateSignature sig = other.getSignature();
        if (sig.acceptance == acc) {
            return other;
        }
        sig = sig.duplicate();
        sig.acceptance = acc;
        sig.updateHashCode();
        return replaceOrRegister(sig);
    }

    /**
     * Clones a state, changing the signature.
     *
     * @param other
     *         the state to clone
     * @param idx
     *         the index of the transition to change
     * @param succ
     *         the new successor state
     *
     * @return the canonical state for the derived signature
     */
    State clone(State other, int idx, State succ) {
        assert other != init;

        StateSignature sig = other.getSignature();
        if (sig.successors.get(idx) == succ) {
            return other;
        }
        sig = sig.duplicate();
        sig.successors.set(idx, succ);
        sig.updateHashCode();
        return replaceOrRegister(sig);
    }

    State clone(State other, Acceptance acc, int idx, State succ) {
        assert other != init;

        StateSignature sig = other.getSignature();
        if (sig.successors.get(idx) == succ && sig.acceptance == acc) {
            return other;
        }
        sig = sig.duplicate();
        sig.successors.set(idx, succ);
        sig.acceptance = acc;
        return replaceOrRegister(sig);
    }

    @Override
    public DFA<?, I> asTransitionSystem() {
        return new AutomatonView();
    }

    @Override
    public Graph<?, ?> asGraph() {
        return new UniversalAutomatonGraphView<>(new AutomatonView(), inputAlphabet) {

            @Override
            public VisualizationHelper<State, TransitionEdge<I, State>> getVisualizationHelper() {
                return new AbstractVisualizationHelper<>(automaton) {

                    @Override
                    public boolean getNodeProperties(State node, Map<String, String> properties) {
                        super.getNodeProperties(node, properties);

                        if (node.isConfluence()) {
                            String shape = (node.getAcceptance() == Acceptance.TRUE) ?
                                    NodeShapes.DOUBLEOCTAGON :
                                    NodeShapes.OCTAGON;
                            properties.put(NodeAttrs.SHAPE, shape);
                        }
                        return true;
                    }

                    @Override
                    protected Acceptance getAcceptance(State state) {
                        return state.getAcceptance();
                    }
                };
            }
        };
    }

    private final class AutomatonView implements DFA<State, I> {

        @Override
        public State getSuccessor(State transition) {
            return transition;
        }

        @Override
        public State getTransition(State state, I input) {
            if (state == sink) {
                return state;
            }
            int idx = inputAlphabet.getSymbolIndex(input);
            return state.getSuccessor(idx);
        }

        @Override
        public State getInitialState() {
            return init;
        }

        @Override
        public boolean isAccepting(State state) {
            return state.getAcceptance() == Acceptance.TRUE;
        }

        @Override
        public Collection<State> getStates() {
            if (sink == null) {
                return Collections.unmodifiableCollection(register.values());
            }
            List<State> result = new ArrayList<>(register.size() + 1);
            result.addAll(register.values());
            result.add(sink);
            return result;
        }
    }

}
