/* Copyright (C) 2013-2023 TU Dortmund
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
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents a {@link ContextFreeModalProcessSystem}-based view on the instrumented language of a given
 * {@link SBA}, which allows one to model-check language properties of {@link SBA}s with tools such as M3C.
 *
 * @param <I>
 *         input symbol type
 */
class CFMPSViewSBA<I> implements ContextFreeModalProcessSystem<I, Void> {

    private final SBA<?, I> sba;
    private final Map<I, ProceduralModalProcessGraph<?, I, ?, Void, ?>> pmpgs;

    CFMPSViewSBA(SBA<?, I> sba) {
        this.sba = sba;

        final Map<I, DFA<?, I>> procedures = sba.getProcedures();
        this.pmpgs = Maps.newHashMapWithExpectedSize(procedures.size());

        final ProceduralInputAlphabet<I> alphabet = sba.getInputAlphabet();
        final Alphabet<I> callAlphabet = alphabet.getCallAlphabet();
        final boolean[] isTerminating = new boolean[callAlphabet.size()];

        for (I i : callAlphabet) {
            isTerminating[callAlphabet.getSymbolIndex(i)] =
                    isTerminating(procedures.get(i), alphabet.getReturnSymbol());
        }

        for (Entry<I, DFA<?, I>> e : procedures.entrySet()) {
            this.pmpgs.put(e.getKey(), new MPGView<>(sba, e.getKey(), e.getValue(), isTerminating));
        }
    }

    @Override
    public Map<I, ProceduralModalProcessGraph<?, I, ?, Void, ?>> getPMPGs() {
        return this.pmpgs;
    }

    @Override
    public @Nullable I getMainProcess() {
        return this.sba.getInitialProcedure();
    }

    private static <S, I> boolean isTerminating(DFA<S, I> dfa, I returnSymbol) {

        for (S s : dfa) {
            final S succ = dfa.getSuccessor(s, returnSymbol);
            if (succ != null && dfa.isAccepting(succ)) {
                return true;
            }
        }

        return false;
    }

    static <S, I> boolean acceptsOnlyEpsilon(DFA<S, I> dfa, Alphabet<I> alphabet) {

        for (S s : dfa) {
            for (I i : alphabet) {
                final S succ = dfa.getSuccessor(s, i);
                if (succ != null && dfa.isAccepting(succ)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static class MPGView<S, I>
            implements ProceduralModalProcessGraph<S, I, PMPGEdge<I, S>, Void, ProceduralModalEdgeProperty> {

        private static final Object INITIAL = new Object();
        private static final Object FINAL = new Object();
        private static final Object NON_TERM_FINAL = new Object();

        private final ProceduralInputAlphabet<I> alphabet;
        private final Collection<I> proceduralInputs;
        private final I procedure;
        private final DFA<S, I> dfa;
        private final S dfaInit;
        private final boolean[] isTerminating;

        private final boolean isNonTerminating;
        private final boolean isEpsilon;

        private final S initialNode;
        private final S finalNode;
        private final S nonTermFinalNode;
        private final List<S> nodes;

        // we make sure to handle 'init' correctly
        @SuppressWarnings("unchecked")
        MPGView(SBA<?, I> sba, I procedure, DFA<S, I> dfa, boolean[] isTerminating) {

            final S dfaInit = dfa.getInitialState();
            if (dfaInit == null) {
                throw new IllegalArgumentException("Empty DFAs cannot be mapped to ModalProcessGraphs");
            }

            this.alphabet = sba.getInputAlphabet();
            this.proceduralInputs = sba.getProceduralInputs();
            this.procedure = procedure;
            this.dfa = dfa;
            this.dfaInit = dfaInit;
            this.isTerminating = isTerminating;

            this.isNonTerminating = !isTerminating[alphabet.getCallSymbolIndex(procedure)];
            this.isEpsilon = acceptsOnlyEpsilon(dfa, alphabet);

            this.initialNode = (S) INITIAL;
            this.nonTermFinalNode = (S) NON_TERM_FINAL;

            this.nodes = new ArrayList<>(dfa.size() + 1);
            this.nodes.add(this.initialNode);

            if (isNonTerminating) { // there exists no accepting r-successor, so use an artificial final node
                this.finalNode = (S) FINAL;
                this.nodes.add(this.finalNode);
            } else { // there must exist at least one accepting r-successor that we can use as the final node
                S finalNode = null;
                for (S s : dfa) {
                    final S rSucc = dfa.getSuccessor(s, alphabet.getReturnSymbol());
                    if (rSucc != null && dfa.isAccepting(rSucc)) {
                        finalNode = rSucc;
                        break;
                    }
                }

                assert finalNode != null;
                this.finalNode = finalNode;
            }

            // if we have at least one call to a non-terminating procedure, we need an additional node to return to but
            // not to progress from
            outer:
            for (S s : dfa) {
                for (I i : alphabet.getCallAlphabet()) {
                    if (!isTerminating[alphabet.getCallSymbolIndex(i)]) {
                        final S succ = dfa.getSuccessor(s, i);
                        if (succ != null && dfa.isAccepting(succ)) {
                            this.nodes.add(this.nonTermFinalNode);
                            break outer;
                        }
                    }
                }
            }

            if (!isEpsilon) {
                for (S s : dfa) {
                    if (dfa.isAccepting(s)) {
                        this.nodes.add(s);
                    }
                }
            }
        }

        @Override
        public Collection<PMPGEdge<I, S>> getOutgoingEdges(S node) {
            if (node == initialNode) {
                return isEpsilon ?
                        Collections.singletonList(new PMPGEdge<>(procedure, finalNode, ProceduralType.INTERNAL)) :
                        Collections.singletonList(new PMPGEdge<>(procedure, dfaInit, ProceduralType.INTERNAL));
            } else if (node == finalNode || node == nonTermFinalNode) {
                return Collections.emptyList();
            } else {
                final List<PMPGEdge<I, S>> result =
                        new ArrayList<>(proceduralInputs.size() * (isNonTerminating ? 2 : 1));

                for (I i : proceduralInputs) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null && this.dfa.isAccepting(succ)) {

                        final ProceduralType type;

                        if (alphabet.isCallSymbol(i)) {
                            type = ProceduralType.PROCESS;

                            if (!isTerminating[alphabet.getCallSymbolIndex(i)]) {
                                result.add(new PMPGEdge<>(i, nonTermFinalNode, type));
                            } else {
                                result.add(new PMPGEdge<>(i, succ, type));
                            }
                        } else {
                            type = ProceduralType.INTERNAL;

                            result.add(new PMPGEdge<>(i, succ, type));
                        }

                        if (isNonTerminating) {
                            result.add(new PMPGEdge<>(i, finalNode, type));
                        }
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
            return Collections.unmodifiableList(this.nodes);
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
