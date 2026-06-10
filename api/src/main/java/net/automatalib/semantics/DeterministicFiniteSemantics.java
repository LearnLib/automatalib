package net.automatalib.semantics;

import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;

public interface DeterministicFiniteSemantics {

    interface SimpleSemantics<S, I> {

        DeterministicAutomaton<S, I, ?> getSemantics();

    }

    interface UniversalSemantics<S, I, SP, TP> extends SimpleSemantics<S, I>,
                                                       FiniteSemantics.UniversalSemantics<S, I, SP, TP>,
                                                       DeterministicSemantics.UniversalSemantics<S, I, SP, TP> {

        @Override
        UniversalDeterministicAutomaton<S, I, ?, SP, TP> getSemantics();
    }

    interface FullSemantics<S, I, T, SP, TP> extends UniversalSemantics<S, I, SP, TP>,
                                                     FiniteSemantics.FullSemantics<S, I, T, SP, TP>,
                                                     DeterministicSemantics.FullSemantics<S, I, T, SP, TP> {

        @Override
        UniversalDeterministicAutomaton<S, I, T, SP, TP> getSemantics();
    }

    interface MutableSemantics<S, I, T, SP, TP> extends FullSemantics<S, I, T, SP, TP>, FiniteSemantics.MutableSemantics<S, I, T, SP, TP> {
        @Override
        MutableDeterministic<S, I, T, SP, TP> getSemantics();

    }

    static <S, I, T, SP, TP> FullSemantics<S, I, T, SP, TP> fromAutomaton(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton) {
        return () -> automaton;
    }
}
