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
package net.automatalib.automaton.simple;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.helper.SimpleStateIDs;
import net.automatalib.automaton.helper.StateIDGrowingMapping;
import net.automatalib.automaton.helper.StateIDStaticMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.ts.simple.SimpleTS;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple automaton, i.e., a {@link SimpleTS} with a finite number of states.
 *
 * @param <S>
 *         state class.
 * @param <I>
 *         input symbol class.
 */
public interface SimpleAutomaton<S, I> extends SimpleTS<S, I>, Iterable<S>, FiniteRepresentation {

    @Override
    default Iterator<S> iterator() {
        return getStates().iterator();
    }

    /**
     * Retrieves all states of the transition system. Implementing classes should return an unmodifiable collection
     *
     * @return all states in the transition system
     */
    Collection<S> getStates();

    @Override
    default <@Nullable V> MutableMapping<S, V> createStaticStateMapping() {
        return new StateIDStaticMapping<>(stateIDs(), size());
    }

    @Override
    default <@Nullable V> MutableMapping<S, V> createDynamicStateMapping() {
        return new StateIDGrowingMapping<>(stateIDs(), size());
    }

    default StateIDs<S> stateIDs() {
        return new SimpleStateIDs<>(this);
    }

    /**
     * Retrieves the size (number of states) of this transition system.
     *
     * @return the number of states of this transition system
     */
    @Override
    default int size() {
        return getStates().size();
    }
}
