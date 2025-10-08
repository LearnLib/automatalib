package net.automatalib.automaton.time.mmlt;

import net.automatalib.alphabet.time.mmlt.ILocalTimerMealyInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimerTimeoutSymbol;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.visualization.AutomatonVisualizationHelper;

import java.util.Map;
import java.util.stream.Collectors;

class LocalTimerMealyVisualizationHelper<S, I, O> extends
        AutomatonVisualizationHelper<S, ILocalTimerMealyInputSymbol<I>, LocalTimerMealy.LocalTimerMealyTransition<S, O>, LocalTimerMealy<S, I, O>> {

    private final boolean colorEdges;
    private final boolean includeResets;

    /**
     * Creates a new visualization helper for an MMLT.
     * Allows edge coloring and explicit resets in transition labels for easier inspection.
     * <p>
     * If you want to serialize the resulting file to graphviz, disable explicit resets.
     * The parse does not know how to treat the reset information.
     *
     * @param automaton     Automaton
     * @param colorEdges    If set, the transitions for local resets, periodic timers, and one-shot timers are colored differently.
     * @param includeResets If set, each transition includes a list of timers that it resets.
     */
    public LocalTimerMealyVisualizationHelper(LocalTimerMealy<S, I, O> automaton, boolean colorEdges, boolean includeResets) {
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
            properties.put("timers", timers);
        }

        return true;
    }

    @Override
    public boolean getEdgeProperties(S src,
                                     TransitionEdge<ILocalTimerMealyInputSymbol<I>, LocalTimerMealy.LocalTimerMealyTransition<S, O>> edge,
                                     S tgt, Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);

        String label = String.format("%s / %s", edge.getInput(), edge.getTransition().output());

        // Infer the label color + reset information for the transition:
        String resetInfo = "";
        String edgeColor = "";
        if (edge.getInput() instanceof TimerTimeoutSymbol<I> ts) {
            // Get info for corresponding timer:
            var optTimer = automaton.getSortedTimers(src).stream()
                    .filter(t -> t.name().equals(ts.getTimer()))
                    .findFirst();
            assert optTimer.isPresent();

            if (optTimer.get().periodic()) {
                // Periodic -> resets itself:
                resetInfo = String.format("%s↦%d", optTimer.get().name(), optTimer.get().initial());
                edgeColor = "cornflowerblue";
            } else {
                // One-shot -> resets all in target:
                resetInfo = automaton.getSortedTimers(tgt).stream()
                        .map(t -> String.format("%s↦%d", t.name(), t.initial()))
                        .sorted()
                        .collect(Collectors.joining(","));
                edgeColor = "chartreuse3";
            }
        } else if (edge.getInput() instanceof NonDelayingInput<I> ndi) {
            if (src.equals(tgt) && automaton.isLocalReset(src, ndi)) {
                // Self-loop + local reset -> resets all in target:
                resetInfo = automaton.getSortedTimers(tgt).stream()
                        .map(t -> String.format("%s↦%d", t.name(), t.initial()))
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
            label += " {" + resetInfo + "}";
        }
        properties.put(EdgeAttrs.LABEL, label);

        return true;
    }
}
