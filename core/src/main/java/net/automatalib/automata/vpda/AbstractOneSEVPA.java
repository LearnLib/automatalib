/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.automata.vpda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.Graph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.VPDAlphabet;

/**
 * Abstract class for 1-SEVPAs that implements functionality shared across different subtypes.
 *
 * @param <L>
 *         location type
 * @param <I>
 *         input alphabet type
 *
 * @author Malte Isberner
 */
public abstract class AbstractOneSEVPA<L, I> implements OneSEVPA<L, I>, Graph<L, AbstractOneSEVPA.SevpaViewEdge<L, I>> {

    protected final VPDAlphabet<I> alphabet;

    public AbstractOneSEVPA(final VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    public VPDAlphabet<I> getAlphabet() {
        return alphabet;
    }

    @Override
    public State<L> getTransition(final State<L> state, final I input) {
        if (state.isSink()) {
            return State.getSink();
        }

        final L loc = state.getLocation();
        final VPDAlphabet.SymbolType type = alphabet.getSymbolType(input);
        switch (type) {
            case CALL:
                final int newStackElem = encodeStackSym(loc, input);
                return new State<>(getInitialLocation(), StackContents.push(newStackElem, state.getStackContents()));
            case RETURN: {
                final StackContents contents = state.getStackContents();
                if (contents == null) {
                    return State.getSink();
                }
                final int stackElem = contents.peek();
                final L succ = getReturnSuccessor(loc, input, stackElem);
                if (succ == null) {
                    return State.getSink();
                }
                return new State<>(succ, contents.pop());
            }
            case INTERNAL: {
                final L succ = getInternalSuccessor(loc, input);
                if (succ == null) {
                    return State.getSink();
                }
                return new State<>(succ, state.getStackContents());
            }
            default:
                throw new IllegalStateException("Unknown symbol type " + type);
        }
    }

    @Override
    public int encodeStackSym(final L srcLoc, final I callSym) {
        return encodeStackSym(srcLoc, alphabet.getCallSymbolIndex(callSym));
    }

    public int encodeStackSym(final L srcLoc, final int callSymIdx) {
        return alphabet.getNumCalls() * getLocationId(srcLoc) + callSymIdx;
    }

    @Override
    public int getNumStackSymbols() {
        return size() * alphabet.getNumCalls();
    }

    // Explicitly declare method, since multiple interfaces define it
    @Override
    public abstract int size();

    @Override
    public Collection<L> getNodes() {
        return Collections.unmodifiableCollection(getLocations());
    }

    @Override
    public Collection<SevpaViewEdge<L, I>> getOutgoingEdges(final L location) {

        final List<SevpaViewEdge<L, I>> result = new ArrayList<>();

        // all internal transitions
        for (final I i : alphabet.getInternalAlphabet()) {
            final L succ = getInternalSuccessor(location, i);
            if (succ != null) {
                result.add(new SevpaViewEdge<>(i, -1, succ));
            }
        }

        // all return transitions for every possible stack contents
        for (final I i : alphabet.getReturnAlphabet()) {
            for (final L loc : getLocations()) {
                for (final I stackSymbol : alphabet.getCallAlphabet()) {
                    final int sym = encodeStackSym(loc, stackSymbol);
                    final L succ = getReturnSuccessor(location, i, sym);

                    if (succ != null) {
                        result.add(new SevpaViewEdge<>(i, sym, succ));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public L getTarget(final SevpaViewEdge<L, I> edge) {
        return edge.target;
    }

    @Override
    public VisualizationHelper<L, SevpaViewEdge<L, I>> getVisualizationHelper() {
        return new DefaultVisualizationHelper<L, SevpaViewEdge<L, I>>() {

            @Override
            protected Collection<L> initialNodes() {
                return Collections.singleton(getInitialLocation());
            }

            @Override
            public boolean getNodeProperties(final L node, final Map<String, String> properties) {
                super.getNodeProperties(node, properties);

                properties.put(NodeAttrs.SHAPE,
                               isAcceptingLocation(node) ? NodeShapes.DOUBLECIRCLE : NodeShapes.CIRCLE);
                properties.put(NodeAttrs.LABEL, "L" + getLocationId(node));

                return true;
            }

            @Override
            public boolean getEdgeProperties(final L src,
                                             final SevpaViewEdge<L, I> edge,
                                             final L tgt,
                                             final Map<String, String> properties) {

                final I input = edge.input;
                final int stack = edge.stack;

                if (alphabet.isInternalSymbol(input)) {
                    properties.put(EdgeAttrs.LABEL, String.valueOf(input));
                } else if (alphabet.isReturnSymbol(input)) {
                    properties.put(EdgeAttrs.LABEL,
                                   input + "/(L" + getLocationId(getStackLoc(stack)) + ',' + getCallSym(stack) + ')');
                } else {
                    throw new IllegalArgumentException();
                }

                return true;
            }
        };
    }

    public L getStackLoc(final int stackSym) {
        return getLocation(stackSym / alphabet.getNumCalls());
    }

    public I getCallSym(final int stackSym) {
        return alphabet.getCallSymbol(stackSym % alphabet.getNumCalls());
    }

    static class SevpaViewEdge<S, I> {

        private final I input;
        private final int stack;
        private final S target;

        SevpaViewEdge(I input, int stack, S target) {
            this.target = target;
            this.input = input;
            this.stack = stack;
        }
    }
}
