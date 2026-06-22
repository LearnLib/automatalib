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
package net.automatalib.automaton.vpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.vpa.SEVPAGraphView.SevpaViewEdge;
import net.automatalib.graph.Graph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SEVPAGraphView<L, I> implements Graph<L, SevpaViewEdge<L, I>> {

    private final SEVPA<L, I> sevpa;
    private final VPAlphabet<I> alphabet;
    private final StateIDs<L> stateIDs;

    public SEVPAGraphView(SEVPA<L, I> sevpa) {
        this.sevpa = sevpa;
        this.alphabet = sevpa.getInputAlphabet();
        this.stateIDs = sevpa.stateIDs();
    }

    @Override
    public Collection<L> getNodes() {
        return Collections.unmodifiableCollection(sevpa.getStates());
    }

    @Override
    public Collection<SevpaViewEdge<L, I>> getOutgoingEdges(L location) {

        final List<SevpaViewEdge<L, I>> result = new ArrayList<>();

        // all call transitions
        for (I i : alphabet.getCallAlphabet()) {
            final L succ = sevpa.getModuleEntry(i);
            if (succ != null) {
                result.add(new SevpaViewEdge<>(i, succ));
            }
        }

        // all internal transitions
        for (I i : alphabet.getInternalAlphabet()) {
            final L succ = sevpa.getInternalSuccessor(location, i);
            if (succ != null) {
                result.add(new SevpaViewEdge<>(i, succ));
            }
        }

        // all return transitions for every possible stack contents
        for (I i : alphabet.getReturnAlphabet()) {
            for (L loc : sevpa.getStates()) {
                for (I stackSymbol : alphabet.getCallAlphabet()) {
                    final int sym = sevpa.encodeStackSym(loc, stackSymbol);
                    final L succ = sevpa.getReturnSuccessor(location, i, sym);

                    if (succ != null) {
                        result.add(new SevpaViewEdge<>(i, succ, loc, stackSymbol, stateIDs));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public L getTarget(SevpaViewEdge<L, I> edge) {
        return edge.target;
    }

    @Override
    public VisualizationHelper<L, SevpaViewEdge<L, I>> getVisualizationHelper() {
        return new DefaultVisualizationHelper<>() {

            @Override
            protected Collection<L> initialNodes() {
                return Collections.unmodifiableCollection(sevpa.getInitialStates());
            }

            @Override
            public boolean getNodeProperties(L node, Map<String, String> properties) {
                super.getNodeProperties(node, properties);

                if (sevpa.getStateProperty(node)) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
                }
                properties.put(NodeAttrs.LABEL, "L" + stateIDs.getStateId(node));

                return true;
            }

            @Override
            public boolean getEdgeProperties(L src, SevpaViewEdge<L, I> edge, L tgt, Map<String, String> properties) {
                super.getEdgeProperties(src, edge, tgt, properties);

                properties.put(EdgeAttrs.LABEL, edge.label);

                return true;
            }
        };
    }

    public static class SevpaViewEdge<S, I> {

        public final I input;
        public final S target;
        public final String label;

        public final @Nullable S callLoc;
        public final @Nullable I callSymbol;

        private SevpaViewEdge(I input, S target, @Nullable S callLoc, @Nullable I callSymbol, String label) {
            this.input = input;
            this.target = target;
            this.callLoc = callLoc;
            this.callSymbol = callSymbol;
            this.label = label;
        }

        SevpaViewEdge(I internalAction, S target) {
            this(internalAction, target, null, null, String.valueOf(internalAction));
        }

        SevpaViewEdge(I returnAction, S target, S callLoc, I callSymbol, StateIDs<S> stateIDs) {
            this(returnAction,
                 target,
                 callLoc,
                 callSymbol,
                 returnAction + "/(L" + stateIDs.getStateId(callLoc) + ',' + callSymbol + ')');
        }

    }
}
