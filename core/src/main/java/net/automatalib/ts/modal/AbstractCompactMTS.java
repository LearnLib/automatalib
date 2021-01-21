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
package net.automatalib.ts.modal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.automata.base.compact.AbstractCompact;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An abstract base-implementation for {@link MutableModalTransitionSystem}s.
 *
 * @param <I>
 *         input symbol type
 * @param <TP>
 *         (specific) transition property type
 *
 * @author msc
 */
public abstract class AbstractCompactMTS<I, TP extends MutableModalEdgeProperty>
        extends AbstractCompact<I, MTSTransition<I, TP>, Void, TP>
        implements MutableModalTransitionSystem<Integer, I, MTSTransition<I, TP>, TP> {

    private final Set<Integer> initialStates; // TODO: replace by primitive specialization
    private @Nullable Set<MTSTransition<I, TP>>[] transitions;

    public AbstractCompactMTS(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public AbstractCompactMTS(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        this.initialStates = new HashSet<>(); // TODO: replace by primitive specialization
        this.transitions = new Set[stateCapacity * numInputs()];
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
        this.removeAllTransitions(state, input);

        final Set<MTSTransition<I, TP>> trans = Sets.newHashSetWithExpectedSize(transitions.size());

        for (final MTSTransition<I, TP> t : transitions) {
            t.setSource(state);
            t.setLabel(input);
            trans.add(t);
        }

        this.transitions[toMemoryIndex(state, getSymbolIndex(input))] = trans;
    }

    @Override
    public void removeAllTransitions(Integer state, I input) {
        transitions[toMemoryIndex(state, getSymbolIndex(input))] = null;
    }

    @Override
    public void removeAllTransitions(Integer state) {
        for (final I i : getInputAlphabet()) {
            removeAllTransitions(state, i);
        }
    }

    @Override
    public MTSTransition<I, TP> createTransition(Integer successor) {
        return createTransition(successor, null);
    }

    @Override
    public MTSTransition<I, TP> createTransition(Integer successor, @Nullable TP properties) {
        return new MTSTransition<>(successor, properties == null ? getDefaultTransitionProperty() : properties);
    }

    @Override
    public MTSTransition<I, TP> addModalTransition(Integer src, I input, Integer tgt, ModalType modalType) {
        return this.addTransition(src, input, tgt, buildModalProperty(modalType));
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
    public void clear() {
        Arrays.fill(transitions, 0, size() * numInputs(), null);
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

    protected abstract TP getDefaultTransitionProperty();

    protected abstract TP buildModalProperty(ModalType type);

}
