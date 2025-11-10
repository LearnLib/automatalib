package net.automatalib.serialization.dot;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.MapAlphabet;
import net.automatalib.automaton.mmlt.MMLTCreator;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.automaton.mmlt.MutableMMLT;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.exception.FormatException;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.SymbolicInput;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.MMLTEdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.MMLTNodeAttrs;

/**
 * Parses a DOT file that defines an MMLT automaton.
 * <p>Expected syntax:</p>
 * <ul>
 *   <li>Mealy labels: <code>input/output</code></li>
 *   <li>Initial node marker: <code>__start0</code></li>
 *   <li>Timeout input: <code>to[x]</code> where <code>x</code> is the name of a local timer</li>
 *   <li>Local timers: node attribute <code>timers</code> with comma-separated assignments <code>x=t</code> with <code>t &gt; 0</code>.
 *       Timer names must be unique per location. For one-shot timers choose values such that they never expire at the
 *       same time as another local timer.</li>
 *   <li>Reset behavior: edge attribute <code>resets</code> as specified below.</li>
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
 *       but hypotheses may still use the former variant; those timers may be highlighted specially for debugging.</li>
 *   <li>Edges with a timeout input must not be silent.</li>
 * </ul>
 * <p>Example DOT:</p>
 * <pre>{@code
 * digraph g {
 *    s0 [label="L0" timers="a=2"]
 *    s1 [label="L1" timers="b=4,c=6"]
 *    s2 [label="L2" timers="d=2,e=3"]
 *
 *    s0 -> s1 [label="to[a] / A"] // one-shot with location change
 *    s1 -> s1 [label="to[b] / B"] // periodic
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
 * }</pre>
 */

public class DOTMMLTParser<S, I, O, A extends MutableMMLT<S, I, ?, O>>
        implements DOTInputModelDeserializer<S, SymbolicInput<I>, A> {

    private static final Pattern assignPattern = Pattern.compile("(\\S+)=(\\d+)");

    private final MMLTCreator<A, I, O> creator;
    private final Function<String, InputSymbol<I>> inputParser;
    private final Function<String, O> outputParser;
    private final O silentSymbol;
    private final SymbolCombiner<O> outputCombiner;
    private final Collection<String> initialNodeIds;
    private final boolean fakeInitialNodeIds;

    public DOTMMLTParser(MMLTCreator<A, I, O> creator,
                         Function<String, InputSymbol<I>> inputParser,
                         Function<String, O> outputParser,
                         O silentOutput,
                         SymbolCombiner<O> outputCombiner,
                         Collection<String> initialNodeIds,
                         boolean fakeInitialNodeIds) {
        this.creator = creator;
        this.inputParser = inputParser;
        this.outputParser = outputParser;
        this.silentSymbol = silentOutput;
        this.outputCombiner = outputCombiner;
        this.initialNodeIds = initialNodeIds;
        this.fakeInitialNodeIds = fakeInitialNodeIds;
    }

    @Override
    public DOTInputModelData<S, SymbolicInput<I>, A> readModel(InputStream is) throws IOException, FormatException {

        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            InternalDOTParser parser = new InternalDOTParser(r);
            parser.parse();

            assert parser.isDirected();

            final Set<InputSymbol<I>> inputs = new HashSet<>();

            for (Edge edge : parser.getEdges()) {
                if (!fakeInitialNodeIds || !initialNodeIds.contains(edge.src)) {
                    final String input = tokenizeLabel(edge)[0].trim();
                    if (!input.startsWith("to[")) {
                        inputs.add(inputParser.apply(input));
                    }
                }
            }

            final Alphabet<InputSymbol<I>> alphabet = Alphabets.fromCollection(inputs);
            final A automaton = creator.createMMLT(alphabet, parser.getNodes().size(), silentSymbol, outputCombiner);

            final Mapping<S, String> labels = parseNodesAndEdges(parser, automaton);

            return new DOTInputModelData<>(automaton, new MapAlphabet<>(alphabet), labels);
        }
    }

    private Mapping<S, String> parseNodesAndEdges(InternalDOTParser parser, MutableMMLT<S, I, ?, O> result) {

        final Collection<Node> nodes = parser.getNodes();
        final Collection<Edge> edges = parser.getEdges();

        final Map<String, Map<String, MealyTimerInfo<O>>> timers =
                new HashMap<>(nodes.size() - 1); // id in dot -> local timers
        final Map<String, S> stateMap = new HashMap<>(nodes.size() - 1); // name in dot -> new id
        final MutableMapping<S, String> mapping = result.createDynamicStateMapping();

        // Parse nodes:
        for (var node : nodes) {
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
                    Matcher m = assignPattern.matcher(setting.trim()); // remove whitespace
                    if (!m.matches()) {
                        continue;
                    }
                    String timerName = m.group(1).trim();
                    int value = Integer.parseInt(m.group(2));
                    if (value <= 0) {
                        throw new IllegalArgumentException(String.format(
                                "Reset for timer %s in location %s must be greater zero.",
                                timerName,
                                node.id));
                    }

                    Map<String, MealyTimerInfo<O>> timeInfo = timers.computeIfAbsent(node.id, k -> new HashMap<>());
                    if (timeInfo.containsKey(timerName)) {
                        throw new IllegalArgumentException(String.format(
                                "Timer %s in location %s must only be set once.",
                                timerName,
                                node.id));
                    }

                    // Add timer:
                    timeInfo.put(timerName, new MealyTimerInfo<>(timerName, value, null));
                }
            } else {
                timers.put(node.id, Collections.emptyMap()); // no timers in this location
            }
        }

        // Parse edges:
        for (var edge : edges) {

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
                if (!timers.get(edge.src).containsKey(timerName)) {
                    throw new IllegalArgumentException(String.format(
                            "Defined %s in state %s, but timer value is not set.",
                            input,
                            edge.src));
                }

                // Add output to timer info:
                final MealyTimerInfo<O> oldInfo = timers.get(edge.src).get(timerName);

                // Infer timer type:
                final long initial = oldInfo.initial();
                boolean periodic = true;
                if (edge.src.equals(edge.tgt)) {
                    if (edgeResets.size() == 1) {
                        if (!edgeResets.contains(timerName)) {
                            // Invalid periodic timer:
                            throw new IllegalArgumentException(String.format("Invalid reset at to[%s]", timerName));
                        }
                    } else if (edgeResets.size() > 1) {
                        // Need to contain all local timers to be one-shot with loop:
                        for (var locTimer : timers.get(edge.tgt).keySet()) {
                            if (!edgeResets.contains(locTimer)) {
                                throw new IllegalArgumentException(String.format("Invalid reset at to[%s]", timerName));
                            }
                        }
                        periodic = false;
                    }
                } else {
                    // No need to check resets on location-change: always resetting all in target
                    periodic = false;
                }

                final O o = outputParser.apply(output);

                // Add timer to location:
                if (periodic) {
                    result.addPeriodicTimer(stateMap.get(edge.src), timerName, initial, o);
                } else {
                    result.addOneShotTimer(stateMap.get(edge.src), timerName, initial, o, stateMap.get(edge.tgt));
                }
            } else {
                // Non-delaying input:
                final InputSymbol<I> i = inputParser.apply(input);
                final O o = outputParser.apply(output);

                result.addTransition(stateMap.get(edge.src), i, stateMap.get(edge.tgt), o);

                // Parse resets of self-loops with untimed input:
                if (edge.src.equals(edge.tgt) && !edgeResets.isEmpty()) {
                    // Reset list needs to contain all local timers:
                    for (var locTimer : timers.get(edge.tgt).keySet()) {
                        if (!edgeResets.contains(locTimer)) {
                            throw new IllegalArgumentException(String.format("Invalid local reset at %s", i));
                        }
                    }
                    result.addLocalReset(stateMap.get(edge.src), i);
                }
            }
        }

        return mapping;
    }

    private static String[] tokenizeLabel(Edge edge) {
        final String label = edge.attributes.get(EdgeAttrs.LABEL);

        if (label == null) {
            throw new IllegalArgumentException("All edges must have an input and an output.");
        }

        final String[] tokens = label.split("/");

        if (tokens.length != 2) {
            throw new IllegalArgumentException("All edges must have an input and an output.");
        }

        return tokens;
    }

}
