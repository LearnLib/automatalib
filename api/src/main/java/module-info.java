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

import net.automatalib.visualization.VisualizationProvider;

/**
 * This module contains the API of AutomataLib, which mainly consists of interfaces for the various concepts and
 * automaton models supported by the AutomataLib core. In addition to that, it also defines some fundamental classes for
 * dealing with words of symbols.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-api&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.api {

    requires net.automatalib.common.setting;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;
    requires org.slf4j;

    // make non-static once https://github.com/typetools/checker-framework/issues/4559 is implemented
    requires static org.checkerframework.checker.qual;

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
    exports net.automatalib.serialization;
    exports net.automatalib.ts;
    exports net.automatalib.ts.acceptor;
    exports net.automatalib.ts.modal;
    exports net.automatalib.ts.modal.transition;
    exports net.automatalib.ts.output;
    exports net.automatalib.ts.powerset;
    exports net.automatalib.ts.simple;
    exports net.automatalib.visualization;
    exports net.automatalib.word;

    uses VisualizationProvider;
}
