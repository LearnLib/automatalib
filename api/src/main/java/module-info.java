import net.automatalib.AutomataLibLocalPropertiesSource;
import net.automatalib.AutomataLibPropertiesSource;
import net.automatalib.AutomataLibSystemPropertiesSource;
import net.automatalib.common.util.setting.SettingsSource;
import net.automatalib.visualization.VisualizationProvider;

open module net.automatalib.api {
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;

    requires com.google.common;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static org.kohsuke.metainf_services;

    uses VisualizationProvider;
    provides SettingsSource with AutomataLibLocalPropertiesSource, AutomataLibSystemPropertiesSource, AutomataLibPropertiesSource;

    exports net.automatalib;
    exports net.automatalib.alphabet;
    exports net.automatalib.automaton;
    exports net.automatalib.automaton.abstraction;
    exports net.automatalib.automaton.concept;
    exports net.automatalib.automaton.fsa;
    exports net.automatalib.automaton.graph;
    exports net.automatalib.automaton.helper;
    exports net.automatalib.automaton.procedural;
    exports net.automatalib.automaton.simple;
    exports net.automatalib.automaton.transducer;
    exports net.automatalib.automaton.transducer.probabilistic;
    exports net.automatalib.automaton.visualization;
    exports net.automatalib.automaton.vpa;
    exports net.automatalib.exception;
    exports net.automatalib.graph;
    exports net.automatalib.graph.ads;
    exports net.automatalib.graph.concept;
    exports net.automatalib.graph.helper;
    exports net.automatalib.graph.visualization;
    exports net.automatalib.modelchecking;
    exports net.automatalib.ts;
    exports net.automatalib.ts.acceptor;
    exports net.automatalib.ts.modal;
    exports net.automatalib.ts.modal.transition;
    exports net.automatalib.ts.output;
    exports net.automatalib.ts.powerset;
    exports net.automatalib.ts.simple;
    exports net.automatalib.visualization;
    exports net.automatalib.word;
}
