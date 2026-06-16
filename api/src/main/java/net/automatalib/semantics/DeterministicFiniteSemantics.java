package net.automatalib.semantics;

import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;

public interface DeterministicFiniteSemantics extends DeterministicSemantics, FiniteSemantics {

    interface WildcardPlainSemantics<S, I> extends DeterministicFiniteSemantics,
                                                   DeterministicSemantics.WildcardPlainSemantics<S, I>,
                                                   FiniteSemantics.WildcardPlainSemantics<S, I> {

        DeterministicAutomaton<S, I, ?> getSemantics();
    }

    interface PlainSemantics<S, I, T> extends WildcardPlainSemantics<S, I>,
                                              DeterministicSemantics.PlainSemantics<S, I, T>,
                                              FiniteSemantics.PlainSemantics<S, I, T>,
                                              DeterministicAutomaton<S, I, T> {

        @Override
        default DeterministicAutomaton<S, I, T> getSemantics() {
            return this;
        }
    }

    interface WildcardUniversalSemantics<S, I, SP, TP> extends WildcardPlainSemantics<S, I>,
                                                               DeterministicSemantics.WildcardUniversalSemantics<S, I, SP, TP>,
                                                               FiniteSemantics.WildcardUniversalSemantics<S, I, SP, TP> {

        @Override
        UniversalDeterministicAutomaton<S, I, ?, SP, TP> getSemantics();
    }

    interface UniversalSemantics<S, I, T, SP, TP> extends PlainSemantics<S, I, T>,
                                                          WildcardUniversalSemantics<S, I, SP, TP>,
                                                          FiniteSemantics.UniversalSemantics<S, I, T, SP, TP>,
                                                          DeterministicSemantics.UniversalSemantics<S, I, T, SP, TP>,
                                                          UniversalDeterministicAutomaton<S, I, T, SP, TP> {

        @Override
        default UniversalDeterministicAutomaton<S, I, T, SP, TP> getSemantics() {
            return this;
        }
    }

    interface MutableSemantics<S, I, T, SP, TP> extends UniversalSemantics<S, I, T, SP, TP>,
                                                        FiniteSemantics.MutableSemantics<S, I, T, SP, TP>,
                                                        MutableDeterministic<S, I, T, SP, TP> {}

}
