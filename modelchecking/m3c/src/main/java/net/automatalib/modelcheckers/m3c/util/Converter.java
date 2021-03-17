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
package net.automatalib.modelcheckers.m3c.util;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.EdgeType;
import net.automatalib.modelcheckers.m3c.cfps.ProceduralProcessGraph;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.cfps.StateClass;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.words.Alphabet;

public final class Converter {

    private Converter() {
        // prevent instantiation
    }

    public static <L, AP> CFPS toCFPS(ModalContextFreeProcessSystem<L, AP> mcfps) {

        final CFPS cfps = new CFPS();

        for (Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> entry : mcfps.getMPGs().entrySet()) {
            final L process = entry.getKey();
            final ModalProcessGraph<?, L, ?, AP, ?> mpg = entry.getValue();
            final boolean isInitial = Objects.equals(process, mcfps.getMainProcess());

            final ProceduralProcessGraph ppg = toPPG(cfps, process, mcfps.getProcessAlphabet(), mpg);

            cfps.addPPG(ppg);

            if (isInitial) {
                cfps.setMainGraph(ppg);
            }
        }

        return cfps;
    }

    public static <N, L, E, AP, TP extends ModalEdgeProperty> ProceduralProcessGraph toPPG(CFPS cfps,
                                                                                           L process,
                                                                                           Alphabet<L> proceduralAlphabet,
                                                                                           ModalProcessGraph<N, L, E, AP, TP> mpg) {

        final String pLabel = Objects.toString(process);
        final ProceduralProcessGraph ppg = new ProceduralProcessGraph(pLabel);

        final State start = cfps.createAndAddState(ppg, StateClass.START).withName("start_" + process);
        final State endState = cfps.createAndAddState(ppg, StateClass.END).withName("end_" + process);

        final NodeIDs<N> nodesIDs = mpg.nodeIDs();
        final MutableMapping<N, State> nodeMapping = mpg.createStaticNodeMapping();

        // states
        for (N n : mpg) {
            if (Objects.equals(mpg.getInitialNode(), n)) {
                nodeMapping.put(n, start);
            } else if (Objects.equals(mpg.getFinalNode(), n)) {
                nodeMapping.put(n, endState);
            } else {
                final int id = nodesIDs.getNodeId(n);
                final State state = cfps.createAndAddState(ppg, StateClass.NORMAL)
                                        .withName(pLabel + id)
                                        .withStateLabels(toNodeLabel(mpg, n));

                nodeMapping.put(n, state);
            }
        }

        // edges
        for (N n : mpg) {
            final State state = nodeMapping.get(n);

            for (E edge : mpg.getOutgoingEdges(n)) {
                final L label = mpg.getEdgeLabel(edge);
                final N target = mpg.getTarget(edge);

                final State stateSucc = nodeMapping.get(target);
                final EdgeType type;

                if (mpg.getEdgeProperty(edge).isMayOnly()) {
                    if (proceduralAlphabet.containsSymbol(label)) {
                        type = EdgeType.MAY_PROCESS;
                    } else {
                        type = EdgeType.MAY;
                    }
                } else {
                    if (proceduralAlphabet.containsSymbol(label)) {
                        type = EdgeType.MUST_PROCESS;
                    } else {
                        type = EdgeType.MUST;
                    }
                }

                state.addEdge(new Edge(state, stateSucc, label.toString(), type));
            }

        }

        return ppg;
    }

    private static <N, AP> String toNodeLabel(ModalProcessGraph<N, ?, ?, AP, ?> mpg, N node) {
        final StringJoiner sj = new StringJoiner(",");
        mpg.getAtomicPropositions(node).forEach(ap -> sj.add(Objects.toString(ap)));
        return sj.toString();
    }
}
