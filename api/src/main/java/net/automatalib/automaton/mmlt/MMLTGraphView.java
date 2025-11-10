package net.automatalib.automaton.mmlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.visualization.MMLTVisualizationHelper;
import net.automatalib.common.util.Triple;
import net.automatalib.graph.Graph;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.SymbolicInput;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimerTimeoutSymbol;
import net.automatalib.visualization.VisualizationHelper;

public class MMLTGraphView<S, I, T, O> implements Graph<S, Triple<SymbolicInput<I>, O, S>> {

    private final MMLT<S, I, T, O> mmlt;

    public MMLTGraphView(MMLT<S, I, T, O> mmlt) {
        this.mmlt = mmlt;
    }

    @Override
    public Collection<Triple<SymbolicInput<I>, O, S>> getOutgoingEdges(S node) {

        Alphabet<I> alphabet = mmlt.getInputAlphabet();
        List<MealyTimerInfo<S, O>> timers = mmlt.getSortedTimers(node);

        List<Triple<SymbolicInput<I>, O, S>> result = new ArrayList<>(alphabet.size() + timers.size());

        for (I i : alphabet) {
            var t = mmlt.getTransition(node, i);
            if (t != null) {
                result.add(Triple.of(TimedInput.input(i), mmlt.getTransitionProperty(t), mmlt.getSuccessor(t)));
            }
        }

        for (MealyTimerInfo<S, O> t : timers) {
            result.add(Triple.of(new TimerTimeoutSymbol<>(t.name()), t.output(), t.target()));

        }

        return result;
    }

    @Override
    public S getTarget(Triple<SymbolicInput<I>, O, S> edge) {
        return edge.getThird();
    }

    @Override
    public Collection<S> getNodes() {
        return mmlt.getStates();
    }

    @Override
    public VisualizationHelper<S, Triple<SymbolicInput<I>, O, S>> getVisualizationHelper() {
        return new MMLTVisualizationHelper<>(mmlt, false, false);
    }
}
