open module net.automatalib.util {
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.core;

    requires com.google.common;

    requires static de.learnlib.tooling.annotation.processor;
    requires static java.compiler;

    exports net.automatalib.util.automaton;
    exports net.automatalib.util.automaton.ads;
    exports net.automatalib.util.automaton.builder;
    exports net.automatalib.util.automaton.conformance;
    exports net.automatalib.util.automaton.copy;
    exports net.automatalib.util.automaton.cover;
    exports net.automatalib.util.automaton.equivalence;
    exports net.automatalib.util.automaton.fsa;
    exports net.automatalib.util.automaton.minimizer.hopcroft;
    exports net.automatalib.util.automaton.minimizer.paigetarjan;
    exports net.automatalib.util.automaton.predicate;
    exports net.automatalib.util.automaton.procedural;
    exports net.automatalib.util.automaton.random;
    exports net.automatalib.util.automaton.transducer;
    exports net.automatalib.util.automaton.vpa;
    exports net.automatalib.util.graph;
    exports net.automatalib.util.graph.apsp;
    exports net.automatalib.util.graph.concept;
    exports net.automatalib.util.graph.copy;
    exports net.automatalib.util.graph.scc;
    exports net.automatalib.util.graph.sssp;
    exports net.automatalib.util.graph.traversal;
    exports net.automatalib.util.minimizer;
    exports net.automatalib.util.partitionrefinement;
    exports net.automatalib.util.traversal;
    exports net.automatalib.util.ts;
    exports net.automatalib.util.ts.acceptor;
    exports net.automatalib.util.ts.comp;
    exports net.automatalib.util.ts.copy;
    exports net.automatalib.util.ts.iterator;
    exports net.automatalib.util.ts.modal;
    exports net.automatalib.util.ts.transducer;
    exports net.automatalib.util.ts.traversal;
}