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
package net.automatalib.modelchecking.modelchecker.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.modelchecking.ModelCheckerCache;

/**
 * An implementation of a cache for model checkers. Based on the size and input alphabet.
 *
 * @param <I>
 *         the input type
 * @param <A>
 *         the automaton type
 * @param <P>
 *         the property type
 * @param <R>
 *         the result type of a call to {@link #findCounterExample(SimpleAutomaton, Collection, Object)}.
 */
class SizeModelCheckerCache<I, A extends SimpleAutomaton<?, I>, P, R> implements ModelCheckerCache<I, A, P, R> {

    /**
     * The actual cache.
     */
    private final Map<Pair<Collection<? extends I>, P>, R> counterExamples = new HashMap<>();

    /**
     * The size of the last automaton.
     */
    private int size = Integer.MAX_VALUE;

    /**
     * A function to any ModelChecker.findCounterExample.
     */
    private final ModelChecker<I, A, P, R> modelChecker;

    /**
     * Constructs a new {@link SizeModelCheckerCache}.
     *
     * @param modelChecker
     *         a function to any ModelChecker.findCounterExample.
     */
    SizeModelCheckerCache(ModelChecker<I, A, P, R> modelChecker) {
        this.modelChecker = modelChecker;
    }

    /**
     * The cached implementation for finding counter examples.
     *
     * @see ModelChecker#findCounterExample(Object, Collection, Object)
     */
    @Nullable
    @Override
    public R findCounterExample(A automaton, Collection<? extends I> inputs, P property) {
        if (automaton.size() > size) {
            counterExamples.clear();
        }

        size = automaton.size();

        return counterExamples.computeIfAbsent(Pair.of(inputs, property),
                                               key -> modelChecker.findCounterExample(automaton, inputs, property));
    }

    @Override
    public void clear() {
        counterExamples.clear();
    }
}
