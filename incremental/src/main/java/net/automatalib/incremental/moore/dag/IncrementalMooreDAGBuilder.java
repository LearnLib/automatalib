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
package net.automatalib.incremental.moore.dag;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.MooreMachine.MooreGraphView;
import net.automatalib.automaton.visualization.MooreVisualizationHelper;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.moore.IncrementalMooreBuilder;
import net.automatalib.incremental.util.DFSepWordFinder;
import net.automatalib.incremental.util.DFSepWordFinder.Visitor;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Incrementally builds an (acyclic) Moore machine from a set of input and corresponding output words.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 */
public class IncrementalMooreDAGBuilder<I, O> implements IncrementalMooreBuilder<I, O>, InputAlphabetHolder<I> {

    private final Map<@Nullable StateSignature<O>, State<O>> register;
    private final Alphabet<I> inputAlphabet;
    private int alphabetSize;
    private @MonotonicNonNull State<O> init;

    /**
     * Constructor.
     *
     * @param inputAlphabet
     *         the input alphabet to use
     */
    public IncrementalMooreDAGBuilder(Alphabet<I> inputAlphabet) {
        this.register = new LinkedHashMap<>();
        this.inputAlphabet = inputAlphabet;
        this.alphabetSize = inputAlphabet.size();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.inputAlphabet.containsSymbol(symbol)) {
            this.inputAlphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        final int newAlphabetSize = this.inputAlphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize) {
            register.values().forEach(n -> n.ensureInputCapacity(newAlphabetSize));
            alphabetSize = newAlphabetSize;
        }
    }

    @Override
    public boolean hasDefinitiveInformation(Word<? extends I> word) {
        return getState(word) != null;
    }

    /**
     * Retrieves the (internal) state reached by the given input word, or {@code null} if no information about the input
     * word is present.
     *
     * @param word
     *         the input word
     *
     * @return the corresponding state
     */
    private @Nullable State<O> getState(Word<? extends I> word) {
        State<O> s = init;

        if (s == null) {
            return null;
        }

        for (I sym : word) {
            int idx = inputAlphabet.getSymbolIndex(sym);
            s = s.getSuccessor(idx);
            if (s == null) {
                break;
            }
        }
        return s;
    }

    @Override
    public boolean lookup(Word<? extends I> word, List<? super O> output) {
        State<O> curr = init;

        if (curr == null) {
            return false;
        }

        output.add(curr.getOutput());

        for (I sym : word) {
            int idx = inputAlphabet.getSymbolIndex(sym);
            State<O> succ = curr.getSuccessor(idx);
            if (succ == null) {
                return false;
            }
            output.add(succ.getOutput());
            curr = succ;
        }

        return true;
    }

    @Override
    public void insert(Word<? extends I> word, Word<? extends O> outputWord) {
        assert word.size() + 1 == outputWord.size();

        Iterator<? extends O> outWordIterator = outputWord.iterator();
        final O rootOut = outWordIterator.next();

        if (init == null) {
            StateSignature<O> initSig = new StateSignature<>(alphabetSize, rootOut);
            this.init = new State<>(initSig);
            register.put(null, init);
        }

        State<O> curr = init;
        State<O> conf = null;

        Deque<Transition<O>> path = new ArrayDeque<>();

        // Find the internal state in the automaton that can be reached by a
        // maximal prefix of the word (i.e., a path of secured information)
        for (I sym : word) {
            // During this, store the *first* confluence state (i.e., state with multiple incoming edges).
            if (conf == null && curr.isConfluence()) {
                conf = curr;
            }

            int idx = inputAlphabet.getSymbolIndex(sym);
            State<O> succ = curr.getSuccessor(idx);
            if (succ == null) {
                break;
            }

            // If a transition exists for the input symbol, it also has an output symbol.
            // Check if this matches the provided one, otherwise there is a conflict
            O outSym = outWordIterator.next();
            if (!Objects.equals(outSym, succ.getOutput())) {
                throw new ConflictException(
                        "Error inserting " + word.prefix(path.size() + 1) + " / " + outputWord.prefix(path.size() + 1) +
                        ": Incompatible output symbols: " + outSym + " vs " + curr.getOutput());
            }
            path.push(new Transition<>(curr, idx));
            curr = succ;
        }

        int len = word.length();
        int prefixLen = path.size();

        // The information was already present - we do not need to continue
        if (prefixLen == len) {
            return;
        }

        State<O> last = curr;

        if (conf != null) {
            if (conf == last) {
                conf = null;
            }
            last = hiddenClone(last);
            if (conf == null) {
                // the root node is never confluent so in this context, the path always contains at least one node
                @SuppressWarnings("nullness")
                @NonNull Transition<O> peek = path.peek();
                State<O> prev = peek.state;
                if (prev == init) {
                    updateInitSignature(peek.transIdx, last);
                } else {
                    updateSignature(prev, peek.transIdx, last);
                }
            }
        } else if (last != init) {
            hide(last);
        }

        // We then create a suffix path, i.e., a linear sequence of states corresponding to
        // the suffix (more precisely: the suffix minus the first symbol, since this is the
        // transition which is used for gluing the suffix path to the existing automaton).
        Word<? extends I> suffix = word.subWord(prefixLen);
        Word<? extends O> suffixOut = outputWord.subWord(prefixLen);

        // Here we prepare the "gluing" transition
        I sym = suffix.firstSymbol();
        int suffTransIdx = inputAlphabet.getSymbolIndex(sym);
        O suffTransOut = suffixOut.firstSymbol();

        State<O> suffixState = createSuffix(suffix.subWord(1), suffixOut.subWord(1));

        if (last == init) {
            updateInitSignature(suffTransIdx, suffixState, suffTransOut);
        } else {
            last = unhide(last, suffTransIdx, suffixState);

            if (conf != null) {
                // in case of a cyclic structure, the suffix may make predecessors of 'conf' confluent due to un-hiding
                // update the reference with whatever confluent state comes first
                final Iterator<Transition<O>> iter = path.descendingIterator();
                while (iter.hasNext()) {
                    final State<O> s = iter.next().state;
                    if (s.isConfluence()) {
                        conf = s;
                        break;
                    }
                }
            }
        }

        if (path.isEmpty()) {
            return;
        }

        if (conf != null) {
            // If there was a confluence state, we have to clone all nodes on
            // the prefix path up to this state, in order to separate it from other
            // prefixes reaching the confluence state (we do not know anything about them plus the suffix).
            Transition<O> next;
            do {
                next = path.pop();
                State<O> state = next.state;
                int idx = next.transIdx;
                state = clone(state, idx, last);
                last = state;
            } while (next.state != conf);
        }

        // Finally, we have to refresh all the signatures, iterating backwards until the updating becomes stable.
        while (path.size() > 1) {
            Transition<O> next = path.pop();
            State<O> state = next.state;
            int idx = next.transIdx;

            // when extending the path we previously traversed (i.e. expanding the suffix), it may happen that we end up
            // adding a cyclic transition. If this is the case, simply clone the current state and update the parent in
            // the next iteration
            if (state == last) {
                last = clone(state, idx, last);
                continue;
            }

            State<O> updated = updateSignature(state, idx, last);
            if (state == updated) {
                return;
            }
            last = updated;
        }

        int finalIdx = path.pop().transIdx;

        updateInitSignature(finalIdx, last);
    }

    private State<O> hiddenClone(State<O> other) {
        StateSignature<O> sig = other.getSignature().duplicate();

        for (int i = 0; i < alphabetSize; i++) {
            State<O> succ = sig.successors.get(i);
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return new State<>(sig);
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
    private State<O> updateSignature(State<O> state, int idx, State<O> succ) {
        StateSignature<O> sig = state.getSignature();
        State<O> oldSucc = sig.successors.get(idx);
        if (oldSucc == succ) {
            return state;
        }

        register.remove(sig);
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors.set(idx, succ);
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
    private void updateInitSignature(int idx, State<O> succ) {
        @SuppressWarnings("nullness") // this internal method is only called after the root node has been initialized
        @NonNull State<O> init = this.init;
        StateSignature<O> sig = init.getSignature();
        State<O> oldSucc = sig.successors.get(idx);
        if (oldSucc == succ) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors.set(idx, succ);
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
    private void updateInitSignature(int idx, State<O> succ, O out) {
        @SuppressWarnings("nullness") // this internal method is only called after the root node has been initialized
        @NonNull State<O> init = this.init;
        StateSignature<O> sig = init.getSignature();
        State<O> oldSucc = sig.successors.get(idx);
        if (oldSucc == succ && Objects.equals(out, succ.getOutput())) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors.set(idx, succ);
        succ.increaseIncoming();
    }

    private void hide(State<O> state) {
        assert state != init;
        StateSignature<O> sig = state.getSignature();

        register.remove(sig);
    }

    private State<O> createSuffix(Word<? extends I> suffix, Word<? extends O> suffixOut) {
        StateSignature<O> sig = new StateSignature<>(alphabetSize, suffixOut.lastSymbol());
        sig.updateHashCode();
        State<O> last = replaceOrRegister(sig);

        int len = suffix.length();
        for (int i = len - 1; i >= 0; i--) {
            sig = new StateSignature<>(alphabetSize, suffixOut.getSymbol(i));
            I sym = suffix.getSymbol(i);
            int idx = inputAlphabet.getSymbolIndex(sym);
            sig.successors.set(idx, last);
            sig.updateHashCode();
            last = replaceOrRegister(sig);
        }

        return last;
    }

    private State<O> unhide(State<O> state, int idx, State<O> succ) {
        StateSignature<O> sig = state.getSignature();
        State<O> prevSucc = sig.successors.get(idx);
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

    private State<O> clone(State<O> other, int idx, State<O> succ) {
        StateSignature<O> sig = other.getSignature();
        if (sig.successors.get(idx) == succ) {
            return other;
        }
        sig = sig.duplicate();
        sig.successors.set(idx, succ);
        sig.updateHashCode();
        return replaceOrRegister(sig);
    }

    private State<O> replaceOrRegister(State<O> state) {
        StateSignature<O> sig = state.getSignature();
        State<O> other = register.get(sig);
        if (other != null) {
            if (state != other) {
                for (State<O> succ : sig.successors) {
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

    private State<O> replaceOrRegister(StateSignature<O> sig) {
        State<O> state = register.get(sig);
        if (state != null) {
            return state;
        }

        state = new State<>(sig);
        register.put(sig, state);
        for (State<O> succ : sig.successors) {
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return state;
    }

    @Override
    public @Nullable Word<I> findSeparatingWord(MooreMachine<?, I, ?, O> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    private <S, T> @Nullable Word<I> doFindSeparatingWord(MooreMachine<S, I, T, O> target,
                                                          Collection<? extends I> inputs,
                                                          boolean omitUndefined) {

        final Visitor<State<O>, S, State<O>, T> visitor = new Visitor<>() {

            @Override
            public boolean initConflict(State<O> incInit, S targetInit) {
                return !Objects.equals(incInit.getOutput(), target.getStateOutput(targetInit));
            }

            @Override
            public boolean succConflict(State<O> incTrans, @Nullable T targetTrans) {
                return targetTrans == null ||
                       !Objects.equals(incTrans.getOutput(), target.getStateOutput(target.getSuccessor(targetTrans)));
            }
        };

        return DFSepWordFinder.findSeparatingWord(new AutomatonView(), target, inputs, omitUndefined, visitor);
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    @Override
    public MooreMachine<?, I, ?, O> asTransitionSystem() {
        return new AutomatonView();
    }

    @Override
    public Graph<?, ?> asGraph() {
        return new MooreGraphView<>(new AutomatonView(), inputAlphabet) {

            @Override
            public VisualizationHelper<State<O>, TransitionEdge<I, State<O>>> getVisualizationHelper() {
                return new MooreVisualizationHelper<>(automaton) {

                    @Override
                    public boolean getNodeProperties(State<O> node, Map<String, String> properties) {
                        super.getNodeProperties(node, properties);

                        properties.put(NodeAttrs.LABEL, String.valueOf(node.getOutput()));
                        if (node.isConfluence()) {
                            properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
                        }

                        return true;
                    }
                };
            }
        };
    }

    private final class AutomatonView implements MooreMachine<State<O>, I, State<O>, O> {

        @Override
        public O getStateOutput(State<O> state) {
            return state.getOutput();
        }

        @Override
        public @Nullable State<O> getTransition(State<O> state, I input) {
            return state.getSuccessor(inputAlphabet.getSymbolIndex(input));
        }

        @Override
        public State<O> getSuccessor(State<O> transition) {
            return transition;
        }

        @Override
        public @Nullable State<O> getInitialState() {
            return init;
        }

        @Override
        public Collection<State<O>> getStates() {
            return Collections.unmodifiableCollection(register.values());
        }
    }
}
