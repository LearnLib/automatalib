package net.automatalib.semantics;

import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;

public interface Semantics {

    interface SimpleSemantics<S, I> {

        TransitionSystem<S, I, ?> getSemantics();

    }

    interface UniversalSemantics<S, I, SP, TP> extends SimpleSemantics<S, I> {

        @Override
        UniversalTransitionSystem<S, I, ?, SP, TP> getSemantics();
    }

    interface FullSemantics<S, I, T, SP, TP> extends UniversalSemantics<S, I, SP, TP> {

        @Override
        UniversalTransitionSystem<S, I, T, SP, TP> getSemantics();
    }

}
