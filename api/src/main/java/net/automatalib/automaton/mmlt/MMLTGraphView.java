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
package net.automatalib.automaton.mmlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.visualization.MMLTVisualizationHelper;
import net.automatalib.common.util.Triple;
import net.automatalib.graph.Graph;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.SymbolicInput;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimerTimeoutSymbol;
import net.automatalib.visualization.VisualizationHelper;

/**
 * A graphview for {@link MMLT}s that explicitly represents timeouts as transitions between locations if possible. For
 * this purpose, edges use {@link SymbolicInput}s which include {@link InputSymbol}s and {@link TimerTimeoutSymbol}s.
 *
 * @param <S>
 *         location type
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <T>
 *         transition type
 * @param <O>
 *         output symbol type
 */
public class MMLTGraphView<S, I, T, O> implements Graph<S, Triple<SymbolicInput<I>, O, S>> {

    private final MMLT<S, I, T, O> mmlt;
    private final SymbolCombiner<O> outputCombiner;

    public MMLTGraphView(MMLT<S, I, T, O> mmlt) {
        this.mmlt = mmlt;
        this.outputCombiner = mmlt.getOutputCombiner();
    }

    @Override
    public Collection<Triple<SymbolicInput<I>, O, S>> getOutgoingEdges(S node) {

        Alphabet<I> alphabet = mmlt.getInputAlphabet();
        List<TimerInfo<S, O>> timers = mmlt.getSortedTimers(node);

        List<Triple<SymbolicInput<I>, O, S>> result = new ArrayList<>(alphabet.size() + timers.size());

        for (I i : alphabet) {
            T t = mmlt.getTransition(node, i);
            if (t != null) {
                result.add(Triple.of(TimedInput.input(i), mmlt.getTransitionProperty(t), mmlt.getSuccessor(t)));
            }
        }

        for (TimerInfo<S, O> t : timers) {
            result.add(Triple.of(new TimerTimeoutSymbol<>(t.name()),
                                 outputCombiner.combineSymbols(t.outputs()),
                                 t.target()));
        }

        return result;
    }

    @Override
    public S getTarget(Triple<SymbolicInput<I>, O, S> edge) {
        return edge.getThird();
    }

    @Override
    public Collection<S> getNodes() {
        return mmlt.getStates();
    }

    @Override
    public VisualizationHelper<S, Triple<SymbolicInput<I>, O, S>> getVisualizationHelper() {
        return new MMLTVisualizationHelper<>(mmlt, false, false);
    }
}
