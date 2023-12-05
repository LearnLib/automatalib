import net.automatalib.visualization.VisualizationProvider;

open module net.automatalib.core {
    requires java.desktop;

    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;

    requires com.google.common;
    requires org.checkerframework.checker.qual;
    requires static org.kohsuke.metainf_services;
    requires org.slf4j;

    uses VisualizationProvider;

    exports net.automatalib.alphabet;
    exports net.automatalib.automaton;
    exports net.automatalib.automaton.base;
    exports net.automatalib.automaton.fsa;
    exports net.automatalib.automaton.procedural;
    exports net.automatalib.automaton.transducer;
    exports net.automatalib.automaton.vpa;
    exports net.automatalib.graph;
    exports net.automatalib.graph.ads;
    exports net.automatalib.graph.base;
    exports net.automatalib.modelchecking;
    exports net.automatalib.ts.modal;
    exports net.automatalib.ts.modal.transition;
    exports net.automatalib.ts.powerset;
    exports net.automatalib.visualization;
    exports net.automatalib.visualization.helper;
}