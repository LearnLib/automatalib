import net.automatalib.api.AutomataLibLocalPropertiesSource;
import net.automatalib.api.AutomataLibPropertiesSource;
import net.automatalib.api.AutomataLibSystemPropertiesSource;
import net.automatalib.common.util.setting.SettingsSource;

open module net.automatalib.api {
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;

    requires com.google.common;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static org.kohsuke.metainf_services;

    provides SettingsSource with AutomataLibLocalPropertiesSource, AutomataLibSystemPropertiesSource, AutomataLibPropertiesSource;

    exports net.automatalib.api;
    exports net.automatalib.api.alphabet;
    exports net.automatalib.api.automaton;
    exports net.automatalib.api.automaton.abstraction;
    exports net.automatalib.api.automaton.concept;
    exports net.automatalib.api.automaton.fsa;
    exports net.automatalib.api.automaton.graph;
    exports net.automatalib.api.automaton.helper;
    exports net.automatalib.api.automaton.procedural;
    exports net.automatalib.api.automaton.simple;
    exports net.automatalib.api.automaton.transducer;
    exports net.automatalib.api.automaton.transducer.probabilistic;
    exports net.automatalib.api.automaton.visualization;
    exports net.automatalib.api.automaton.vpa;
    exports net.automatalib.api.exception;
    exports net.automatalib.api.graph;
    exports net.automatalib.api.graph.ads;
    exports net.automatalib.api.graph.concept;
    exports net.automatalib.api.graph.helper;
    exports net.automatalib.api.graph.visualization;
    exports net.automatalib.api.modelchecking;
    exports net.automatalib.api.ts;
    exports net.automatalib.api.ts.acceptor;
    exports net.automatalib.api.ts.modal;
    exports net.automatalib.api.ts.modal.transition;
    exports net.automatalib.api.ts.output;
    exports net.automatalib.api.ts.powerset;
    exports net.automatalib.api.ts.simple;
    exports net.automatalib.api.visualization;
    exports net.automatalib.api.word;
}
