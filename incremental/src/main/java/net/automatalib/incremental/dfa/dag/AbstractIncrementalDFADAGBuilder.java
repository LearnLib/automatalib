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
package net.automatalib.incremental.dfa.dag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.common.util.IntDisjointSets;
import net.automatalib.common.util.UnionFind;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder;
import net.automatalib.incremental.dfa.AbstractVisualizationHelper;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
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

    private <S> @Nullable Word<I> doFindSeparatingWord(DFA<S, I> target,
                                                       Collection<? extends I> inputs,
                                                       boolean omitUndefined) {
        int thisStates = register.size();
        Map<State, Integer> stateIds = new HashMap<>();
        if (sink != null) {
            stateIds.put(sink, 0);
            thisStates++;
        }
        int targetStates = target.size();
        if (!omitUndefined) {
            targetStates++;
        }

        S init2 = target.getInitialState();

        if (init2 == null && omitUndefined) {
            return null;
        }

        State init1 = init;

        boolean acc = init2 != null && target.isAccepting(init2);
        if (init1.getAcceptance().conflicts(acc)) {
            return Word.epsilon();
        }

        IntDisjointSets uf = new UnionFind(thisStates + targetStates);
        StateIDs<S> tgtIds = target.stateIDs();
        int id1 = getStateId(init1, stateIds);
        int id2 = (init2 != null ? tgtIds.getStateId(init2) : targetStates - 1) + thisStates;

        uf.link(id1, id2);

        Queue<Record<@Nullable S, I>> queue = new ArrayDeque<>();

        queue.add(new Record<>(init1, init2));

        I lastSym = null;

        Record<@Nullable S, I> current;

        explore:
        while ((current = queue.poll()) != null) {
            State state1 = current.state1;
            @Nullable S state2 = current.state2;

            for (I sym : inputs) {
                @Nullable S succ2 = (state2 != null) ? target.getSuccessor(state2, sym) : null;
                if (succ2 == null && omitUndefined) {
                    continue;
                }

                int idx = inputAlphabet.getSymbolIndex(sym);
                State succ1 = state1 == sink ? sink : state1.getSuccessor(idx);

                if (succ1 == null) {
                    continue;
                }

                id1 = getStateId(succ1, stateIds);
                id2 = (succ2 != null ? tgtIds.getStateId(succ2) : targetStates - 1) + thisStates;

                int r1 = uf.find(id1), r2 = uf.find(id2);

                if (r1 == r2) {
                    continue;
                }

                if (succ1 == sink) {
                    if (succ2 == null) {
                        continue;
                    }
                    if (target.isAccepting(succ2)) {
                        lastSym = sym;
                        break explore;
                    }
                } else {
                    boolean succ2acc = succ2 != null && target.isAccepting(succ2);
                    if (succ1.getAcceptance().conflicts(succ2acc)) {
                        lastSym = sym;
                        break explore;
                    }
                }

                uf.link(r1, r2);

                queue.add(new Record<>(succ1, succ2, sym, current));
            }
        }

        if (current == null) {
            return null;
        }

        int ceLength = current.depth;
        if (lastSym != null) {
            ceLength++;
        }

        @SuppressWarnings("nullness") // we make sure to set each index to a value of type I
        WordBuilder<I> wb = new WordBuilder<>(null, ceLength);

        int index = ceLength;

        if (lastSym != null) {
            wb.setSymbol(--index, lastSym);
        }

        while (current.reachedFrom != null) {
            final I reachedVia = current.reachedVia;
            wb.setSymbol(--index, reachedVia);
            current = current.reachedFrom;
        }

        return wb.toWord();
    }

    private static int getStateId(State s, Map<State, Integer> idMap) {
        Integer id = idMap.get(s);
        if (id != null) {
            return id;
        }
        id = idMap.size();
        idMap.put(s, id);
        return id;
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
    public UniversalDTS<?, I, ?, Acceptance, Void> asTransitionSystem() {
        return new TransitionSystemView();
    }

    @Override
    public Graph<?, ?> asGraph() {
        return new UniversalAutomatonGraphView<State, I, State, Acceptance, Void, TransitionSystemView>(new TransitionSystemView(),
                                                                                                        inputAlphabet) {

            @Override
            public VisualizationHelper<State, TransitionEdge<I, State>> getVisualizationHelper() {
                return new AbstractVisualizationHelper<State, I, State, TransitionSystemView>(automaton) {

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
                    public Acceptance getAcceptance(State state) {
                        return state.getAcceptance();
                    }
                };
            }
        };
    }

    private static final class Record<S, I> {

        public final State state1;
        public final S state2;
        public final I reachedVia;
        public final @Nullable Record<S, I> reachedFrom;
        public final int depth;

        @SuppressWarnings("nullness") // we will only access reachedVia after checking reachedFrom for null
        Record(State state1, S state2) {
            this.state1 = state1;
            this.state2 = state2;
            this.reachedVia = null;
            this.reachedFrom = null;
            this.depth = 0;
        }

        Record(State state1, S state2, I reachedVia, Record<S, I> reachedFrom) {
            this.state1 = state1;
            this.state2 = state2;
            this.reachedVia = reachedVia;
            this.reachedFrom = reachedFrom;
            this.depth = reachedFrom.depth + 1;
        }
    }

    private final class TransitionSystemView implements UniversalDTS<State, I, State, Acceptance, Void>,
                                                        UniversalAutomaton<State, I, State, Acceptance, Void> {

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
        public Acceptance getStateProperty(State state) {
            return state.getAcceptance();
        }

        @Override
        public Void getTransitionProperty(State transition) {
            return null;
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
