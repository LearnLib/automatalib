/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.automaton.visualization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;

import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.common.util.Triple;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.SymbolicInput;
import net.automatalib.symbol.time.TimerTimeoutSymbol;
import net.automatalib.visualization.DefaultVisualizationHelper;

/**
 * A visualization helper for MMLTs which Allows edge coloring and explicit resets in transition labels for easier
 * inspection.
 * <p>
 * If you want to serialize MMLTs using this visualization helper, you should consider disabling explicit resets as the
 * additional markup may cause problems during parsing.
 *
 * @param <S>
 *         location type
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class MMLTVisualizationHelper<S, I, O> extends DefaultVisualizationHelper<S, Triple<SymbolicInput<I>, O, S>> {

    private final MMLT<S, I, ?, O> mmlt;
    private final boolean colorEdges;
    private final boolean includeResets;

    /**
     * Default constructor.
     *
     * @param mmlt
     *         the MMLT
     * @param colorEdges
     *         a flag indicating whether the transitions for local resets, periodic timers, and one-shot timers should
     *         be colored differently
     * @param includeResets
     *         a flag indicating whether each transition label should include a list of timers that it resets
     */
    public MMLTVisualizationHelper(MMLT<S, I, ?, O> mmlt, boolean colorEdges, boolean includeResets) {
        this.mmlt = mmlt;
        this.colorEdges = colorEdges;
        this.includeResets = includeResets;
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        if (Objects.equals(node, mmlt.getInitialState())) {
            properties.put(NodeAttrs.INITIAL, String.valueOf(true));
        }

        // Include timer assignments:
        final List<TimerInfo<S, O>> timers = mmlt.getSortedTimers(node);

        if (!timers.isEmpty()) {
            // Add local timer info:
            properties.put(MMLTNodeAttrs.TIMERS,
                           renderTimers(timers, t -> String.format("%s=%d", t.name(), t.initial())));
        }

        return true;
    }

    @Override
    public boolean getEdgeProperties(S src,
                                     Triple<SymbolicInput<I>, O, S> edge,
                                     S tgt,
                                     Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);

        final SymbolicInput<I> input = edge.getFirst();
        final List<TimerInfo<S, O>> timers = mmlt.getSortedTimers(tgt);

        final String label = String.format("%s / %s", input, edge.getSecond());
        final StringBuilder labelBuilder = new StringBuilder(label);

        if (input instanceof TimerTimeoutSymbol<I> ts) {
            // Get info for corresponding timer:
            TimerInfo<S, O> timer = mmlt.getSortedTimers(src)
                                        .stream()
                                        .filter(t -> t.name().equals(ts.timer()))
                                        .findFirst()
                                        .orElseThrow();

            if (timer.periodic()) {
                // Periodic -> resets itself:
                appendResetInfo(labelBuilder, timer);
                colorEdges(properties, "blue");
            } else {
                // One-shot -> resets all in target:
                appendResetInfo(labelBuilder, timers);
                if (Objects.equals(tgt, src)) {
                    // If the target is another location, reset info can always be inferred from context.
                    // --> Only include if self-loop:
                    properties.put(MMLTEdgeAttrs.RESETS, renderTimers(timers, TimerInfo::name));
                }
                colorEdges(properties, "green");
            }
        } else if (input instanceof InputSymbol<I> ndi && Objects.equals(src, tgt) &&
                   mmlt.isLocalReset(src, ndi.symbol())) {
            // Self-loop + local reset -> resets all in target:
            appendResetInfo(labelBuilder, timers);
            colorEdges(properties, "orange");
            properties.put(MMLTEdgeAttrs.RESETS, renderTimers(timers, TimerInfo::name));
        }

        properties.put(EdgeAttrs.LABEL, labelBuilder.toString());

        return true;
    }

    private void colorEdges(Map<String, String> properties, String color) {
        if (this.colorEdges) {
            properties.put(EdgeAttrs.COLOR, color);
            properties.put(EdgeAttrs.FONTCOLOR, color);
        }
    }

    private void appendResetInfo(StringBuilder labelBuilder, TimerInfo<S, O> timer) {
        appendResetInfo(labelBuilder, Collections.singletonList(timer));
    }

    private void appendResetInfo(StringBuilder labelBuilder, List<TimerInfo<S, O>> timers) {
        if (includeResets && !timers.isEmpty()) {
            labelBuilder.append(" {")
                        .append(renderTimersInternal(timers, t -> String.format("%s↦%d", t.name(), t.initial())))
                        .append('}');
        }
    }

    private String renderTimers(List<TimerInfo<S, O>> timers, Function<TimerInfo<S, O>, String> extractor) {
        return renderTimersInternal(timers, extractor).toString();
    }

    private StringJoiner renderTimersInternal(List<TimerInfo<S, O>> timers,
                                              Function<TimerInfo<S, O>, String> extractor) {
        final StringJoiner sj = new StringJoiner(",");
        for (TimerInfo<S, O> t : timers) {
            sj.add(extractor.apply(t));
        }
        return sj;
    }

}
