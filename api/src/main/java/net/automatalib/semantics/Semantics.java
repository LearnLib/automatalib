package net.automatalib.semantics;

import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;

public interface Semantics {

    interface WildcardPlainSemantics<S, I> extends Semantics {

        TransitionSystem<S, I, ?> getSemantics();
    }

    interface PlainSemantics<S, I, T> extends WildcardPlainSemantics<S, I>, TransitionSystem<S, I, T> {

        @Override
        default TransitionSystem<S, I, T> getSemantics() {
            return this;
        }
    }

    interface WildcardUniversalSemantics<S, I, SP, TP> extends WildcardPlainSemantics<S, I> {

        @Override
        UniversalTransitionSystem<S, I, ?, SP, TP> getSemantics();
    }

    interface UniversalSemantics<S, I, T, SP, TP> extends PlainSemantics<S, I, T>,
                                                          WildcardUniversalSemantics<S, I, SP, TP>,
                                                          UniversalTransitionSystem<S, I, T, SP, TP> {

        @Override
        default UniversalTransitionSystem<S, I, T, SP, TP> getSemantics() {
            return this;
        }
    }

}
