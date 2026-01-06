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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MutableMMLT;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.exception.FormatException;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.MMLTEdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.MMLTNodeAttrs;

/**
 * Parses a DOT file that defines an {@link MMLT}.
 * <p>Expected syntax:</p>
 * <ul>
 *   <li>Mealy labels: {@code input/output}</li>
 *   <li>Timeout input: {@code to[x]} where {@code x} is the name of a local timer</li>
 *   <li>Local timers: node attribute {@value MMLTNodeAttrs#TIMERS} with comma-separated assignments {@code x=t} with {@code t > 0}.
 *       Timer names must be unique per location. For one-shot timers choose values such that they never expire at the
 *       same time as another local timer.</li>
 *   <li>Reset behavior: edge attribute {@value MMLTEdgeAttrs#RESETS} as specified below.</li>
 *   <li>Timer outputs: a timer can produce multiple outputs at timeout. The output must be formatted according
 *   to the {@link SymbolCombiner} used for parsing. These outputs must not be empty and must not contain
 *   a silent output.</li>
 * </ul>
 * <p>Resets:</p>
 * <ul>
 *   <li>If the input is a timeout symbol:
 *     <ul>
 *       <li>Attribute omitted: periodic if self-loop, one-shot otherwise.</li>
 *       <li>Self-loop and value is list of all local timers: one-shot timer.</li>
 *       <li>Self-loop and value is the name of the timed-out timer: periodic.</li>
 *       <li>Any other value: invalid.</li>
 *     </ul>
 *   </li>
 *   <li>If the input is a normal input and the edge is a self-loop:
 *     <ul>
 *       <li>Attribute omitted: regular edge.</li>
 *       <li>Value is list of all local timers: local reset.</li>
 *       <li>Any other value: invalid.</li>
 *     </ul>
 *     If the edge is not a self-loop, reset values are ignored.
 *   </li>
 * </ul>
 * <p>Notes:</p>
 * <ul>
 *   <li>It is currently not possible to define a location with a single timer that is one-shot and whose timeout
 *       causes a self-loop. Such a timer is always considered periodic. This is semantically equivalent for learning,
 *       but hypotheses may still use the former variant; those timers may be highlighted for debugging.</li>
 *   <li>Edges with a timeout input must not be silent.</li>
 * </ul>
 * <p>Example DOT:</p>
 * <pre><code>
 * digraph g {
 *    s0 [label="L0" timers="a=2"]
 *    s1 [label="L1" timers="b=4,c=6"]
 *    s2 [label="L2" timers="d=2,e=3"]
 *
 *    s0 -> s1 [label="to[a] / A"] // one-shot with location change
 *    s1 -> s1 [label="to[b] / B|Z"] // periodic with multiple outputs (assuming a {@link StringSymbolCombiner} to separate outputs)
 *    s1 -> s1 [label="to[c] / C" resets="b,c"] // one-shot with loop
 *
 *    s2 -> s2 [label="to[d] / D" resets="d"] // periodic with explicit resets
 *    s2 -> s2 [label="to[e] / E"] // periodic
 *
 *    s1 -> s2 [label="x / void"]
 *    s1 -> s1 [label="y / Y" resets="b,c"] // loop with reset
 *    s2 -> s2 [label="y / D"] // loop without reset
 *
 *    __start0 [label="" shape="none" width="0" height="0"];
 *    __start0 -> s0;
 * }
 * </code></pre>
 */
public class DOTMMLTParser<S, I, O, A extends MutableMMLT<S, I, ?, O>> implements DOTInputModelDeserializer<S, I, A> {

    private static final Pattern ASSIGN_PATTERN = Pattern.compile("(\\S+)=(\\d+)");

    private final AutomatonCreator<A, I> creator;
    private final Function<String, I> inputParser;
    private final Function<String, List<O>> outputParser;
    private final Collection<String> initialNodeIds;
    private final boolean fakeInitialNodeIds;

    public DOTMMLTParser(AutomatonCreator<A, I> creator,
                         Function<String, I> inputParser,
                         Function<String, List<O>> outputParser,
                         Collection<String> initialNodeIds,
                         boolean fakeInitialNodeIds) {
        this.creator = creator;
        this.inputParser = inputParser;
        this.outputParser = outputParser;
        this.initialNodeIds = initialNodeIds;
        this.fakeInitialNodeIds = fakeInitialNodeIds;
    }

    @Override
    public DOTInputModelData<S, I, A> readModel(InputStream is) throws IOException, FormatException {

        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            InternalDOTParser parser = new InternalDOTParser(r);
            parser.parse();

            assert parser.isDirected();

            final Set<I> inputs = new HashSet<>();

            for (Edge edge : parser.getEdges()) {
                if (!fakeInitialNodeIds || !initialNodeIds.contains(edge.src)) {
                    final String input = tokenizeLabel(edge)[0].trim();
                    if (!input.startsWith("to[")) {
                        inputs.add(inputParser.apply(input));
                    }
                }
            }

            final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);
            final A automaton = creator.createAutomaton(alphabet, parser.getNodes().size());

            final Mapping<S, String> labels = parseNodesAndEdges(parser, automaton);

            return new DOTInputModelData<>(automaton, alphabet, labels);
        }
    }

    private Mapping<S, String> parseNodesAndEdges(InternalDOTParser parser, MutableMMLT<S, I, ?, O> result)
            throws FormatException {

        final Collection<Node> nodes = parser.getNodes();
        final Collection<Edge> edges = parser.getEdges();

        final Map<String, Map<String, TimerSpec>> timers = new HashMap<>(nodes.size() - 1); // id in dot -> local timers
        final Map<String, S> stateMap = new HashMap<>(nodes.size() - 1); // name in dot -> new id
        final MutableMapping<S, String> mapping = result.createDynamicStateMapping();

        // Parse nodes:
        for (Node node : nodes) {
            final S n;

            if (fakeInitialNodeIds && initialNodeIds.contains(node.id)) {
                continue;
            } else if (!fakeInitialNodeIds && initialNodeIds.contains(node.id)) {
                n = result.addInitialState();
            } else {
                n = result.addState();
            }

            stateMap.put(node.id, n);
            mapping.put(n, node.id);

            // Parse timers:
            final String timersAttr = node.attributes.get(MMLTNodeAttrs.TIMERS);
            if (timersAttr != null) {
                String[] settings = timersAttr.split(",");
                for (String setting : settings) {
                    Matcher m = ASSIGN_PATTERN.matcher(setting.trim()); // remove whitespace
                    if (!m.matches()) {
                        continue;
                    }
                    String g1 = m.group(1);
                    String g2 = m.group(2);

                    assert g1 != null && g2 != null;

                    String timerName = g1.trim();
                    int value = Integer.parseInt(g2);

                    if (value <= 0) {
                        throw new FormatException(String.format(
                                "Reset for timer %s in location %s must be greater zero.",
                                timerName,
                                node.id));
                    }

                    Map<String, TimerSpec> timeInfo = timers.computeIfAbsent(node.id, k -> new HashMap<>());
                    if (timeInfo.containsKey(timerName)) {
                        throw new FormatException(String.format("Timer %s in location %s must only be set once.",
                                                                timerName,
                                                                node.id));
                    }

                    // Add timer:
                    timeInfo.put(timerName, new TimerSpec(timerName, value));
                }
            } else {
                timers.put(node.id, Collections.emptyMap()); // no timers in this location
            }
        }

        // Parse edges:
        for (Edge edge : edges) {

            if (fakeInitialNodeIds && initialNodeIds.contains(edge.src)) {
                result.setInitial(stateMap.get(edge.tgt), true);
                continue;
            }

            // Check for resets:
            Set<String> edgeResets = new HashSet<>();
            String resetAttr = edge.attributes.get(MMLTEdgeAttrs.RESETS);
            if (resetAttr != null) {
                // Parse resets:
                for (String timer : resetAttr.split(",")) {
                    edgeResets.add(timer.strip());
                }
            }

            final String[] tokens = tokenizeLabel(edge);
            final String input = tokens[0].trim();
            final String output = tokens[1].trim();

            if (input.startsWith("to[")) {
                // Ensure that we defined the corresponding timer:
                String timerName = input.substring(3, input.length() - 1);
                if (!timers.getOrDefault(edge.src, Collections.emptyMap()).containsKey(timerName)) {
                    throw new FormatException(String.format("Defined %s in state %s, but timer value is not set.",
                                                            input,
                                                            edge.src));
                }

                // Add output to timer info:
                final TimerSpec oldInfo = timers.getOrDefault(edge.src, Collections.emptyMap()).get(timerName);
                assert oldInfo != null;

                // Infer timer type:
                final long initial = oldInfo.initial();
                boolean periodic = true;
                if (edge.src.equals(edge.tgt)) {
                    if (edgeResets.size() == 1) {
                        if (!edgeResets.contains(timerName)) {
                            // Invalid periodic timer:
                            throw new FormatException(String.format("Invalid reset at to[%s]", timerName));
                        }
                    } else if (edgeResets.size() > 1) {
                        // Need to contain all local timers to be one-shot with loop:
                        Set<String> targetTimers = timers.getOrDefault(edge.tgt, Collections.emptyMap()).keySet();
                        if (!edgeResets.equals(targetTimers)) {
                            throw new FormatException(String.format("Invalid reset at to[%s]", timerName));
                        }
                        periodic = false;
                    }
                } else {
                    // No need to check resets on location-change: always resetting all in target
                    periodic = false;
                }

                final List<O> outputs = outputParser.apply(output);

                // Add timer to location:
                if (periodic) {
                    result.addPeriodicTimer(stateMap.get(edge.src), timerName, initial, outputs);
                } else {
                    result.addOneShotTimer(stateMap.get(edge.src), timerName, initial, outputs, stateMap.get(edge.tgt));
                }
            } else {
                // Non-delaying input:
                final I i = inputParser.apply(input);
                final List<O> outputs = outputParser.apply(output);
                assert outputs.size() == 1;

                result.addTransition(stateMap.get(edge.src), i, stateMap.get(edge.tgt), outputs.get(0));

                // Parse resets of self-loops with untimed input:
                if (edge.src.equals(edge.tgt) && !edgeResets.isEmpty()) {
                    // Reset list needs to contain all local timers:
                    Set<String> targetTimers = timers.getOrDefault(edge.tgt, Collections.emptyMap()).keySet();
                    if (!edgeResets.equals(targetTimers)) {
                        throw new FormatException(String.format("Invalid local reset at %s", i));
                    }
                    result.addLocalReset(stateMap.get(edge.src), i);
                }
            }
        }

        return mapping;
    }

    private static String[] tokenizeLabel(Edge edge) throws FormatException {
        final String label = edge.attributes.get(EdgeAttrs.LABEL);

        if (label == null) {
            throw new FormatException("All edges must have an input and an output.");
        }

        final String[] tokens = label.split("/");

        if (tokens.length != 2) {
            throw new FormatException("All edges must have an input and an output.");
        }

        return tokens;
    }

    private record TimerSpec(String name, long initial) {}

}
