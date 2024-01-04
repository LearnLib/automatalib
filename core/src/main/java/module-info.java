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
 * This module contains concrete implementations for many of the automaton model interfaces defined in the API module.
 * Additionally, it provides abstract base classes that facilitate implementing new automaton model classes. Note that
 * concrete algorithms (traversal, reachability analysis etc.) are part of the {@code net.automatalib.util} module.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-core&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.core {

    requires com.google.common;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;
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