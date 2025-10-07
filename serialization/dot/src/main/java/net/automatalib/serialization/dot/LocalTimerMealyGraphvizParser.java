package net.automatalib.serialization.dot;

import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.impl.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.*;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class LocalTimerMealyGraphvizParser {

    private static final Pattern assignPattern = Pattern.compile("(\\S+)=(\\d+)");

    public static LocalTimerMealy<Integer, String, String> parseLocalTimerMealy(File path, String silentOutput, AbstractSymbolCombiner<String> outputCombiner) {
        InternalDOTParser parser;
        try (InputStream r = IOUtil.asUncompressedBufferedInputStream(new FileInputStream(path))) {
            parser = new InternalDOTParser(r);
            parser.parse();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Parsing \"%s\" failed: %n %s", path, e));
        }
        return parseLocalTimerMealy(parser, silentOutput, outputCombiner);
    }

    private static LocalTimerMealy<Integer, String, String> parseLocalTimerMealy(InternalDOTParser parser, String silentOutput, AbstractSymbolCombiner<String> outputCombiner) {
        final Collection<Node> nodes = parser.getNodes();
        final Collection<Edge> edges = parser.getEdges();

        final Map<String, Map<String, MealyTimerInfo<String>>> timers = new HashMap<>(nodes.size() - 1); // id in dot -> local timers
        final Map<String, Integer> stateMap = new HashMap<>(nodes.size() - 1); // name in dot -> new id

        final CompactLocalTimerMealy<String, String> result = new CompactLocalTimerMealy<>(new GrowingMapAlphabet<>(), silentOutput, outputCombiner);

        // Parse nodes:
        for (var node : nodes) {
            if (node.id.equals(GraphDOT.initialLabel(0))) {
                continue; // initial node marker
            }
            final int n = result.addState();
            stateMap.put(node.id, n);

            // Parse timers:
            if (node.attributes.containsKey("timers")) {
                String[] settings = node.attributes.get("timers").split(",");
                for (String setting : settings) {
                    Matcher m = assignPattern.matcher(setting.trim()); // remove whitespace
                    if (!m.matches()) {
                        continue;
                    }
                    String timerName = m.group(1).trim();
                    int value = Integer.parseInt(m.group(2));
                    if (value <= 0) {
                        throw new IllegalArgumentException(String.format("Reset for timer %s in location %s must be greater zero.",
                                timerName, node.id));
                    }

                    timers.putIfAbsent(node.id, new HashMap<>());
                    if (timers.get(node.id).containsKey(timerName)) {
                        throw new IllegalArgumentException(String.format("Timer %s in location %s must only be set once.",
                                timerName, node.id));
                    }

                    // Add timer:
                    timers.get(node.id).put(timerName, new MealyTimerInfo<>(timerName, value, null));
                }
            } else {
                timers.put(node.id, Collections.emptyMap()); // no timers in this location
            }
        }

        // Parse edges:
        for (var edge : edges) {
            // Parse input/output:
            Pair<@Nullable String, @Nullable String> props = DOTParsers.DEFAULT_MEALY_EDGE_PARSER.apply(edge.attributes);
            if (props.getFirst() == null || props.getSecond() == null) {
                if (edge.src.equals(GraphDOT.initialLabel(0))) {
                    result.setInitialState(stateMap.get(edge.tgt));
                    continue;
                }
                throw new IllegalArgumentException("All edges must have an input and an output.");
            }

            // Check for resets:
            HashSet<String> edgeResets = new HashSet<>();
            if (edge.attributes.containsKey("resets")) {
                // Parse resets:
                for (String timer : edge.attributes.get("resets").split(",")) {
                    edgeResets.add(timer.strip());
                }
            }


            if (props.getFirst().startsWith("to[")) {
                // Ensure that we defined the corresponding timer:
                String timerName = props.getFirst().substring(3, props.getFirst().length() - 1);
                if (!timers.get(edge.src).containsKey(timerName)) {
                    throw new IllegalArgumentException(String.format("Defined %s in state %s, but timer value is not set.",
                            props.getFirst(), edge.src));
                }

                // Add output to timer info:
                MealyTimerInfo<String> oldInfo = timers.get(edge.src).get(timerName);

                // Infer timer type:
                var updatedInfo = new MealyTimerInfo<>(timerName, oldInfo.initial(), props.getSecond());
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
                        updatedInfo.setOneShot();
                    }
                } else {
                    // No need to check resets on location-change: always resetting all in target
                    updatedInfo.setOneShot();
                }

                // Add timer to location:
                if (updatedInfo.periodic()) {
                    result.addPeriodicTimer(stateMap.get(edge.src), updatedInfo.name(), updatedInfo.initial(), updatedInfo.output());
                } else {
                    result.addOneShotTimer(stateMap.get(edge.src),
                            updatedInfo.name(), updatedInfo.initial(), updatedInfo.output(),
                            stateMap.get(edge.tgt));
                }
            } else {
                // Non-delaying input:
                var nonDelInput = new NonDelayingInput<>(props.getFirst());

                result.addTransition(stateMap.get(edge.src), nonDelInput, props.getSecond(),
                        stateMap.get(edge.tgt));

                // Parse resets of self-loops with untimed input:
                if (edge.src.equals(edge.tgt) && !edgeResets.isEmpty()) {
                    // Reset list needs to contain all local timers:
                    for (var locTimer : timers.get(edge.tgt).keySet()) {
                        if (!edgeResets.contains(locTimer)) {
                            throw new IllegalArgumentException(String.format("Invalid local reset at %s", nonDelInput));
                        }
                    }
                    result.addLocalReset(stateMap.get(edge.src), nonDelInput);
                }
            }
        }

        // Ensure initial location:
        if (result.getInitialState() == null) {
            throw new IllegalArgumentException("Automaton must have an initial location.");
        }

        return result;
    }

}
