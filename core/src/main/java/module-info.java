open module net.automatalib.core {
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;

    requires com.google.common;
    requires org.checkerframework.checker.qual;

    exports net.automatalib.alphabet.impl;
    exports net.automatalib.automaton.base;
    exports net.automatalib.automaton.fsa.impl;
    exports net.automatalib.automaton.impl;
    exports net.automatalib.automaton.procedural.impl;
    exports net.automatalib.automaton.transducer.impl;
    exports net.automatalib.automaton.transducer.probabilistic.impl;
    exports net.automatalib.automaton.vpa.impl;
    exports net.automatalib.graph.ads.impl;
    exports net.automatalib.graph.base;
    exports net.automatalib.graph.impl;
    exports net.automatalib.modelchecking.impl;
    exports net.automatalib.ts.modal.impl;
    exports net.automatalib.ts.modal.transition.impl;
    exports net.automatalib.ts.powerset.impl;
}