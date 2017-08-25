/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.AutomataLibSettings;
import net.automatalib.automata.Automaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.graphs.dot.AggregateDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;

public final class Visualization {

    private static final Visualization INSTANCE = new Visualization();
    private final VPManager manager = new VPManager();
    private final VisualizationProvider provider;

    private Visualization() {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        String providerId = settings.getProperty("visualization.provider");
        VisualizationProvider vp = null;

        manager.load();

        if (providerId != null) {
            vp = manager.getProviderByName(providerId);
        }

        if (vp == null) {
            vp = manager.getBestProvider();
        }

        if (vp == null) {
            System.err.println("Error setting visualization provider, defaulting to dummy provider...");
        }

        provider = vp;
    }

    @SafeVarargs
    public static <N, E> void visualizeGraph(Graph<N, E> graph,
                                             boolean modal,
                                             GraphDOTHelper<N, ? super E>... addlHelpers) {
        INSTANCE.visualize(graph, modal, addlHelpers);
    }

    public static void visualize(GraphViewable gv, boolean modal) {
        visualizeGraph(gv.graphView(), modal);
    }

    @SafeVarargs
    public final <N, E> void visualize(Graph<N, E> graph, boolean modal, GraphDOTHelper<N, ? super E>... addlHelpers) {
        List<GraphDOTHelper<N, ? super E>> helpers = new ArrayList<>(addlHelpers.length + 1);
        helpers.add(graph.getGraphDOTHelper());
        for (GraphDOTHelper<N, ? super E> h : addlHelpers) {
            helpers.add(h);
        }
        GraphDOTHelper<N, E> aggHelper = new AggregateDOTHelper<>(helpers);

        visualize(graph, aggHelper, modal, Collections.emptyMap());
    }

    public <N, E> void visualize(Graph<N, E> graph,
                                 GraphDOTHelper<N, ? super E> helper,
                                 boolean modal,
                                 Map<String, String> options) {
        provider.visualize(graph, helper, modal, options);
    }

    @SafeVarargs
    public static <S, I, T> void visualizeAutomaton(Automaton<S, I, T> automaton,
                                                    Collection<? extends I> inputs,
                                                    boolean modal,
                                                    GraphDOTHelper<S, TransitionEdge<I, T>>... addlHelpers) {
        visualizeGraph(automaton.transitionGraphView(inputs), modal, addlHelpers);
    }

}
