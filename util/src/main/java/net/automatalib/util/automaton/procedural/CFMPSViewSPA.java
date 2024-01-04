/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.util.automaton.procedural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents a {@link ContextFreeModalProcessSystem}-based view of the instrumented language for a given
 * {@link SPA}, which allows one to model-check language properties of {@link SPA}s with tools such as M3C.
 *
 * @param <I>
 *         input symbol type
 */
class CFMPSViewSPA<I> implements ContextFreeModalProcessSystem<I, Void> {

    private final SPA<?, I> spa;
    private final Map<I, ProceduralModalProcessGraph<?, I, ?, Void, ?>> pmpgs;

    CFMPSViewSPA(SPA<?, I> spa) {
        this.spa = spa;

        final Map<I, DFA<?, I>> procedures = spa.getProcedures();
        this.pmpgs = Maps.newHashMapWithExpectedSize(procedures.size());

        for (Entry<I, DFA<?, I>> e : procedures.entrySet()) {
            this.pmpgs.put(e.getKey(), new MPGView<>(spa, e.getKey(), e.getValue()));
        }
    }

    @Override
    public Map<I, ProceduralModalProcessGraph<?, I, ?, Void, ?>> getPMPGs() {
        return this.pmpgs;
    }

    @Override
    public @Nullable I getMainProcess() {
        return this.spa.getInitialProcedure();
    }

    private static class MPGView<S, I>
            implements ProceduralModalProcessGraph<S, I, PMPGEdge<I, S>, Void, ProceduralModalEdgeProperty> {

        private static final Object INITIAL = new Object();
        private static final Object FINAL = new Object();

        private final ProceduralInputAlphabet<I> alphabet;
        private final Collection<I> proceduralInputs;
        private final I procedure;
        private final DFA<S, I> dfa;
        private final S dfaInit;

        private final S initialNode;
        private final S finalNode;

        // we make sure to handle 'init' and 'end' correctly
        @SuppressWarnings("unchecked")
        MPGView(SPA<?, I> spa, I procedure, DFA<S, I> dfa) {

            final S dfaInit = dfa.getInitialState();

            if (dfaInit == null) {
                throw new IllegalArgumentException("Empty DFAs cannot be mapped to ModalProcessGraphs");
            }

            this.alphabet = spa.getInputAlphabet();
            this.proceduralInputs = spa.getProceduralInputs();
            this.procedure = procedure;
            this.dfa = dfa;
            this.dfaInit = dfaInit;

            this.initialNode = (S) INITIAL;
            this.finalNode = (S) FINAL;
        }

        @Override
        public Collection<PMPGEdge<I, S>> getOutgoingEdges(S node) {
            if (node == initialNode) {
                return Collections.singletonList(new PMPGEdge<>(this.procedure, this.dfaInit, ProceduralType.INTERNAL));
            } else if (node == finalNode) {
                return Collections.emptyList();
            } else {
                final List<PMPGEdge<I, S>> result;

                if (this.dfa.isAccepting(node)) {
                    result = new ArrayList<>(this.proceduralInputs.size() + 1);
                    result.add(new PMPGEdge<>(this.alphabet.getReturnSymbol(),
                                              this.getFinalNode(),
                                              ProceduralType.INTERNAL));
                } else {
                    result = new ArrayList<>(this.proceduralInputs.size());
                }

                for (I i : proceduralInputs) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null) {
                        final ProceduralType type;

                        if (alphabet.isCallSymbol(i)) {
                            type = ProceduralType.PROCESS;
                        } else if (alphabet.isInternalSymbol(i)) {
                            type = ProceduralType.INTERNAL;
                        } else {
                            throw new IllegalStateException("Unexpected symbol type");
                        }

                        result.add(new PMPGEdge<>(i, succ, type));
                    }
                }

                return result;
            }
        }

        @Override
        public S getTarget(PMPGEdge<I, S> edge) {
            return edge.succ;
        }

        @Override
        public Collection<S> getNodes() {
            final List<S> nodes = new ArrayList<>(dfa.size() + 2);
            nodes.add(this.initialNode);
            nodes.add(this.finalNode);
            nodes.addAll(dfa.getStates());
            return nodes;
        }

        @Override
        public Set<Void> getNodeProperty(S node) {
            return Collections.emptySet();
        }

        @Override
        public ProceduralModalEdgeProperty getEdgeProperty(PMPGEdge<I, S> edge) {
            return edge;
        }

        @Override
        public I getEdgeLabel(PMPGEdge<I, S> edge) {
            return edge.input;
        }

        @Override
        public S getFinalNode() {
            return this.finalNode;
        }

        @Override
        public S getInitialNode() {
            return this.initialNode;
        }
    }
}
