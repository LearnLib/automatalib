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
package net.automatalib.automata.base.compact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.words.Alphabet;

public abstract class AbstractCompactSimpleNondet<I, SP> extends AbstractCompact<I, Integer, SP, Void> implements
                                                                                                       MutableAutomaton<Integer, I, Integer, SP, Void>,
                                                                                                       UniversalFiniteAlphabetAutomaton<Integer, I, Integer, SP, Void>,
                                                                                                       StateIDs<Integer>,
                                                                                                       GrowableAlphabetAutomaton<I> {

    //protected final TIntSet initial;
    protected final Set<Integer> initial; // TODO: replace by primitive specialization
    //protected TIntSet[] transitions;
    protected Set<Integer>[] transitions; // TODO: replace by primitive specialization

    public AbstractCompactSimpleNondet(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public AbstractCompactSimpleNondet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        //this.transitions = new TIntSet[stateCapacity * alphabetSize];
        this.transitions = new Set[stateCapacity * alphabetSize]; // TODO: replace by primitive specialization

        //this.initial = new TIntHashSet();
        this.initial = new HashSet<>(); // TODO: replace by primitive specialization
    }

    protected AbstractCompactSimpleNondet(Alphabet<I> alphabet, AbstractCompactSimpleNondet<?, ?> other) {
        super(alphabet, other);
        this.transitions = other.transitions.clone();
        for (int i = 0; i < transitions.length; i++) {
            //TIntSet tgts = transitions[i];
            Set<Integer> tgts = transitions[i]; // TODO: replace by primitive specialization
            if (tgts != null) {
                //transitions[i] = new TIntHashSet(tgts);
                transitions[i] = new HashSet<>(tgts); // TODO: replace by primitive specialization
            }
        }

        //this.initial = new TIntHashSet(other.initial);
        this.initial = new HashSet<>(other.initial); // TODO: replace by primitive specialization
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateStorage(UpdatePayload payload) {
        this.transitions = (Set<Integer>[]) updateStorage(this.transitions, Set[]::new, null, payload);
    }

    @Override
    public SP getStateProperty(Integer state) {
        return getStateProperty(state.intValue());
    }

    public abstract SP getStateProperty(int stateId);

    @Override
    public Void getTransitionProperty(Integer transition) {
        return null;
    }

    @Override
    public void setInitial(Integer state, boolean initial) {
        setInitial(state.intValue(), initial);
    }

    public void setInitial(int state, boolean initial) {
        if (initial) {
            this.initial.add(state);
        } else {
            this.initial.remove(state);
        }
    }

    @Override
    public void clear() {
        Arrays.fill(transitions, 0, size() * alphabetSize, null);
        this.initial.clear();

        super.clear();
    }

    @Override
    public void setStateProperty(Integer state, SP property) {
        setStateProperty(state.intValue(), property);
    }

    @Override
    public void setTransitionProperty(Integer transition, Void property) {
    }

    @Override
    public void removeTransition(Integer state, I input, Integer transition) {
        removeTransition(state.intValue(), input, transition.intValue());
    }

    public void removeTransition(int stateId, I input, int successorId) {
        removeTransition(stateId, alphabet.getSymbolIndex(input), successorId);
    }

    public void removeTransition(int stateId, int inputIdx, int successorId) {
        int transIdx = stateId * alphabetSize + inputIdx;
        //TIntCollection successors = transitions[transIdx];
        Collection<Integer> successors = transitions[transIdx]; // TODO: replace by primitive specialization
        if (successors != null) {
            successors.remove(successorId);
        }
    }

    @Override
    public void removeAllTransitions(Integer state, I input) {
        removeAllTransitions(state.intValue(), input);
    }

    public void removeAllTransitions(int stateId, I input) {
        removeAllTransitions(stateId, alphabet.getSymbolIndex(input));
    }

    public void removeAllTransitions(int stateId, int inputIdx) {
        int transIdx = stateId * alphabetSize + inputIdx;
        transitions[transIdx] = null;
    }

    @Override
    public void removeAllTransitions(Integer state) {
        removeAllTransitions(state.intValue());
    }

    public void removeAllTransitions(int state) {
        int base = state * alphabetSize;

        Arrays.fill(transitions, base, base + alphabetSize, null);
    }

    @Override
    public Integer createTransition(Integer successor, Void properties) {
        return successor;
    }

    @Override
    public void addTransition(Integer state, I input, Integer transition) {
        addTransition(state.intValue(), input, transition.intValue());
    }

    public void addTransition(int stateId, I input, int succId) {
        addTransition(stateId, alphabet.getSymbolIndex(input), succId);
    }

    public void addTransition(int stateId, int inputIdx, int succId) {
        int transIdx = stateId * alphabetSize + inputIdx;
        //TIntSet successors = transitions[transIdx];
        Set<Integer> successors = transitions[transIdx]; // TODO: replace by primitive specialization
        if (successors == null) {
            //successors = new TIntHashSet();
            successors = new HashSet<>(); // TODO: replace by primitive specialization
            transitions[transIdx] = successors;
        }
        successors.add(succId);
    }

    @Override
    public Integer copyTransition(Integer trans, Integer succ) {
        return succ;
    }

    @Override
    public void setTransitions(Integer state, I input, Collection<? extends Integer> transitions) {
        //TIntList successors = new TIntArrayList(transitions.size());
        List<Integer> successors = new ArrayList<>(transitions.size()); // TODO: replace by primitive specialization
        successors.addAll(transitions);
        setTransitions(state.intValue(), input, successors);
    }

    //public void setTransitions(int state, I input, TIntCollection successors) {
    public void setTransitions(int state,
                               I input,
                               Collection<? extends Integer> successors) { // TODO: replace by primitive specialization
        setTransitions(state, alphabet.getSymbolIndex(input), successors);
    }

    //public void setTransitions(int state, int inputIdx, TIntCollection successors) {
    public void setTransitions(int state,
                               int inputIdx,
                               Collection<? extends Integer> successors) { // TODO: replace by primitive specialization
        int transIdx = state * alphabetSize + inputIdx;
        //TIntSet succs = transitions[transIdx];
        Set<Integer> succs = transitions[transIdx]; // TODO: replace by primitive specialization
        if (succs == null) {
            //succs = new TIntHashSet(successors);
            succs = new HashSet<>(); // TODO: replace by primitive specialization
            transitions[transIdx] = succs;
        } else {
            succs.clear();
        }
        succs.addAll(successors);
    }

    @Override
    public Integer getSuccessor(Integer transition) {
        return transition;
    }

    @Override
    public Collection<Integer> getTransitions(Integer state, I input) {
        //return new TIntSetDecorator(getTransitions(state.intValue(), input));
        return getTransitions(state.intValue(), input); // TODO: replace by primitive specialization
    }

    //public TIntSet getTransitions(int state, I input) {
    public Set<Integer> getTransitions(int state, I input) { // TODO: replace by primitive specialization
        return getTransitions(state, alphabet.getSymbolIndex(input));
    }

    //public TIntSet getTransitions(int state, int inputIdx) {
    public Set<Integer> getTransitions(int state, int inputIdx) { // TODO: replace by primitive specialization
        Set<Integer> transition = transitions[state * alphabetSize + inputIdx];

        return transition == null ? Collections.emptySet() : transition;
    }

    @Override
    public Set<Integer> getInitialStates() {
        //return new TIntSetDecorator(initial);
        return initial; // TODO: replace by primitive specialization
    }
}
