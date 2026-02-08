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
package net.automatalib.serialization.dot;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.MutableProceduralModalProcessGraph;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.graph.concept.FinalNode;
import net.automatalib.graph.impl.DefaultCFMPS;
import net.automatalib.serialization.ModelDeserializer;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import net.automatalib.visualization.VisualizationHelper.PMPGEdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.PMPGNodeAttrs;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parses a DOT file that defines an {@link ContextFreeModalProcessSystem}.
 * <p>
 * Besides the typical structure of DOT files, this parser expects/supports the following attributes present in order to
 * correctly parse the semantics of {@link ContextFreeModalProcessSystem}s:
 * <ul>
 *     <li>nodes must provide a {@value PMPGNodeAttrs#PROCESS} attribute that denotes the process the node belongs to</li>
 *     <li>nodes may provide a {@value PMPGNodeAttrs#MAIN} attribute that denotes whether their respective process is the {@link ContextFreeModalProcessSystem#getMainProcess() main process}</li>
 *     <li>nodes may provide a {@value PMPGNodeAttrs#FINAL} attribute to denote their {@link FinalNode final} state</li>
 *     <li>edges must provide {@value PMPGEdgeAttrs#MODALITY} and {@value PMPGEdgeAttrs#PROCEDURALITY} attributes to denote their {@link ModalType} and {@link ProceduralType}</li>
 * </ul>
 *
 * @param <N>
 *         the node type
 * @param <L>
 *         the label type
 * @param <E>
 *         the edge type
 * @param <AP>
 *         the atomic proposition type
 * @param <TP>
 *         the transition property type
 * @param <P>
 *         the type of internal {@link ProceduralModalProcessGraph}s
 */
public class DOTCFMPSParser<N, L, E, AP, TP extends MutableProceduralModalEdgeProperty, P extends MutableProceduralModalProcessGraph<N, L, E, AP, TP>>
        implements ModelDeserializer<ContextFreeModalProcessSystem<L, AP>> {

    private final Function<L, P> creator;
    private final Function<Map<String, String>, Set<AP>> apParser;
    private final Function<Map<String, String>, @Nullable L> labelParser;
    private final Function<Map<String, String>, L> processParser;
    private final Function<Map<String, String>, TP> tpParser;
    private final String initialNodePrefix;
    private final boolean fakeInitialNodeIds;

    public DOTCFMPSParser(Function<L, P> creator,
                          Function<Map<String, String>, Set<AP>> apParser,
                          Function<Map<String, String>, @Nullable L> labelParser,
                          Function<Map<String, String>, L> processParser,
                          Function<Map<String, String>, TP> tpParser,
                          String initialNodePrefix,
                          boolean fakeInitialNodeIds) {
        this.creator = creator;
        this.apParser = apParser;
        this.labelParser = labelParser;
        this.processParser = processParser;
        this.tpParser = tpParser;
        this.initialNodePrefix = initialNodePrefix;
        this.fakeInitialNodeIds = fakeInitialNodeIds;
    }

    @Override
    public ContextFreeModalProcessSystem<L, AP> readModel(InputStream is) throws IOException, FormatException {

        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            InternalDOTParser parser = new InternalDOTParser(r);
            parser.parse();

            final Map<L, P> pmpgs = new HashMap<>();
            final L mainLabel = parseNodesAndEdges(parser, pmpgs);

            return new DefaultCFMPS<>(mainLabel, pmpgs);
        }
    }

    private L parseNodesAndEdges(InternalDOTParser parser, Map<L, P> out) throws FormatException {
        final Collection<Node> nodes = parser.getNodes();
        final Collection<Edge> edges = parser.getEdges();

        final Map<String, N> stateMap = new HashMap<>(HashUtil.capacity(nodes.size()));
        final Map<String, L> labelMap = new HashMap<>(HashUtil.capacity(nodes.size()));
        L mainLabel = null;

        for (Node node : nodes) {
            if (!fakeInitialNodeIds || !node.id.startsWith(initialNodePrefix)) {
                L label = processParser.apply(node.attributes);
                P pmpg = out.computeIfAbsent(label, creator);
                N n = pmpg.addNode(apParser.apply(node.attributes));

                if (!fakeInitialNodeIds && node.id.startsWith(initialNodePrefix)) {
                    pmpg.setInitialNode(n);
                }
                if (node.attributes.containsKey(PMPGNodeAttrs.FINAL)) {
                    pmpg.setFinalNode(n);
                }
                if (node.attributes.containsKey(PMPGNodeAttrs.MAIN)) {
                    if (mainLabel == null) {
                        mainLabel = label;
                    } else if (!Objects.equals(mainLabel, label)) {
                        throw new FormatException("multiple main labels are not allowed");
                    }
                }

                stateMap.put(node.id, n);
                labelMap.put(node.id, label);
            }
        }

        for (Edge edge : edges) {
            final L srcLabel = labelMap.get(edge.src);
            final L tgtLabel = labelMap.get(edge.tgt);

            if (fakeInitialNodeIds && edge.src.startsWith(initialNodePrefix)) {
                @SuppressWarnings("nullness") // we iterated over all nodes
                final @NonNull P pmpg = out.get(tgtLabel);
                @SuppressWarnings("nullness") // we iterated over all nodes
                final @NonNull N node = stateMap.get(edge.tgt);
                pmpg.setInitialNode(node);
            } else {
                if (!Objects.equals(srcLabel, tgtLabel)) {
                    throw new FormatException("edges connect nodes across different processes");
                }

                @SuppressWarnings("nullness") // we iterated over all nodes
                final @NonNull P pmpg = out.get(srcLabel);
                @SuppressWarnings("nullness") // we iterated over all nodes
                final @NonNull N src = stateMap.get(edge.src);
                @SuppressWarnings("nullness") // we iterated over all nodes
                final @NonNull N tgt = stateMap.get(edge.tgt);
                final E e = pmpg.connect(src, tgt, tpParser.apply(edge.attributes));
                final L l = labelParser.apply(edge.attributes);
                if (l != null) {
                    pmpg.setEdgeLabel(e, l);
                }
            }
        }

        if (mainLabel == null) {
            throw new FormatException("main label missing");
        }

        return mainLabel;

    }

    /**
     * Reads the {@value PMPGNodeAttrs#LABEL} attribute from the given map, splits the string at {@code ,} and applies
     * the given parser to each element individually.
     *
     * @param attr
     *         the attribute map (of a node)
     * @param parser
     *         the parser of individual elements
     * @param <L>
     *         label type
     *
     * @return the union of all parsed labels
     */
    public static <L> Set<L> parseLabelAsProperties(Map<String, String> attr, Function<String, L> parser) {
        String aps = attr.get(PMPGNodeAttrs.LABEL);

        if (aps == null || aps.isEmpty()) {
            return Collections.emptySet();
        }

        if (aps.startsWith("[") && aps.endsWith("]")) { // toString from collections
            aps = aps.substring(1, aps.length() - 1);
        }

        final String[] tokens = aps.split(",");
        final Set<L> result = new HashSet<>(HashUtil.capacity(tokens.length));

        for (String t : tokens) {
            result.add(parser.apply(t.trim()));
        }

        return result;
    }
}
