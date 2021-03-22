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
package net.automatalib.automata.spa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import net.automatalib.words.SPAAlphabet;

public class MCFPSView<I> implements ModalContextFreeProcessSystem<I, Void> {

    private final SPA<?, I> spa;
    private final Map<I, ModalProcessGraph<?, I, ?, Void, ?>> mpgs;

    public MCFPSView(SPA<?, I> spa) {
        this.spa = spa;

        final Map<I, DFA<?, I>> procedures = spa.getProcedures();
        this.mpgs = Maps.newHashMapWithExpectedSize(procedures.size());
        for (Entry<I, DFA<?, I>> e : procedures.entrySet()) {
            this.mpgs.put(e.getKey(), new MPGView<>(e.getKey(), e.getValue()));
        }
    }

    @Override
    public Map<I, ModalProcessGraph<?, I, ?, Void, ?>> getMPGs() {
        return this.mpgs;
    }

    @Override
    public I getMainProcess() {
        return this.spa.getInitialProcedure();
    }

    private class MPGView<S> implements ModalProcessGraph<S, I, SPAEdge<I, S>, Void, ProceduralModalEdgeProperty> {

        private final I procedure;
        private final DFA<S, I> dfa;

        private final S init;
        private final S end;
        private final List<S> nodes;

        // we make sure to handle 'init' and 'end' correctly
        @SuppressWarnings("unchecked")
        MPGView(I procedure, DFA<S, I> dfa) {
            this.procedure = procedure;
            this.dfa = dfa;

            this.init = (S) new Object();
            this.end = (S) new Object();
            this.nodes = new ArrayList<>(dfa.size() + 2);
            this.nodes.add(this.init);
            this.nodes.add(this.end);
            this.nodes.addAll(dfa.getStates());
        }

        @Override
        public Collection<SPAEdge<I, S>> getOutgoingEdges(S node) {
            if (node == init) {
                return Collections.singletonList(new SPAEdge<>(this.procedure,
                                                               dfa.getInitialState(),
                                                               ProceduralType.INTERNAL));
            } else if (node == end) {
                return Collections.emptyList();
            } else {
                final SPAAlphabet<I> spaAlphabet = spa.getInputAlphabet();
                final List<SPAEdge<I, S>> result = new ArrayList<>(spaAlphabet.size());

                for (I i : spaAlphabet.getInternalAlphabet()) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null) {
                        result.add(new SPAEdge<>(i, succ, ProceduralType.INTERNAL));
                    }
                }

                for (I i : spaAlphabet.getCallAlphabet()) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null) {
                        result.add(new SPAEdge<>(i, succ, ProceduralType.PROCESS));
                    }
                }

                if (this.dfa.isAccepting(node)) {
                    result.add(new SPAEdge<>(spaAlphabet.getReturnSymbol(),
                                             this.getFinalNode(),
                                             ProceduralType.INTERNAL));
                }

                return result;
            }
        }

        @Override
        public S getTarget(SPAEdge<I, S> edge) {
            return edge.succ;
        }

        @Override
        public Collection<S> getNodes() {
            return this.nodes;
        }

        @Override
        public Set<Void> getNodeProperty(S node) {
            return Collections.emptySet();
        }

        @Override
        public ProceduralModalEdgeProperty getEdgeProperty(SPAEdge<I, S> edge) {
            return edge;
        }

        @Override
        public I getEdgeLabel(SPAEdge<I, S> edge) {
            return edge.input;
        }

        @Override
        public S getFinalNode() {
            return this.end;
        }

        @Override
        public S getInitialNode() {
            return this.init;
        }
    }

    private static class SPAEdge<I, S> implements ProceduralModalEdgeProperty {

        private final I input;
        private final S succ;
        private final ProceduralType type;

        SPAEdge(I input, S succ, ProceduralType type) {
            this.input = input;
            this.succ = succ;
            this.type = type;
        }

        @Override
        public ModalType getModalType() {
            return ModalType.MUST;
        }

        @Override
        public ProceduralType getProceduralType() {
            return this.type;
        }
    }

}
