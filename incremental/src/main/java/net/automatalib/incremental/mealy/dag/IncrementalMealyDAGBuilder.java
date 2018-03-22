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
package net.automatalib.incremental.mealy.dag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.IntDisjointSets;
import net.automatalib.commons.util.UnionFind;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.AbstractIncrementalMealyBuilder;
import net.automatalib.ts.transout.MealyTransitionSystem;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.helper.DelegateVisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Incrementally builds an (acyclic) Mealy machine, from a set of input and corresponding output words.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Malte Isberner
 */
public class IncrementalMealyDAGBuilder<I, O> extends AbstractIncrementalMealyBuilder<I, O> {

    private final Map<StateSignature, State> register = new HashMap<>();
    private final int alphabetSize;
    private final State init;

    /**
     * Constructor.
     *
     * @param inputAlphabet
     *         the input alphabet to use
     */
    public IncrementalMealyDAGBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        this.alphabetSize = inputAlphabet.size();
        StateSignature initSig = new StateSignature(alphabetSize);
        this.init = new State(initSig);
        register.put(null, init);
    }

    /**
     * Checks whether there exists secured information about the output for the given word.
     *
     * @param word
     *         the input word
     *
     * @return a boolean indicating whether information about the output for the given input word exists.
     *
     * @deprecated since 2014-01-22. Use {@link #hasDefinitiveInformation(Word)}
     */
    @Deprecated
    public boolean isComplete(Word<? extends I> word) {
        return hasDefinitiveInformation(word);
    }

    @Override
    public boolean hasDefinitiveInformation(Word<? extends I> word) {
        State s = getState(word);
        return (s != null);
    }

    /**
     * Retrieves the (internal) state reached by the given input word, or <tt>null</tt> if no information about the
     * input word is present.
     *
     * @param word
     *         the input word
     *
     * @return the corresponding state
     */
    private State getState(Word<? extends I> word) {
        State s = init;

        for (I sym : word) {
            int idx = inputAlphabet.getSymbolIndex(sym);
            s = s.getSuccessor(idx);
            if (s == null) {
                break;
            }
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean lookup(Word<? extends I> word, List<? super O> output) {
        State curr = init;
        for (I sym : word) {
            int idx = inputAlphabet.getSymbolIndex(sym);
            State succ = curr.getSuccessor(idx);
            if (succ == null) {
                return false;
            }
            output.add((O) curr.getOutput(idx));
            curr = succ;
        }

        return true;
    }

    @Override
    public void insert(Word<? extends I> word, Word<? extends O> outputWord) {
        State curr = init;
        State conf = null;

        Deque<PathElem> path = new ArrayDeque<>();

        // Find the internal state in the automaton that can be reached by a
        // maximal prefix of the word (i.e., a path of secured information)
        Iterator<? extends O> outWordIterator = outputWord.iterator();
        for (I sym : word) {
            // During this, store the *first* confluence state (i.e., state with
            // multiple incoming edges).
            if (conf == null && curr.isConfluence()) {
                conf = curr;
            }

            int idx = inputAlphabet.getSymbolIndex(sym);
            State succ = curr.getSuccessor(idx);
            if (succ == null) {
                break;
            }

            // If a transition exists for the input symbol, it also has an
            // output symbol.
            // Check if this matches the provided one, otherwise there is a
            // conflict
            O outSym = outWordIterator.next();
            if (!Objects.equals(outSym, curr.getOutput(idx))) {
                throw new ConflictException(
                        "Error inserting " + word.prefix(path.size() + 1) + " / " + outputWord.prefix(path.size() + 1) +
                        ": Incompatible output symbols: " + outSym + " vs " + curr.getOutput(idx));
            }
            path.push(new PathElem(curr, idx));
            curr = succ;
        }

        int len = word.length();
        int prefixLen = path.size();

        // The information was already present - we do not need to continue
        if (prefixLen == len) {
            return;
        }

        State last = curr;

        if (conf != null) {
            if (conf == last) {
                conf = null;
            }
            last = hiddenClone(last);
            if (conf == null) {
                State prev = path.peek().state;
                if (prev != init) {
                    updateSignature(prev, path.peek().transIdx, last);
                } else {
                    updateInitSignature(path.peek().transIdx, last);
                }
            }
        } else if (last != init) {
            hide(last);
        }

        // We then create a suffix path, i.e., a linear sequence of states
        // corresponding to
        // the suffix (more precisely: the suffix minus the first symbol, since
        // this is the
        // transition which is used for gluing the suffix path to the existing
        // automaton).
        Word<? extends I> suffix = word.subWord(prefixLen);
        Word<? extends O> suffixOut = outputWord.subWord(prefixLen);

        // Here we prepare the "gluing" transition
        I sym = suffix.firstSymbol();
        int suffTransIdx = inputAlphabet.getSymbolIndex(sym);
        O suffTransOut = suffixOut.firstSymbol();

        State suffixState = createSuffix(suffix.subWord(1), suffixOut.subWord(1));

        if (last != init) {
            last = unhide(last, suffTransIdx, suffixState, suffTransOut);
        } else {
            updateInitSignature(suffTransIdx, suffixState, suffTransOut);
        }

        if (path.isEmpty()) {
            return;
        }

        if (conf != null) {
            // If there was a confluence state, we have to clone all nodes on
            // the prefix path up to this state, in order to separate it from
            // other
            // prefixes reaching the confluence state (we do not know anything
            // about them
            // plus the suffix).
            PathElem next;
            do {
                next = path.pop();
                State state = next.state;
                int idx = next.transIdx;
                state = clone(state, idx, last);
                last = state;
            } while (next.state != conf);
        }

        // Finally, we have to refresh all the signatures, iterating backwards
        // until the updating becomes stable.
        while (path.size() > 1) {
            PathElem next = path.pop();
            State state = next.state;
            int idx = next.transIdx;
            State updated = updateSignature(state, idx, last);
            if (state == updated) {
                return;
            }
            last = updated;
        }

        int finalIdx = path.pop().transIdx;

        updateInitSignature(finalIdx, last);
    }

    private State hiddenClone(State other) {
        StateSignature sig = other.getSignature().duplicate();

        for (int i = 0; i < alphabetSize; i++) {
            State succ = sig.successors[i];
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return new State(sig);
    }

    /**
     * Update the signature of a state, changing only the successor state of a single transition index.
     *
     * @param state
     *         the state which's signature to update
     * @param idx
     *         the transition index to modify
     * @param succ
     *         the new successor state
     *
     * @return the resulting state, which can either be the same as the input state (if the new signature is unique), or
     * the result of merging with another state.
     */
    private State updateSignature(State state, int idx, State succ) {
        StateSignature sig = state.getSignature();
        if (sig.successors[idx] == succ) {
            return state;
        }

        register.remove(sig);
        if (sig.successors[idx] != null) {
            sig.successors[idx].decreaseIncoming();
        }
        sig.successors[idx] = succ;
        succ.increaseIncoming();
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    /**
     * Update the signature of the initial state. This requires special handling, as the initial state is not stored in
     * the register (since it can never legally act as a predecessor).
     *
     * @param idx
     *         the transition index being changed
     * @param succ
     *         the new successor state
     */
    private void updateInitSignature(int idx, State succ) {
        StateSignature sig = init.getSignature();
        State oldSucc = sig.successors[idx];
        if (oldSucc == succ) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors[idx] = succ;
        succ.increaseIncoming();
    }

    /**
     * Updates the signature of the initial state, changing both the successor state and the output symbol.
     *
     * @param idx
     *         the transition index to change
     * @param succ
     *         the new successor state
     * @param out
     *         the output symbol
     */
    private void updateInitSignature(int idx, State succ, O out) {
        StateSignature sig = init.getSignature();
        State oldSucc = sig.successors[idx];
        if (oldSucc == succ && Objects.equals(out, sig.outputs[idx])) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors[idx] = succ;
        sig.outputs[idx] = out;
        succ.increaseIncoming();
    }

    private void hide(State state) {
        assert state != init;
        StateSignature sig = state.getSignature();

        register.remove(sig);
    }

    private State createSuffix(Word<? extends I> suffix, Word<? extends O> suffixOut) {
        StateSignature sig = new StateSignature(alphabetSize);
        sig.updateHashCode();
        State last = replaceOrRegister(sig);

        int len = suffix.length();
        for (int i = len - 1; i >= 0; i--) {
            sig = new StateSignature(alphabetSize);
            I sym = suffix.getSymbol(i);
            O outsym = suffixOut.getSymbol(i);
            int idx = inputAlphabet.getSymbolIndex(sym);
            sig.successors[idx] = last;
            sig.outputs[idx] = outsym;
            sig.updateHashCode();
            last = replaceOrRegister(sig);
        }

        return last;
    }

    private State unhide(State state, int idx, State succ, O out) {
        StateSignature sig = state.getSignature();
        State prevSucc = sig.successors[idx];
        if (prevSucc != null) {
            prevSucc.decreaseIncoming();
        }
        sig.successors[idx] = succ;
        if (succ != null) {
            succ.increaseIncoming();
        }
        sig.outputs[idx] = out;
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    private State clone(State other, int idx, State succ) {
        StateSignature sig = other.getSignature();
        if (sig.successors[idx] == succ) {
            return other;
        }
        sig = sig.duplicate();
        sig.successors[idx] = succ;
        sig.updateHashCode();
        return replaceOrRegister(sig);
    }

    private State replaceOrRegister(State state) {
        StateSignature sig = state.getSignature();
        State other = register.get(sig);
        if (other != null) {
            if (state != other) {
                for (int i = 0; i < sig.successors.length; i++) {
                    State succ = sig.successors[i];
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

    private State replaceOrRegister(StateSignature sig) {
        State state = register.get(sig);
        if (state != null) {
            return state;
        }

        state = new State(sig);
        register.put(sig, state);
        for (int i = 0; i < sig.successors.length; i++) {
            State succ = sig.successors[i];
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return state;
    }

    @Override
    public GraphView asGraph() {
        return new GraphView();
    }

    @Override
    public AutomatonView asTransitionSystem() {
        return new AutomatonView();
    }

    @Override
    public Word<I> findSeparatingWord(MealyMachine<?, I, ?, O> target,
                                      Collection<? extends I> inputs,
                                      boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    private <S, T> Word<I> doFindSeparatingWord(MealyMachine<S, I, T, O> mealy,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        S init2 = mealy.getInitialState();

        if (init2 == null) {
            return omitUndefined ? null : Word.epsilon();
        }

        State init1 = init;

        Map<State, Integer> ids = new HashMap<>();
        StateIDs<S> mealyIds = mealy.stateIDs();

        int thisStates = register.size();
        int id1 = getStateId(init1, ids), id2 = mealyIds.getStateId(init2) + thisStates;

        IntDisjointSets uf = new UnionFind(thisStates + mealy.size());
        uf.link(id1, id2);

        Queue<Record<S, I>> queue = new ArrayDeque<>();

        queue.offer(new Record<>(init1, init2));

        I lastSym = null;

        Record<S, I> current;

        explore:
        while ((current = queue.poll()) != null) {
            State state1 = current.state1;
            S state2 = current.state2;

            for (I sym : inputs) {
                int idx = inputAlphabet.getSymbolIndex(sym);
                State succ1 = state1.getSuccessor(idx);
                if (succ1 == null) {
                    continue;
                }

                T trans2 = mealy.getTransition(state2, sym);
                if (trans2 == null) {
                    if (omitUndefined) {
                        continue;
                    }
                    lastSym = sym;
                    break explore;
                }

                Object out1 = state1.getOutput(idx);
                Object out2 = mealy.getTransitionOutput(trans2);
                if (!Objects.equals(out1, out2)) {
                    lastSym = sym;
                    break explore;
                }

                S succ2 = mealy.getSuccessor(trans2);

                id1 = getStateId(succ1, ids);
                id2 = mealyIds.getStateId(succ2) + thisStates;

                int r1 = uf.find(id1), r2 = uf.find(id2);

                if (r1 == r2) {
                    continue;
                }

                uf.link(r1, r2);

                queue.offer(new Record<>(succ1, succ2, current, sym));
            }
        }

        if (current == null) {
            return null;
        }

        int ceLength = current.depth;
        if (lastSym != null) {
            ceLength++;
        }

        WordBuilder<I> wb = new WordBuilder<>(null, ceLength);

        int index = ceLength;

        if (lastSym != null) {
            wb.setSymbol(--index, lastSym);
        }

        while (current.reachedFrom != null) {
            wb.setSymbol(--index, current.reachedVia);
            current = current.reachedFrom;
        }

        return wb.toWord();
    }

    private static int getStateId(State state, Map<State, Integer> ids) {
        return ids.computeIfAbsent(state, k -> ids.size());
    }

    // /////////////////////////////////////////////////////////////////////
    // Equivalence test //
    // /////////////////////////////////////////////////////////////////////

    private static final class Record<S, I> {

        private final State state1;
        private final S state2;
        private final I reachedVia;
        private final Record<S, I> reachedFrom;
        private final int depth;

        Record(State state1, S state2) {
            this(state1, state2, null, null);
        }

        Record(State state1, S state2, Record<S, I> reachedFrom, I reachedVia) {
            this.state1 = state1;
            this.state2 = state2;
            this.reachedFrom = reachedFrom;
            this.reachedVia = reachedVia;
            this.depth = (reachedFrom != null) ? reachedFrom.depth + 1 : 0;
        }
    }

    public class GraphView extends AbstractGraphView<I, O, State, TransitionRecord> {

        @Override
        public Collection<TransitionRecord> getOutgoingEdges(State node) {
            List<TransitionRecord> edges = new ArrayList<>();
            for (int i = 0; i < alphabetSize; i++) {
                if (node.getSuccessor(i) != null) {
                    edges.add(new TransitionRecord(node, i));
                }
            }
            return edges;
        }

        @Override
        public State getTarget(TransitionRecord edge) {
            return edge.source.getSuccessor(edge.transIdx);
        }

        @Override
        public Collection<State> getNodes() {
            return Collections.unmodifiableCollection(register.values());
        }

        @Override
        @Nullable
        public I getInputSymbol(TransitionRecord edge) {
            return inputAlphabet.getSymbol(edge.transIdx);
        }

        @Override
        @Nullable
        @SuppressWarnings("unchecked")
        public O getOutputSymbol(TransitionRecord edge) {
            return (O) edge.source.getOutput(edge.transIdx);
        }

        @Override
        @Nonnull
        public State getInitialNode() {
            return init;
        }

        @Override
        public VisualizationHelper<State, TransitionRecord> getVisualizationHelper() {
            return new DelegateVisualizationHelper<State, TransitionRecord>(super.getVisualizationHelper()) {

                private int id;

                @Override
                public boolean getNodeProperties(State node, Map<String, String> properties) {
                    if (!super.getNodeProperties(node, properties)) {
                        return false;
                    }
                    properties.put(NodeAttrs.LABEL, "n" + (id++));
                    if (node.isConfluence()) {
                        properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
                    }
                    return true;
                }

            };
        }

    }

    public class AutomatonView implements MealyTransitionSystem<State, I, TransitionRecord, O> {

        @Override
        public State getSuccessor(TransitionRecord transition) {
            State src = transition.source;
            return src.getSuccessor(transition.transIdx);
        }

        @Override
        public State getInitialState() {
            return init;
        }

        @Override
        public TransitionRecord getTransition(State state, I input) {
            int inputIdx = inputAlphabet.getSymbolIndex(input);
            if (state.getSuccessor(inputIdx) == null) {
                return null;
            }
            return new TransitionRecord(state, inputIdx);
        }

        @Override
        @SuppressWarnings("unchecked")
        public O getTransitionOutput(TransitionRecord transition) {
            State src = transition.source;
            return (O) src.getOutput(transition.transIdx);
        }
    }
}
