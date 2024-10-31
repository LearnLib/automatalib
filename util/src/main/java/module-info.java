/* Copyright (C) 2013-2024 TU Dortmund University
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This module contains several utility methods for the different types of automata, transition systems, and graphs
 * supported by AutomataLib.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-util&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.util {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.core;

    requires static de.learnlib.tooling.annotation;
    // make non-static once https://github.com/typetools/checker-framework/issues/4559 is implemented
    requires static org.checkerframework.checker.qual;

    exports net.automatalib.util.automaton;
    exports net.automatalib.util.automaton.ads;
    exports net.automatalib.util.automaton.builder;
    exports net.automatalib.util.automaton.conformance;
    exports net.automatalib.util.automaton.copy;
    exports net.automatalib.util.automaton.cover;
    exports net.automatalib.util.automaton.equivalence;
    exports net.automatalib.util.automaton.fsa;
    exports net.automatalib.util.automaton.minimizer;
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
