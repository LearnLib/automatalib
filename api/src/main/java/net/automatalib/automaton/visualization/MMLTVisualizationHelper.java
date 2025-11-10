package net.automatalib.automaton.visualization;

import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.symbol.time.SymbolicInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimerTimeoutSymbol;
import net.automatalib.automaton.graph.TransitionEdge;

import java.util.Map;
import java.util.stream.Collectors;

public class MMLTVisualizationHelper<S, I, T, O>
        extends AutomatonVisualizationHelper<S, SymbolicInput<I>, T, MMLT<S, I, T, O>> {

    private final boolean colorEdges;
    private final boolean includeResets;

    /**
     * Creates a new visualization helper for an MMLT. Allows edge coloring and explicit resets in transition labels for
     * easier inspection.
     * <p>
     * If you want to serialize the resulting file to graphviz, disable explicit resets. The parse does not know how to
     * treat the reset information.
     *
     * @param automaton
     *         Automaton
     * @param colorEdges
     *         If set, the transitions for local resets, periodic timers, and one-shot timers are colored differently.
     * @param includeResets
     *         If set, each transition includes a list of timers that it resets.
     */
    public MMLTVisualizationHelper(MMLT<S, I, T, O> automaton,
                                   boolean colorEdges,
                                   boolean includeResets) {
        super(automaton);
        this.colorEdges = colorEdges;
        this.includeResets = includeResets;
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        // Include timer assignments:
        var localTimers = automaton.getSortedTimers(node);
        if (!localTimers.isEmpty()) {
            // Add local timer info:
            String timers = localTimers.stream()
                                       .map(t -> String.format("%s=%d", t.name(), t.initial()))
                                       .sorted()
                                       .collect(Collectors.joining(","));
            properties.put(MMLTNodeAttrs.TIMERS, timers);
        }

        return true;
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<SymbolicInput<I>, T> edge, S tgt, Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);

        final SymbolicInput<I> input = edge.getInput();

        String label = String.format("%s / %s", input, automaton.getTransitionProperty(edge.getTransition()));

        // Infer the label color + reset information for the transition:
        String resetExtraInfo = "";
        String resetInfo = "";
        String edgeColor = "";
        if (edge.getInput() instanceof TimerTimeoutSymbol<I> ts) {
            // Get info for corresponding timer:
            var optTimer = automaton.getSortedTimers(src).stream()
                    .filter(t -> t.name().equals(ts.timer()))
                    .findFirst();
            assert optTimer.isPresent();

            if (optTimer.get().periodic()) {
                // Periodic -> resets itself:
                resetExtraInfo = String.format("%s↦%d", optTimer.get().name(), optTimer.get().initial());
                edgeColor = "cornflowerblue";
            } else {
                // One-shot -> resets all in target:
                resetExtraInfo = automaton.getSortedTimers(tgt)
                                     .stream()
                                     .map(t -> String.format("%s↦%d", t.name(), t.initial()))
                                     .sorted()
                                     .collect(Collectors.joining(","));
                if (tgt.equals(src)) {
                    // If the target is another location, reset info can always be inferred from context.
                    // --> Only include if self-loop:
                    resetInfo = automaton.getSortedTimers(tgt).stream()
                                         .map(MealyTimerInfo::name)
                                         .sorted()
                                         .collect(Collectors.joining(","));
                }
                edgeColor = "chartreuse3";
            }
        } else if (edge.getInput() instanceof InputSymbol<I> ndi) {
            if (src.equals(tgt) && automaton.isLocalReset(src, ndi)) {
                // Self-loop + local reset -> resets all in target:
                resetExtraInfo = automaton.getSortedTimers(tgt)
                                     .stream()
                                     .map(t -> String.format("%s↦%d", t.name(), t.initial()))
                                     .sorted()
                                     .collect(Collectors.joining(","));
                resetInfo = automaton.getSortedTimers(tgt).stream()
                                     .map(MealyTimerInfo::name)
                                     .sorted()
                                     .collect(Collectors.joining(","));
                edgeColor = "orange";
            }
        }

        if (this.colorEdges && !edgeColor.isBlank()) {
            properties.put(EdgeAttrs.COLOR, edgeColor);
            properties.put("fontcolor", edgeColor);
        }
        if (this.includeResets) {
            label += " {" + resetExtraInfo + "}";
        }
        if (!resetInfo.isEmpty()) {
            properties.put(MMLTEdgeAttrs.RESETS, resetInfo);
        }
        properties.put(EdgeAttrs.LABEL, label);

        return true;
    }
}
