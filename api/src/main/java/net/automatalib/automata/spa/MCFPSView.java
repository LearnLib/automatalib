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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;

public class MCFPSView<I> implements ModalContextFreeProcessSystem<I, Void> {

    private final SPA<?, I> spa;
    private final Map<I, ModalProcessGraph<?, I, ?, Void, ?>> mpgs;
    private final Map<I, I> call2NonTerminal;
    private final Alphabet<I> processAlphabet;

    public MCFPSView(SPA<?, I> spa, Mapping<I, I> call2NonTerminal) {
        this.spa = spa;

        final Alphabet<I> callAlphabet = spa.getInputAlphabet().getCallAlphabet();
        final int numProcedures = callAlphabet.size();
        final List<I> nonTerminals = new ArrayList<>(numProcedures);
        this.call2NonTerminal = Maps.newHashMapWithExpectedSize(numProcedures);

        for (int i = 0; i < callAlphabet.size(); i++) {
            final I callSym = callAlphabet.getSymbol(i);
            final I nonTerminal = call2NonTerminal.get(callSym);
            this.call2NonTerminal.put(callSym, nonTerminal);
            nonTerminals.add(nonTerminal);
        }
        this.processAlphabet = new NonTerminalAlphabet<>(nonTerminals);

        this.mpgs = Maps.newHashMapWithExpectedSize(numProcedures);
        for (Entry<I, DFA<?, I>> e : spa.getProcedures().entrySet()) {
            this.mpgs.put(this.call2NonTerminal.get(e.getKey()), new MPGView<>(e.getKey(), e.getValue()));
        }
    }

    @Override
    public Alphabet<I> getTerminalAlphabet() {
        return spa.getInputAlphabet().getProceduralAlphabet();
    }

    @Override
    public Alphabet<I> getProcessAlphabet() {
        return processAlphabet;
    }

    @Override
    public Map<I, ModalProcessGraph<?, I, ?, Void, ?>> getMPGs() {
        return this.mpgs;
    }

    @Override
    public I getMainProcess() {
        return this.call2NonTerminal.get(this.spa.getInitialProcedure());
    }

    private class MPGView<S> implements ModalProcessGraph<S, I, TransitionEdge<I, S>, Void, ModalEdgeProperty> {

        private final I procedure;
        private final DFA<S, I> dfa;

        private final S init;
        private final S end;
        private final List<S> nodes;

        @SuppressWarnings("unchecked") // we make sure to handle 'init' and 'end' correctly
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
        public Collection<TransitionEdge<I, S>> getOutgoingEdges(S node) {
            if (node == init) {
                return Collections.singletonList(new TransitionEdge<>(this.procedure, dfa.getInitialState()));
            } else if (node == end) {
                return Collections.emptyList();
            } else {
                final SPAAlphabet<I> spaAlphabet = spa.getInputAlphabet();
                final List<TransitionEdge<I, S>> result = new ArrayList<>(spaAlphabet.size());

                for (I i : spaAlphabet.getInternalAlphabet()) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null) {
                        result.add(new TransitionEdge<>(i, succ));
                    }
                }

                for (I i : spaAlphabet.getCallAlphabet()) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null) {
                        result.add(new TransitionEdge<>(call2NonTerminal.get(i), succ));
                    }
                }

                if (this.dfa.isAccepting(node)) {
                    result.add(new TransitionEdge<>(spaAlphabet.getReturnSymbol(), this.getFinalNode()));
                }

                return result;
            }
        }

        @Override
        public S getTarget(TransitionEdge<I, S> edge) {
            return edge.getTransition();
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
        public ModalEdgeProperty getEdgeProperty(TransitionEdge<I, S> edge) {
            return () -> ModalType.MUST;
        }

        @Override
        public I getEdgeLabel(TransitionEdge<I, S> edge) {
            return edge.getInput();
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

    private static class NonTerminalAlphabet<I> extends AbstractList<I> implements Alphabet<I>, Serializable {

        private final List<I> symbols;

        NonTerminalAlphabet(List<I> symbols) {
            this.symbols = symbols;
        }

        @Override
        public I get(int index) {
            return symbols.get(index);
        }

        @Override
        public int size() {
            return symbols.size();
        }

        @Override
        public I getSymbol(int index) {
            return get(index);
        }

        @Override
        public int getSymbolIndex(I symbol) {
            return symbols.indexOf(symbol);
        }
    }
}
