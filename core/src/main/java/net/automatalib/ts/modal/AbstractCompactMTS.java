/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.ts.modal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.ShrinkableAutomaton;
import net.automatalib.automata.base.compact.AbstractCompact;
import net.automatalib.graphs.BidirectionalGraph;
import net.automatalib.words.Alphabet;

/**
 * A Kripke Transition System with a finite number of states (in this library usually denoted to as {@link Automaton}),
 * transitions and a input symbols.
 *
 * <p>This interface adds support for various transition and state properties while having a large number of supported
 * operations on the transition system (see {@link MutableAutomaton}, {@link ShrinkableAutomaton}).
 *
 * <p> This model enables e.g. modal transitions or final states (different from accepting states).
 *
 * @param <I>
 *         input symbol class.
 * @param <TP>
 *         transition property.
 *
 * @author msc
 */
public abstract class AbstractCompactMTS<I, TP extends MutableModalEdgeProperty>
        extends AbstractCompact<I, MTSTransition<I, TP>, Void, TP>
        implements MutableModalTransitionSystem<Integer, I, MTSTransition<I, TP>, TP>,
                   BidirectionalGraph<Integer, MTSTransition<I, TP>> {

    private final Set<Integer> initialStates; // TODO: replace by primitive specialization
    private Set<MTSTransition<I, TP>>[] transitions;
    private Set<MTSTransition<I, TP>>[] backwardTransitions;

    public AbstractCompactMTS(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public AbstractCompactMTS(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        this.initialStates = new HashSet<>(); // TODO: replace by primitive specialization
        this.transitions = new Set[stateCapacity * numInputs()];
        this.backwardTransitions = new Set[stateCapacity];
    }

    @Override
    public void setInitial(Integer state, boolean initial) {
        if (initial) {
            this.initialStates.add(state);
        } else {
            this.initialStates.remove(state);
        }
    }

    @Override
    public void setStateProperty(Integer state, Void property) {}

    @Override
    public void setStateProperty(int state, Void property) {}

    @Override
    public void setTransitionProperty(MTSTransition<I, TP> transition, TP property) {
        transition.setProperty(property);
    }

    @Override
    public void setTransitions(Integer state, I input, Collection<? extends MTSTransition<I, TP>> transitions) {
        // remove first to update back-references
        this.removeAllTransitions(state, input);

        final Set<MTSTransition<I, TP>> trans = Sets.newHashSetWithExpectedSize(transitions.size());

        for (final MTSTransition<I, TP> t : transitions) {
            t.setLabel(input);
            trans.add(t);

            final Set<MTSTransition<I, TP>> incoming;

            if (this.backwardTransitions[t.getTarget()] != null) {
                incoming = this.backwardTransitions[t.getTarget()];
            } else {
                incoming = new HashSet<>();
                this.backwardTransitions[t.getTarget()] = incoming;
            }

            incoming.add(t);
        }

        this.transitions[toMemoryIndex(state, getSymbolIndex(input))] = trans;
    }

    @Override
    public void removeAllTransitions(Integer state, I input) {
        final Collection<MTSTransition<I, TP>> trans = transitions[toMemoryIndex(state, getSymbolIndex(input))];

        if (trans != null) {
            for (MTSTransition<I, TP> t : trans) {
                if (backwardTransitions[t.getTarget()] != null) {
                    backwardTransitions[t.getTarget()].remove(t);
                }
            }
        }

        transitions[toMemoryIndex(state, getSymbolIndex(input))] = null;
        backwardTransitions[state] = null;
    }

    @Override
    public void removeAllTransitions(Integer state) {
        for (final I i : getInputAlphabet()) {
            removeAllTransitions(state, i);
        }
    }

    @Override
    public MTSTransition<I, TP> createTransition(Integer successor, TP properties) {
        return new MTSTransition<>(successor, properties == null ? getDefaultTransitionProperty() : properties);
    }

    @Override
    public Collection<MTSTransition<I, TP>> getOutgoingEdges(Integer node) {
        final List<MTSTransition<I, TP>> result = new ArrayList<>();

        for (final I i : getInputAlphabet()) {
            result.addAll(getTransitions(node, i));
        }

        return result;
    }

    @Override
    public Integer getTarget(MTSTransition<I, TP> edge) {
        return edge.getTarget();
    }

    @Override
    public Collection<Integer> getNodes() {
        return getStates();
    }

    @Override
    public I getEdgeLabel(MTSTransition<I, TP> edge) {
        return edge.getLabel();
    }

    @Override
    public Void getStateProperty(Integer state) {
        return null;
    }

    @Override
    public TP getTransitionProperty(MTSTransition<I, TP> transition) {
        return transition.getProperty();
    }

    @Override
    public Collection<MTSTransition<I, TP>> getTransitions(Integer state, I input) {
        final Set<MTSTransition<I, TP>> trans = transitions[toMemoryIndex(state, getSymbolIndex(input))];
        return trans == null ? Collections.emptySet() : Collections.unmodifiableCollection(trans);
    }

    @Override
    public Integer getSuccessor(MTSTransition<I, TP> transition) {
        return transition.getTarget();
    }

    @Override
    public Set<Integer> getInitialStates() {
        return this.initialStates;
    }

    @Override
    public Collection<MTSTransition<I, TP>> getIncomingEdges(Integer node) {
        return Collections.unmodifiableCollection(backwardTransitions[node]);
    }

    @Override
    public Integer getSource(MTSTransition<I, TP> edge) {
        return edge.getSource();
    }

    @Override
    public void clear() {
        Arrays.fill(transitions, 0, size() * numInputs(), null);
        Arrays.fill(backwardTransitions, 0, size(), null);
        initialStates.clear();

        super.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateTransitionStorage(Payload payload) {
        this.transitions =
                (Set<MTSTransition<I, TP>>[]) updateTransitionStorage(this.transitions, Set[]::new, null, payload);
        super.updateTransitionStorage(payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateStateStorage(Payload payload) {
        this.backwardTransitions =
                (Set<MTSTransition<I, TP>>[]) updateStateStorage(this.backwardTransitions, null, payload);
    }

    protected abstract TP getDefaultTransitionProperty();

}
